package com.pocketnews.controller;

import com.pocketnews.dto.BookmarkResponse;
import com.pocketnews.dto.NewsDTO;
import com.pocketnews.service.BookmarkService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Value("${app.max-news-per-page:10}")
    private int pageSize;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    /* ============================================================
       ADD BOOKMARK
       POST /bookmarks/{newsId}
       ============================================================ */

    @PostMapping("/{newsId}")
    public ResponseEntity<BookmarkResponse> addBookmark(
            @PathVariable Long newsId,
            @RequestHeader("Device-Id") String deviceId) {

        return ResponseEntity.ok(bookmarkService.addBookmark(newsId, deviceId));
    }

    /* ============================================================
       REMOVE BOOKMARK
       DELETE /bookmarks/{newsId}
       ============================================================ */

    @DeleteMapping("/{newsId}")
    public ResponseEntity<Void> removeBookmark(
            @PathVariable Long newsId,
            @RequestHeader("Device-Id") String deviceId) {

        bookmarkService.removeBookmark(newsId, deviceId);
        return ResponseEntity.noContent().build();
    }

    /* ============================================================
       CHECK BOOKMARK STATUS
       GET /bookmarks/{newsId}/status
       ============================================================ */

    @GetMapping("/{newsId}/status")
    public ResponseEntity<BookmarkResponse> getBookmarkStatus(
            @PathVariable Long newsId,
            @RequestHeader("Device-Id") String deviceId) {

        return ResponseEntity.ok(bookmarkService.getBookmarkStatus(newsId, deviceId));
    }

    /* ============================================================
       GET ALL BOOKMARKS (paginated, full article data)
       GET /bookmarks?page=0
       ============================================================ */

    @GetMapping
    public ResponseEntity<Page<NewsDTO>> getUserBookmarks(
            @RequestHeader("Device-Id") String deviceId,
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, pageSize,
                Sort.by("createdAt").descending());

        return ResponseEntity.ok(bookmarkService.getUserBookmarks(deviceId, pageable));
    }
}