package com.cleanloop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "waste_classifications")
public class WasteClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_image_id", nullable = false, unique = true)
    private ReportImage reportImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predicted_category_id")
    private WasteCategory predictedCategory;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "classification_status", nullable = false, length = 30)
    private ClassificationStatus classificationStatus = ClassificationStatus.PENDING;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public WasteClassification() {
    }

    public WasteClassification(ReportImage reportImage) {
        this.reportImage = reportImage;
        this.classificationStatus = ClassificationStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReportImage getReportImage() {
        return reportImage;
    }

    public void setReportImage(ReportImage reportImage) {
        this.reportImage = reportImage;
    }

    public WasteCategory getPredictedCategory() {
        return predictedCategory;
    }

    public void setPredictedCategory(WasteCategory predictedCategory) {
        this.predictedCategory = predictedCategory;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public ClassificationStatus getClassificationStatus() {
        return classificationStatus;
    }

    public void setClassificationStatus(ClassificationStatus classificationStatus) {
        this.classificationStatus = classificationStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
