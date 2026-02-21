package com.pocketnews.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingStep3Request {
    private String deviceId;
    private List<Long> preferredCategoryIds; // Selected category IDs
}

