package com.cleanloop.controller;

import com.cleanloop.dto.ApiResponse;
import com.cleanloop.dto.CreateEcoTipRequest;
import com.cleanloop.dto.EcoTipResponse;
import com.cleanloop.security.UserPrincipal;
import com.cleanloop.service.EcoTipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/eco-tips")
@Tag(name = "Eco Tips", description = "Endpoints for environmental awareness, recycling best practices, and waste reduction tips")
public class EcoTipController {

    private final EcoTipService ecoTipService;

    public EcoTipController(EcoTipService ecoTipService) {
        this.ecoTipService = ecoTipService;
    }

    @GetMapping
    @Operation(summary = "Get published eco tips", description = "Retrieves public educational recycling and waste tips, optionally filtered by category.")
    public ResponseEntity<ApiResponse<List<EcoTipResponse>>> getPublishedTips(
            @RequestParam(required = false) String category) {
        List<EcoTipResponse> tips = ecoTipService.getPublishedTips(category);
        return ResponseEntity.ok(ApiResponse.ok(tips, "Eco tips retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get eco tip by ID", description = "Retrieves details of a specific eco tip.")
    public ResponseEntity<ApiResponse<EcoTipResponse>> getTipById(@PathVariable Long id) {
        EcoTipResponse tip = ecoTipService.getTipById(id);
        return ResponseEntity.ok(ApiResponse.ok(tip, "Eco tip retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create eco tip", description = "Publishes a new educational eco tip. Restricted to authorities and administrators.")
    public ResponseEntity<ApiResponse<EcoTipResponse>> createTip(
            @Valid @RequestBody CreateEcoTipRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long authorId = principal != null ? principal.getId() : null;
        EcoTipResponse created = ecoTipService.createTip(request, authorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Eco tip created successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete eco tip", description = "Deletes an educational eco tip.")
    public ResponseEntity<ApiResponse<Void>> deleteTip(@PathVariable Long id) {
        ecoTipService.deleteTip(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Eco tip deleted successfully"));
    }
}
