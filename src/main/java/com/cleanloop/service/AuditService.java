package com.cleanloop.service;

import com.cleanloop.dto.AuditLogResponse;
import com.cleanloop.dto.PagedResponse;
import com.cleanloop.entity.AuditLog;
import com.cleanloop.entity.User;
import com.cleanloop.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logAction(User actor, String action, String resourceType, String resourceId, String details, String ipAddress) {
        String actorEmail = actor != null ? actor.getEmail() : "SYSTEM";
        AuditLog auditLog = new AuditLog(actor, actorEmail, action, resourceType, resourceId, details, ipAddress);
        auditLogRepository.save(auditLog);
        log.info("AUDIT: actor={} action={} resource={}/{}", actorEmail, action, resourceType, resourceId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAuditLogs(Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findByOrderByCreatedAtDesc(pageable);
        return PagedResponse.from(page.map(AuditLogResponse::fromEntity));
    }
}
