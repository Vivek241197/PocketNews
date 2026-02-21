package com.pocketnews.controller;

import com.pocketnews.dto.CommentDTO;
import com.pocketnews.dto.CommentCreateRequest;
import com.pocketnews.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news/{newsId}/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping
    public ResponseEntity<Page<CommentDTO>> getComments(
            @PathVariable Long newsId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentDTO> comments = commentService.getCommentsByNewsId(newsId, pageable);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable Long newsId,
            @RequestParam String deviceId,
            @RequestBody CommentCreateRequest request) {
        CommentDTO comment = commentService.createComment(newsId, deviceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentCreateRequest request) {
        CommentDTO comment = commentService.updateComment(commentId, request);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}

