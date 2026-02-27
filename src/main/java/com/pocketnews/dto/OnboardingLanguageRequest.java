package com.pocketnews.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingLanguageRequest {

    @NotBlank(message = "Language code is required")
    private String languageCode;
}
