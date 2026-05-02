package com.codewithmosh.store.aspects;

import com.codewithmosh.store.annotations.Monitored;
import com.codewithmosh.store.services.metrics.MetricsStore;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that intercepts every method annotated with @Monitored.
 *
 * Runs at @Order(1) — before @Transactional — so the measured time includes
 * the full database transaction commit, giving a realistic wall-clock number.
 *
 * How it works:
 *   1. Records System.nanoTime() before calling the target method.
 *   2. Calls the method (pjp.proceed()).
 *   3. Computes elapsed milliseconds and delegates to MetricsStore.record().
 *   4. Rethrows any exception unchanged (transparent to callers).
 *
 * Thread-safety: this aspect is stateless; MetricsStore handles concurrency.
 */
@Aspect
@Component
@Order(1)
@AllArgsConstructor
@Slf4j
public class PerformanceAspect {

    private final MetricsStore metricsStore;

    /**
     * Intercepts any method annotated with @Monitored.
     * The annotation instance is bound automatically via parameter name matching.
     */
    @Around("@annotation(monitored)")
    public Object measure(ProceedingJoinPoint pjp, Monitored monitored) throws Throwable {
        String label = resolveLabel(monitored, pjp);
        long startNanos = System.nanoTime();
        boolean success = false;

        try {
            Object result = pjp.proceed();
            success = true;
            return result;
        } catch (Throwable t) {
            // Re-throw unchanged — the caller must handle it normally
            throw t;
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            metricsStore.record(label, durationMs, success);
            log.debug("[AOP] {} → {}ms {}", label, durationMs, success ? "OK" : "ERROR");
        }
    }

    /**
     * Resolves the effective label:
     *   - Uses @Monitored("explicit.label") when provided.
     *   - Falls back to "ClassName.methodName" when blank.
     */
    private String resolveLabel(Monitored monitored, ProceedingJoinPoint pjp) {
        if (!monitored.value().isBlank()) {
            return monitored.value();
        }
        var signature = (MethodSignature) pjp.getSignature();
        return pjp.getTarget().getClass().getSimpleName() + "." + signature.getMethod().getName();
    }
}
