package com.pocketnews.repository;

import com.pocketnews.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByNewsId(Long newsId, Pageable pageable);
    long countByNewsId(Long newsId);
}

