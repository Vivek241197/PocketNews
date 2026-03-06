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
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Service
public class AiSummarizationService {

    private static final Logger logger = LoggerFactory.getLogger(AiSummarizationService.class);

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-haiku-4-5-20251001";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    // Word count constants — change these to adjust app-wide
    private static final int TARGET_WORDS = 60;
    private static final int MAX_HEADLINE_WORDS = 10;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${anthropic.api.key}")
    private String apiKey;

    public AiSummarizationService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
    }

    public record SummaryResult(String shortHeadline, String shortContent) {}

    public record AiResult(
            String shortHeadline,
            String shortContent,
            String assignedCategory,
            boolean isDuplicate
    ) {}

    /* ============================================================
       PUBLIC — SIMPLE SUMMARIZE (legacy, kept for compatibility)
       ============================================================ */

    public SummaryResult summarize(String title, String content) {
        try {
            return callClaudeApi(title, content);
        } catch (Exception e) {
            logger.error("Claude summarization failed, falling back to truncation: {}", e.getMessage());
            return fallback(title, content);
        }
    }

    /* ============================================================
       PUBLIC — FULL ANALYSIS (category + duplicate + summary)
       ============================================================ */

    public AiResult analyzeArticle(String title, String content,
                                   List<String> categorySlugs,
                                   List<String> recentHeadlines) {
        try {
            return callClaudeForAnalysis(title, content, categorySlugs, recentHeadlines);
        } catch (Exception e) {
            logger.error("Claude analysis failed: {}", e.getMessage(), e);
            return fallbackResult(title, content);
        }
    }

    /* ============================================================
       PRIVATE — SIMPLE API CALL
       ============================================================ */

    private SummaryResult callClaudeApi(String title, String content) {
        try {
            String safeTitle = title != null ? title : "";
            String safeContent = content != null ? content : "";
            String truncated = safeContent.length() > 2500 ? safeContent.substring(0, 2500) : safeContent;

            String prompt = """
                    You are a news editor. Summarize the article below.

                    RULES:
                    - SHORT_HEADLINE: Max 10 words. Write in your OWN words. Never copy the title.
                    - SHORT_CONTENT: Write 80-90 words in your OWN words. Single line only. No line breaks.
                      Cover who, what, when, where, why. Must end with a full stop.

                    RESPOND IN EXACTLY THIS FORMAT — no extra text:
                    SHORT_HEADLINE: [your headline]
                    SHORT_CONTENT: [your 80-90 word summary on a single line]

                    TITLE: %s
                    CONTENT: %s
                    """.formatted(safeTitle, truncated);

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
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .doOnNext(body -> logger.error("Claude API error body: {}", body))
                                    .then(Mono.error(new RuntimeException("Claude API error"))))
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseBody);
            String text = root.path("content").get(0).path("text").asText();
            logger.info("RAW CLAUDE RESPONSE (simple): {}", text);

            String shortHeadline = "";
            boolean capturingContent = false;
            StringBuilder contentBuilder = new StringBuilder();

            for (String line : text.split("\n")) {
                if (line.startsWith("SHORT_HEADLINE:")) {
                    capturingContent = false;
                    shortHeadline = line.substring("SHORT_HEADLINE:".length()).trim();
                } else if (line.startsWith("SHORT_CONTENT:")) {
                    capturingContent = true;
                    contentBuilder.append(line.substring("SHORT_CONTENT:".length()).trim());
                } else if (capturingContent && !line.isBlank()) {
                    contentBuilder.append(" ").append(line.trim());
                }
            }

            // Java enforces word count — NOT Claude
            String shortContent = trimToTargetWords(contentBuilder.toString().trim());
            shortHeadline = enforceHeadline(shortHeadline, safeTitle);

            if (shortHeadline.isBlank() || shortContent.isBlank()) {
                throw new RuntimeException("Claude response missing fields: " + text);
            }

            return new SummaryResult(shortHeadline, shortContent);

        } catch (Exception e) {
            logger.error("Claude API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Claude API call failed", e);
        }
    }

    /* ============================================================
       PRIVATE — ANALYSIS API CALL
       ============================================================ */

    private AiResult callClaudeForAnalysis(String title, String content,
                                           List<String> categorySlugs,
                                           List<String> recentHeadlines) {
        try {
            // Null guards — prevent bad prompts
            String safeTitle = title != null ? title : "";
            String safeContent = content != null ? content : "";
            List<String> safeSlugs = categorySlugs != null ? categorySlugs : List.of("top-stories");
            List<String> safeHeadlines = recentHeadlines != null ? recentHeadlines : List.of();

            String prompt = buildAnalysisPrompt(safeTitle, safeContent, safeSlugs, safeHeadlines);

            logger.info("=== CALLING CLAUDE API ===");
            logger.info("Title: {}", safeTitle);
            logger.info("Prompt length: {} chars", prompt.length());

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", MODEL);
            requestBody.put("max_tokens", 600); // Claude writes 80-90 words, needs enough room
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
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .doOnNext(body -> logger.error("Claude 400/500 error body: {}", body))
                                    .then(Mono.error(new RuntimeException("Claude API error"))))
                    .bodyToMono(String.class)
                    .block();

            logger.info("RAW CLAUDE RESPONSE: {}", responseBody);

            return parseAnalysisResponse(responseBody, safeTitle);

        } catch (Exception e) {
            logger.error("Claude analysis API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Claude analysis API call failed", e);
        }
    }

    private String buildAnalysisPrompt(String title, String content,
                                       List<String> categorySlugs,
                                       List<String> recentHeadlines) {

        String truncatedContent = content.length() > 2500
                ? content.substring(0, 2500)
                : content;

        List<String> limitedHeadlines = recentHeadlines.stream()
                .limit(10)
                .map(h -> h.length() > 50 ? h.substring(0, 50) : h)
                .toList();

        return """
                You are a news editor for a mobile app like Inshorts.

                AVAILABLE CATEGORIES: %s

                RECENT HEADLINES (for duplicate check ONLY — do NOT use these in your summary):
                %s

                === ARTICLE TO SUMMARIZE — IGNORE EVERYTHING ABOVE THIS LINE ===
                ARTICLE TITLE: %s
                ARTICLE CONTENT: %s

                STRICT RULES:
                - CATEGORY: Pick exactly one slug from AVAILABLE CATEGORIES.
                - DUPLICATE: YES if this article covers the same event as a recent headline. Otherwise NO.
                - SHORT_HEADLINE: Max 10 words. Write your OWN headline. Never copy the title.
                - SHORT_CONTENT: Write 80-90 words in your OWN words. Single line only. No line breaks.
                  Cover who, what, when, where, why. Must end with a full stop.

                RESPOND IN EXACTLY THIS FORMAT — no extra text, no explanations:
                CATEGORY: [slug]
                DUPLICATE: [YES or NO]
                SHORT_HEADLINE: [your headline]
                SHORT_CONTENT: [your 80-90 word summary on a single line]
                """.formatted(
                String.join(", ", categorySlugs),
                limitedHeadlines.isEmpty() ? "none" : String.join("\n", limitedHeadlines),
                title,
                truncatedContent
        );
    }

    private AiResult parseAnalysisResponse(String responseBody, String title) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String text = root.path("content").get(0).path("text").asText();
        logger.info("RAW CLAUDE TEXT: {}", text);

        String category = "";
        boolean isDuplicate = false;
        String shortHeadline = "";
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
                // Continuation line — Claude wrapped to next line
                contentBuilder.append(" ").append(line.trim());
            }
        }

        // Java trims to exactly TARGET_WORDS — Claude just needs to give us enough
        String shortContent = trimToTargetWords(contentBuilder.toString().trim());
        shortHeadline = enforceHeadline(shortHeadline, title);

        logger.info("Final headline ({} words): {}",
                shortHeadline.split("\\s+").length, shortHeadline);
        logger.info("Final summary ({} words): {}",
                shortContent.split("\\s+").length, shortContent);

        if (shortHeadline.isBlank() || shortContent.isBlank() || category.isBlank()) {
            logger.error("Incomplete Claude response: {}", text);
            throw new RuntimeException("Claude response missing required fields: " + text);
        }

        return new AiResult(shortHeadline, shortContent, category, isDuplicate);
    }

    /* ============================================================
       PRIVATE — WORD COUNT ENFORCEMENT (Java, not Claude)
       ============================================================ */

    /**
     * Trims Claude's 80-90 word response down to TARGET_WORDS (60).
     * Always ends at a clean sentence boundary.
     *
     * Key insight: Claude writes MORE than needed → Java cuts precisely.
     * This is the ONLY reliable way to enforce word count with an LLM.
     */
    private String trimToTargetWords(String content) {
        if (content == null || content.isBlank()) return content;

        String[] words = content.trim().split("\\s+");
        logger.info("Claude raw word count: {}", words.length);

        // Already within target — just clean ending
        if (words.length <= TARGET_WORDS) {
            return ensureEndsProperly(content);
        }

        // Cut to TARGET_WORDS
        String trimmed = String.join(" ", Arrays.copyOfRange(words, 0, TARGET_WORDS));

        // Find last complete sentence in second half of trimmed text
        // (checking second half avoids cutting too early on articles with
        //  a period in the first sentence)
        int searchFrom = trimmed.length() / 2;
        String secondHalf = trimmed.substring(searchFrom);

        int lastPeriod = Math.max(
                secondHalf.lastIndexOf('.'),
                Math.max(secondHalf.lastIndexOf('!'), secondHalf.lastIndexOf('?'))
        );

        if (lastPeriod >= 0) {
            // Found a clean sentence end in second half — use it
            return trimmed.substring(0, searchFrom + lastPeriod + 1).trim();
        }

        // No clean boundary found — just ensure it ends properly
        return ensureEndsProperly(trimmed);
    }

    /**
     * Enforces headline is max MAX_HEADLINE_WORDS.
     * Falls back to truncated title if Claude returns blank.
     */
    private String enforceHeadline(String headline, String title) {
        if (headline == null || headline.isBlank()) {
            logger.warn("Claude returned blank headline, falling back to title");
            return truncateToWords(title, MAX_HEADLINE_WORDS);
        }
        String[] words = headline.trim().split("\\s+");
        if (words.length > MAX_HEADLINE_WORDS) {
            logger.warn("Headline too long ({} words), trimming", words.length);
            return String.join(" ", Arrays.copyOfRange(words, 0, MAX_HEADLINE_WORDS));
        }
        return headline.trim();
    }

    /**
     * Only adds a period if missing. Never chops content.
     * This replaces the old cleanShortContent which was the root cause of truncation.
     */
    private String ensureEndsProperly(String content) {
        if (content == null || content.isBlank()) return content;
        content = content.replaceAll("[,;]+$", "").trim();
        if (!content.endsWith(".") && !content.endsWith("!") && !content.endsWith("?")) {
            content = content + ".";
        }
        return content;
    }

    /* ============================================================
       PRIVATE — FALLBACK (when Claude is unavailable)
       ============================================================ */

    private SummaryResult fallback(String title, String content) {
        String headline = truncateToWords(title, MAX_HEADLINE_WORDS);
        String summary = truncateToWords(content != null ? content : title, TARGET_WORDS);
        return new SummaryResult(headline, ensureEndsProperly(summary));
    }

    private AiResult fallbackResult(String title, String content) {
        String safeContent = content != null ? content : title;
        String summary = truncateToWords(safeContent, TARGET_WORDS);
        return new AiResult(
                truncateToWords(title, MAX_HEADLINE_WORDS),
                ensureEndsProperly(summary),
                "top-stories",
                false
        );
    }

    private String truncateToWords(String text, int maxWords) {
        if (text == null || text.isBlank()) return "";
        String[] words = text.trim().split("\\s+");
        if (words.length <= maxWords) return text;
        return String.join(" ", Arrays.copyOfRange(words, 0, maxWords));
    }
}