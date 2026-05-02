package com.codewithmosh.store.strategy;

import org.springframework.stereotype.Component;

/**
 * Holds the active strategy name for the current HTTP request thread.
 *
 * RequestStrategyFilter reads the X-Strategy header and calls set() at the start
 * of each request. StrategySelector reads it to pick the right implementation.
 * The filter always calls clear() in its finally block to prevent leaking between requests.
 *
 * Thread-safety: ThreadLocal — each request thread has its own isolated value.
 */
@Component
public class StrategyContext {

    private static final String DEFAULT = "default";

    private final ThreadLocal<String> current = new ThreadLocal<>();

    /** Sets the active strategy name for this request thread. */
    public void set(String strategyName) {
        current.set(strategyName != null && !strategyName.isBlank() ? strategyName : DEFAULT);
    }

    /**
     * Returns the active strategy name, or "default" if none was set.
     * "default" is used as the Spring bean name for the standard implementation.
     */
    public String get() {
        String value = current.get();
        return value != null ? value : DEFAULT;
    }

    /** Must be called after every request (in finally block) to prevent ThreadLocal leaks. */
    public void clear() {
        current.remove();
    }
}
