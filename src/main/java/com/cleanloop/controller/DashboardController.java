package com.cleanloop.controller;

import com.cleanloop.dto.AdminDashboardResponse;
import com.cleanloop.dto.ApiResponse;
import com.cleanloop.dto.AuthorityDashboardResponse;
import com.cleanloop.dto.CitizenDashboardResponse;
import com.cleanloop.security.UserPrincipal;
import com.cleanloop.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboards", description = "Endpoints for role-tailored dashboards and operational metrics")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/citizen")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    @Operation(summary = "Get citizen dashboard metrics", description = "Retrieves statistics for the authenticated citizen's reports, pickups, and local schedules.")
    public ResponseEntity<ApiResponse<CitizenDashboardResponse>> getCitizenDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        CitizenDashboardResponse data = dashboardService.getCitizenDashboard(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(data, "Citizen dashboard retrieved successfully"));
    }

    @GetMapping("/authority")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @Operation(summary = "Get authority operational dashboard", description = "Retrieves pending workloads, report breakdowns, and pickup tasks.")
    public ResponseEntity<ApiResponse<AuthorityDashboardResponse>> getAuthorityDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        AuthorityDashboardResponse data = dashboardService.getAuthorityDashboard(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(data, "Authority dashboard retrieved successfully"));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get system-wide administrator dashboard", description = "Retrieves total users, system-wide metrics, and operational distribution.")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard() {
        AdminDashboardResponse data = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.ok(data, "Admin dashboard retrieved successfully"));
    }
}
