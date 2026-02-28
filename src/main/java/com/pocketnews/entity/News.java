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
                @Index(name = "idx_news_source_url", columnList = "source_url")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // AI generated — shown as card title
    @Column(name = "short_headline", nullable = false, length = 100)
    private String shortHeadline;

    // AI generated — 60 word summary shown on card
    @Column(name = "short_content", nullable = false, columnDefinition = "TEXT")
    private String shortContent;

    // Shown at bottom — user taps to read full article
    @Column(name = "source_url", nullable = false, length = 500)
    private String sourceUrl;

    // Source name shown with URL e.g "Read more on NDTV"
    @Column(name = "source", length = 100)
    private String source;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
        if (this.publishedAt == null) this.publishedAt = now;
        if (this.expiresAt == null) this.expiresAt = now.plusDays(2);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}