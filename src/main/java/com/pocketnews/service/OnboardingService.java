package com.pocketnews.service;

import com.pocketnews.dto.*;
import com.pocketnews.entity.User;
import com.pocketnews.entity.UserPreference;
import com.pocketnews.exception.BadRequestException;
import com.pocketnews.repository.UserRepository;
import com.pocketnews.repository.UserPreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OnboardingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    // Supported Indian languages
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

    /**
     * Step 1: Get available languages
     */
    public AvailableLanguagesResponse getAvailableLanguages() {
        List<LanguageOption> languages = new ArrayList<>(SUPPORTED_LANGUAGES.values());
        return new AvailableLanguagesResponse(languages);
    }

    /**
     * Step 1: Set language preference for new user
     */
    public UserProfileDTO completeStep1_SetLanguage(OnboardingStep1Request request) {
        // Validate language code
        if (!SUPPORTED_LANGUAGES.containsKey(request.getPreferredLanguage())) {
            throw new BadRequestException("Invalid language code: " + request.getPreferredLanguage());
        }

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByDeviceId(request.getDeviceId());
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setPreferredLanguage(request.getPreferredLanguage());
            user = userRepository.save(user);
            return mapToUserProfileDTO(user);
        }

        // Create new anonymous user
        User user = new User();
        user.setDeviceId(request.getDeviceId());
        user.setPreferredLanguage(request.getPreferredLanguage());
        user.setAge(0); // Placeholder, will be updated in step 2
        user = userRepository.save(user);

        return mapToUserProfileDTO(user);
    }

    /**
     * Step 2: Set age
     */
    public UserProfileDTO completeStep2_SetAge(OnboardingStep2Request request) {
        User user = userRepository.findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new BadRequestException("User not found. Please complete Step 1 first."));

        if (request.getAge() < 13) {
            throw new BadRequestException("User must be at least 13 years old");
        }

        user.setAge(request.getAge());
        user = userRepository.save(user);

        return mapToUserProfileDTO(user);
    }

    /**
     * Step 3: Set category preferences
     */
    public UserProfileDTO completeStep3_SetCategoryPreferences(OnboardingStep3Request request) {
        User user = userRepository.findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new BadRequestException("User not found. Please complete Step 1 first."));

        if (request.getPreferredCategoryIds() == null || request.getPreferredCategoryIds().isEmpty()) {
            throw new BadRequestException("At least one category must be selected");
        }

        // Convert category IDs to JSON string
        String categoriesJson = request.getPreferredCategoryIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));

        // Check if user preference exists
        UserPreference preference = userPreferenceRepository.findByUserId(user.getId())
                .orElse(new UserPreference());

        preference.setUser(user);
        preference.setPreferredCategories(categoriesJson);
        userPreferenceRepository.save(preference);

        return mapToUserProfileDTO(user);
    }

    /**
     * Get user profile by device ID
     */
    public UserProfileDTO getUserProfile(String deviceId) {
        User user = userRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return mapToUserProfileDTO(user);
    }

    /**
     * Get user preferences with selected categories
     */
    public UserPreferenceDTO getUserPreferences(String deviceId) {
        User user = userRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        UserPreference preference = userPreferenceRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("User preferences not set"));

        return mapToUserPreferenceDTO(preference);
    }

    /**
     * Update language preference
     */
    public UserProfileDTO updateLanguage(String deviceId, String languageCode) {
        if (!SUPPORTED_LANGUAGES.containsKey(languageCode)) {
            throw new BadRequestException("Invalid language code: " + languageCode);
        }

        User user = userRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setPreferredLanguage(languageCode);
        user = userRepository.save(user);

        return mapToUserProfileDTO(user);
    }

    /**
     * Helper method to map User to UserProfileDTO
     */
    private UserProfileDTO mapToUserProfileDTO(User user) {
        return new UserProfileDTO(
                user.getId(),
                user.getDeviceId(),
                user.getAge(),
                user.getPreferredLanguage()
        );
    }

    /**
     * Helper method to map UserPreference to UserPreferenceDTO
     */
    private UserPreferenceDTO mapToUserPreferenceDTO(UserPreference preference) {
        return new UserPreferenceDTO(
                preference.getId(),
                preference.getUser().getId(),
                preference.getPreferredCategories(),
                "",            // language - empty string
                "",            // theme - empty string
                false,         // notificationsEnabled - false
                preference.getCreatedAt(),
                preference.getUpdatedAt()
        );
    }
}

