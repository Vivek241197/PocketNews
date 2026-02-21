package com.pocketnews.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.pocketnews.repository.HealthCheckRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

    @Autowired
    private HealthCheckRepository healthCheckRepository;

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> databaseCheck() {
        Map<String, Object> response = new HashMap<>();

        try {
            long count = healthCheckRepository.count();
            response.put("status", "UP");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            return ResponseEntity.status(503).body(response);
        }
    }

}

