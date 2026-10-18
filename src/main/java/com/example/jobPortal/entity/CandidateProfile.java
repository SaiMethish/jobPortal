package com.example.jobPortal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.example.jobPortal.dto.common.ExperienceDTO;
import com.example.jobPortal.dto.common.EducationDTO;
import com.example.jobPortal.dto.common.CertificationDTO;

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

    @Column(name = "resume_text", columnDefinition = "TEXT")
    private String resumeText;

    @Column(name = "about", columnDefinition = "TEXT")
    private String about;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "experience", columnDefinition = "json")
    private List<ExperienceDTO> experience;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "education", columnDefinition = "json")
    private List<EducationDTO> education;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "certifications", columnDefinition = "json")
    private List<CertificationDTO> certifications;

    @Column(name = "resume_parsed_at")
    private LocalDateTime resumeParsedAt;

    @Column(name = "resume_parser_version")
    private String resumeParserVersion;

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
