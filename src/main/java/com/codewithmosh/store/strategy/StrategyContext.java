package com.codewithmosh.store.strategy;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the active strategy selection for the current HTTP request thread.
 *
 * Supports two header formats:
 *
 *   Simple (applies to ALL strategies):
 *     X-Strategy: naive
 *     → every strategySelector.resolve(...) returns the "naive" bean
 *
 *   Namespaced (per strategy type):
 *     X-Strategy: order=naive,invoice=async
 *     → OrderCreationStrategy resolves "naive", InvoiceProcessingStrategy resolves "async"
 *     → any unspecified type falls back to "default"
 *
 *   Mixed (type-specific + global fallback):
 *     X-Strategy: order=naive
 *     → OrderCreationStrategy resolves "naive", everything else resolves "default"
 *
 * Thread-safety: ThreadLocal — each request thread has its own isolated value.
 * RequestStrategyFilter always calls clear() in its finally block.
 */
@Component
public class StrategyContext {

    private static final String DEFAULT = "default";
    private static final String GLOBAL_KEY = "*";

    // Map of type-key → strategy-name, e.g. { "order" → "naive", "invoice" → "async" }
    // A special "*" key means "apply to all types not explicitly listed"
    private final ThreadLocal<Map<String, String>> current = new ThreadLocal<>();

    /**
     * Parses and stores the X-Strategy header value.
     *
     * Formats accepted:
     *   "naive"                     → global: all types use "naive"
     *   "order=naive"               → only "order" type uses "naive"
     *   "order=naive,invoice=async" → each type uses its own value
     */
    public void set(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            current.set(Collections.emptyMap());
            return;
        }

        var result = new HashMap<String, String>();

        for (String part : headerValue.split(",")) {
            part = part.trim();
            if (part.contains("=")) {
                // Namespaced: "order=naive"
                var kv = part.split("=", 2);
                result.put(kv[0].trim(), kv[1].trim());
            } else {
                // Simple global: "naive"
                result.put(GLOBAL_KEY, part);
            }
        }

        current.set(result);
    }

    /**
     * Returns the strategy name for the given type key (e.g. "order", "invoice").
     *
     * Resolution order:
     *   1. Explicit type key ("order" → "naive")
     *   2. Global fallback ("*" → "naive")
     *   3. "default"
     */
    public String get(String typeKey) {
        var map = current.get();
        if (map == null || map.isEmpty()) return DEFAULT;

        // Explicit match for this type
        if (map.containsKey(typeKey)) return map.get(typeKey);

        // Global fallback
        if (map.containsKey(GLOBAL_KEY)) return map.get(GLOBAL_KEY);

        return DEFAULT;
    }

    /** Must be called after every request (in finally block) to prevent ThreadLocal leaks. */
    public void clear() {
        current.remove();
    }
}
