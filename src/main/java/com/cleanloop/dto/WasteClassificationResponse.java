package com.cleanloop.dto;

import com.cleanloop.entity.ClassificationStatus;
import com.cleanloop.entity.WasteClassification;
import java.math.BigDecimal;
import java.time.Instant;

public record WasteClassificationResponse(
        Long id,
        WasteCategoryResponse predictedCategory,
        BigDecimal confidenceScore,
        String modelVersion,
        ClassificationStatus status,
        String errorMessage,
        Instant createdAt
) {
    public static WasteClassificationResponse fromEntity(WasteClassification classification) {
        if (classification == null) return null;
        return new WasteClassificationResponse(
                classification.getId(),
                WasteCategoryResponse.fromEntity(classification.getPredictedCategory()),
                classification.getConfidenceScore(),
                classification.getModelVersion(),
                classification.getClassificationStatus(),
                classification.getErrorMessage(),
                classification.getCreatedAt()
        );
    }
}
