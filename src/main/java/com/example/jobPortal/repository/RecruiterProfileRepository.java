package com.example.jobPortal.repository;

import com.example.jobPortal.entity.RecruiterProfile;
import com.example.jobPortal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RecruiterProfileRepository extends JpaRepository<RecruiterProfile, UUID> {

    /** Fetch a recruiter's profile by the linked auth user. */
    Optional<RecruiterProfile> findByUser(User user);

    /** Check whether a profile already exists for the given user. */
    boolean existsByUser(User user);
}
