package com.pocketnews.controller;

import com.pocketnews.dto.*;
import com.pocketnews.service.OnboardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {

    @Autowired
    private OnboardingService onboardingService;

    /**
     * Get list of available languages
     * GET /onboarding/languages
     */
    @GetMapping("/languages")
    public ResponseEntity<AvailableLanguagesResponse> getAvailableLanguages() {
        AvailableLanguagesResponse response = onboardingService.getAvailableLanguages();
        return ResponseEntity.ok(response);
    }

    /**
     * Step 1: Set language preference
     * POST /onboarding/step1
     */
    @PostMapping("/step1")
    public ResponseEntity<UserProfileDTO> completeStep1(@RequestBody OnboardingStep1Request request) {
        UserProfileDTO response = onboardingService.completeStep1_SetLanguage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Step 2: Set age
     * POST /onboarding/step2
     */
    @PostMapping("/step2")
    public ResponseEntity<UserProfileDTO> completeStep2(@RequestBody OnboardingStep2Request request) {
        UserProfileDTO response = onboardingService.completeStep2_SetAge(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Step 3: Select category preferences
     * POST /onboarding/step3
     */
    @PostMapping("/step3")
    public ResponseEntity<UserProfileDTO> completeStep3(@RequestBody OnboardingStep3Request request) {
        UserProfileDTO response = onboardingService.completeStep3_SetCategoryPreferences(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user profile by device ID
     * GET /onboarding/profile/{deviceId}
     */
    @GetMapping("/profile/{deviceId}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String deviceId) {
        UserProfileDTO profile = onboardingService.getUserProfile(deviceId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Get user preferences (selected categories)
     * GET /onboarding/preferences/{deviceId}
     */
    @GetMapping("/preferences/{deviceId}")
    public ResponseEntity<UserPreferenceDTO> getUserPreferences(@PathVariable String deviceId) {
        UserPreferenceDTO preferences = onboardingService.getUserPreferences(deviceId);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Update language preference
     * PUT /onboarding/{deviceId}/language
     */
    @PutMapping("/{deviceId}/language")
    public ResponseEntity<UserProfileDTO> updateLanguage(
            @PathVariable String deviceId,
            @RequestParam String languageCode) {
        UserProfileDTO response = onboardingService.updateLanguage(deviceId, languageCode);
        return ResponseEntity.ok(response);
    }
}

