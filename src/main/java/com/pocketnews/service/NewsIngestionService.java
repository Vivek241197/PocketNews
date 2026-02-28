package com.pocketnews.service;

import com.pocketnews.RssFeedClient;
import com.pocketnews.RssFeedSources;
import com.pocketnews.entity.Category;
import com.pocketnews.entity.News;
import com.pocketnews.repository.CategoryRepository;
import com.pocketnews.repository.NewsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;


    @Service
    public class NewsIngestionService {

        private static final Logger logger = LoggerFactory.getLogger(NewsIngestionService.class);

        private final RssFeedClient rssFeedClient;
        private final NewsRepository newsRepository;
        private final CategoryRepository categoryRepository;
        private final AiSummarizationService aiSummarizationService;

        @Value("${app.news.retention-days:2}")
        private int retentionDays;

        public NewsIngestionService(
                RssFeedClient rssFeedClient,
                NewsRepository newsRepository,
                CategoryRepository categoryRepository,
                AiSummarizationService aiSummarizationService) {
            this.rssFeedClient = rssFeedClient;
            this.newsRepository = newsRepository;
            this.categoryRepository = categoryRepository;
            this.aiSummarizationService = aiSummarizationService;
        }

        @Transactional
        public void ingestAllFeeds() {
            logger.info("Starting RSS ingestion...");

            List<String> categorySlugs = categoryRepository.findAll()
                    .stream()
                    .map(Category::getSlug)
                    .toList();

            // ✅ Mutable list for duplicate detection within same batch
            List<String> recentHeadlines = new ArrayList<>(newsRepository
                    .findByActiveTrueOrderByPublishedAtDesc(PageRequest.of(0, 50))
                    .stream()
                    .map(News::getShortHeadline)
                    .toList());

            for (RssFeedSources.RssFeed feed : RssFeedSources.FEEDS) {
                List<RssFeedClient.RawArticle> articles =
                        rssFeedClient.fetchFeed(feed.url(), extractSourceName(feed.url()));

                int saved = 0;
                for (RssFeedClient.RawArticle raw : articles) {
                    try {
                        if (newsRepository.existsBySourceUrl(raw.sourceUrl())) continue;

                        String content = raw.description() != null ? raw.description() : raw.title();

                        AiSummarizationService.AiResult result =
                                aiSummarizationService.analyzeArticle(
                                        raw.title(), content,
                                        categorySlugs, recentHeadlines
                                );

                        if (result.isDuplicate()) {
                            logger.info("Skipping duplicate: {}", raw.title());
                            continue;
                        }

                        Category category = categoryRepository
                                .findBySlug(result.assignedCategory())
                                .orElse(categoryRepository.findBySlug("top-stories").orElseThrow());

                        News news = new News();
                        news.setCategory(category);
                        news.setShortHeadline(result.shortHeadline());
                        news.setShortContent(result.shortContent());
                        news.setImageUrl(raw.imageUrl());
                        news.setSource(raw.sourceName());
                        news.setSourceUrl(raw.sourceUrl());   // ✅ full article link
                        news.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));
                        news.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(retentionDays));
                        news.setActive(true);

                        newsRepository.save(news);
                        recentHeadlines.add(result.shortHeadline());
                        saved++;

                    } catch (Exception e) {
                        logger.error("Failed to process article '{}': {}", raw.title(), e.getMessage());
                    }
                }
                logger.info("Saved {} articles from feed: {}", saved, feed.url());
            }
            logger.info("RSS ingestion completed.");
        }

        @Transactional
        public void deleteExpiredNews() {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            long deleted = newsRepository.deleteByExpiresAtBefore(now);
            logger.info("Deleted {} expired news articles.", deleted);
        }

        private String extractSourceName(String url) {
            if (url.contains("ndtv")) return "NDTV";
            if (url.contains("timesofindia")) return "Times of India";
            if (url.contains("thehindu")) return "The Hindu";
            if (url.contains("economictimes")) return "Economic Times";
            if (url.contains("business-standard")) return "Business Standard";
            if (url.contains("gadgets360")) return "Gadgets 360";
            if (url.contains("yourstory")) return "YourStory";
            if (url.contains("autocarindia")) return "Autocar India";
            if (url.contains("vogue")) return "Vogue India";
            return "News";
        }
}
