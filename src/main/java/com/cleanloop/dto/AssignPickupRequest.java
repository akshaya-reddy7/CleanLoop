package com.cleanloop.dto;

import jakarta.validation.constraints.NotNull;

public record AssignPickupRequest(
        @NotNull(message = "Service provider user ID is required")
        Long assignedProviderId,

        String notes
) {}
