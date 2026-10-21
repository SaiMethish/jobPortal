package com.example.jobPortal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Job {

    // ── Primary Key ──────────────────────────────────────────
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ── Relationship ─────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_profile_id", nullable = true)
    private RecruiterProfile recruiterProfile;

    // ── Company / ATS Source Details ─────────────────────────
    @Column(name = "company_name")
    private String companyName;

    @Column(name = "company_category")
    private String companyCategory; // "PRODUCT", "SERVICE"

    @Column(name = "source")
    @Builder.Default
    private String source = "INTERNAL"; // "INTERNAL", "GREENHOUSE", "LEVER"

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "application_url", columnDefinition = "TEXT")
    private String applicationUrl;

    @Column(name = "city")
    private String city;

    // ── Job Details ──────────────────────────────────────────
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    @Column(name = "experience_min")
    private int experienceMin;

    @Column(name = "experience_max")
    private int experienceMax;

    @Column(name = "salary_min")
    private Long salaryMin;

    @Column(name = "salary_max")
    private Long salaryMax;

    @Column(name = "location")
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type")
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private JobStatus status = JobStatus.OPEN;

    @Column(name = "application_count")
    @Builder.Default
    private Integer applicationCount = 0;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

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

    public String getEffectiveCompanyName() {
        if (this.companyName != null && !this.companyName.trim().isEmpty()) {
            return this.companyName;
        }
        if (this.recruiterProfile != null && this.recruiterProfile.getCompanyName() != null) {
            return this.recruiterProfile.getCompanyName();
        }
        return "Company";
    }
}
