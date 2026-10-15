package com.cleanloop.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateReportCategoryRequest(
        @NotNull(message = "Category ID is required")
        Long categoryId
) {}
