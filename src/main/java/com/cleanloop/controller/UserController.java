package com.cleanloop.controller;

import com.cleanloop.dto.ApiResponse;
import com.cleanloop.dto.ChangePasswordRequest;
import com.cleanloop.dto.UpdateProfileRequest;
import com.cleanloop.dto.UserProfileResponse;
import com.cleanloop.security.UserPrincipal;
import com.cleanloop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Endpoints for viewing and updating current user profile")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieves profile details for the authenticated user.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse profile = userService.getCurrentUserProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(profile, "Profile retrieved successfully"));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Updates personal contact information for the authenticated user.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse updatedProfile = userService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(updatedProfile, "Profile updated successfully"));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change account password", description = "Updates the account password after verifying current credentials.")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Password changed successfully"));
    }
}
