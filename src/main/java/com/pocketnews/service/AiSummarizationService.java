package com.pocketnews.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Calls the Claude API to generate a short headline and 60-word summary
 * from raw news content. Results are stored in the DB at insert time
 * so summarization only ever runs once per article.
 */
@Service
public class AiSummarizationService {

    private static final Logger logger = LoggerFactory.getLogger(AiSummarizationService.class);

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-haiku-4-5-20251001"; // fast + cheap for summarization
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${anthropic.api.key}")
    private String apiKey;

    public AiSummarizationService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .baseUrl(CLAUDE_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * Result holder for the two generated fields.
     */
    public record SummaryResult(String shortHeadline, String shortContent) {}

    /**
     * Generates a short headline (max 10 words) and a 60-word summary
     * from the given title and full article content.
     *
     * Falls back to simple truncation if the API call fails, so news
     * creation is never blocked by an AI outage.
     */
    public SummaryResult summarize(String title, String content) {
        try {
            return callClaudeApi(title, content);
        } catch (Exception e) {
            logger.error("Claude summarization failed, falling back to truncation: {}", e.getMessage());
            return fallback(title, content);
        }
    }

    /* ============================================================
       PRIVATE — API CALL
       ============================================================ */

    private SummaryResult callClaudeApi(String title, String content) {

        try {
            String prompt = buildPrompt(title, content);

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", MODEL);
            requestBody.put("max_tokens", 600);
            requestBody.put("temperature", 0.2);

            ArrayNode messages = requestBody.putArray("messages");
            ObjectNode message = messages.addObject();
            message.put("role", "user");
            message.put("content", prompt);

            String responseBody = webClient.post()
                    .uri(CLAUDE_API_URL)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // blocking is fine here — this runs at write time, not on reads

            return parseResponse(responseBody);
        }
        catch(Exception e){
            logger.error("Claude API call failed: {}", e.getMessage());
            throw new RuntimeException("Claude API call failed", e);
        }
    }

    private String buildPrompt(String title, String content) {
        return """
                CRITICAL RULES:
-               Generate the headline in your own words max should be 10 words
                Generate the short content summary in your own words should be 
                exactly 60 words generate this in your own words.
                Respond in EXACTLY this format with no other text:
                SHORT_HEADLINE: <your headline here>
                SHORT_CONTENT: <your 60-word summary here>

                TITLE: %s

                CONTENT: %s
                """.formatted(title, content);
    }

    private SummaryResult parseResponse(String responseBody) throws Exception {

        JsonNode root = objectMapper.readTree(responseBody);
        String text = root
                .path("content")
                .get(0)
                .path("text")
                .asText();

        String shortHeadline = "";
        String shortContent = "";

        for (String line : text.split("\n")) {
            if (line.startsWith("SHORT_HEADLINE:")) {
                shortHeadline = line.substring("SHORT_HEADLINE:".length()).trim();
            } else if (line.startsWith("SHORT_CONTENT:")) {
                shortContent = line.substring("SHORT_CONTENT:".length()).trim();
            }
        }

        if (shortHeadline.isBlank() || shortContent.isBlank()) {
            throw new RuntimeException("Claude response did not match expected format: " + text);
        }

        return new SummaryResult(shortHeadline, shortContent);
    }

    /* ============================================================
       PRIVATE — FALLBACK (if Claude is unavailable)
       ============================================================ */

    private SummaryResult fallback(String title, String content) {
        String headline = truncateToWords(title, 10);
        String summary = truncateToWords(content != null ? content : title, 60) + "...";
        return new SummaryResult(headline, summary);
    }

    private String truncateToWords(String text, int maxWords) {
        if (text == null || text.isBlank()) return "";
        String[] words = text.trim().split("\\s+");
        if (words.length <= maxWords) return text;
        return String.join(" ", java.util.Arrays.copyOfRange(words, 0, maxWords));
    }

    public record AiResult(
            String shortHeadline,
            String shortContent,
            String assignedCategory,  // slug assigned by Claude
            boolean isDuplicate       // Claude says if it's duplicate
    ) {}

    public AiResult analyzeArticle(String title, String content,
                                   List<String> categorySlugs,
                                   List<String> recentHeadlines) {
        try {
            return callClaudeForAnalysis(title, content, categorySlugs, recentHeadlines);
        } catch (Exception e) {
            logger.error("Claude analysis failed: {}", e.getMessage());
            return fallbackResult(title, content);
        }
    }

    private AiResult callClaudeForAnalysis(String title, String content,
                                           List<String> categorySlugs,
                                           List<String> recentHeadlines) {
        try {
            String prompt = buildAnalysisPrompt(title, content, categorySlugs, recentHeadlines);

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", MODEL);
            requestBody.put("max_tokens", 600);

            ArrayNode messages = requestBody.putArray("messages");
            ObjectNode message = messages.addObject();
            message.put("role", "user");
            message.put("content", prompt);

            String responseBody = webClient.post()
                    .uri(CLAUDE_API_URL)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseAnalysisResponse(responseBody);
        }
        catch (Exception e){
            logger.error("Claude analysis API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Claude analysis API call failed", e);
        }
    }

    private String buildAnalysisPrompt(String title, String content,
                                       List<String> categorySlugs,
                                       List<String> recentHeadlines) {

        List<String> limitedHeadlines = recentHeadlines.stream()
                .limit(10)
                .map(h -> h.length() > 50 ? h.substring(0, 50) : h)
                .toList();

        String truncatedContent = content != null && content.length() > 2500
                ? content.substring(0, 2500)
                : content;

        return """
        You are a news editor for a mobile app like Inshorts. Your job is to write crisp, complete summaries.

        AVAILABLE CATEGORIES: %s

        RECENT HEADLINES (for duplicate check only - DO NOT use these in your summary):
                    %s
                
                    === ARTICLE TO SUMMARIZE BELOW — IGNORE EVERYTHING ABOVE THIS LINE ===
                    ARTICLE TITLE: %s
                    ARTICLE CONTENT: %s
    STRICT RULES:
    - SHORT_HEADLINE: Max 10 words. Write your OWN headline. Never copy the title.
    - SHORT_CONTENT: Write 55-60 words. Must be complete sentences only.
    SHORT_CONTENT must be on a SINGLE LINE. Do not use line breaks inside it.
      NEVER copy sentences from the article directly.
      NEVER repeat the same sentence twice.
      NEVER end mid-sentence or with a comma.
      Cover: who, what, when, where, why.
      Write like a journalist summarizing for a busy reader.

    RESPOND IN EXACTLY THIS FORMAT (no extra text):
    CATEGORY: [slug]
    DUPLICATE: [YES or NO]
    SHORT_HEADLINE: [your headline]
    SHORT_CONTENT: [your 55-60 word summary]
        """.formatted(
                String.join(", ", categorySlugs),
                limitedHeadlines.isEmpty() ? "none" : String.join("\n", limitedHeadlines),
                title,
                truncatedContent
        );
    }
    private AiResult parseAnalysisResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String text = root.path("content").get(0).path("text").asText();

        String category = "";
        boolean isDuplicate = false;
        String shortHeadline = "";
        String shortContent = "";

        boolean capturingContent = false;
        StringBuilder contentBuilder = new StringBuilder();

        for (String line : text.split("\n")) {
            if (line.startsWith("CATEGORY:")) {
                capturingContent = false;
                category = line.substring("CATEGORY:".length()).trim();
            } else if (line.startsWith("DUPLICATE:")) {
                capturingContent = false;
                isDuplicate = line.substring("DUPLICATE:".length()).trim().equalsIgnoreCase("YES");
            } else if (line.startsWith("SHORT_HEADLINE:")) {
                capturingContent = false;
                shortHeadline = line.substring("SHORT_HEADLINE:".length()).trim();
            } else if (line.startsWith("SHORT_CONTENT:")) {
                capturingContent = true;
                contentBuilder.append(line.substring("SHORT_CONTENT:".length()).trim());
            } else if (capturingContent && !line.isBlank()) {
                contentBuilder.append(" ").append(line.trim());
            }
        }

        shortContent = contentBuilder.toString().trim();
        // ✅ Clean up shortContent
        shortContent = cleanShortContent(shortContent);

        String text1 = root.path("content").get(0).path("text").asText();
        logger.info("RAW CLAUDE RESPONSE: {}", text1);

        return new AiResult(shortHeadline, shortContent, category, isDuplicate);
    }

    private String cleanShortContent(String content) {
        if (content == null || content.isBlank()) return content;

        // Only remove trailing comma or semicolon
        content = content.replaceAll("[,;]+$", "").trim();

        // Only add period if missing — nothing else
        if (!content.endsWith(".") && !content.endsWith("!") && !content.endsWith("?")) {
            content = content + ".";
        }

        return content;
    }
    private AiResult fallbackResult(String title, String content) {
        // ✅ Pad to 60 words if content is too short
        String summary = truncateToWords(content != null ? content : title, 60);
        String[] words = summary.trim().split("\\s+");

        if (words.length < 60 && content != null) {
            // Repeat content to reach 60 words if source is too short
            summary = truncateToWords(title + ". " + content + ". " + content, 60);
        }

        return new AiResult(
                truncateToWords(title, 10),
                summary,
                "top-stories",
                false
        );
    }
}
