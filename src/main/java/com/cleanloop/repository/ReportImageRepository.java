package com.cleanloop.repository;

import com.cleanloop.entity.ReportImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportImageRepository extends JpaRepository<ReportImage, Long> {

    List<ReportImage> findByReportId(Long reportId);
}
