package com.pocketnews.service;

import com.pocketnews.dto.BookmarkResponse;
import com.pocketnews.dto.NewsDTO;
import com.pocketnews.entity.Bookmark;
import com.pocketnews.entity.News;
import com.pocketnews.exception.ResourceNotFoundException;
import com.pocketnews.repository.BookmarkRepository;
import com.pocketnews.repository.NewsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Transactional
@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final NewsRepository newsRepository;

    @Value("${bookmark.expiry.days}")
    private int expiryDays;

    public BookmarkService(BookmarkRepository bookmarkRepository,
                           NewsRepository newsRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.newsRepository = newsRepository;
    }

    /* ============================================================
       ADD BOOKMARK
       ============================================================ */

    public BookmarkResponse addBookmark(Long newsId, String deviceId) {

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // Return existing active bookmark if present
        Optional<Bookmark> activeBookmark =
                bookmarkRepository.findByDeviceIdAndNewsIdAndExpiresAtAfter(deviceId, newsId, now);

        if (activeBookmark.isPresent()) {
            return new BookmarkResponse(true, activeBookmark.get().getExpiresAt());
        }

        // Clean up any expired bookmark for this device+news pair
        bookmarkRepository.deleteByDeviceIdAndNewsIdAndExpiresAtBefore(deviceId, newsId, now);

        try {
            News news = newsRepository.findById(newsId)
                    .filter(News::isActive)
                    .orElseThrow(() -> new ResourceNotFoundException("News not found"));

            Bookmark bookmark = new Bookmark();
            bookmark.setDeviceId(deviceId);
            bookmark.setNews(news);
            bookmark.setCreatedAt(now);
            bookmark.setExpiresAt(now.plusDays(expiryDays));

            bookmarkRepository.save(bookmark);

            return new BookmarkResponse(true, bookmark.getExpiresAt());

        } catch (DataIntegrityViolationException e) {
            // Race condition: another request saved it simultaneously
            Bookmark existing = bookmarkRepository
                    .findByDeviceIdAndNewsIdAndExpiresAtAfter(deviceId, newsId, now)
                    .orElseThrow();
            return new BookmarkResponse(true, existing.getExpiresAt());
        }
    }

    /* ============================================================
       REMOVE BOOKMARK
       ============================================================ */

    public void removeBookmark(Long newsId, String deviceId) {
        bookmarkRepository.deleteByDeviceIdAndNewsId(deviceId, newsId);
    }

    /* ============================================================
       CHECK STATUS
       ============================================================ */

    public BookmarkResponse getBookmarkStatus(Long newsId, String deviceId) {

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        return bookmarkRepository
                .findByDeviceIdAndNewsIdAndExpiresAtAfter(deviceId, newsId, now)
                .map(b -> new BookmarkResponse(true, b.getExpiresAt()))
                .orElse(new BookmarkResponse(false, null));
    }

    /* ============================================================
       GET USER BOOKMARKS — returns full NewsDTO not just IDs
       ============================================================ */

    public Page<NewsDTO> getUserBookmarks(String deviceId, Pageable pageable) {

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        return bookmarkRepository
                .findByDeviceIdAndExpiresAtAfter(deviceId, now, pageable)
                .map(b -> mapNewsToDTO(b.getNews()));
    }

    /* ============================================================
       MAPPING
       ============================================================ */

    private NewsDTO mapNewsToDTO(News news) {
        return new NewsDTO(
                news.getId(),
                news.getCategory().getId(),
                news.getCategory().getName(),
                news.getShortHeadline(),
                news.getShortContent(),
                news.getImageUrl(),
                news.getSource(),
                news.getViewCount(),
                news.getPublishedAt(),
                news.getCreatedAt(),
                news.getUpdatedAt()
        );
    }
}