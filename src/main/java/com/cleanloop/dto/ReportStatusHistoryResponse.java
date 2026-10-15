package com.cleanloop.dto;

import com.cleanloop.entity.ReportStatusHistory;
import java.time.Instant;

public record ReportStatusHistoryResponse(
        Long id,
        String fromStatus,
        String toStatus,
        Long changedById,
        String changedByName,
        String notes,
        Instant createdAt
) {
    public static ReportStatusHistoryResponse fromEntity(ReportStatusHistory history) {
        if (history == null) return null;
        return new ReportStatusHistoryResponse(
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
