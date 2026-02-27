package com.pocketnews.repository;

import com.pocketnews.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    /* ============================================================
       FEED BY PREFERRED CATEGORIES (initial load)
       ============================================================ */
    Page<News> findByCategoryIdInAndActiveTrueOrderByPublishedAtDesc(
            List<Long> categoryIds,
            Pageable pageable
    );

    /* ============================================================
       FEED BY PREFERRED CATEGORIES — REFRESH (only newer than `after`)
       ============================================================ */
    Page<News> findByCategoryIdInAndActiveTrueAndPublishedAtAfterOrderByPublishedAtDesc(
            List<Long> categoryIds,
            LocalDateTime after,
            Pageable pageable
    );

    /* ============================================================
       BALANCED FEED — per-category slice, after timestamp
       (used internally by getBalancedFeed when `after` is present)
       ============================================================ */
    @Query("""
            SELECT n FROM News n
            WHERE n.category.id IN :categoryIds
              AND n.active = true
              AND n.publishedAt > :after
            ORDER BY n.publishedAt DESC
            """)
    Page<News> findByCategoryIdsAndAfter(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("after") LocalDateTime after,
            Pageable pageable
    );

    /* ============================================================
       TRENDING FEED (fallback)
       ============================================================ */
    Page<News> findByActiveTrueOrderByViewCountDesc(Pageable pageable);

    /* ============================================================
       LATEST NEWS
       ============================================================ */
    Page<News> findByActiveTrueOrderByPublishedAtDesc(Pageable pageable);

    /* ============================================================
       INCREMENT VIEW COUNT
       ============================================================ */
    @Modifying
    @Query("UPDATE News n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
    void incrementViewCount(@Param("id") Long id);

    /* ============================================================
       DELETE EXPIRED NEWS
       ============================================================ */
    @Modifying
    @Query("DELETE FROM News n WHERE n.expiresAt < :now")
    long deleteByExpiresAtBefore(@Param("now") LocalDateTime now);
}