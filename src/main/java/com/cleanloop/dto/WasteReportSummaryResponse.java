package com.cleanloop.dto;

import com.cleanloop.entity.WasteReport;
import com.cleanloop.entity.WasteReportStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record WasteReportSummaryResponse(
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
        Long assignedToId,
        String assignedToName,
        String primaryImageUrl,
        Instant createdAt
) {
    public static WasteReportSummaryResponse fromEntity(WasteReport report) {
        String primaryImage = null;
        if (report.getImages() != null && !report.getImages().isEmpty()) {
            primaryImage = report.getImages().get(0).getFilePath();
        }

        return new WasteReportSummaryResponse(
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
                report.getAssignedTo() != null ? report.getAssignedTo().getId() : null,
                report.getAssignedTo() != null ? report.getAssignedTo().getFullName() : null,
                primaryImage,
                report.getCreatedAt()
        );
    }
}
