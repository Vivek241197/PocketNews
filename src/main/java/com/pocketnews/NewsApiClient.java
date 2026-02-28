package com.pocketnews;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

    @Component
    public class NewsApiClient {

        private static final Logger logger = LoggerFactory.getLogger(NewsApiClient.class);

        private final WebClient webClient;

        @Value("${newsapi.key}")
        private String apiKey;

        public NewsApiClient(WebClient.Builder webClientBuilder,
                             @Value("${newsapi.base-url}") String baseUrl) {
            this.webClient = webClientBuilder
                    .baseUrl(baseUrl)
                    .build();
        }

        public record RawArticle(
                String title,
                String description,
                String content,
                String imageUrl,
                String sourceUrl,
                String sourceName
        ) {}

        public List<RawArticle> fetchTopHeadlines(String category) {
            try {
                JsonNode response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/top-headlines")
                                .queryParam("country", "in")
                                .queryParam("category", category)
                                .queryParam("pageSize", 20)
                                .queryParam("apiKey", apiKey)
                                .build())
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block();

                List<RawArticle> articles = new ArrayList<>();

                if (response == null || !response.has("articles")) return articles;

                for (JsonNode node : response.get("articles")) {
                    String title = node.path("title").asText(null);
                    String description = node.path("description").asText(null);
                    String content = node.path("content").asText(null);
                    String imageUrl = node.path("urlToImage").asText(null);
                    String sourceUrl = node.path("url").asText(null);
                    String sourceName = node.path("source").path("name").asText(null);

                    // Skip articles with missing essential fields
                    if (title == null || title.isBlank() || title.equals("[Removed]")) continue;
                    if (description == null && content == null) continue;

                    articles.add(new RawArticle(
                            title, description, content,
                            imageUrl, sourceUrl, sourceName
                    ));
                }

                return articles;

            } catch (Exception e) {
                logger.error("Failed to fetch news for category {}: {}", category, e.getMessage());
                return new ArrayList<>();
            }
        }

}
