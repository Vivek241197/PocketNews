package com.pocketnews.repository;

import com.pocketnews.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    Page<News> findByCategoryId(Long categoryId, Pageable pageable);

    Page<News> findByIsFeatured(Boolean isFeatured, Pageable pageable);

    @Query("SELECT n FROM News n WHERE n.category.id = :categoryId AND n.isFeatured = true")
    List<News> findFeaturedNewsByCategory(@Param("categoryId") Long categoryId);

    Page<News> findByTitleIgnoreCaseContaining(String title, Pageable pageable);

    // Delete news articles that have expired (older than 5 days)
    @Modifying
    @Transactional
    @Query("DELETE FROM News n WHERE n.expiresAt < :currentTime")
    long deleteByExpiresAtBefore(@Param("currentTime") LocalDateTime currentTime);
}

