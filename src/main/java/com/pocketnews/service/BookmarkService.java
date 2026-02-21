package com.pocketnews.service;

import com.pocketnews.dto.BookmarkResponse;
import com.pocketnews.entity.Bookmark;
import com.pocketnews.entity.News;
import com.pocketnews.exception.ResourceNotFoundException;
import com.pocketnews.repository.BookmarkRepository;
import com.pocketnews.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookmarkService {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private NewsRepository newsRepository;

    public BookmarkResponse toggleBookmark(Long newsId, String deviceId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("News not found"));

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByDeviceIdAndNewsId(deviceId, newsId);

        boolean isBookmarked;
        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());
            isBookmarked = false;
        } else {
            Bookmark bookmark = new Bookmark();
            bookmark.setDeviceId(deviceId);
            bookmark.setNews(news);
            bookmarkRepository.save(bookmark);
            isBookmarked = true;
        }

        long totalBookmarks = bookmarkRepository.countByNewsId(newsId);
        return new BookmarkResponse(isBookmarked, totalBookmarks);
    }

    public BookmarkResponse getBookmarkStatus(Long newsId, String deviceId) {
        boolean isBookmarked = bookmarkRepository.existsByDeviceIdAndNewsId(deviceId, newsId);
        long totalBookmarks = bookmarkRepository.countByNewsId(newsId);
        return new BookmarkResponse(isBookmarked, totalBookmarks);
    }

    public Page<Long> getUserBookmarks(String deviceId, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findByDeviceId(deviceId, pageable);
        List<Long> newsIds = bookmarks.getContent().stream()
                .map(b -> b.getNews().getId())
                .collect(Collectors.toList());
        return new PageImpl<>(newsIds, pageable, bookmarks.getTotalElements());
    }
}

