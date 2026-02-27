package com.pocketnews.controller;

import com.pocketnews.dto.CommentCreateRequest;
import com.pocketnews.dto.CommentDTO;
import com.pocketnews.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news/{newsId}/comments")
public class CommentController {

    private final CommentService commentService;

    @Value("${app.max-news-per-page:10}")
    private int pageSize;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<Page<CommentDTO>> getComments(
            @PathVariable Long newsId,
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, pageSize);
        return ResponseEntity.ok(commentService.getCommentsByNewsId(newsId, pageable));
    }

    @PostMapping
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable Long newsId,
            @RequestHeader("Device-Id") String deviceId,
            @Valid @RequestBody CommentCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(newsId, deviceId, request));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long newsId,
            @PathVariable Long commentId,
            @RequestHeader("Device-Id") String deviceId,
            @Valid @RequestBody CommentCreateRequest request) {

        return ResponseEntity.ok(
                commentService.updateComment(newsId, commentId, deviceId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long newsId,
            @PathVariable Long commentId,
            @RequestHeader("Device-Id") String deviceId) {

        commentService.deleteComment(newsId, commentId, deviceId);
        return ResponseEntity.noContent().build();
    }
}