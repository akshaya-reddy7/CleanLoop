package com.cleanloop.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    /**
     * Stores an uploaded file in the given sub-directory and returns the stored relative file path.
     *
     * @param file         Multipart file to store
     * @param subDirectory Subfolder (e.g., "reports", "pickups")
     * @return Relative path where the file is stored
     */
    String storeFile(MultipartFile file, String subDirectory);

    /**
     * Loads a stored file as a Spring Resource.
     *
     * @param relativePath Relative path of the stored file
     * @return Resource representing the file
     */
    Resource loadFileAsResource(String relativePath);

    /**
     * Deletes a stored file.
     *
     * @param relativePath Relative path of the stored file
     */
    void deleteFile(String relativePath);
}
