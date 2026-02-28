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
    private String shortHeadline;
    private String shortContent;
    private String imageUrl;
    private String source;
    private String sourceUrl;      // ✅ frontend shows this at bottom
    private Long viewCount;
    private LocalDateTime publishedAt;
}