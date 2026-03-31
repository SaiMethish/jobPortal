package com.example.jobPortal.service.Impl;

import com.example.jobPortal.entity.AuditLog;
import com.example.jobPortal.repository.AuditLogRepository;
import com.example.jobPortal.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async // Prevents Blocking Main Thread API Calls
    public void logAction(String action, String entityType, String entityId, String performedBy) {
        
        String ipAddress = "N/A";
        String endpoint = "N/A";
        
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = request.getRemoteAddr();
                endpoint = request.getMethod() + " " + request.getRequestURI();
            }
        } catch (Exception e) {
            log.warn("Failed to extract HTTP attributes for Audit Logging securely. Defaulting metadata. Exception: {}", e.getMessage());
        }

        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .performedBy(performedBy)
                .ipAddress(ipAddress)
                .endpoint(endpoint)
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Persistent AUDIT captured >> Action: '{}', Entity: '{}', User: '{}'", action, entityType, performedBy);
    }
}
