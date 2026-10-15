package com.cleanloop.dto;

import com.cleanloop.entity.PickupStatusHistory;
import java.time.Instant;

public record PickupStatusHistoryResponse(
        Long id,
        String fromStatus,
        String toStatus,
        Long changedById,
        String changedByName,
        String notes,
        Instant createdAt
) {
    public static PickupStatusHistoryResponse fromEntity(PickupStatusHistory history) {
        if (history == null) return null;
        return new PickupStatusHistoryResponse(
                history.getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getChangedBy() != null ? history.getChangedBy().getId() : null,
                history.getChangedBy() != null ? history.getChangedBy().getFullName() : null,
                history.getNotes(),
                history.getCreatedAt()
        );
    }
}
