package com.pocketnews.repository;

import com.pocketnews.entity.DeviceCategoryPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceCategoryPreferenceRepository extends JpaRepository<DeviceCategoryPreference, Long> {

    List<DeviceCategoryPreference> findByDeviceId(String deviceId);

    void deleteByDeviceId(String deviceId);
}
