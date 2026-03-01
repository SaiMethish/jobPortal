package com.example.jobPortal.repository;

import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, UUID> {

    /** Fetch a candidate's profile by the linked auth user. */
    Optional<CandidateProfile> findByUser(User user);

    /** Check whether a profile already exists for the given user. */
    boolean existsByUser(User user);
}
