package com.cleanloop.repository;

import com.cleanloop.entity.RecyclingCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecyclingCenterRepository extends JpaRepository<RecyclingCenter, Long> {

    List<RecyclingCenter> findByIsActiveTrue();

    List<RecyclingCenter> findByAcceptedMaterialsContainingIgnoreCaseAndIsActiveTrue(String material);

    @Query("SELECT r FROM RecyclingCenter r WHERE r.isActive = true AND LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<RecyclingCenter> searchActiveCenters(@Param("keyword") String keyword);
}
