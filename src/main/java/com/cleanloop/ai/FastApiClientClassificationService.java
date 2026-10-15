package com.cleanloop.ai;

import com.cleanloop.entity.ClassificationStatus;
import com.cleanloop.entity.ReportImage;
import com.cleanloop.entity.WasteCategory;
import com.cleanloop.entity.WasteClassification;
import com.cleanloop.repository.WasteCategoryRepository;
import com.cleanloop.repository.WasteClassificationRepository;
import com.cleanloop.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Optional;

@Service
public class FastApiClientClassificationService implements AiClassificationService {

    private static final Logger log = LoggerFactory.getLogger(FastApiClientClassificationService.class);

    private final String serviceUrl;
    private final WasteClassificationRepository classificationRepository;
    private final WasteCategoryRepository categoryRepository;
    private final StorageService storageService;
    private final RestClient restClient;

    public FastApiClientClassificationService(
            @Value("${cleanloop.ai.service-url:http://localhost:8000}") String serviceUrl,
            @Value("${cleanloop.ai.timeout-seconds:5}") int timeoutSeconds,
            WasteClassificationRepository classificationRepository,
            WasteCategoryRepository categoryRepository,
            StorageService storageService) {
        this.serviceUrl = serviceUrl;
        this.classificationRepository = classificationRepository;
        this.categoryRepository = categoryRepository;
        this.storageService = storageService;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        this.restClient = RestClient.builder()
                .baseUrl(serviceUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    @Transactional
    public WasteClassification classifyReportImage(ReportImage image) {
        WasteClassification classification = classificationRepository.findByReportImageId(image.getId())
                .orElseGet(() -> {
                    WasteClassification wc = new WasteClassification(image);
                    return classificationRepository.save(wc);
                });

        try {
            Resource imageResource = storageService.loadFileAsResource(image.getFilePath());

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", imageResource);

            AiPredictionDto prediction = restClient.post()
                    .uri("/classify")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(AiPredictionDto.class);

            if (prediction != null && prediction.success() && prediction.prediction() != null) {
                String classCode = prediction.prediction().trim().toUpperCase();
                Optional<WasteCategory> categoryOpt = categoryRepository.findByCode(classCode);

                categoryOpt.ifPresent(classification::setPredictedCategory);
                classification.setConfidenceScore(prediction.confidence());
                classification.setModelVersion(prediction.modelVersion() != null ? prediction.modelVersion() : "MobileNetV2-TrashNet-v1.0");
                classification.setClassificationStatus(ClassificationStatus.SUCCESS);
                classification.setErrorMessage(null);

                log.info("AI classification SUCCESS for image ID {}: class={}, confidence={}",
                        image.getId(), classCode, prediction.confidence());
            } else {
                classification.setClassificationStatus(ClassificationStatus.FAILED);
                classification.setErrorMessage(prediction != null ? prediction.message() : "Empty prediction response");
                log.warn("AI classification returned failure for image ID {}: {}", image.getId(), classification.getErrorMessage());
            }
        } catch (Exception ex) {
            log.warn("AI classification service unreachable or returned error for image ID {}: {}",
                    image.getId(), ex.getMessage());
            classification.setClassificationStatus(ClassificationStatus.UNAVAILABLE);
            classification.setErrorMessage("AI service unavailable: " + ex.getMessage());
            classification.setConfidenceScore(null);
            classification.setPredictedCategory(null);
        }

        return classificationRepository.save(classification);
    }

    @Override
    public boolean isServiceAvailable() {
        try {
            String response = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(String.class);
            return response != null && response.contains("UP");
        } catch (Exception e) {
            return false;
        }
    }
}
