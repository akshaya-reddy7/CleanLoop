package com.cleanloop.repository;

import com.cleanloop.entity.EcoTip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EcoTipRepository extends JpaRepository<EcoTip, Long> {

    List<EcoTip> findByIsPublishedTrue();

    List<EcoTip> findByCategoryIgnoreCaseAndIsPublishedTrue(String category);
}
