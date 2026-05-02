package com.codewithmosh.store.controllers;

import com.codewithmosh.store.services.metrics.MetricsStore;
import com.codewithmosh.store.services.metrics.MetricsSummary;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes collected performance metrics so you can compare implementations.
 *
 * Workflow for benchmarking two approaches:
 *   1. DELETE /metrics          — clear previous data
 *   2. Run load test with X-Strategy: v1 header (e.g. JMeter, k6, Apache Bench)
 *   3. GET /metrics             — note v1 numbers
 *   4. DELETE /metrics          — clear again
 *   5. Run load test with X-Strategy: v2
 *   6. GET /metrics             — compare v2 numbers with v1
 */
@RestController
@RequestMapping("/metrics")
@AllArgsConstructor
public class MetricsController {

    private final MetricsStore metricsStore;

    /**
     * Returns aggregated stats (avg, min, max, p95, error rate) for every @Monitored label.
     * Sorted alphabetically by label so related implementations appear next to each other.
     */
    @GetMapping
    public org.springframework.data.domain.Page<MetricsSummary> getMetrics(
            @RequestParam(required = false, defaultValue = "0", name = "page") int page,
            @RequestParam(required = false, defaultValue = "10", name = "size") int size) {
        var all = metricsStore.getSummary();
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        var sub = all.subList(from, to);
        return new org.springframework.data.domain.PageImpl<>(sub, org.springframework.data.domain.PageRequest.of(page, size), all.size());
    }

    /**
     * Resets all collected data. Call this before each test run to get a clean baseline.
     */
    @DeleteMapping
    public ResponseEntity<Void> resetMetrics() {
        metricsStore.reset();
        return ResponseEntity.noContent().build();
    }
}
