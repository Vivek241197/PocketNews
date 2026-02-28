package com.pocketnews.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
    @Table(
            name = "article_translations",
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"article_id", "language_code"})
            }
    )
    @Setter
    @Getter
    public class ArticleTranslation {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "article_id", nullable = false)
        private Article article;

        @Column(name = "language_code", nullable = false)
        private String languageCode;

        @Column(nullable = false)
        private String title;

        @Column(columnDefinition = "TEXT", nullable = false)
        private String summary;

        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        @PrePersist
        protected void onCreate() {
            this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
        }

}
