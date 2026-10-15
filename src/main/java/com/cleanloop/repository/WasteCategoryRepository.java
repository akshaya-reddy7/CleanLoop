package com.cleanloop.repository;

import com.cleanloop.entity.WasteCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WasteCategoryRepository extends JpaRepository<WasteCategory, Long> {

    Optional<WasteCategory> findByCode(String code);

    boolean existsByCode(String code);

    List<WasteCategory> findByIsRecyclableTrue();
}
