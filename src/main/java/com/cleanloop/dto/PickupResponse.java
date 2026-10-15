package com.cleanloop.dto;

import com.cleanloop.entity.PickupRequest;
import com.cleanloop.entity.PickupStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PickupResponse(
        Long id,
        Long requesterId,
        String requesterName,
        String requesterEmail,
        String requesterPhone,
        WasteCategoryResponse category,
        String description,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String locality,
        LocalDate preferredDate,
        String timeWindow,
        String imagePath,
        boolean isSpecialHandling,
        String specialHandlingNotes,
        PickupStatus status,
        Long assignedProviderId,
        String assignedProviderName,
        String completionNotes,
        Instant completedAt,
        Instant createdAt,
        List<PickupStatusHistoryResponse> statusHistory
) {
    public static PickupResponse fromEntity(PickupRequest p, List<PickupStatusHistoryResponse> history) {
        return new PickupResponse(
                p.getId(),
                p.getRequester() != null ? p.getRequester().getId() : null,
                p.getRequester() != null ? p.getRequester().getFullName() : null,
                p.getRequester() != null ? p.getRequester().getEmail() : null,
                p.getRequester() != null ? p.getRequester().getPhoneNumber() : null,
                WasteCategoryResponse.fromEntity(p.getCategory()),
                p.getDescription(),
                p.getAddress(),
                p.getLatitude(),
                p.getLongitude(),
                p.getLocality(),
                p.getPreferredDate(),
                p.getTimeWindow(),
                p.getImagePath(),
                p.isSpecialHandling(),
                p.getSpecialHandlingNotes(),
                p.getStatus(),
                p.getAssignedProvider() != null ? p.getAssignedProvider().getId() : null,
                p.getAssignedProvider() != null ? p.getAssignedProvider().getFullName() : null,
                p.getCompletionNotes(),
                p.getCompletedAt(),
                p.getCreatedAt(),
                history != null ? history : List.of()
        );
    }

    public static PickupResponse summaryFromEntity(PickupRequest p) {
        return fromEntity(p, List.of());
    }
}
