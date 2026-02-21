package com.pocketnews.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceDTO {
    private Long id;
    private Long userId;
    private String preferredCategories;
    private String language;
    private String theme;
    private Boolean notificationsEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

