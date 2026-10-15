package com.cleanloop.controller;

import com.cleanloop.dto.*;
import com.cleanloop.entity.PickupStatus;
import com.cleanloop.security.UserPrincipal;
import com.cleanloop.service.PickupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/pickups")
@Tag(name = "Pickup Requests", description = "Endpoints for scheduling, assigning, and managing special waste pickups")
@SecurityRequirement(name = "bearerAuth")
public class PickupController {

    private final PickupService pickupService;

    public PickupController(PickupService pickupService) {
        this.pickupService = pickupService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Request waste pickup", description = "Allows citizens to schedule a pickup for bulky or recyclable waste.")
    public ResponseEntity<ApiResponse<PickupResponse>> createPickup(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("pickup") @Valid CreatePickupRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        PickupResponse response = pickupService.createPickup(principal.getId(), request, image);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Pickup request submitted successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pickup request details", description = "Retrieves full pickup details and status history.")
    public ResponseEntity<ApiResponse<PickupResponse>> getPickupById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        PickupResponse response = pickupService.getPickupDetail(id, principal);
        return ResponseEntity.ok(ApiResponse.ok(response, "Pickup details retrieved successfully"));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user pickups", description = "Returns paginated pickup requests created by the authenticated citizen.")
    public ResponseEntity<ApiResponse<PagedResponse<PickupResponse>>> getMyPickups(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "preferredDate", direction = Sort.Direction.ASC) Pageable pageable) {
        PagedResponse<PickupResponse> response = pickupService.getUserPickups(principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(response, "User pickups retrieved successfully"));
    }

    @GetMapping
    @Operation(summary = "Search and filter pickups", description = "Filter pickups by status, category, date, or provider with pagination.")
    public ResponseEntity<ApiResponse<PagedResponse<PickupResponse>>> filterPickups(
            @RequestParam(required = false) PickupStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String locality,
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate preferredDate,
            @PageableDefault(size = 10, sort = "preferredDate", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        PagedResponse<PickupResponse> response = pickupService.filterPickups(
                status, categoryId, locality, providerId, preferredDate, pageable, principal);
        return ResponseEntity.ok(ApiResponse.ok(response, "Pickups retrieved successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'SERVICE_PROVIDER', 'ADMIN')")
    @Operation(summary = "Update pickup status", description = "Advances pickup status with audit notes. Restricted to providers and authorities.")
    public ResponseEntity<ApiResponse<PickupResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePickupStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PickupResponse response = pickupService.updatePickupStatus(id, request, principal);
        return ResponseEntity.ok(ApiResponse.ok(response, "Pickup status updated successfully"));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @Operation(summary = "Assign pickup to service provider", description = "Assigns an open pickup to a licensed service provider.")
    public ResponseEntity<ApiResponse<PickupResponse>> assignPickup(
            @PathVariable Long id,
            @Valid @RequestBody AssignPickupRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PickupResponse response = pickupService.assignPickup(id, request, principal);
        return ResponseEntity.ok(ApiResponse.ok(response, "Pickup assigned successfully"));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel pickup request", description = "Allows citizen to cancel their own pending pickup request.")
    public ResponseEntity<ApiResponse<PickupResponse>> cancelPickup(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal principal) {
        PickupResponse response = pickupService.cancelPickup(id, reason, principal);
        return ResponseEntity.ok(ApiResponse.ok(response, "Pickup request cancelled successfully"));
    }
}
