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

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    /* ============================================================
       GET AVAILABLE LANGUAGES
       ============================================================ */

    @GetMapping("/languages")
    public ResponseEntity<AvailableLanguagesResponse> getAvailableLanguages() {
        return ResponseEntity.ok(onboardingService.getAvailableLanguages());
    }

    /* ============================================================
       SET LANGUAGE (ONLY ONCE)
       ============================================================ */

    @PostMapping("/language")
    public ResponseEntity<UserProfileDTO> setLanguage(
            @RequestHeader("Device-Id") String deviceId,
            @RequestBody OnboardingLanguageRequest request) {

        UserProfileDTO response =
                onboardingService.setLanguage(deviceId, request.getLanguageCode());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /* ============================================================
       GET USER PROFILE
       ============================================================ */

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(
            @RequestHeader("Device-Id") String deviceId) {

        return ResponseEntity.ok(
                onboardingService.getUserProfile(deviceId)
        );
    }
}

