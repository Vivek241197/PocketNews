package com.pocketnews.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {
    private String name;
    private String description;
    private String iconUrl;
    private Integer displayOrder;
}

