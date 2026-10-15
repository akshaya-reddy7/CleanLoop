package com.cleanloop.ai;

import com.cleanloop.entity.ReportImage;
import com.cleanloop.entity.WasteClassification;

public interface AiClassificationService {

    /**
     * Sends the report image to the AI service for classification.
     * Persists and returns a WasteClassification entity.
     * If the service is unreachable, records status UNAVAILABLE without erroring out.
     *
     * @param image Stored report image entity
     * @return Resulting WasteClassification entity
     */
    WasteClassification classifyReportImage(ReportImage image);

    /**
     * Checks if the AI classification service is currently healthy and reachable.
     *
     * @return true if service is reachable, false otherwise
     */
    boolean isServiceAvailable();
}
