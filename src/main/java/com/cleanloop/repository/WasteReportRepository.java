package com.cleanloop.repository;

import com.cleanloop.entity.WasteReport;
import com.cleanloop.entity.WasteReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface WasteReportRepository extends JpaRepository<WasteReport, Long> {

    Page<WasteReport> findByReporterId(Long reporterId, Pageable pageable);

    Page<WasteReport> findByStatus(WasteReportStatus status, Pageable pageable);

    Page<WasteReport> findByAssignedToId(Long assignedToId, Pageable pageable);

    Page<WasteReport> findByLocality(String locality, Pageable pageable);

    long countByReporterId(Long reporterId);

    long countByReporterIdAndStatus(Long reporterId, WasteReportStatus status);

    long countByStatus(WasteReportStatus status);

    long countByAssignedToIdAndStatus(Long assignedToId, WasteReportStatus status);

    long countByCreatedAtAfter(Instant after);

    @Query("""
        SELECT r FROM WasteReport r
        WHERE (:status IS NULL OR r.status = :status)
          AND (:categoryId IS NULL OR r.userCategory.id = :categoryId)
          AND (:locality IS NULL OR LOWER(r.locality) LIKE LOWER(CONCAT('%', :locality, '%')))
          AND (:reporterId IS NULL OR r.reporter.id = :reporterId)
          AND (:assignedToId IS NULL OR r.assignedTo.id = :assignedToId)
        ORDER BY r.createdAt DESC
    """)
    Page<WasteReport> filterReports(
            @Param("status") WasteReportStatus status,
            @Param("categoryId") Long categoryId,
            @Param("locality") String locality,
            @Param("reporterId") Long reporterId,
            @Param("assignedToId") Long assignedToId,
            Pageable pageable
    );

    @Query("SELECT r FROM WasteReport r WHERE r.latitude IS NOT NULL AND r.longitude IS NOT NULL AND r.status != 'REJECTED'")
    List<WasteReport> findAllActiveMapMarkers();
}
