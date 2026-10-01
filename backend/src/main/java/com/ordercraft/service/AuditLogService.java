package com.ordercraft.service;

import com.ordercraft.entity.AuditLog;
import com.ordercraft.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String module, String entityName, String entityId, String oldValue, String newValue) {
        String username = "SYSTEM";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            username = auth.getName();
        }
        AuditLog log = new AuditLog(username, action, module, entityName, entityId, oldValue, newValue);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getLogs(String query, String module, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.searchAuditLogs(query, module, startDate, endDate, pageable);
    }
}
