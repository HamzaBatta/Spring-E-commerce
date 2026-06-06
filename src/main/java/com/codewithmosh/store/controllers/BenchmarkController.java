package com.codewithmosh.store.controllers;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/benchmark")
public class BenchmarkController {

    private final MeterRegistry meterRegistry;

    public BenchmarkController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> getOrderMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();

        Timer simpleTimer = meterRegistry.find("order.simple").timer();
        Timer optimizedTimer = meterRegistry.find("order.optimized").timer();

        if (simpleTimer != null) {
            Map<String, Object> simple = new LinkedHashMap<>();
            simple.put("count", simpleTimer.count());
            simple.put("mean_ms", simpleTimer.mean(TimeUnit.MILLISECONDS));
            simple.put("max_ms", simpleTimer.max(TimeUnit.MILLISECONDS));
            simple.put("p95_ms", simpleTimer.percentile(0.95, TimeUnit.MILLISECONDS));
            simple.put("p99_ms", simpleTimer.percentile(0.99, TimeUnit.MILLISECONDS));
            metrics.put("simple_order", simple);
        }

        if (optimizedTimer != null) {
            Map<String, Object> optimized = new LinkedHashMap<>();
            optimized.put("count", optimizedTimer.count());
            optimized.put("mean_ms", optimizedTimer.mean(TimeUnit.MILLISECONDS));
            optimized.put("max_ms", optimizedTimer.max(TimeUnit.MILLISECONDS));
            optimized.put("p95_ms", optimizedTimer.percentile(0.95, TimeUnit.MILLISECONDS));
            optimized.put("p99_ms", optimizedTimer.percentile(0.99, TimeUnit.MILLISECONDS));
            metrics.put("optimized_order", optimized);
        }

        return ResponseEntity.ok(metrics);
    }
}

