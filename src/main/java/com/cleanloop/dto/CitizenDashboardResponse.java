package com.cleanloop.dto;

import java.util.List;

public record CitizenDashboardResponse(
        long totalReports,
        long resolvedReports,
        long pendingReports,
        long activePickups,
        long completedPickups,
        List<ScheduleResponse> upcomingSchedules,
        List<WasteReportSummaryResponse> recentReports
) {}
