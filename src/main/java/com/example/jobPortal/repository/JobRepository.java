package com.example.jobPortal.repository;

import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.RecruiterProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    /** Fetch all jobs posted by a specific recruiter. */
    List<Job> findByRecruiterProfile(RecruiterProfile recruiterProfile);
}
