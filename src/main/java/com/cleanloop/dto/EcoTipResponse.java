package com.cleanloop.dto;

import com.cleanloop.entity.EcoTip;
import java.time.Instant;

public record EcoTipResponse(
        Long id,
        String title,
        String content,
        String category,
        String imageUrl,
        boolean isPublished,
        String createdByName,
        Instant createdAt
) {
    public static EcoTipResponse fromEntity(EcoTip tip) {
        return new EcoTipResponse(
                tip.getId(),
                tip.getTitle(),
                tip.getContent(),
                tip.getCategory(),
                tip.getImageUrl(),
                tip.isPublished(),
                tip.getCreatedBy() != null ? tip.getCreatedBy().getFullName() : null,
                tip.getCreatedAt()
        );
    }
}
