package com.cleanloop.dto;

import com.cleanloop.entity.Notification;
import java.time.Instant;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        String type,
        Long referenceId,
        String referenceType,
        boolean isRead,
        Instant createdAt
) {
    public static NotificationResponse fromEntity(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getReferenceId(),
                n.getReferenceType(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
