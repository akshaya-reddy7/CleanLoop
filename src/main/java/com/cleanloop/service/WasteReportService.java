package com.cleanloop.service;

import com.cleanloop.dto.*;
import com.cleanloop.entity.*;
import com.cleanloop.exception.BadRequestException;
import com.cleanloop.exception.ForbiddenException;
import com.cleanloop.exception.ResourceNotFoundException;
import com.cleanloop.repository.ReportImageRepository;
import com.cleanloop.repository.ReportStatusHistoryRepository;
import com.cleanloop.repository.UserRepository;
import com.cleanloop.repository.WasteReportRepository;
import com.cleanloop.security.UserPrincipal;
import com.cleanloop.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class WasteReportService {

    private static final Logger log = LoggerFactory.getLogger(WasteReportService.class);

    private final WasteReportRepository reportRepository;
    private final ReportImageRepository imageRepository;
    private final ReportStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final WasteCategoryService categoryService;
    private final StorageService storageService;
    private final com.cleanloop.ai.AiClassificationService aiClassificationService;

    public WasteReportService(
            WasteReportRepository reportRepository,
            ReportImageRepository imageRepository,
            ReportStatusHistoryRepository historyRepository,
            UserRepository userRepository,
            WasteCategoryService categoryService,
            StorageService storageService,
            com.cleanloop.ai.AiClassificationService aiClassificationService) {
        this.reportRepository = reportRepository;
        this.imageRepository = imageRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.storageService = storageService;
        this.aiClassificationService = aiClassificationService;
    }

    @Transactional
    public WasteReportDetailResponse createReport(
            Long reporterId,
            CreateReportRequest request,
            List<MultipartFile> imageFiles) {

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reporterId));

        WasteCategory userCategory = null;
        if (request.userCategoryId() != null) {
            userCategory = categoryService.getCategoryEntity(request.userCategoryId());
        }

        WasteReport report = new WasteReport(
                reporter,
                request.title().trim(),
                request.description().trim(),
                request.issueType().trim(),
                userCategory,
                request.latitude(),
                request.longitude(),
                request.address() != null ? request.address().trim() : null,
                request.locality() != null ? request.locality().trim() : null
        );

        WasteReport savedReport = reportRepository.save(report);

        // Store attached images and trigger AI classification
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    String storedPath = storageService.storeFile(file, "reports");
                    ReportImage image = new ReportImage(
                            savedReport,
                            file.getOriginalFilename(),
                            storedPath,
                            file.getSize(),
                            file.getContentType()
                    );
                    savedReport.addImage(image);
                    ReportImage savedImage = imageRepository.save(image);
                    try {
                        aiClassificationService.classifyReportImage(savedImage);
                    } catch (Exception ex) {
                        log.warn("Non-blocking AI classification error for image: {}", ex.getMessage());
                    }
                }
            }
            savedReport = reportRepository.save(savedReport);
        }

        // Record initial status history
        ReportStatusHistory initialHistory = new ReportStatusHistory(
                savedReport,
                null,
                WasteReportStatus.SUBMITTED.name(),
                reporter,
                "Report submitted by citizen"
        );
        historyRepository.save(initialHistory);

        log.info("Waste report created with ID: {} by user: {}", savedReport.getId(), reporter.getEmail());
        return getReportDetail(savedReport.getId(), UserPrincipal.create(reporter));
    }

    @Transactional(readOnly = true)
    public WasteReportDetailResponse getReportDetail(Long reportId, UserPrincipal currentUser) {
        WasteReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("WasteReport", "id", reportId));

        // Ownership enforcement for citizens
        if (currentUser.getRole() == Role.CITIZEN && !report.getReporter().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You do not have permission to view this report");
        }

        List<ReportStatusHistoryResponse> history = historyRepository.findByReportIdOrderByCreatedAtDesc(reportId)
                .stream()
                .map(ReportStatusHistoryResponse::fromEntity)
                .toList();

        return WasteReportDetailResponse.fromEntity(report, history);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WasteReportSummaryResponse> getUserReports(Long reporterId, Pageable pageable) {
        Page<WasteReport> page = reportRepository.findByReporterId(reporterId, pageable);
        return PagedResponse.from(page.map(WasteReportSummaryResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public PagedResponse<WasteReportSummaryResponse> filterReports(
            WasteReportStatus status,
            Long categoryId,
            String locality,
            Long assignedToId,
            Pageable pageable,
            UserPrincipal currentUser) {

        Long effectiveReporterId = null;
        Long effectiveAssignedToId = assignedToId;

        // If user is a citizen, restrict query strictly to their own reports
        if (currentUser.getRole() == Role.CITIZEN) {
            effectiveReporterId = currentUser.getId();
        } else if (currentUser.getRole() == Role.SERVICE_PROVIDER && assignedToId == null) {
            effectiveAssignedToId = currentUser.getId();
        }

        Page<WasteReport> page = reportRepository.filterReports(
                status,
                categoryId,
                locality,
                effectiveReporterId,
                effectiveAssignedToId,
                pageable
        );

        return PagedResponse.from(page.map(WasteReportSummaryResponse::fromEntity));
    }

    @Transactional
    public WasteReportDetailResponse updateReportStatus(
            Long reportId,
            UpdateReportStatusRequest request,
            UserPrincipal currentUser) {

        WasteReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("WasteReport", "id", reportId));

        User actor = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        WasteReportStatus currentStatus = report.getStatus();
        WasteReportStatus targetStatus = request.status();

        validateStatusTransition(currentStatus, targetStatus, currentUser.getRole());

        report.setStatus(targetStatus);
        if (targetStatus == WasteReportStatus.RESOLVED) {
            report.setResolvedAt(Instant.now());
            if (request.resolutionNotes() != null) {
                report.setResolutionNotes(request.resolutionNotes().trim());
            }
        }

        WasteReport updated = reportRepository.save(report);

        ReportStatusHistory statusHistory = new ReportStatusHistory(
                updated,
                currentStatus.name(),
                targetStatus.name(),
                actor,
                request.notes()
        );
        historyRepository.save(statusHistory);

        log.info("Report {} status transitioned from {} to {} by {}",
                reportId, currentStatus, targetStatus, actor.getEmail());

        return getReportDetail(reportId, currentUser);
    }

    @Transactional
    public WasteReportDetailResponse assignReport(
            Long reportId,
            AssignReportRequest request,
            UserPrincipal currentUser) {

        WasteReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("WasteReport", "id", reportId));

        User actor = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        User assignee = userRepository.findById(request.assignedToUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assignedToUserId()));

        if (assignee.getRole() != Role.AUTHORITY && assignee.getRole() != Role.SERVICE_PROVIDER) {
            throw new BadRequestException("Reports can only be assigned to AUTHORITY or SERVICE_PROVIDER accounts");
        }

        WasteReportStatus previousStatus = report.getStatus();
        report.setAssignedTo(assignee);

        // Advance to ASSIGNED if still submitted or under review
        if (report.getStatus() == WasteReportStatus.SUBMITTED || report.getStatus() == WasteReportStatus.UNDER_REVIEW) {
            report.setStatus(WasteReportStatus.ASSIGNED);
        }

        WasteReport updated = reportRepository.save(report);

        String note = (request.notes() != null ? request.notes() + " | " : "") +
                "Assigned to " + assignee.getFullName() + " (" + assignee.getRole() + ")";

        ReportStatusHistory statusHistory = new ReportStatusHistory(
                updated,
                previousStatus.name(),
                updated.getStatus().name(),
                actor,
                note
        );
        historyRepository.save(statusHistory);

        log.info("Report {} assigned to {} by {}", reportId, assignee.getEmail(), actor.getEmail());
        return getReportDetail(reportId, currentUser);
    }

    @Transactional
    public WasteReportDetailResponse updateUserCategory(
            Long reportId,
            UpdateReportCategoryRequest request,
            UserPrincipal currentUser) {

        WasteReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("WasteReport", "id", reportId));

        // Ownership or authority check
        if (currentUser.getRole() == Role.CITIZEN && !report.getReporter().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You cannot modify another user's report category");
        }

        WasteCategory category = categoryService.getCategoryEntity(request.categoryId());
        report.setUserCategory(category);
        reportRepository.save(report);

        log.info("Report {} category updated to {} by {}", reportId, category.getCode(), currentUser.getUsername());
        return getReportDetail(reportId, currentUser);
    }

    @Transactional
    public WasteReportDetailResponse classifyImageOnDemand(
            Long reportId,
            Long imageId,
            UserPrincipal currentUser) {

        WasteReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("WasteReport", "id", reportId));

        if (currentUser.getRole() == Role.CITIZEN && !report.getReporter().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You cannot request classification for another user's report");
        }

        ReportImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ReportImage", "id", imageId));

        if (!image.getReport().getId().equals(reportId)) {
            throw new BadRequestException("Image does not belong to the specified report");
        }

        aiClassificationService.classifyReportImage(image);
        return getReportDetail(reportId, currentUser);
    }

    private void validateStatusTransition(WasteReportStatus current, WasteReportStatus target, Role actorRole) {
        if (current == target) {
            return;
        }

        // Only ADMIN can reopen a resolved report
        if (current == WasteReportStatus.RESOLVED && actorRole != Role.ADMIN) {
            throw new BadRequestException("Only administrators can reopen a resolved report");
        }

        // Terminal rejected state
        if (current == WasteReportStatus.REJECTED && actorRole != Role.ADMIN) {
            throw new BadRequestException("Rejected reports cannot be transitioned to another status");
        }

        boolean valid = switch (current) {
            case SUBMITTED -> Set.of(WasteReportStatus.UNDER_REVIEW, WasteReportStatus.ASSIGNED, WasteReportStatus.REJECTED).contains(target);
            case UNDER_REVIEW -> Set.of(WasteReportStatus.ASSIGNED, WasteReportStatus.IN_PROGRESS, WasteReportStatus.REJECTED).contains(target);
            case ASSIGNED -> Set.of(WasteReportStatus.IN_PROGRESS, WasteReportStatus.UNDER_REVIEW, WasteReportStatus.REJECTED).contains(target);
            case IN_PROGRESS -> Set.of(WasteReportStatus.RESOLVED, WasteReportStatus.ASSIGNED, WasteReportStatus.REJECTED).contains(target);
            case RESOLVED -> target == WasteReportStatus.IN_PROGRESS;
            case REJECTED -> target == WasteReportStatus.UNDER_REVIEW;
        };

        if (!valid) {
            throw new BadRequestException("Invalid status transition from " + current + " to " + target);
        }
    }
}
