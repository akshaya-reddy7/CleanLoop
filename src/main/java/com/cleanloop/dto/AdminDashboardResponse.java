package com.cleanloop.dto;

import java.util.Map;

public record AdminDashboardResponse(
        long totalUsers,
        long citizenCount,
        long authorityCount,
        long serviceProviderCount,
        long totalReports,
        long totalPickups,
        long totalRecyclingCenters,
        long totalSchedules,
        Map<String, Long> reportsByStatus,
        Map<String, Long> pickupsByStatus
) {}
