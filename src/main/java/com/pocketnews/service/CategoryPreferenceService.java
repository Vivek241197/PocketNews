package com.pocketnews.service;

import com.pocketnews.dto.CategoryDTO;
import com.pocketnews.entity.Category;
import com.pocketnews.entity.DeviceCategoryPreference;
import com.pocketnews.exception.ResourceNotFoundException;
import com.pocketnews.repository.CategoryRepository;
import com.pocketnews.repository.DeviceCategoryPreferenceRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
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

    @Transactional
    public void updatePreferences(String deviceId, List<Long> categoryIds) {

        // Step 1: Delete and flush immediately
        preferenceRepository.deleteByDeviceId(deviceId);

        // Step 2: If null or empty → nothing more to do
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        // Step 3: Remove duplicates
        Set<Long> uniqueCategoryIds = new HashSet<>(categoryIds);

        // Step 4: Fetch valid categories
        List<Category> categories = categoryRepository.findAllById(uniqueCategoryIds);

        if (categories.size() != uniqueCategoryIds.size()) {
            throw new ResourceNotFoundException("One or more categories not found");
        }

        // Step 5: Save new preferences
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

    @Transactional(readOnly = true)
    public List<CategoryDTO> getPreferences(String deviceId) {
        return preferenceRepository.findByDeviceId(deviceId)
                .stream()
                .map(pref -> {
                    Category c = pref.getCategory();
                    return new CategoryDTO(
                            c.getId(),
                            c.getName(),
                            c.getSlug(),
                            c.getDescription(),
                            c.getIconUrl(),
                            c.getDisplayOrder(),
                            c.isActive(),
                            c.getCreatedAt(),
                            c.getUpdatedAt()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAvailableCategories() {
        return categoryRepository.findByActiveTrue(Pageable.unpaged())
                .stream()
                .map(c -> new CategoryDTO(
                        c.getId(),
                        c.getName(),
                        c.getSlug(),
                        c.getDescription(),
                        c.getIconUrl(),
                        c.getDisplayOrder(),
                        c.isActive(),
                        c.getCreatedAt(),
                        c.getUpdatedAt()
                ))
                .toList();
    }
}