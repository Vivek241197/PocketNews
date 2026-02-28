package com.pocketnews.service;



import com.pocketnews.NewsApiClient;
import com.pocketnews.entity.Category;
import com.pocketnews.entity.News;
import com.pocketnews.repository.CategoryRepository;
import com.pocketnews.repository.NewsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NewsIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(NewsIngestionService.class);

    // Maps NewsAPI category names → your category slugs
    private static final Map<String, String> NEWSAPI_TO_SLUG = Map.of(
            "general",       "top-stories",
            "business",      "business",
            "technology",    "technology",
            "sports",        "sports",
            "entertainment", "entertainment",
            "science",       "science",
            "health",        "health-fitness"
    );

    private final NewsApiClient newsApiClient;
    private final NewsRepository newsRepository;
    private final CategoryRepository categoryRepository;
    private final AiSummarizationService aiSummarizationService;

    @Value("${app.news.retention-days:2}")
    private int retentionDays;

    public NewsIngestionService(
            NewsApiClient newsApiClient,
            NewsRepository newsRepository,
            CategoryRepository categoryRepository,
            AiSummarizationService aiSummarizationService) {
        this.newsApiClient = newsApiClient;
        this.newsRepository = newsRepository;
        this.categoryRepository = categoryRepository;
        this.aiSummarizationService = aiSummarizationService;
    }

    @Transactional
    public void ingestAllCategories() {
        logger.info("Starting news ingestion...");

        for (Map.Entry<String, String> entry : NEWSAPI_TO_SLUG.entrySet()) {
            String newsApiCategory = entry.getKey();
            String slug = entry.getValue();

            Optional<Category> categoryOpt = categoryRepository.findBySlug(slug);
            if (categoryOpt.isEmpty()) {
                logger.warn("Category not found for slug: {}", slug);
                continue;
            }

            Category category = categoryOpt.get();
            List<NewsApiClient.RawArticle> articles =
                    newsApiClient.fetchTopHeadlines(newsApiCategory);

            int saved = 0;
            for (NewsApiClient.RawArticle raw : articles) {
                try {
                    // Skip if already exists (avoid duplicates by source URL)
                    if (newsRepository.existsBySourceUrl(raw.sourceUrl())) continue;

                    String rawContent = (raw.content() != null && raw.content().length() > 100)
                            ? raw.content()
                            : (raw.description() != null ? raw.description() : raw.title());

                    // AI summarization
                    AiSummarizationService.SummaryResult summary =
                            aiSummarizationService.summarize(raw.title(), rawContent);

                    News news = new News();
                    news.setCategory(category);
                    news.setTitle(raw.title());
                    news.setContent(rawContent);
                    news.setShortHeadline(summary.shortHeadline());
                    news.setShortContent(summary.shortContent());
                    news.setImageUrl(raw.imageUrl());
                    news.setSource(raw.sourceName());
                    news.setSourceUrl(raw.sourceUrl());
                    news.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));
                    news.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(retentionDays));
                    news.setActive(true);

                    newsRepository.save(news);
                    saved++;

                } catch (Exception e) {
                    logger.error("Failed to save article '{}': {}", raw.title(), e.getMessage());
                }
            }

            logger.info("Ingested {} articles for category: {}", saved, slug);
        }

        logger.info("News ingestion completed.");
    }

    @Transactional
    public void deleteExpiredNews() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        long deleted = newsRepository.deleteByExpiresAtBefore(now);
        logger.info("Deleted {} expired news articles.", deleted);
    }
}
