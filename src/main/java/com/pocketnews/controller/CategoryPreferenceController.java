package com.pocketnews.controller;


import com.pocketnews.service.CategoryPreferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/preferences")
public class CategoryPreferenceController {

        private final CategoryPreferenceService preferenceService;

        public CategoryPreferenceController(CategoryPreferenceService preferenceService) {
            this.preferenceService = preferenceService;
        }

        /**
         * Replace entire category preference set for a device.
         * Empty list = no preference.
         */
        @PostMapping("/categories")
        public ResponseEntity<Void> updatePreferences(
                @RequestHeader("Device-Id") String deviceId,
                @RequestBody(required = false) List<Long> categoryIds) {

            preferenceService.updatePreferences(deviceId, categoryIds);
            return ResponseEntity.noContent().build();
        }

    @GetMapping("/categories")
    public ResponseEntity<List<Long>> getPreferences(
            @RequestHeader("Device-Id") String deviceId) {

        return ResponseEntity.ok(preferenceService.getPreferences(deviceId));
    }
}
