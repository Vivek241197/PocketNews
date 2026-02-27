package com.pocketnews.repository;

import com.pocketnews.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByDeviceIdAndNewsIdAndExpiresAtAfter(
            String deviceId,
            Long newsId,
            LocalDateTime now
    );

    Page<Bookmark> findByDeviceIdAndExpiresAtAfter(
            String deviceId,
            LocalDateTime now,
            Pageable pageable
    );

    void deleteByDeviceIdAndNewsId(String deviceId, Long newsId);

    void deleteByDeviceIdAndNewsIdAndExpiresAtBefore(
            String deviceId,
            Long newsId,
            LocalDateTime now
    );

    void deleteByExpiresAtBefore(LocalDateTime now);
}

