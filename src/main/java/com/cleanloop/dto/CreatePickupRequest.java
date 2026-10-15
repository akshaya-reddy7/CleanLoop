package com.cleanloop.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePickupRequest(
        @NotNull(message = "Waste category is required")
        Long categoryId,

        @NotBlank(message = "Description of waste items is required")
        String description,

        @NotBlank(message = "Pickup address is required")
        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,

        BigDecimal latitude,

        BigDecimal longitude,

        @Size(max = 100, message = "Locality must not exceed 100 characters")
        String locality,

        @NotNull(message = "Preferred pickup date is required")
        @FutureOrPresent(message = "Preferred date cannot be in the past")
        LocalDate preferredDate,

        @NotBlank(message = "Time window is required (e.g., '09:00 - 12:00')")
        @Size(max = 50, message = "Time window must not exceed 50 characters")
        String timeWindow,

        boolean isSpecialHandling,

        @Size(max = 255, message = "Special handling notes must not exceed 255 characters")
        String specialHandlingNotes
) {}
