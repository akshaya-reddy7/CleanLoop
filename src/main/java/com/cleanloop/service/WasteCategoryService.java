package com.cleanloop.service;

import com.cleanloop.dto.CreateWasteCategoryRequest;
import com.cleanloop.dto.WasteCategoryResponse;
import com.cleanloop.entity.WasteCategory;
import com.cleanloop.exception.ConflictException;
import com.cleanloop.exception.ResourceNotFoundException;
import com.cleanloop.repository.WasteCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WasteCategoryService {

    private static final Logger log = LoggerFactory.getLogger(WasteCategoryService.class);

    private final WasteCategoryRepository categoryRepository;

    public WasteCategoryService(WasteCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<WasteCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(WasteCategoryResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WasteCategoryResponse> getRecyclableCategories() {
        return categoryRepository.findByIsRecyclableTrue().stream()
                .map(WasteCategoryResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public WasteCategoryResponse getCategoryById(Long id) {
        WasteCategory category = getCategoryEntity(id);
        return WasteCategoryResponse.fromEntity(category);
    }

    @Transactional(readOnly = true)
    public WasteCategory getCategoryEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WasteCategory", "id", id));
    }

    @Transactional(readOnly = true)
    public WasteCategory getCategoryEntityByCode(String code) {
        return categoryRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("WasteCategory", "code", code));
    }

    @Transactional
    public WasteCategoryResponse createCategory(CreateWasteCategoryRequest request) {
        String normalizedCode = request.code().trim().toUpperCase();

        if (categoryRepository.existsByCode(normalizedCode)) {
            throw new ConflictException("Category with code " + normalizedCode + " already exists");
        }

        WasteCategory category = new WasteCategory(
                normalizedCode,
                request.name().trim(),
                request.description(),
                request.isRecyclable()
        );

        WasteCategory saved = categoryRepository.save(category);
        log.info("Created new waste category: {}", saved.getCode());
        return WasteCategoryResponse.fromEntity(saved);
    }
}
