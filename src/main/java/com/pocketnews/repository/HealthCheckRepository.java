package com.pocketnews.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.pocketnews.entity.HealthCheck;

@Repository
public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long> {

}
