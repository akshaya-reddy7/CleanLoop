package com.cleanloop.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Health check status response.
 */
public record HealthResponse(
        String status,
        String appName,
        String version,
        String environment,
        Instant timestamp,
        Map<String, Object> components
) {}
