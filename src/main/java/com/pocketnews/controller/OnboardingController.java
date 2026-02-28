package com.pocketnews.controller;

import com.pocketnews.dto.*;
import com.pocketnews.service.FeedService;
import com.pocketnews.service.OnboardingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final FeedService feedService;

    public OnboardingController(OnboardingService onboardingService,FeedService feedService) {
        this.onboardingService = onboardingService;
        this.feedService=feedService;
    }

    /* ============================================================
       GET AVAILABLE LANGUAGES
       ============================================================ */

    @GetMapping("/languages")
    public ResponseEntity<AvailableLanguagesResponse> getAvailableLanguages() {
        return ResponseEntity.ok(onboardingService.getAvailableLanguages());
    }


    @PostMapping("/language")
    public ResponseEntity<UserProfileDTO> setLanguage(
            @RequestHeader("Device-Id") String deviceId,
            @RequestBody OnboardingLanguageRequest request) {

        UserProfileDTO response =
                onboardingService.setLanguage(deviceId, request.getLanguageCode());

        return ResponseEntity.status(HttpStatus.OK).body(response);
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

    @GetMapping("/feed")
    public ResponseEntity<List<ArticleDTO>> getFeed(
            @RequestHeader("Device-Id") String deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(feedService.getFeed(deviceId, page, size));
    }
}

