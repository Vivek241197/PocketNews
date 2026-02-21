package com.pocketnews.repository;

import com.pocketnews.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByNewsIdAndDeviceId(Long newsId, String deviceId);
    long countByNewsId(Long newsId);
    boolean existsByNewsIdAndDeviceId(Long newsId, String deviceId);
}

