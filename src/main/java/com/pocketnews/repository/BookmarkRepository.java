package com.pocketnews.repository;

import com.pocketnews.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByDeviceIdAndNewsId(String deviceId, Long newsId);
    Page<Bookmark> findByDeviceId(String deviceId, Pageable pageable);
    boolean existsByDeviceIdAndNewsId(String deviceId, Long newsId);
    long countByNewsId(Long newsId);
}

