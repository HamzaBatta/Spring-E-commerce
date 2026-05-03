package com.codewithmosh.store.strategy;

import com.codewithmosh.store.annotations.StrategyType;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Resolves the right Spring bean for the active strategy using the X-Strategy header.
 *
 * Header format (only namespaced is supported):
 *   X-Strategy: order=naive,invoice=async
 *
 * Each key maps to a @StrategyType annotation on the interface:
 *   @StrategyType("order")   → OrderCreationStrategy
 *   @StrategyType("invoice") → InvoiceProcessingStrategy
 *
 * Resolution order:
 *   1. Read @StrategyType("key") from the interface
 *   2. Look up that key in the header (e.g. "order" → "naive")
 *   3. Find @Component("naive") bean of that interface type
 *   4. Fall back to @Component("default") if not found
 */
@Component
@AllArgsConstructor
public class StrategySelector {

    private final ApplicationContext context;
    private final StrategyContext strategyContext;

    public <T> T resolve(Class<T> strategyType) {
        var beans = context.getBeansOfType(strategyType);

        if (beans.isEmpty()) {
            throw new IllegalStateException(
                    "No Spring bean found for strategy type: " + strategyType.getName());
        }

        String typeKey = resolveTypeKey(strategyType);
        String requestedName = strategyContext.get(typeKey);
        String scopedDefault = typeKey + "-default";

        if (requestedName != null && beans.containsKey(requestedName)) {
            return beans.get(requestedName);
        }

        // Try a type-specific default bean name (e.g. "default-order", "default-invoice", "default-daily-sales")
        String typeDefault = "default-" + typeKey;
        if (beans.containsKey(typeDefault)) {
            return beans.get(typeDefault);
        }

        // Legacy fallback: plain "default" if present
        if (beans.containsKey("default")) {
            return beans.get("default");
        }

        return beans.values().iterator().next();
    }

    private String resolveTypeKey(Class<?> strategyType) {
        var annotation = strategyType.getAnnotation(StrategyType.class);
        if (annotation != null) return annotation.value();
        return strategyType.getSimpleName().toLowerCase();
    }
}
