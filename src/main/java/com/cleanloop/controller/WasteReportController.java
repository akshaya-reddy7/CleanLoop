package com.cleanloop.controller;

import com.cleanloop.dto.*;
import com.cleanloop.entity.WasteReportStatus;
import com.cleanloop.security.UserPrincipal;
import com.cleanloop.service.WasteReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Waste Reporting", description = "Endpoints for creating, managing, and tracking waste issue reports")
@SecurityRequirement(name = "bearerAuth")
public class WasteReportController {

    private final WasteReportService reportService;

    public WasteReportController(WasteReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit waste issue report", description = "Allows citizens to submit a new waste issue report with optional image attachments.")
    public ResponseEntity<ApiResponse<WasteReportDetailResponse>> createReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("report") @Valid CreateReportRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        WasteReportDetailResponse response = reportService.createReport(principal.getId(), request, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Waste report submitted successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get waste report details", description = "Retrieves full report details including status history and attachments.")
    public ResponseEntity<ApiResponse<WasteReportDetailResponse>> getReportById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        WasteReportDetailResponse detail = reportService.getReportDetail(id, principal);
        return ResponseEntity.ok(ApiResponse.ok(detail, "Report details retrieved successfully"));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user reports", description = "Returns a paginated list of reports submitted by the authenticated citizen.")
    public ResponseEntity<ApiResponse<PagedResponse<WasteReportSummaryResponse>>> getMyReports(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<WasteReportSummaryResponse> reports = reportService.getUserReports(principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(reports, "User reports retrieved successfully"));
    }

    @GetMapping
    @Operation(summary = "Search and filter waste reports", description = "Filter reports by status, category, locality, or assignee with pagination.")
    public ResponseEntity<ApiResponse<PagedResponse<WasteReportSummaryResponse>>> filterReports(
            @RequestParam(required = false) WasteReportStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String locality,
            @RequestParam(required = false) Long assignedToId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        PagedResponse<WasteReportSummaryResponse> reports = reportService.filterReports(
                status, categoryId, locality, assignedToId, pageable, principal);
        return ResponseEntity.ok(ApiResponse.ok(reports, "Reports retrieved successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'SERVICE_PROVIDER', 'ADMIN')")
    @Operation(summary = "Update report status", description = "Transitions report status with audit notes. Restricted to authorities and service providers.")
    public ResponseEntity<ApiResponse<WasteReportDetailResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReportStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        WasteReportDetailResponse updated = reportService.updateReportStatus(id, request, principal);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Report status updated successfully"));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @Operation(summary = "Assign report to service provider or authority", description = "Assigns an open report to a service provider or authority.")
    public ResponseEntity<ApiResponse<WasteReportDetailResponse>> assignReport(
            @PathVariable Long id,
            @Valid @RequestBody AssignReportRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        WasteReportDetailResponse updated = reportService.assignReport(id, request, principal);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Report assigned successfully"));
    }

    @PatchMapping("/{id}/category")
    @Operation(summary = "Update user-confirmed report category", description = "Allows reporter or authorities to confirm or correct the waste category.")
    public ResponseEntity<ApiResponse<WasteReportDetailResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReportCategoryRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        WasteReportDetailResponse updated = reportService.updateUserCategory(id, request, principal);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Report category updated successfully"));
    }

    @PostMapping("/{id}/images/{imageId}/classify")
    @Operation(summary = "Classify report image with AI", description = "Triggers AI classification or re-classification for an attached report image.")
    public ResponseEntity<ApiResponse<WasteReportDetailResponse>> classifyImage(
            @PathVariable Long id,
            @PathVariable Long imageId,
            @AuthenticationPrincipal UserPrincipal principal) {
        WasteReportDetailResponse detail = reportService.classifyImageOnDemand(id, imageId, principal);
        return ResponseEntity.ok(ApiResponse.ok(detail, "Image classification triggered successfully"));
    }
}
