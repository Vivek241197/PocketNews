package com.pocketnews.controller;

import com.pocketnews.dto.BookmarkResponse;
import com.pocketnews.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news/{newsId}/bookmarks")
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    @PostMapping("/toggle")
    public ResponseEntity<BookmarkResponse> toggleBookmark(
            @PathVariable Long newsId,
            @RequestParam String deviceId) {
        BookmarkResponse response = bookmarkService.toggleBookmark(newsId, deviceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<BookmarkResponse> getBookmarkStatus(
            @PathVariable Long newsId,
            @RequestParam String deviceId) {
        BookmarkResponse response = bookmarkService.getBookmarkStatus(newsId, deviceId);
        return ResponseEntity.ok(response);
    }
}

@RestController
@RequestMapping("/user/bookmarks")
class UserBookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    @GetMapping
    public ResponseEntity<Page<Long>> getUserBookmarks(
            @RequestParam String deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Long> bookmarks = bookmarkService.getUserBookmarks(deviceId, pageable);
        return ResponseEntity.ok(bookmarks);
    }
}

