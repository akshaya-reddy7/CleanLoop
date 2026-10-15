package com.cleanloop.dto;

import com.cleanloop.entity.WasteReportStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateReportStatusRequest(
        @NotNull(message = "Target status is required")
        WasteReportStatus status,

        String notes,

        String resolutionNotes
) {}
