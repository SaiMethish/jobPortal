package com.example.jobPortal.repository;

import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.JobStatus;
import com.example.jobPortal.entity.JobType;
import com.example.jobPortal.entity.RecruiterProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

        /** Fetch all jobs posted by a specific recruiter. */
        List<Job> findByRecruiterProfile(RecruiterProfile recruiterProfile);

        /** Fetch a single job only if it has the given status (e.g. OPEN). */
        Optional<Job> findByIdAndStatus(UUID id, JobStatus status);

        /** Search jobs with optional filters — only OPEN jobs are returned. */
        @Query("SELECT j FROM Job j WHERE j.status = :status "
                        + "AND (:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%'))) "
                        + "AND (:skills IS NULL OR LOWER(j.skills) LIKE LOWER(CONCAT('%', CAST(:skills AS string), '%'))) "
                        + "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', CAST(:location AS string), '%'))) "
                        + "AND (:experienceMin IS NULL OR j.experienceMin >= :experienceMin) "
                        + "AND (:experienceMax IS NULL OR j.experienceMax <= :experienceMax) "
                        + "AND (:salaryMin IS NULL OR j.salaryMin >= :salaryMin) "
                        + "AND (:salaryMax IS NULL OR j.salaryMax <= :salaryMax) "
                        + "AND (:jobType IS NULL OR j.jobType = :jobType)")
        Page<Job> searchJobs(
                        @Param("status") JobStatus status,
                        @Param("title") String title,
                        @Param("skills") String skills,
                        @Param("location") String location,
                        @Param("experienceMin") Integer experienceMin,
                        @Param("experienceMax") Integer experienceMax,
                        @Param("salaryMin") Long salaryMin,
                        @Param("salaryMax") Long salaryMax,
                        @Param("jobType") JobType jobType,
                        Pageable pageable);
}
