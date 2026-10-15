package com.cleanloop.ai;

import com.cleanloop.entity.*;
import com.cleanloop.repository.WasteCategoryRepository;
import com.cleanloop.repository.WasteClassificationRepository;
import com.cleanloop.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FastApiClientClassificationServiceTest {

    @Mock
    private WasteClassificationRepository classificationRepository;

    @Mock
    private WasteCategoryRepository categoryRepository;

    @Mock
    private StorageService storageService;

    private FastApiClientClassificationService service;

    @BeforeEach
    void setUp() {
        // Pointing to a dummy unreachable port to test offline handling
        service = new FastApiClientClassificationService(
                "http://localhost:59999",
                1,
                classificationRepository,
                categoryRepository,
                storageService
        );
    }

    @Test
    void testOfflineAiServiceFallsBackToUnavailableGracefully() {
        ReportImage image = new ReportImage();
        image.setId(101L);
        image.setFilePath("reports/sample.jpg");

        Resource dummyResource = new ByteArrayResource("dummy image bytes".getBytes()) {
            @Override
            public String getFilename() {
                return "sample.jpg";
            }
        };

        when(classificationRepository.findByReportImageId(101L)).thenReturn(Optional.empty());
        when(classificationRepository.save(any(WasteClassification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(storageService.loadFileAsResource("reports/sample.jpg")).thenReturn(dummyResource);

        WasteClassification result = service.classifyReportImage(image);

        assertNotNull(result);
        assertEquals(ClassificationStatus.UNAVAILABLE, result.getClassificationStatus());
        assertNull(result.getConfidenceScore());
        assertNull(result.getPredictedCategory());
        assertTrue(result.getErrorMessage().contains("AI service unavailable"));
    }
}
