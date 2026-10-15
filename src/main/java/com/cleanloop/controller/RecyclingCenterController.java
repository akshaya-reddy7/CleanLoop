package com.cleanloop.controller;

import com.cleanloop.dto.ApiResponse;
import com.cleanloop.dto.CreateRecyclingCenterRequest;
import com.cleanloop.dto.RecyclingCenterResponse;
import com.cleanloop.service.RecyclingCenterService;
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
@RequestMapping("/api/v1/recycling-centers")
@Tag(name = "Recycling Centers", description = "Endpoints for discovering nearby recycling facilities and material drop-off points")
public class RecyclingCenterController {

    private final RecyclingCenterService centerService;

    public RecyclingCenterController(RecyclingCenterService centerService) {
        this.centerService = centerService;
    }

    @GetMapping
    @Operation(summary = "Search recycling centers", description = "Finds recycling centers with optional nearby geolocation and material filters.")
    public ResponseEntity<ApiResponse<List<RecyclingCenterResponse>>> searchCenters(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false) String material) {
        List<RecyclingCenterResponse> centers = centerService.searchCenters(lat, lng, radiusKm, material);
        return ResponseEntity.ok(ApiResponse.ok(centers, "Recycling centers retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recycling center by ID", description = "Retrieves full details of a specific recycling center.")
    public ResponseEntity<ApiResponse<RecyclingCenterResponse>> getCenterById(@PathVariable Long id) {
        RecyclingCenterResponse center = centerService.getCenterById(id);
        return ResponseEntity.ok(ApiResponse.ok(center, "Recycling center retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create recycling center", description = "Registers a new recycling center. Restricted to authorities and administrators.")
    public ResponseEntity<ApiResponse<RecyclingCenterResponse>> createCenter(
            @Valid @RequestBody CreateRecyclingCenterRequest request) {
        RecyclingCenterResponse created = centerService.createCenter(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Recycling center created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update recycling center", description = "Updates details of an existing recycling center.")
    public ResponseEntity<ApiResponse<RecyclingCenterResponse>> updateCenter(
            @PathVariable Long id,
            @Valid @RequestBody CreateRecyclingCenterRequest request) {
        RecyclingCenterResponse updated = centerService.updateCenter(id, request);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Recycling center updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete recycling center", description = "Removes a recycling center from the system.")
    public ResponseEntity<ApiResponse<Void>> deleteCenter(@PathVariable Long id) {
        centerService.deleteCenter(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Recycling center deleted successfully"));
    }
}
