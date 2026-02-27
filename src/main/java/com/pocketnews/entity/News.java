package com.pocketnews.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(
        name = "news",
        indexes = {
                @Index(name = "idx_news_category", columnList = "category_id"),
                @Index(name = "idx_news_published", columnList = "published_at"),
                @Index(name = "idx_news_view_count", columnList = "view_count")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ============================================================
       BASIC INFO
       ============================================================ */

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "short_headline", nullable = false, length = 300)
    private String shortHeadline;

    @Column(name = "short_content", nullable = false, columnDefinition = "TEXT")
    private String shortContent;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "source")
    private String source;

    /* ============================================================
       RELATIONSHIP
       ============================================================ */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /* ============================================================
       METRICS
       ============================================================ */

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    /* ============================================================
       STATUS
       ============================================================ */

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /* ============================================================
       TIMESTAMPS
       ============================================================ */

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;



    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /* ============================================================
       LIFECYCLE CALLBACKS
       ============================================================ */

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;

        if (this.publishedAt == null) {
            this.publishedAt = now;
        }

        if (this.expiresAt == null) {
            this.expiresAt = now.plusDays(2);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}