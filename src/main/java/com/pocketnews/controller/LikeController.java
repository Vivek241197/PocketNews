package com.pocketnews.controller;

import com.pocketnews.dto.LikeResponse;
import com.pocketnews.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news/{newsId}/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/toggle")
    public ResponseEntity<LikeResponse> toggleLike(
            @PathVariable Long newsId,
            @RequestParam String deviceId) {
        LikeResponse response = likeService.toggleLike(newsId, deviceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<LikeResponse> getLikeStatus(
            @PathVariable Long newsId,
            @RequestParam String deviceId) {
        LikeResponse response = likeService.getLikeStatus(newsId, deviceId);
        return ResponseEntity.ok(response);
    }
}

