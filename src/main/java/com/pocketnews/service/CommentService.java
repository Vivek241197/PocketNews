package com.pocketnews.service;

import com.pocketnews.dto.CommentDTO;
import com.pocketnews.dto.CommentCreateRequest;
import com.pocketnews.entity.Comment;
import com.pocketnews.entity.News;
import com.pocketnews.exception.ResourceNotFoundException;
import com.pocketnews.repository.CommentRepository;
import com.pocketnews.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NewsRepository newsRepository;

    public Page<CommentDTO> getCommentsByNewsId(Long newsId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByNewsId(newsId, pageable);
        List<CommentDTO> dtos = comments.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, comments.getTotalElements());
    }

    public CommentDTO createComment(Long newsId, String deviceId, CommentCreateRequest request) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("News not found"));

        Comment comment = new Comment();
        comment.setNews(news);
        comment.setDeviceId(deviceId);
        comment.setContent(request.getContent());

        comment = commentRepository.save(comment);
        return mapToDTO(comment);
    }

    public CommentDTO updateComment(Long commentId, CommentCreateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);
        return mapToDTO(comment);
    }

    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        commentRepository.delete(comment);
    }

    private CommentDTO mapToDTO(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getNews().getId(),
                comment.getDeviceId(),         // UUID of the user (device identifier)
                comment.getContent(),
                comment.getLikesCount(),
                comment.getIsActive(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}

