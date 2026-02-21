package com.pocketnews.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "health_check")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;
}

