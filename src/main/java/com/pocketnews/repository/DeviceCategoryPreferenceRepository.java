package com.pocketnews.repository;

import com.pocketnews.entity.DeviceCategoryPreference;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeviceCategoryPreferenceRepository extends JpaRepository<DeviceCategoryPreference, Long> {

    List<DeviceCategoryPreference> findByDeviceId(String deviceId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM DeviceCategoryPreference d WHERE d.deviceId = :deviceId")
    void deleteByDeviceId(@Param("deviceId") String deviceId);
}
