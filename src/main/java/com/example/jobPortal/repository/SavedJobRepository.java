package com.example.jobPortal.repository;

import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, UUID> {
    
    boolean existsByCandidateProfileAndJob(CandidateProfile candidateProfile, Job job);
    
    List<SavedJob> findByCandidateProfile(CandidateProfile candidateProfile);
    
    void deleteByCandidateProfileAndJob(CandidateProfile candidateProfile, Job job);
}
