package com.codewithmosh.store.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "daily_sales_reports")
public class DailySalesReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "processed_count")
    private Long processedCount;

    @Column(name = "total_revenue", precision = 19, scale = 4)
    private BigDecimal totalRevenue;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "strategy")
    private String strategy;

    @Column(name = "created_at")
    private Instant createdAt;

    public DailySalesReportEntity(Long processedCount, BigDecimal totalRevenue, Long durationMs, String strategy) {
        this.processedCount = processedCount;
        this.totalRevenue = totalRevenue;
        this.durationMs = durationMs;
        this.strategy = strategy;
        this.createdAt = Instant.now();
    }
}
