package com.example.jobPortal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "action", nullable = false)
    private String action; // e.g. "JOB_CREATED", "USER_LOGIN"

    @Column(name = "entity_type", nullable = false)
    private String entityType; // e.g. "JOB", "USER", "APPLICATION"

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "performed_by", nullable = false)
    private String performedBy; // user email

    @Column(name = "ip_address")
    private String ipAddress; // Extract via request.getRemoteAddr()

    @Column(name = "endpoint")
    private String endpoint; // Request URI

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}
