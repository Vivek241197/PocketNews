package com.pocketnews.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ArticleDTO {
    private Long id;
    private String title;
    private String summary;
    private String imageUrl;
    private String sourceUrl;
    private String sourceName;
    private String category;
    private LocalDateTime publishedAt;
}