package com.cleanloop.dto;

import com.cleanloop.entity.PickupStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePickupStatusRequest(
        @NotNull(message = "Status is required")
        PickupStatus status,

        String notes,

        String completionNotes
) {}
