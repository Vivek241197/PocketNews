package com.pocketnews.controller;

import com.pocketnews.dto.NewsCreateRequest;
import com.pocketnews.dto.NewsDTO;
import com.pocketnews.service.NewsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    @Value("${app.max-news-per-page:10}")
    private int pageSize;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * GET /news        → initial feed (newest first)
     * GET /news?page=1 → next page (infinite scroll)
     * GET /news?after= → pull-to-refresh (only new articles)
     */
    @GetMapping
    public ResponseEntity<Page<NewsDTO>> getFeed(
            @RequestHeader("Device-Id") String deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after) {

        Pageable pageable = PageRequest.of(page, pageSize);
        return ResponseEntity.ok(newsService.getFeed(deviceId, pageable, after));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsDTO> getNewsById(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getNewsById(id));
    }

    @PostMapping
    public ResponseEntity<NewsDTO> createNews(@Valid @RequestBody NewsCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(newsService.createNews(request));
    }
}