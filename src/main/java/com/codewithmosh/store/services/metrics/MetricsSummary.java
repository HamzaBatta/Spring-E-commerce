package com.codewithmosh.store.services.metrics;

/**
 * Aggregated performance data for one @Monitored label.
 * Returned by GET /metrics so you can compare implementations side-by-side.
 */
public record MetricsSummary(
        String label,
        long totalCalls,
        long successCalls,
        long errorCalls,
        double avgMs,
        long minMs,
        long maxMs,
        long p95Ms
) {}
