package com.codewithmosh.store.strategy;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Generic resolver that picks the right Spring bean for the active strategy.
 *
 * HOW TO ADD A NEW FEATURE WITH MULTIPLE IMPLEMENTATIONS
 * --------------------------------------------------------
 * 1. Define an interface for the operation:
 *
 *      public interface OrderCreationStrategy {
 *          OrderResource create(CreateOrderRequest request);
 *      }
 *
 * 2. Create implementations named after their strategy:
 *
 *      @Component("default")   // used when no X-Strategy header is sent
 *      class DefaultOrderCreation implements OrderCreationStrategy {
 *          @Monitored("order.create.default")
 *          public OrderResource create(...) { ... }
 *      }
 *
 *      @Component("v2")        // used when X-Strategy: v2 header is sent
 *      class V2OrderCreation implements OrderCreationStrategy {
 *          @Monitored("order.create.v2")
 *          public OrderResource create(...) { ... }
 *      }
 *
 * 3. Inject StrategySelector in your controller/service and call:
 *
 *      var strategy = strategySelector.resolve(OrderCreationStrategy.class);
 *      return strategy.create(request);
 *
 * 4. The caller sends X-Strategy: v2 to activate a specific implementation.
 *    No code changes needed to switch between strategies.
 *
 * FALLBACK RULE
 * -------------
 * If no bean matches the requested strategy name, "default" is used.
 * If no "default" bean exists either, the first registered bean of that type is used.
 */
@Component
@AllArgsConstructor
public class StrategySelector {

    private final ApplicationContext context;
    private final StrategyContext strategyContext;

    /**
     * Resolves the implementation of {@code strategyType} for the active strategy.
     *
     * @param strategyType the interface class (e.g. OrderCreationStrategy.class)
     * @param <T>          the strategy type
     * @return the matching Spring bean, falling back to "default" or the first available
     * @throws IllegalStateException if no bean of the requested type exists at all
     */
    public <T> T resolve(Class<T> strategyType) {
        String requestedName = strategyContext.get();
        var beans = context.getBeansOfType(strategyType);

        if (beans.isEmpty()) {
            throw new IllegalStateException("No Spring bean found for strategy type: " + strategyType.getName());
        }

        // Try the explicitly requested name first
        if (beans.containsKey(requestedName)) {
            return beans.get(requestedName);
        }

        // Fall back to "default"
        if (beans.containsKey("default")) {
            return beans.get("default");
        }

        // Last resort: use whatever bean is registered
        return beans.values().iterator().next();
    }
}
