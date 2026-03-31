package com.example.jobPortal.repository;

import com.example.jobPortal.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    
    // Abstracted hooks resolving chronological Audit fetches natively
    java.util.List<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType);
    java.util.List<AuditLog> findByPerformedByOrderByTimestampDesc(String performedBy);
    java.util.List<AuditLog> findByActionOrderByTimestampDesc(String action);
}
