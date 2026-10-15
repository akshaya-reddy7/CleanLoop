package com.cleanloop.dto;

import com.cleanloop.entity.RecyclingCenter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public record RecyclingCenterResponse(
        Long id,
        String name,
        String description,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String contactPhone,
        String contactEmail,
        String operatingHours,
        String websiteUrl,
        List<String> acceptedMaterials,
        Double distanceKm
) {
    public static RecyclingCenterResponse fromEntity(RecyclingCenter c) {
        return fromEntityWithDistance(c, null);
    }

    public static RecyclingCenterResponse fromEntityWithDistance(RecyclingCenter c, Double distanceKm) {
        List<String> materials = c.getAcceptedMaterials() != null
                ? Arrays.stream(c.getAcceptedMaterials().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList()
                : List.of();

        return new RecyclingCenterResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getAddress(),
                c.getLatitude(),
                c.getLongitude(),
                c.getContactPhone(),
                c.getContactEmail(),
                c.getOperatingHours(),
                c.getWebsiteUrl(),
                materials,
                distanceKm != null ? Math.round(distanceKm * 100.0) / 100.0 : null
        );
    }
}
