package com.cleanloop.service;

import com.cleanloop.dto.CreateRecyclingCenterRequest;
import com.cleanloop.dto.RecyclingCenterResponse;
import com.cleanloop.entity.RecyclingCenter;
import com.cleanloop.exception.ResourceNotFoundException;
import com.cleanloop.repository.RecyclingCenterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class RecyclingCenterService {

    private static final Logger log = LoggerFactory.getLogger(RecyclingCenterService.class);
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final RecyclingCenterRepository centerRepository;

    public RecyclingCenterService(RecyclingCenterRepository centerRepository) {
        this.centerRepository = centerRepository;
    }

    @Transactional(readOnly = true)
    public List<RecyclingCenterResponse> getAllActiveCenters() {
        return centerRepository.findByIsActiveTrue().stream()
                .map(RecyclingCenterResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecyclingCenterResponse getCenterById(Long id) {
        RecyclingCenter center = centerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecyclingCenter", "id", id));
        return RecyclingCenterResponse.fromEntity(center);
    }

    @Transactional(readOnly = true)
    public List<RecyclingCenterResponse> searchCenters(
            Double userLat,
            Double userLng,
            Double radiusKm,
            String material) {

        List<RecyclingCenter> centers = centerRepository.findByIsActiveTrue();

        // Filter by material if specified
        if (material != null && !material.isBlank()) {
            String target = material.trim().toLowerCase();
            centers = centers.stream()
                    .filter(c -> c.getAcceptedMaterials() != null &&
                            c.getAcceptedMaterials().toLowerCase().contains(target))
                    .toList();
        }

        // If coordinates provided, compute Haversine distance and sort
        if (userLat != null && userLng != null) {
            double maxRadius = (radiusKm != null && radiusKm > 0) ? radiusKm : 50.0;

            return centers.stream()
                    .map(c -> {
                        double distance = calculateDistanceKm(
                                userLat, userLng,
                                c.getLatitude().doubleValue(),
                                c.getLongitude().doubleValue()
                        );
                        return RecyclingCenterResponse.fromEntityWithDistance(c, distance);
                    })
                    .filter(r -> r.distanceKm() == null || r.distanceKm() <= maxRadius)
                    .sorted(Comparator.comparing(RecyclingCenterResponse::distanceKm, Comparator.nullsLast(Double::compareTo)))
                    .toList();
        }

        return centers.stream()
                .map(RecyclingCenterResponse::fromEntity)
                .toList();
    }

    @Transactional
    public RecyclingCenterResponse createCenter(CreateRecyclingCenterRequest request) {
        RecyclingCenter center = new RecyclingCenter(
                request.name().trim(),
                request.description(),
                request.address().trim(),
                request.latitude(),
                request.longitude(),
                request.contactPhone(),
                request.contactEmail(),
                request.operatingHours(),
                request.websiteUrl(),
                request.acceptedMaterials().trim().toUpperCase()
        );

        RecyclingCenter saved = centerRepository.save(center);
        log.info("Created recycling center: {} with ID {}", saved.getName(), saved.getId());
        return RecyclingCenterResponse.fromEntity(saved);
    }

    @Transactional
    public RecyclingCenterResponse updateCenter(Long id, CreateRecyclingCenterRequest request) {
        RecyclingCenter center = centerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecyclingCenter", "id", id));

        center.setName(request.name().trim());
        center.setDescription(request.description());
        center.setAddress(request.address().trim());
        center.setLatitude(request.latitude());
        center.setLongitude(request.longitude());
        center.setContactPhone(request.contactPhone());
        center.setContactEmail(request.contactEmail());
        center.setOperatingHours(request.operatingHours());
        center.setWebsiteUrl(request.websiteUrl());
        center.setAcceptedMaterials(request.acceptedMaterials().trim().toUpperCase());

        RecyclingCenter updated = centerRepository.save(center);
        log.info("Updated recycling center ID: {}", updated.getId());
        return RecyclingCenterResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteCenter(Long id) {
        RecyclingCenter center = centerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecyclingCenter", "id", id));
        centerRepository.delete(center);
        log.info("Deleted recycling center ID: {}", id);
    }

    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
