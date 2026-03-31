package com.example.jobPortal.service;

public interface AuditService {
    /**
     * Synthesizes and intercepts Application mutations resolving explicitly natively onto Persistent Audits.
     * Extracts `HttpServletRequest` optionally utilizing `RequestContextHolder`.
     */
    void logAction(String action, String entityType, String entityId, String performedBy);
}
