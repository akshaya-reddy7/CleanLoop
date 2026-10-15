package com.cleanloop.controller;

import com.cleanloop.dto.ApiResponse;
import com.cleanloop.dto.CreateScheduleRequest;
import com.cleanloop.dto.ScheduleResponse;
import com.cleanloop.service.CollectionScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@Tag(name = "Collection Schedules", description = "Endpoints for viewing and managing municipal waste collection schedules")
public class CollectionScheduleController {

    private final CollectionScheduleService scheduleService;

    public CollectionScheduleController(CollectionScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    @Operation(summary = "Get all active collection schedules", description = "Retrieves all currently active municipal collection schedules.")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getAllActiveSchedules() {
        List<ScheduleResponse> schedules = scheduleService.getAllActiveSchedules();
        return ResponseEntity.ok(ApiResponse.ok(schedules, "Collection schedules retrieved successfully"));
    }

    @GetMapping("/locality")
    @Operation(summary = "Get schedules by locality", description = "Retrieves schedules for a specific neighborhood or district.")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByLocality(
            @RequestParam String locality) {
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByLocality(locality);
        return ResponseEntity.ok(ApiResponse.ok(schedules, "Locality schedules retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get schedule by ID", description = "Retrieves schedule details by its ID.")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getScheduleById(@PathVariable Long id) {
        ScheduleResponse schedule = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(ApiResponse.ok(schedule, "Schedule retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create collection schedule", description = "Creates a new municipal waste collection schedule. Restricted to authorities.")
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(
            @Valid @RequestBody CreateScheduleRequest request) {
        ScheduleResponse created = scheduleService.createSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Collection schedule created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update collection schedule", description = "Updates an existing collection schedule. Restricted to authorities.")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody CreateScheduleRequest request) {
        ScheduleResponse updated = scheduleService.updateSchedule(id, request);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Collection schedule updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete collection schedule", description = "Deletes a collection schedule. Restricted to authorities.")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Collection schedule deleted successfully"));
    }
}
