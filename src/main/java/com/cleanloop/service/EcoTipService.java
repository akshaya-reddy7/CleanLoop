package com.cleanloop.service;

import com.cleanloop.dto.CreateEcoTipRequest;
import com.cleanloop.dto.EcoTipResponse;
import com.cleanloop.entity.EcoTip;
import com.cleanloop.entity.User;
import com.cleanloop.exception.ResourceNotFoundException;
import com.cleanloop.repository.EcoTipRepository;
import com.cleanloop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EcoTipService {

    private static final Logger log = LoggerFactory.getLogger(EcoTipService.class);

    private final EcoTipRepository tipRepository;
    private final UserRepository userRepository;

    public EcoTipService(EcoTipRepository tipRepository, UserRepository userRepository) {
        this.tipRepository = tipRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<EcoTipResponse> getPublishedTips(String category) {
        List<EcoTip> tips = (category != null && !category.isBlank())
                ? tipRepository.findByCategoryIgnoreCaseAndIsPublishedTrue(category.trim())
                : tipRepository.findByIsPublishedTrue();

        return tips.stream().map(EcoTipResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public EcoTipResponse getTipById(Long id) {
        EcoTip tip = tipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EcoTip", "id", id));
        return EcoTipResponse.fromEntity(tip);
    }

    @Transactional
    public EcoTipResponse createTip(CreateEcoTipRequest request, Long authorId) {
        User author = null;
        if (authorId != null) {
            author = userRepository.findById(authorId).orElse(null);
        }

        EcoTip tip = new EcoTip(
                request.title().trim(),
                request.content().trim(),
                request.category().trim().toUpperCase(),
                request.imageUrl(),
                author
        );

        EcoTip saved = tipRepository.save(tip);
        log.info("Created new eco tip: {} with ID {}", saved.getTitle(), saved.getId());
        return EcoTipResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteTip(Long id) {
        EcoTip tip = tipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EcoTip", "id", id));
        tipRepository.delete(tip);
        log.info("Deleted eco tip ID: {}", id);
    }
}
