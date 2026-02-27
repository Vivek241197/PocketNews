package com.pocketnews.service;

import com.pocketnews.entity.Category;
import com.pocketnews.entity.DeviceCategoryPreference;
import com.pocketnews.exception.ResourceNotFoundException;
import com.pocketnews.repository.CategoryRepository;
import com.pocketnews.repository.DeviceCategoryPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryPreferenceService {

    private final DeviceCategoryPreferenceRepository preferenceRepository;
    private final CategoryRepository categoryRepository;

    public CategoryPreferenceService(
            DeviceCategoryPreferenceRepository preferenceRepository,
            CategoryRepository categoryRepository) {

        this.preferenceRepository = preferenceRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Replace entire category preference set for a device.
     */
    public void updatePreferences(String deviceId, List<Long> categoryIds) {

        // Step 1: Delete existing preferences
        preferenceRepository.deleteByDeviceId(deviceId);

        // Step 2: If null or empty → nothing more to do
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        // Step 3: Remove duplicates (defensive programming)
        Set<Long> uniqueCategoryIds = categoryIds.stream()
                .collect(Collectors.toSet());

        // Step 4: Fetch all valid categories
        List<Category> categories = categoryRepository.findAllById(uniqueCategoryIds);

        if (categories.size() != uniqueCategoryIds.size()) {
            throw new ResourceNotFoundException("One or more categories not found");
        }

        // Step 5: Create preference entities
        List<DeviceCategoryPreference> preferences = categories.stream()
                .map(category -> {
                    DeviceCategoryPreference pref = new DeviceCategoryPreference();
                    pref.setDeviceId(deviceId);
                    pref.setCategory(category);
                    return pref;
                })
                .toList();

        preferenceRepository.saveAll(preferences);
    }

    /**
     * Fetch selected category IDs for device.
     */
    public List<Long> getPreferences(String deviceId) {

        return preferenceRepository.findByDeviceId(deviceId)
                .stream()
                .map(pref -> pref.getCategory().getId())
                .toList();
    }
}