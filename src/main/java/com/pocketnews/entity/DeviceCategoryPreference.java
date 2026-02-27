package com.pocketnews.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(
        name = "device_category_preferences",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"device_id", "category_id"})
        },
        indexes = {
                @Index(name = "idx_pref_device", columnList = "device_id"),
                @Index(name = "idx_pref_category", columnList = "category_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class DeviceCategoryPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}