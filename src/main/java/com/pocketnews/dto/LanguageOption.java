package com.pocketnews.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LanguageOption {
    private String code;        // e.g., "en", "hi", "ta", "te"
    private String name;        // e.g., "English", "हिंदी", "தமிழ்"
    private String nativeName;  // Name in the language itself
}

