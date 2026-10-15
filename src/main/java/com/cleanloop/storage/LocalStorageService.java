package com.cleanloop.storage;

import com.cleanloop.exception.BadRequestException;
import com.cleanloop.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private final Path rootLocation;

    public LocalStorageService(@Value("${cleanloop.storage.upload-dir:./uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            log.info("Storage directory initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload storage location", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File exceeds maximum allowed size of 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Unsupported file type: " + contentType + ". Allowed: JPG, PNG, WEBP");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Invalid filename containing path traversal sequence");
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase();
        }

        String safeFileName = UUID.randomUUID() + extension;
        Path targetDir = this.rootLocation.resolve(subDirectory).normalize();

        try {
            Files.createDirectories(targetDir);
            Path targetLocation = targetDir.resolve(safeFileName).normalize();

            // Security check against directory traversal outside root
            if (!targetLocation.startsWith(this.rootLocation)) {
                throw new BadRequestException("Cannot store file outside current storage directory");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = subDirectory + "/" + safeFileName;
            log.info("File successfully stored at: {}", relativePath);
            return relativePath;
        } catch (IOException e) {
            log.error("Failed to store file {}", originalFilename, e);
            throw new RuntimeException("Failed to store file: " + originalFilename, e);
        }
    }

    @Override
    public Resource loadFileAsResource(String relativePath) {
        try {
            Path filePath = this.rootLocation.resolve(relativePath).normalize();
            if (!filePath.startsWith(this.rootLocation)) {
                throw new BadRequestException("Cannot access file outside storage directory");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File", "path", relativePath);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File", "path", relativePath);
        }
    }

    @Override
    public void deleteFile(String relativePath) {
        try {
            Path filePath = this.rootLocation.resolve(relativePath).normalize();
            if (filePath.startsWith(this.rootLocation)) {
                Files.deleteIfExists(filePath);
                log.info("File deleted: {}", relativePath);
            }
        } catch (IOException e) {
            log.warn("Could not delete file at: {}", relativePath, e);
        }
    }
}
