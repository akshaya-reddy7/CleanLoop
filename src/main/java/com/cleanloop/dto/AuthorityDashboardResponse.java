package com.cleanloop.dto;

import java.util.List;

public record AuthorityDashboardResponse(
        long submittedReports,
        long underReviewReports,
        long assignedReports,
        long inProgressReports,
        long resolvedReports,
        long pendingPickups,
        long activePickups,
        long activeSchedulesCount,
        List<WasteReportSummaryResponse> pendingActionReports
) {}
