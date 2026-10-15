package com.cleanloop.controller;

import com.cleanloop.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "File Storage", description = "Endpoints for downloading and displaying uploaded media files")
public class FileController {

    private final StorageService storageService;

    public FileController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/{subDirectory}/{fileName}")
    @Operation(summary = "Get stored file", description = "Serves uploaded report and pickup images.")
    public ResponseEntity<Resource> getFile(
            @PathVariable String subDirectory,
            @PathVariable String fileName) {

        String relativePath = subDirectory + "/" + fileName;
        Resource resource = storageService.loadFileAsResource(relativePath);

        String contentType = "application/octet-stream";
        try {
            String probeContentType = Files.probeContentType(Paths.get(resource.getFile().getAbsolutePath()));
            if (probeContentType != null) {
                contentType = probeContentType;
            }
        } catch (IOException ignored) {
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
