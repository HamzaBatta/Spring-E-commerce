package com.codewithmosh.store.filters;

import com.codewithmosh.store.strategy.StrategyContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads the X-Strategy request header and stores it in StrategyContext (ThreadLocal)
 * for the duration of the request.
 *
 * This is what makes the system "pluggable without code changes":
 *   - Send  X-Strategy: default  →  uses the @Component("default") bean
 *   - Send  X-Strategy: v2       →  uses the @Component("v2") bean
 *   - Omit the header            →  falls back to "default"
 *
 * The StrategyContext is always cleared in the finally block to prevent
 * ThreadLocal leaks between requests on pooled threads.
 *
 * Runs at @Order(0) — before JWT auth — so the context is available to all
 * downstream components including security filters.
 */
@Component
@Order(0)
@AllArgsConstructor
public class RequestStrategyFilter extends OncePerRequestFilter {

    private final StrategyContext strategyContext;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String strategy = request.getHeader("X-Strategy");
        strategyContext.set(strategy);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // CRITICAL: always clear — thread pool reuses threads across requests
            strategyContext.clear();
        }
    }
}
