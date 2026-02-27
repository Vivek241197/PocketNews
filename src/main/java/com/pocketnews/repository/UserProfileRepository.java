package com.pocketnews.repository;

import com.pocketnews.dto.UserProfileDTO;
import com.pocketnews.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByDeviceId(String deviceId);

    boolean existsByDeviceId(String deviceId);
}

