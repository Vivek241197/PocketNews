package com.pocketnews.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "articles")
@Getter
@Setter
public class Article {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "image_url")
        private String imageUrl;

        @Column(name = "source_url", nullable = false)
        private String sourceUrl;

        @Column(name = "source_name")
        private String sourceName;

        private String category;

        @Column(name = "published_at")
        private LocalDateTime publishedAt;

        // English is always stored here as the base
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

