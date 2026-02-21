package com.pocketnews.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingStep1Request {
    private String deviceId;
    private String preferredLanguage; // Language code: "en", "hi", "ta", etc.
}

