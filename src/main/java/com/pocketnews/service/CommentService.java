package com.pocketnews.service;

import com.pocketnews.dto.CommentDTO;
import com.pocketnews.dto.CommentCreateRequest;
import com.pocketnews.entity.Comment;
import com.pocketnews.entity.News;
import com.pocketnews.exception.ResourceNotFoundException;
import com.pocketnews.repository.CommentRepository;
import com.pocketnews.repository.NewsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final NewsRepository newsRepository;

    public CommentService(CommentRepository commentRepository,
                          NewsRepository newsRepository) {
        this.commentRepository = commentRepository;
        this.newsRepository = newsRepository;
    }

    /* ============================================================
       GET COMMENTS (ONLY ACTIVE + NEWS MUST BE ACTIVE)
       ============================================================ */

    public Page<CommentDTO> getCommentsByNewsId(Long newsId, Pageable pageable) {

        News news = newsRepository.findById(newsId)
                .filter(News::isActive)
                .orElseThrow(() ->
                        new ResourceNotFoundException("News not found or inactive"));

        return commentRepository
                .findByNewsIdAndActiveTrueOrderByCreatedAtDesc(newsId, pageable)
                .map(this::mapToDTO);
    }

    /* ============================================================
       CREATE COMMENT
       ============================================================ */

    public CommentDTO createComment(Long newsId,
                                    String deviceId,
                                    CommentCreateRequest request) {

        News news = newsRepository.findById(newsId)
                .filter(News::isActive)
                .orElseThrow(() ->
                        new ResourceNotFoundException("News not found or inactive"));

        Comment comment = new Comment();
        comment.setNews(news);
        comment.setDeviceId(deviceId);
        comment.setContent(request.getContent());

        comment = commentRepository.save(comment);

        return mapToDTO(comment);
    }

    /* ============================================================
       UPDATE COMMENT (ONLY OWNER + ACTIVE)
       ============================================================ */

    public CommentDTO updateComment(Long newsId,
                                    Long commentId,
                                    String deviceId,
                                    CommentCreateRequest request) {

        Comment comment = commentRepository
                .findByIdAndNewsIdAndDeviceIdAndActiveTrue(
                        commentId, newsId, deviceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Comment not found"));

        comment.setContent(request.getContent());

        return mapToDTO(comment);
    }

    /* ============================================================
       SOFT DELETE COMMENT (ONLY OWNER)
       ============================================================ */

    public void deleteComment(Long newsId,
                              Long commentId,
                              String deviceId) {

        Comment comment = commentRepository
                .findByIdAndNewsIdAndDeviceIdAndActiveTrue(
                        commentId, newsId, deviceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Comment not found"));

        comment.setActive(false);
    }

    /* ============================================================
       DTO MAPPING
       ============================================================ */

    private CommentDTO mapToDTO(Comment comment) {

        return new CommentDTO(
                comment.getId(),
                comment.getNews().getId(),
                comment.getDeviceId(),
                comment.getContent(),
                comment.getLikesCount(),
                comment.isActive(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}

