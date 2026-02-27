package com.pocketnews.service;

import com.pocketnews.repository.NewsRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Service to handle scheduled tasks for PocketNews.
 * Automatically deletes news articles that have passed their expiry (2 days after publish).
 */
@Service
public class NewsSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(NewsSchedulerService.class);

    private final NewsRepository newsRepository;

    public NewsSchedulerService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    /**
     * Runs every hour and hard-deletes any news whose expires_at has passed.
     * News articles expire 2 days after they are published (set in News.onCreate()).
     */
    @Transactional
    @Scheduled(fixedRate = 3_600_000) // every hour
    public void deleteExpiredNews() {
        try {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            long deletedCount = newsRepository.deleteByExpiresAtBefore(now);
            if (deletedCount > 0) {
                logger.info("Deleted {} expired news articles", deletedCount);
            } else {
                logger.debug("No expired news articles found");
            }
        } catch (Exception e) {
            logger.error("Error deleting expired news: {}", e.getMessage(), e);
        }
    }
}