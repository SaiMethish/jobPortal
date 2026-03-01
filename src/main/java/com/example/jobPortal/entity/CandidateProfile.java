package com.example.jobPortal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "candidate_profiles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CandidateProfile {

    // ── Primary Key ──────────────────────────────────────────
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ── Relationship ─────────────────────────────────────────
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ── Profile Fields ────────────────────────────────────────
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "location")
    private String location;

    @Column(name = "experience_years")
    private Integer experienceYears;

    /**
     * Stored as a JSON array in the DB (e.g. ["Java","Spring","React"]).
     * Future-proof for resume-parsing integration (Feature Day 17).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skills", columnDefinition = "json")
    private List<String> skills;

    /** Placeholder for the resume file URL — will be populated on Day 17. */
    @Column(name = "resume_url")
    private String resumeUrl;

    // ── Auditing ──────────────────────────────────────────────
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
