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

        /** Fetch all jobs mapping by status integrating page sizes */
        Page<Job> findByStatus(JobStatus status, Pageable pageable);

        /** Fetch all jobs matching a status that have expired past the provided time limit. */
        List<Job> findByStatusAndExpiresAtBefore(JobStatus status, java.time.LocalDateTime time);

        /** Count jobs by status (e.g. OPEN). */
        long countByStatus(JobStatus status);

        /** Count jobs by source (e.g. GREENHOUSE). */
        long countBySource(String source);

        /** Lookup external ATS job by source and external ID. */
        Optional<Job> findBySourceAndExternalId(String source, String externalId);

        /** Find all jobs for a specific company and ATS source. */
        List<Job> findBySourceAndCompanyName(String source, String companyName);

        /** Distinct Indian cities for search filters. */
        @Query("SELECT DISTINCT j.city FROM Job j WHERE j.city IS NOT NULL AND j.status = 'OPEN' ORDER BY j.city ASC")
        List<String> findDistinctCities();

        /** Distinct company names for search filters. */
        @Query("SELECT DISTINCT j.companyName FROM Job j WHERE j.companyName IS NOT NULL AND j.status = 'OPEN' ORDER BY j.companyName ASC")
        List<String> findDistinctCompanyNames();

        /** Search jobs with optional filters — only OPEN jobs are returned. */
        @Query("SELECT j FROM Job j WHERE j.status = :status "
                        + "AND (:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%'))) "
                        + "AND (:skills IS NULL OR LOWER(j.skills) LIKE LOWER(CONCAT('%', CAST(:skills AS string), '%'))) "
                        + "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', CAST(:location AS string), '%')) "
                        + "     OR (j.city IS NOT NULL AND LOWER(j.city) LIKE LOWER(CONCAT('%', CAST(:location AS string), '%')))) "
                        + "AND (:experienceMin IS NULL OR j.experienceMin >= :experienceMin) "
                        + "AND (:experienceMax IS NULL OR j.experienceMax <= :experienceMax) "
                        + "AND (:salaryMin IS NULL OR j.salaryMin >= :salaryMin) "
                        + "AND (:salaryMax IS NULL OR j.salaryMax <= :salaryMax) "
                        + "AND (:jobType IS NULL OR j.jobType = :jobType) "
                        + "AND (:source IS NULL OR j.source = :source) "
                        + "AND (:companyCategory IS NULL OR j.companyCategory = :companyCategory) "
                        + "AND (:companyName IS NULL OR LOWER(j.companyName) LIKE LOWER(CONCAT('%', CAST(:companyName AS string), '%')))")
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
                        @Param("source") String source,
                        @Param("companyCategory") String companyCategory,
                        @Param("companyName") String companyName,
                        Pageable pageable);
}
