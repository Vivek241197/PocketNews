package com.pocketnews.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {
    private String name;
    private String slug;
    private String description;
    private String iconUrl;
    private Integer displayOrder;
}

