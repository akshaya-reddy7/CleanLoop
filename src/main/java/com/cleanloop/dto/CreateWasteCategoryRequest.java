package com.cleanloop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWasteCategoryRequest(
        @NotBlank(message = "Category code is required")
        @Size(max = 50, message = "Category code must not exceed 50 characters")
        String code,

        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name must not exceed 100 characters")
        String name,

        String description,

        boolean isRecyclable
) {}
