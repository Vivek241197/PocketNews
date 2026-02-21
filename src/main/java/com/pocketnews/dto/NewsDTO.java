package com.pocketnews.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsDTO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private String content;
    private String imageUrl;
    private String sourceUrl;
    private String sourceName;
    private String author;
    private Boolean isFeatured;
    private Integer viewsCount;
    private Long likesCount;
    private Long commentsCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

