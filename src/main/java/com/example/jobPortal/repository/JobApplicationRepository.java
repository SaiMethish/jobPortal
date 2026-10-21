package com.example.jobPortal.repository;

import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.JobApplication;
import com.example.jobPortal.entity.JobApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    /** Verify if a candidate has already applied to a specific job. */
    boolean existsByJobAndCandidateProfile(Job job, CandidateProfile candidateProfile);

    /** Fetch all applications made by a specific candidate. */
    Page<JobApplication> findByCandidateProfile(CandidateProfile candidateProfile, Pageable pageable);

    /** Fetch all applications for a specific job. */
    Page<JobApplication> findByJob(Job job, Pageable pageable);

    /** Fetch applications for a specific job with a given status. */
    Page<JobApplication> findByJobAndStatus(Job job, JobApplicationStatus status, Pageable pageable);

    /** Delete all applications for a collection of jobs (e.g. when purging expired jobs). */
    void deleteByJobIn(java.util.Collection<Job> jobs);
}
