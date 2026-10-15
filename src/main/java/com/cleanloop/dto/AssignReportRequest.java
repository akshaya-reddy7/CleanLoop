package com.cleanloop.dto;

import jakarta.validation.constraints.NotNull;

public record AssignReportRequest(
        @NotNull(message = "Assigned user ID is required")
        Long assignedToUserId,

        String notes
) {}
