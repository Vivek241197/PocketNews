package com.pocketnews.service;

import com.pocketnews.dto.LikeResponse;
import com.pocketnews.entity.Like;
import com.pocketnews.entity.News;
import com.pocketnews.exception.ResourceNotFoundException;
import com.pocketnews.repository.LikeRepository;
import com.pocketnews.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private NewsRepository newsRepository;

    public LikeResponse toggleLike(Long newsId, String deviceId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("News not found"));

        Optional<Like> existingLike = likeRepository.findByNewsIdAndDeviceId(newsId, deviceId);

        boolean isLiked;
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            isLiked = false;
        } else {
            Like like = new Like();
            like.setNews(news);
            like.setDeviceId(deviceId);
            likeRepository.save(like);
            isLiked = true;
        }

        long totalLikes = likeRepository.countByNewsId(newsId);
        return new LikeResponse(isLiked, totalLikes);
    }

    public LikeResponse getLikeStatus(Long newsId, String deviceId) {
        boolean isLiked = likeRepository.existsByNewsIdAndDeviceId(newsId, deviceId);
        long totalLikes = likeRepository.countByNewsId(newsId);
        return new LikeResponse(isLiked, totalLikes);
    }
}

