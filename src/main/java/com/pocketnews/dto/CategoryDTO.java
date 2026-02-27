package com.pocketnews.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String iconUrl;
    private Integer displayOrder=0;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

