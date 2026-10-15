package com.cleanloop.controller;

import com.cleanloop.dto.ApiResponse;
import com.cleanloop.dto.AuditLogResponse;
import com.cleanloop.dto.PagedResponse;
import com.cleanloop.dto.UserProfileResponse;
import com.cleanloop.entity.Role;
import com.cleanloop.security.UserPrincipal;
import com.cleanloop.service.AuditService;
import com.cleanloop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administrator Operations", description = "Endpoints for user management, role provisioning, and audit log inspection")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserService userService;
    private final AuditService auditService;

    public AdminController(UserService userService, AuditService auditService) {
        this.userService = userService;
        this.auditService = auditService;
    }

    @GetMapping("/users")
    @Operation(summary = "List and filter all users", description = "Retrieves paginated user accounts with optional role filter.")
    public ResponseEntity<ApiResponse<PagedResponse<UserProfileResponse>>> getAllUsers(
            @RequestParam(required = false) Role role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<UserProfileResponse> users = userService.getAllUsers(role, pageable);
        return ResponseEntity.ok(ApiResponse.ok(users, "Users retrieved successfully"));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Update user role", description = "Promotes or updates user permissions (e.g., granting AUTHORITY or SERVICE_PROVIDER role).")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role,
            @AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse updated = userService.updateUserRole(id, role);
        auditService.logAction(
                userService.getUserById(principal.getId()),
                "UPDATE_USER_ROLE",
                "User",
                String.valueOf(id),
                "Role updated to " + role,
                null
        );
        return ResponseEntity.ok(ApiResponse.ok(updated, "User role updated successfully"));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Update user active status", description = "Deactivates or activates a user account.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            @AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse updated = userService.updateUserStatus(id, active);
        auditService.logAction(
                userService.getUserById(principal.getId()),
                active ? "ACTIVATE_USER" : "DEACTIVATE_USER",
                "User",
                String.valueOf(id),
                "Status set to active=" + active,
                null
        );
        return ResponseEntity.ok(ApiResponse.ok(updated, "User status updated successfully"));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "View audit logs", description = "Inspects chronological audit records of administrative operations.")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAuditLogs(
            @PageableDefault(size = 25, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<AuditLogResponse> logs = auditService.getAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.ok(logs, "Audit logs retrieved successfully"));
    }
}
