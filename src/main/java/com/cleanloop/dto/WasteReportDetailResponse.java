package com.cleanloop.dto;

import com.cleanloop.entity.WasteReport;
import com.cleanloop.entity.WasteReportStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record WasteReportDetailResponse(
        Long id,
        String title,
        String description,
        String issueType,
        WasteCategoryResponse userCategory,
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        String locality,
        WasteReportStatus status,
        Long reporterId,
        String reporterName,
        String reporterEmail,
        Long assignedToId,
        String assignedToName,
        String resolutionNotes,
        Instant resolvedAt,
        Instant createdAt,
        Instant updatedAt,
        List<ReportImageResponse> images,
        List<ReportStatusHistoryResponse> statusHistory
) {
    public static WasteReportDetailResponse fromEntity(
            WasteReport report,
            List<ReportStatusHistoryResponse> history) {
        List<ReportImageResponse> imageResponses = report.getImages() != null
                ? report.getImages().stream().map(ReportImageResponse::fromEntity).toList()
                : List.of();

        return new WasteReportDetailResponse(
                report.getId(),
                report.getTitle(),
                report.getDescription(),
                report.getIssueType(),
                WasteCategoryResponse.fromEntity(report.getUserCategory()),
                report.getLatitude(),
                report.getLongitude(),
                report.getAddress(),
                report.getLocality(),
                report.getStatus(),
                report.getReporter() != null ? report.getReporter().getId() : null,
                report.getReporter() != null ? report.getReporter().getFullName() : null,
                report.getReporter() != null ? report.getReporter().getEmail() : null,
                report.getAssignedTo() != null ? report.getAssignedTo().getId() : null,
                report.getAssignedTo() != null ? report.getAssignedTo().getFullName() : null,
                report.getResolutionNotes(),
                report.getResolvedAt(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                imageResponses,
                history != null ? history : List.of()
        );
    }
}
