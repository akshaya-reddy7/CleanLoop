package com.cleanloop.service;

import com.cleanloop.dto.CreateScheduleRequest;
import com.cleanloop.dto.ScheduleResponse;
import com.cleanloop.entity.CollectionSchedule;
import com.cleanloop.entity.User;
import com.cleanloop.entity.WasteCategory;
import com.cleanloop.exception.ResourceNotFoundException;
import com.cleanloop.repository.CollectionScheduleRepository;
import com.cleanloop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CollectionScheduleService {

    private static final Logger log = LoggerFactory.getLogger(CollectionScheduleService.class);

    private final CollectionScheduleRepository scheduleRepository;
    private final WasteCategoryService categoryService;
    private final UserRepository userRepository;

    public CollectionScheduleService(
            CollectionScheduleRepository scheduleRepository,
            WasteCategoryService categoryService,
            UserRepository userRepository) {
        this.scheduleRepository = scheduleRepository;
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAllActiveSchedules() {
        return scheduleRepository.findByIsActiveTrue().stream()
                .map(ScheduleResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesByLocality(String locality) {
        return scheduleRepository.findByLocalityIgnoreCaseAndIsActiveTrue(locality).stream()
                .map(ScheduleResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ScheduleResponse getScheduleById(Long id) {
        CollectionSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CollectionSchedule", "id", id));
        return ScheduleResponse.fromEntity(schedule);
    }

    @Transactional
    public ScheduleResponse createSchedule(CreateScheduleRequest request) {
        WasteCategory category = categoryService.getCategoryEntity(request.categoryId());

        User provider = null;
        if (request.assignedProviderId() != null) {
            provider = userRepository.findById(request.assignedProviderId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assignedProviderId()));
        }

        CollectionSchedule schedule = new CollectionSchedule(
                request.locality().trim(),
                request.serviceArea().trim(),
                request.collectionDayOfWeek().trim().toUpperCase(),
                request.startTime(),
                request.endTime(),
                category,
                provider,
                request.notes() != null ? request.notes().trim() : null
        );

        CollectionSchedule saved = scheduleRepository.save(schedule);
        log.info("Created collection schedule with ID {} for locality {}", saved.getId(), saved.getLocality());
        return ScheduleResponse.fromEntity(saved);
    }

    @Transactional
    public ScheduleResponse updateSchedule(Long id, CreateScheduleRequest request) {
        CollectionSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CollectionSchedule", "id", id));

        WasteCategory category = categoryService.getCategoryEntity(request.categoryId());

        User provider = null;
        if (request.assignedProviderId() != null) {
            provider = userRepository.findById(request.assignedProviderId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assignedProviderId()));
        }

        schedule.setLocality(request.locality().trim());
        schedule.setServiceArea(request.serviceArea().trim());
        schedule.setCollectionDayOfWeek(request.collectionDayOfWeek().trim().toUpperCase());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setCategory(category);
        schedule.setAssignedProvider(provider);
        schedule.setNotes(request.notes() != null ? request.notes().trim() : null);

        CollectionSchedule updated = scheduleRepository.save(schedule);
        log.info("Updated collection schedule ID {}", updated.getId());
        return ScheduleResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        CollectionSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CollectionSchedule", "id", id));
        scheduleRepository.delete(schedule);
        log.info("Deleted collection schedule ID {}", id);
    }
}
