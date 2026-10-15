package com.cleanloop.controller;

import com.cleanloop.dto.ApiResponse;
import com.cleanloop.dto.CreateWasteCategoryRequest;
import com.cleanloop.dto.WasteCategoryResponse;
import com.cleanloop.service.WasteCategoryService;
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
@RequestMapping("/api/v1/categories")
@Tag(name = "Waste Categories", description = "Endpoints for retrieving and managing standard waste categories")
public class WasteCategoryController {

    private final WasteCategoryService categoryService;

    public WasteCategoryController(WasteCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Get all waste categories", description = "Retrieves all standard and AI-supported waste categories.")
    public ResponseEntity<ApiResponse<List<WasteCategoryResponse>>> getAllCategories() {
        List<WasteCategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.ok(categories, "Waste categories retrieved successfully"));
    }

    @GetMapping("/recyclable")
    @Operation(summary = "Get recyclable categories", description = "Retrieves categories designated as recyclable.")
    public ResponseEntity<ApiResponse<List<WasteCategoryResponse>>> getRecyclableCategories() {
        List<WasteCategoryResponse> categories = categoryService.getRecyclableCategories();
        return ResponseEntity.ok(ApiResponse.ok(categories, "Recyclable categories retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get waste category by ID", description = "Retrieves details of a specific waste category.")
    public ResponseEntity<ApiResponse<WasteCategoryResponse>> getCategoryById(@PathVariable Long id) {
        WasteCategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.ok(category, "Waste category retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create waste category (Admin)", description = "Creates a new waste category. Restricted to administrators.")
    public ResponseEntity<ApiResponse<WasteCategoryResponse>> createCategory(
            @Valid @RequestBody CreateWasteCategoryRequest request) {
        WasteCategoryResponse created = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Waste category created successfully"));
    }
}
