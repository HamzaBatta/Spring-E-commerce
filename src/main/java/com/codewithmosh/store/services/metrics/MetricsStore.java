package com.codewithmosh.store.services.metrics;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe in-memory store for performance metrics collected by PerformanceAspect.
 *
 * Uses ConcurrentHashMap + ConcurrentLinkedQueue so concurrent requests never block
 * each other while recording — important during stress tests.
 */
@Component
public class MetricsStore {

    // One queue of durations per label, for successful calls
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> successTimings = new ConcurrentHashMap<>();
    // Error count per label (separate from timings — failed calls may not have meaningful timing)
    private final ConcurrentHashMap<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();

    /**
     * Records one execution. Called by PerformanceAspect after every monitored method.
     *
     * @param label      the @Monitored label (e.g. "order.create.v1")
     * @param durationMs how long the method took in milliseconds
     * @param success    true if the method returned normally, false if it threw
     */
    public void record(String label, long durationMs, boolean success) {
        if (success) {
            successTimings
                    .computeIfAbsent(label, k -> new ConcurrentLinkedQueue<>())
                    .add(durationMs);
        } else {
            errorCounts
                    .computeIfAbsent(label, k -> new AtomicLong(0))
                    .incrementAndGet();
        }
    }

    /**
     * Returns aggregated summary for every label seen so far.
     * Snapshot is taken at call time; concurrent writes during aggregation are safe.
     */
    public List<MetricsSummary> getSummary() {
        // Collect all known labels from both maps
        var allLabels = new java.util.HashSet<String>();
        allLabels.addAll(successTimings.keySet());
        allLabels.addAll(errorCounts.keySet());

        return allLabels.stream()
                .map(label -> {
                    List<Long> timings = new ArrayList<>(
                            successTimings.getOrDefault(label, new ConcurrentLinkedQueue<>())
                    );
                    long errors = errorCounts.getOrDefault(label, new AtomicLong(0)).get();
                    long successes = timings.size();
                    long total = successes + errors;

                    if (timings.isEmpty()) {
                        return new MetricsSummary(label, total, successes, errors, 0, 0, 0, 0);
                    }

                    Collections.sort(timings);
                    long min = timings.getFirst();
                    long max = timings.getLast();
                    double avg = timings.stream().mapToLong(Long::longValue).average().orElse(0);
                    long p95 = timings.get((int) Math.ceil(timings.size() * 0.95) - 1);

                    return new MetricsSummary(label, total, successes, errors, avg, min, max, p95);
                })
                .sorted(java.util.Comparator.comparing(MetricsSummary::label))
                .toList();
    }

    /**
     * Clears all recorded data. Call this between test runs via DELETE /metrics
     * to get a clean baseline for each implementation comparison.
     */
    public void reset() {
        successTimings.clear();
        errorCounts.clear();
    }
}
