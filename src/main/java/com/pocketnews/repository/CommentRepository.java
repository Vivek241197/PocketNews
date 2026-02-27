package com.pocketnews.repository;

import com.pocketnews.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByNewsIdAndActiveTrueOrderByCreatedAtDesc(
            Long newsId,
            Pageable pageable
    );


    long countByNewsIdAndActiveTrue(Long newsId);

    Optional<Comment> findByIdAndNewsIdAndDeviceIdAndActiveTrue(
            Long id,
            Long newsId,
            String deviceId
    );
}

