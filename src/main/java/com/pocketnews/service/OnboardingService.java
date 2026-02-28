package com.pocketnews.service;

import com.pocketnews.dto.*;
import com.pocketnews.entity.UserProfile;
import com.pocketnews.exception.BadRequestException;
import com.pocketnews.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class OnboardingService {
    private final UserProfileRepository userProfileRepository;

    public OnboardingService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    private static final Map<String, LanguageOption> SUPPORTED_LANGUAGES = Map.ofEntries(
            Map.entry("en", new LanguageOption("en", "English", "English")),
            Map.entry("hi", new LanguageOption("hi", "Hindi", "हिंदी")),
            Map.entry("ta", new LanguageOption("ta", "Tamil", "தமிழ்")),
            Map.entry("te", new LanguageOption("te", "Telugu", "తెలుగు")),
            Map.entry("kn", new LanguageOption("kn", "Kannada", "ಕನ್ನಡ")),
            Map.entry("ml", new LanguageOption("ml", "Malayalam", "മലയാളം")),
            Map.entry("bn", new LanguageOption("bn", "Bengali", "বাংলা")),
            Map.entry("pa", new LanguageOption("pa", "Punjabi", "ਪੰਜਾਬੀ")),
            Map.entry("mr", new LanguageOption("mr", "Marathi", "मराठी")),
            Map.entry("gu", new LanguageOption("gu", "Gujarati", "ગુજરાતી")),
            Map.entry("ur", new LanguageOption("ur", "Urdu", "اردو")),
            Map.entry("od", new LanguageOption("od", "Odia", "ଓଡ଼ିଆ"))
    );

    public AvailableLanguagesResponse getAvailableLanguages() {
        return new AvailableLanguagesResponse(
                new ArrayList<>(SUPPORTED_LANGUAGES.values())
        );
    }

    public UserProfileDTO setLanguage(String deviceId, String languageCode) {

        if (!SUPPORTED_LANGUAGES.containsKey(languageCode)) {
            throw new BadRequestException("Invalid language code: " + languageCode);
        }

        UserProfile profile = userProfileRepository
                .findByDeviceId(deviceId)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setDeviceId(deviceId);
                    return newProfile;
                });

        profile.setLanguageCode(languageCode);
        profile = userProfileRepository.save(profile);

        return mapToDTO(profile);
    }

    public UserProfileDTO getUserProfile(String deviceId) {

        UserProfile profile = userProfileRepository
                .findByDeviceId(deviceId)
                .orElseThrow(() ->
                        new BadRequestException("User profile not found"));

        return mapToDTO(profile);
    }

    private UserProfileDTO mapToDTO(UserProfile profile) {
        return new UserProfileDTO(
                profile.getId(),
                profile.getDeviceId(),
                profile.getLanguageCode(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}

