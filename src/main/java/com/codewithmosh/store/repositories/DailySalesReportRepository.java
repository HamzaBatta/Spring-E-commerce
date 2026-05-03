package com.codewithmosh.store.repositories;

import com.codewithmosh.store.entities.DailySalesReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DailySalesReportRepository extends JpaRepository<DailySalesReportEntity, Long> {
    Optional<DailySalesReportEntity> findTopByOrderByCreatedAtDesc();
}
