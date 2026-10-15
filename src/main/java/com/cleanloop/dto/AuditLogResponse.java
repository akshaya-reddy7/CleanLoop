package com.cleanloop.dto;

import com.cleanloop.entity.AuditLog;
import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Long actorId,
        String actorEmail,
        String action,
        String resourceType,
        String resourceId,
        String details,
        String ipAddress,
        Instant createdAt
) {
    public static AuditLogResponse fromEntity(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getActor() != null ? log.getActor().getId() : null,
                log.getActorEmail(),
                log.getAction(),
                log.getResourceType(),
                log.getResourceId(),
                log.getDetails(),
                log.getIpAddress(),
                log.getCreatedAt()
        );
    }
}
