package com.example.jobPortal.repository;

import com.example.jobPortal.entity.ApplicationStatusHistory;
import com.example.jobPortal.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, UUID> {
    
    /** Fetch status history for a specific application ordered by change date. */
    List<ApplicationStatusHistory> findByApplicationOrderByChangedAtDesc(JobApplication application);
}
