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
            requestBody.put("max_tokens", 300);

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
            return null;
        }
    }

    private String buildPrompt(String title, String content) {
        return """
                You are a news editor for a mobile news app. Given the title and full content of a news article, generate two things:

                1. SHORT_HEADLINE: A punchy, clear headline in 10 words or fewer. Must be self-contained and informative — do NOT just truncate the title.

                2. SHORT_CONTENT: A neutral 60-word summary of the article. Cover the key facts (who, what, when, where). No opinion. No filler phrases like "In a surprising turn...".

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
}
