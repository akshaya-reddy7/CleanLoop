package com.cleanloop.repository;

import com.cleanloop.entity.CollectionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionScheduleRepository extends JpaRepository<CollectionSchedule, Long> {

    List<CollectionSchedule> findByIsActiveTrue();

    List<CollectionSchedule> findByLocalityIgnoreCaseAndIsActiveTrue(String locality);

    List<CollectionSchedule> findByServiceAreaIgnoreCaseAndIsActiveTrue(String serviceArea);

    List<CollectionSchedule> findByCategoryIdAndIsActiveTrue(Long categoryId);

    List<CollectionSchedule> findByAssignedProviderIdAndIsActiveTrue(Long providerId);
}
