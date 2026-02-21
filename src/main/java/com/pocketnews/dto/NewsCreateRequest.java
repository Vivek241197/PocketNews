package com.pocketnews.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsCreateRequest {
    private Long categoryId;
    private String title;
    private String description;
    private String content;
    private String imageUrl;
    private String sourceUrl;
    private String sourceName;
    private String author;
    private Boolean isFeatured;
}

