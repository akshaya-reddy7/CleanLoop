package com.cleanloop.dto;

import com.cleanloop.entity.ReportImage;
import java.time.Instant;

public record ReportImageResponse(
        Long id,
        String fileName,
        String filePath,
        Long fileSizeBytes,
        String contentType,
        Instant createdAt,
        WasteClassificationResponse classification
) {
    public static ReportImageResponse fromEntity(ReportImage image) {
        if (image == null) return null;
        return new ReportImageResponse(
                image.getId(),
                image.getFileName(),
                image.getFilePath(),
                image.getFileSizeBytes(),
                image.getContentType(),
                image.getCreatedAt(),
                WasteClassificationResponse.fromEntity(image.getClassification())
        );
    }
}
