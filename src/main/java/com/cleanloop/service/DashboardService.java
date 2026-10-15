package com.cleanloop.service;

import com.cleanloop.dto.*;
import com.cleanloop.entity.PickupStatus;
import com.cleanloop.entity.Role;
import com.cleanloop.entity.WasteReportStatus;
import com.cleanloop.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final WasteReportRepository reportRepository;
    private final PickupRequestRepository pickupRepository;
    private final CollectionScheduleRepository scheduleRepository;
    private final RecyclingCenterRepository centerRepository;

    public DashboardService(
            UserRepository userRepository,
            WasteReportRepository reportRepository,
            PickupRequestRepository pickupRepository,
            CollectionScheduleRepository scheduleRepository,
            RecyclingCenterRepository centerRepository) {
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.pickupRepository = pickupRepository;
        this.scheduleRepository = scheduleRepository;
        this.centerRepository = centerRepository;
    }

    @Transactional(readOnly = true)
    public CitizenDashboardResponse getCitizenDashboard(Long userId) {
        long totalReports = reportRepository.countByReporterId(userId);
        long resolvedReports = reportRepository.countByReporterIdAndStatus(userId, WasteReportStatus.RESOLVED);
        long rejectedReports = reportRepository.countByReporterIdAndStatus(userId, WasteReportStatus.REJECTED);
        long pendingReports = Math.max(0, totalReports - resolvedReports - rejectedReports);

        long requested = pickupRepository.countByRequesterIdAndStatus(userId, PickupStatus.REQUESTED);
        long approved = pickupRepository.countByRequesterIdAndStatus(userId, PickupStatus.APPROVED);
        long scheduled = pickupRepository.countByRequesterIdAndStatus(userId, PickupStatus.SCHEDULED);
        long assigned = pickupRepository.countByRequesterIdAndStatus(userId, PickupStatus.ASSIGNED);
        long inProgress = pickupRepository.countByRequesterIdAndStatus(userId, PickupStatus.IN_PROGRESS);
        long activePickups = requested + approved + scheduled + assigned + inProgress;

        long completedPickups = pickupRepository.countByRequesterIdAndStatus(userId, PickupStatus.COMPLETED);

        List<ScheduleResponse> upcomingSchedules = scheduleRepository.findByIsActiveTrue().stream()
                .limit(5)
                .map(ScheduleResponse::fromEntity)
                .toList();

        List<WasteReportSummaryResponse> recentReports = reportRepository.findByReporterId(
                userId,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent().stream()
                .map(WasteReportSummaryResponse::fromEntity)
                .toList();

        return new CitizenDashboardResponse(
                totalReports,
                resolvedReports,
                pendingReports,
                activePickups,
                completedPickups,
                upcomingSchedules,
                recentReports
        );
    }

    @Transactional(readOnly = true)
    public AuthorityDashboardResponse getAuthorityDashboard(Long authorityUserId) {
        long submitted = reportRepository.countByStatus(WasteReportStatus.SUBMITTED);
        long underReview = reportRepository.countByStatus(WasteReportStatus.UNDER_REVIEW);
        long assigned = reportRepository.countByStatus(WasteReportStatus.ASSIGNED);
        long inProgress = reportRepository.countByStatus(WasteReportStatus.IN_PROGRESS);
        long resolved = reportRepository.countByStatus(WasteReportStatus.RESOLVED);

        long pendingPickups = pickupRepository.countByStatus(PickupStatus.REQUESTED);
        long activePickups = pickupRepository.countByStatus(PickupStatus.APPROVED)
                + pickupRepository.countByStatus(PickupStatus.SCHEDULED)
                + pickupRepository.countByStatus(PickupStatus.ASSIGNED)
                + pickupRepository.countByStatus(PickupStatus.IN_PROGRESS);

        long activeSchedulesCount = scheduleRepository.count();

        List<WasteReportSummaryResponse> pendingActionReports = reportRepository.findByStatus(
                WasteReportStatus.SUBMITTED,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent().stream()
                .map(WasteReportSummaryResponse::fromEntity)
                .toList();

        return new AuthorityDashboardResponse(
                submitted,
                underReview,
                assigned,
                inProgress,
                resolved,
                pendingPickups,
                activePickups,
                activeSchedulesCount,
                pendingActionReports
        );
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {
        long totalUsers = userRepository.count();
        long citizenCount = userRepository.countByRole(Role.CITIZEN);
        long authorityCount = userRepository.countByRole(Role.AUTHORITY);
        long serviceProviderCount = userRepository.countByRole(Role.SERVICE_PROVIDER);

        long totalReports = reportRepository.count();
        long totalPickups = pickupRepository.count();
        long totalRecyclingCenters = centerRepository.count();
        long totalSchedules = scheduleRepository.count();

        Map<String, Long> reportsByStatus = new HashMap<>();
        for (WasteReportStatus status : WasteReportStatus.values()) {
            reportsByStatus.put(status.name(), reportRepository.countByStatus(status));
        }

        Map<String, Long> pickupsByStatus = new HashMap<>();
        for (PickupStatus status : PickupStatus.values()) {
            pickupsByStatus.put(status.name(), pickupRepository.countByStatus(status));
        }

        return new AdminDashboardResponse(
                totalUsers,
                citizenCount,
                authorityCount,
                serviceProviderCount,
                totalReports,
                totalPickups,
                totalRecyclingCenters,
                totalSchedules,
                reportsByStatus,
                pickupsByStatus
        );
    }
}
