package com.cleanloop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record CreateScheduleRequest(
        @NotBlank(message = "Locality is required")
        @Size(max = 100, message = "Locality must not exceed 100 characters")
        String locality,

        @NotBlank(message = "Service area is required")
        @Size(max = 100, message = "Service area must not exceed 100 characters")
        String serviceArea,

        @NotBlank(message = "Collection day of week is required")
        @Size(max = 20, message = "Collection day of week must not exceed 20 characters")
        String collectionDayOfWeek,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        Long assignedProviderId,

        @Size(max = 255, message = "Notes must not exceed 255 characters")
        String notes
) {}
