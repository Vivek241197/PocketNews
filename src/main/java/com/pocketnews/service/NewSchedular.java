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

    @Scheduled(initialDelay = 0, fixedRate = 3600000)
    public void fetchNews() {
        logger.info("Scheduler triggered: fetching RSS news...");
        newsIngestionService.ingestAllFeeds(); // ✅ updated method name
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredNews() {
        logger.info("Scheduler triggered: cleaning up expired news...");
        newsIngestionService.deleteExpiredNews();
    }
}
