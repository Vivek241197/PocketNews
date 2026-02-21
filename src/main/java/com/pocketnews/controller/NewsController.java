package com.pocketnews.controller;

import com.pocketnews.dto.NewsDTO;
import com.pocketnews.dto.NewsCreateRequest;
import com.pocketnews.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping
    public ResponseEntity<Page<NewsDTO>> getAllNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsDTO> news = newsService.getAllNews(pageable);
        return ResponseEntity.ok(news);
    }

    @GetMapping("/featured")
    public ResponseEntity<Page<NewsDTO>> getFeaturedNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsDTO> news = newsService.getFeaturedNews(pageable);
        return ResponseEntity.ok(news);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<NewsDTO>> getNewsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsDTO> news = newsService.getNewsByCategory(categoryId, pageable);
        return ResponseEntity.ok(news);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsDTO> getNewsById(@PathVariable Long id) {
        NewsDTO news = newsService.getNewsById(id);
        return ResponseEntity.ok(news);
    }

    @PostMapping
    public ResponseEntity<NewsDTO> createNews(@RequestBody NewsCreateRequest request) {
        NewsDTO news = newsService.createNews(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(news);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NewsDTO> updateNews(@PathVariable Long id, @RequestBody NewsCreateRequest request) {
        NewsDTO news = newsService.updateNews(id, request);
        return ResponseEntity.ok(news);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }
}

