package com.cleanloop.repository;

import com.cleanloop.entity.ClassificationStatus;
import com.cleanloop.entity.WasteClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WasteClassificationRepository extends JpaRepository<WasteClassification, Long> {

    Optional<WasteClassification> findByReportImageId(Long reportImageId);

    List<WasteClassification> findByClassificationStatus(ClassificationStatus status);
}
