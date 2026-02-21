package com.pocketnews.service;

import com.pocketnews.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Service to handle scheduled tasks for PocketNews
 * - Automatically delete news articles older than 5 days
 */
@Service
public class NewsSchedulerService {

    private static final Logger logger = Logger.getLogger(NewsSchedulerService.class.getName());

    @Autowired
    private NewsRepository newsRepository;

    /**
     * Runs every hour to delete expired news (older than 5 days)
     * Uses @Scheduled to run at a fixed rate
     */
    @Scheduled(fixedRate = 3600000) // Runs every hour (3600000 ms)
    public void deleteExpiredNews() {
        try {
            LocalDateTime now = LocalDateTime.now();
            long deletedCount = newsRepository.deleteByExpiresAtBefore(now);
            if (deletedCount > 0) {
                logger.info("Deleted " + deletedCount + " expired news articles");
            }
        } catch (Exception e) {
            logger.severe("Error deleting expired news: " + e.getMessage());
        }
    }
}

