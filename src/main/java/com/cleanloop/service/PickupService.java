package com.cleanloop.service;

import com.cleanloop.dto.*;
import com.cleanloop.entity.*;
import com.cleanloop.exception.BadRequestException;
import com.cleanloop.exception.ForbiddenException;
import com.cleanloop.exception.ResourceNotFoundException;
import com.cleanloop.repository.PickupRequestRepository;
import com.cleanloop.repository.PickupStatusHistoryRepository;
import com.cleanloop.repository.UserRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class PickupService {

    private static final Logger log = LoggerFactory.getLogger(PickupService.class);

    private final PickupRequestRepository pickupRepository;
    private final PickupStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final WasteCategoryService categoryService;
    private final StorageService storageService;

    public PickupService(
            PickupRequestRepository pickupRepository,
            PickupStatusHistoryRepository historyRepository,
            UserRepository userRepository,
            WasteCategoryService categoryService,
            StorageService storageService) {
        this.pickupRepository = pickupRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.storageService = storageService;
    }

    @Transactional
    public PickupResponse createPickup(
            Long requesterId,
            CreatePickupRequest request,
            MultipartFile image) {

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", requesterId));

        WasteCategory category = categoryService.getCategoryEntity(request.categoryId());

        PickupRequest pickup = new PickupRequest(
                requester,
                category,
                request.description().trim(),
                request.address().trim(),
                request.latitude(),
                request.longitude(),
                request.locality() != null ? request.locality().trim() : null,
                request.preferredDate(),
                request.timeWindow().trim(),
                request.isSpecialHandling(),
                request.specialHandlingNotes() != null ? request.specialHandlingNotes().trim() : null
        );

        if (image != null && !image.isEmpty()) {
            String storedPath = storageService.storeFile(image, "pickups");
            pickup.setImagePath(storedPath);
        }

        PickupRequest saved = pickupRepository.save(pickup);

        PickupStatusHistory history = new PickupStatusHistory(
                saved,
                null,
                PickupStatus.REQUESTED.name(),
                requester,
                "Pickup requested by citizen"
        );
        historyRepository.save(history);

        log.info("Pickup request created with ID {} by {}", saved.getId(), requester.getEmail());
        return getPickupDetail(saved.getId(), UserPrincipal.create(requester));
    }

    @Transactional(readOnly = true)
    public PickupResponse getPickupDetail(Long pickupId, UserPrincipal currentUser) {
        PickupRequest pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new ResourceNotFoundException("PickupRequest", "id", pickupId));

        if (currentUser.getRole() == Role.CITIZEN && !pickup.getRequester().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You do not have permission to view this pickup request");
        }

        List<PickupStatusHistoryResponse> history = historyRepository.findByPickupIdOrderByCreatedAtDesc(pickupId)
                .stream()
                .map(PickupStatusHistoryResponse::fromEntity)
                .toList();

        return PickupResponse.fromEntity(pickup, history);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PickupResponse> getUserPickups(Long requesterId, Pageable pageable) {
        Page<PickupRequest> page = pickupRepository.findByRequesterId(requesterId, pageable);
        return PagedResponse.from(page.map(PickupResponse::summaryFromEntity));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PickupResponse> filterPickups(
            PickupStatus status,
            Long categoryId,
            String locality,
            Long providerId,
            LocalDate preferredDate,
            Pageable pageable,
            UserPrincipal currentUser) {

        Long effectiveRequesterId = null;
        Long effectiveProviderId = providerId;

        if (currentUser.getRole() == Role.CITIZEN) {
            effectiveRequesterId = currentUser.getId();
        } else if (currentUser.getRole() == Role.SERVICE_PROVIDER && providerId == null) {
            effectiveProviderId = currentUser.getId();
        }

        Page<PickupRequest> page = pickupRepository.filterPickups(
                status,
                categoryId,
                locality,
                effectiveRequesterId,
                effectiveProviderId,
                preferredDate,
                pageable
        );

        return PagedResponse.from(page.map(PickupResponse::summaryFromEntity));
    }

    @Transactional
    public PickupResponse updatePickupStatus(
            Long pickupId,
            UpdatePickupStatusRequest request,
            UserPrincipal currentUser) {

        PickupRequest pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new ResourceNotFoundException("PickupRequest", "id", pickupId));

        User actor = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        PickupStatus currentStatus = pickup.getStatus();
        PickupStatus targetStatus = request.status();

        validateStatusTransition(currentStatus, targetStatus, currentUser.getRole());

        pickup.setStatus(targetStatus);
        if (targetStatus == PickupStatus.COMPLETED) {
            pickup.setCompletedAt(Instant.now());
            if (request.completionNotes() != null) {
                pickup.setCompletionNotes(request.completionNotes().trim());
            }
        }

        PickupRequest saved = pickupRepository.save(pickup);

        PickupStatusHistory history = new PickupStatusHistory(
                saved,
                currentStatus.name(),
                targetStatus.name(),
                actor,
                request.notes()
        );
        historyRepository.save(history);

        log.info("Pickup {} status updated from {} to {} by {}",
                pickupId, currentStatus, targetStatus, actor.getEmail());

        return getPickupDetail(pickupId, currentUser);
    }

    @Transactional
    public PickupResponse assignPickup(
            Long pickupId,
            AssignPickupRequest request,
            UserPrincipal currentUser) {

        PickupRequest pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new ResourceNotFoundException("PickupRequest", "id", pickupId));

        User actor = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        User provider = userRepository.findById(request.assignedProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assignedProviderId()));

        if (provider.getRole() != Role.SERVICE_PROVIDER && provider.getRole() != Role.AUTHORITY) {
            throw new BadRequestException("Pickups can only be assigned to SERVICE_PROVIDER or AUTHORITY accounts");
        }

        PickupStatus previousStatus = pickup.getStatus();
        pickup.setAssignedProvider(provider);

        // Advance to ASSIGNED if still in earlier stages
        if (pickup.getStatus() == PickupStatus.REQUESTED || pickup.getStatus() == PickupStatus.APPROVED || pickup.getStatus() == PickupStatus.SCHEDULED) {
            pickup.setStatus(PickupStatus.ASSIGNED);
        }

        PickupRequest saved = pickupRepository.save(pickup);

        String note = (request.notes() != null ? request.notes() + " | " : "") +
                "Assigned to " + provider.getFullName() + " (" + provider.getRole() + ")";

        PickupStatusHistory history = new PickupStatusHistory(
                saved,
                previousStatus.name(),
                saved.getStatus().name(),
                actor,
                note
        );
        historyRepository.save(history);

        log.info("Pickup {} assigned to {} by {}", pickupId, provider.getEmail(), actor.getEmail());
        return getPickupDetail(pickupId, currentUser);
    }

    @Transactional
    public PickupResponse cancelPickup(Long pickupId, String reason, UserPrincipal currentUser) {
        PickupRequest pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new ResourceNotFoundException("PickupRequest", "id", pickupId));

        User actor = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        if (currentUser.getRole() == Role.CITIZEN && !pickup.getRequester().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You cannot cancel another citizen's pickup request");
        }

        if (pickup.getStatus() == PickupStatus.COMPLETED || pickup.getStatus() == PickupStatus.CANCELLED || pickup.getStatus() == PickupStatus.REJECTED) {
            throw new BadRequestException("Cannot cancel pickup in status: " + pickup.getStatus());
        }

        PickupStatus previousStatus = pickup.getStatus();
        pickup.setStatus(PickupStatus.CANCELLED);
        PickupRequest saved = pickupRepository.save(pickup);

        PickupStatusHistory history = new PickupStatusHistory(
                saved,
                previousStatus.name(),
                PickupStatus.CANCELLED.name(),
                actor,
                reason != null ? "Cancelled: " + reason : "Cancelled by requester"
        );
        historyRepository.save(history);

        log.info("Pickup {} cancelled by {}", pickupId, actor.getEmail());
        return getPickupDetail(pickupId, currentUser);
    }

    private void validateStatusTransition(PickupStatus current, PickupStatus target, Role actorRole) {
        if (current == target) {
            return;
        }

        if ((current == PickupStatus.COMPLETED || current == PickupStatus.CANCELLED || current == PickupStatus.REJECTED)
                && actorRole != Role.ADMIN) {
            throw new BadRequestException("Terminal status " + current + " cannot be changed");
        }

        boolean valid = switch (current) {
            case REQUESTED -> Set.of(PickupStatus.APPROVED, PickupStatus.REJECTED, PickupStatus.CANCELLED).contains(target);
            case APPROVED -> Set.of(PickupStatus.SCHEDULED, PickupStatus.ASSIGNED, PickupStatus.CANCELLED, PickupStatus.REJECTED).contains(target);
            case SCHEDULED -> Set.of(PickupStatus.ASSIGNED, PickupStatus.IN_PROGRESS, PickupStatus.CANCELLED).contains(target);
            case ASSIGNED -> Set.of(PickupStatus.IN_PROGRESS, PickupStatus.CANCELLED, PickupStatus.SCHEDULED).contains(target);
            case IN_PROGRESS -> Set.of(PickupStatus.COMPLETED, PickupStatus.ASSIGNED, PickupStatus.CANCELLED).contains(target);
            case COMPLETED, CANCELLED, REJECTED -> actorRole == Role.ADMIN;
        };

        if (!valid) {
            throw new BadRequestException("Invalid pickup status transition from " + current + " to " + target);
        }
    }
}
