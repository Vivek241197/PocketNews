package com.pocketnews.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String deviceId;          // Device identifier (UUID)
    private Integer age;              // User age
    private String preferredLanguage; // User's preferred language
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

