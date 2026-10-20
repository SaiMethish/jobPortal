package com.example.jobPortal.repository;

import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, UUID> {

    /** Fetch a candidate's profile by the linked auth user. */
    Optional<CandidateProfile> findByUser(User user);

    /** Check whether a profile already exists for the given user. */
    boolean existsByUser(User user);

    /** Select strictly explicit strings mapped into active Profiles for GC cleanup algorithms */
    @Query("SELECT c.resumeUrl FROM CandidateProfile c WHERE c.resumeUrl IS NOT NULL")
    List<String> findAllResumeUrls();

    /** Locate candidate resume storage paths ending with or matching a specific filename */
    @Query("SELECT c.resumeUrl FROM CandidateProfile c WHERE c.resumeUrl LIKE %:filename%")
    List<String> findResumeUrlsMatchingFilename(@Param("filename") String filename);
}
