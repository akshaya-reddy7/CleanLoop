package com.cleanloop.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateRecyclingCenterRequest(
        @NotBlank(message = "Center name is required")
        @Size(max = 150, message = "Center name must not exceed 150 characters")
        String name,

        String description,

        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
        BigDecimal latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
        BigDecimal longitude,

        @Size(max = 30, message = "Contact phone must not exceed 30 characters")
        String contactPhone,

        @Size(max = 100, message = "Contact email must not exceed 100 characters")
        String contactEmail,

        @Size(max = 100, message = "Operating hours must not exceed 100 characters")
        String operatingHours,

        @Size(max = 255, message = "Website URL must not exceed 255 characters")
        String websiteUrl,

        @NotBlank(message = "Accepted materials must be specified (comma-separated)")
        @Size(max = 255, message = "Accepted materials must not exceed 255 characters")
        String acceptedMaterials
) {}
