package com.cleanloop.repository;

import com.cleanloop.entity.PickupRequest;
import com.cleanloop.entity.PickupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PickupRequestRepository extends JpaRepository<PickupRequest, Long> {

    Page<PickupRequest> findByRequesterId(Long requesterId, Pageable pageable);

    Page<PickupRequest> findByAssignedProviderId(Long providerId, Pageable pageable);

    Page<PickupRequest> findByStatus(PickupStatus status, Pageable pageable);

    long countByRequesterId(Long requesterId);

    long countByRequesterIdAndStatus(Long requesterId, PickupStatus status);

    long countByAssignedProviderIdAndStatus(Long providerId, PickupStatus status);

    long countByStatus(PickupStatus status);

    @Query("""
        SELECT p FROM PickupRequest p
        WHERE (:status IS NULL OR p.status = :status)
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:locality IS NULL OR LOWER(p.locality) LIKE LOWER(CONCAT('%', :locality, '%')))
          AND (:requesterId IS NULL OR p.requester.id = :requesterId)
          AND (:providerId IS NULL OR p.assignedProvider.id = :providerId)
          AND (:preferredDate IS NULL OR p.preferredDate = :preferredDate)
        ORDER BY p.preferredDate ASC, p.createdAt DESC
    """)
    Page<PickupRequest> filterPickups(
            @Param("status") PickupStatus status,
            @Param("categoryId") Long categoryId,
            @Param("locality") String locality,
            @Param("requesterId") Long requesterId,
            @Param("providerId") Long providerId,
            @Param("preferredDate") LocalDate preferredDate,
            Pageable pageable
    );

    @Query("SELECT p FROM PickupRequest p WHERE p.latitude IS NOT NULL AND p.longitude IS NOT NULL AND p.status IN ('APPROVED', 'SCHEDULED', 'ASSIGNED', 'IN_PROGRESS')")
    List<PickupRequest> findActiveMapPickups();
}
