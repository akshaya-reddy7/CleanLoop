package com.cleanloop.controller;

import com.cleanloop.dto.ApiResponse;
import com.cleanloop.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check endpoint providing real-time system and database status.
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health Check", description = "Endpoints for backend and database health diagnostics")
public class HealthController {

    private final DataSource dataSource;

    @Value("${spring.application.name:cleanloop-backend}")
    private String appName;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    @Operation(summary = "System Health Check", description = "Returns the operational status of the CleanLoop backend and its database connection.")
    public ResponseEntity<ApiResponse<HealthResponse>> getHealth() {
        Map<String, Object> components = new LinkedHashMap<>();

        boolean dbHealthy = checkDatabaseHealth();
        components.put("database", dbHealthy ? "UP" : "DOWN");
        components.put("storage", "LOCAL_READY");
        components.put("aiServiceBoundary", "CONFIGURED");

        String overallStatus = dbHealthy ? "UP" : "DEGRADED";

        HealthResponse healthResponse = new HealthResponse(
                overallStatus,
                appName,
                "1.0.0",
                "development",
                Instant.now(),
                components
        );

        return ResponseEntity.ok(ApiResponse.ok(healthResponse, "CleanLoop Backend is running"));
    }

    private boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}
