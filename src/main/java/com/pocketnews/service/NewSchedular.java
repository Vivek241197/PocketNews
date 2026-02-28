package com.pocketnews.service;

import com.pocketnews.service.NewsIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NewSchedular {

    private static final Logger logger = LoggerFactory.getLogger(NewSchedular.class);

    private final NewsIngestionService newsIngestionService;

    public NewSchedular(NewsIngestionService newsIngestionService) {
        this.newsIngestionService = newsIngestionService;
    }

    // Fetch new articles every 1 hour
    @Scheduled(fixedRateString = "PT1H")
    public void fetchNews() {
        logger.info("Scheduler triggered: fetching news...");
        newsIngestionService.ingestAllCategories();
    }

    // Delete expired articles every day at midnight UTC
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredNews() {
        logger.info("Scheduler triggered: cleaning up expired news...");
        newsIngestionService.deleteExpiredNews();
    }
}
