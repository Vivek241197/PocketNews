package com.pocketnews.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(
        name = "user_profiles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "device_id")
        },
        indexes = {
                @Index(name = "idx_user_device", columnList = "device_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {

    /* ============================================================
       ID
       ============================================================ */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ============================================================
       DEVICE (UNIQUE)
       ============================================================ */

    @Column(name = "device_id", nullable = false, unique = true)
    private String deviceId;

    /* ============================================================
       LANGUAGE (IMMUTABLE AFTER SET)
       ============================================================ */

    @Column(name = "language_code")
    private String languageCode;

    /* ============================================================
       TIMESTAMPS
       ============================================================ */

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* ============================================================
       LIFECYCLE CALLBACKS
       ============================================================ */

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}

