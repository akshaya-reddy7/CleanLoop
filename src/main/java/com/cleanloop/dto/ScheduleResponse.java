package com.cleanloop.dto;

import com.cleanloop.entity.CollectionSchedule;
import java.time.Instant;
import java.time.LocalTime;

public record ScheduleResponse(
        Long id,
        String locality,
        String serviceArea,
        String collectionDayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        WasteCategoryResponse category,
        Long assignedProviderId,
        String assignedProviderName,
        boolean isActive,
        String notes,
        Instant createdAt
) {
    public static ScheduleResponse fromEntity(CollectionSchedule s) {
        return new ScheduleResponse(
                s.getId(),
                s.getLocality(),
                s.getServiceArea(),
                s.getCollectionDayOfWeek(),
                s.getStartTime(),
                s.getEndTime(),
                WasteCategoryResponse.fromEntity(s.getCategory()),
                s.getAssignedProvider() != null ? s.getAssignedProvider().getId() : null,
                s.getAssignedProvider() != null ? s.getAssignedProvider().getFullName() : null,
                s.isActive(),
                s.getNotes(),
                s.getCreatedAt()
        );
    }
}
