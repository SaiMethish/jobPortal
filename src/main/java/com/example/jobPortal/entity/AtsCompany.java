package com.example.jobPortal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ats_companies")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AtsCompany {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "source", nullable = false)
    @Builder.Default
    private String source = "GREENHOUSE";

    @Column(name = "category")
    @Builder.Default
    private String category = "PRODUCT"; // "PRODUCT" or "SERVICE"

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "active_job_count")
    @Builder.Default
    private Integer activeJobCount = 0;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
