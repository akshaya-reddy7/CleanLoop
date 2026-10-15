package com.cleanloop.dto;

import com.cleanloop.entity.WasteCategory;

public record WasteCategoryResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean isRecyclable
) {
    public static WasteCategoryResponse fromEntity(WasteCategory category) {
        if (category == null) return null;
        return new WasteCategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getDescription(),
                category.isRecyclable()
        );
    }
}
