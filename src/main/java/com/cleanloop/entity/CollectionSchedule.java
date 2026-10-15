package com.cleanloop.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name = "collection_schedules")
public class CollectionSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String locality;

    @Column(name = "service_area", nullable = false, length = 100)
    private String serviceArea;

    @Column(name = "collection_day_of_week", nullable = false, length = 20)
    private String collectionDayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private WasteCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_provider_id")
    private User assignedProvider;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CollectionSchedule() {
    }

    public CollectionSchedule(String locality, String serviceArea, String collectionDayOfWeek,
                              LocalTime startTime, LocalTime endTime, WasteCategory category,
                              User assignedProvider, String notes) {
        this.locality = locality;
        this.serviceArea = serviceArea;
        this.collectionDayOfWeek = collectionDayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.assignedProvider = assignedProvider;
        this.notes = notes;
        this.isActive = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public String getCollectionDayOfWeek() {
        return collectionDayOfWeek;
    }

    public void setCollectionDayOfWeek(String collectionDayOfWeek) {
        this.collectionDayOfWeek = collectionDayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public WasteCategory getCategory() {
        return category;
    }

    public void setCategory(WasteCategory category) {
        this.category = category;
    }

    public User getAssignedProvider() {
        return assignedProvider;
    }

    public void setAssignedProvider(User assignedProvider) {
        this.assignedProvider = assignedProvider;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
