package com.codewithmosh.store.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a public Spring bean method for performance monitoring.
 *
 * PerformanceAspect intercepts every annotated method, records execution time
 * and success/failure in MetricsStore. The label groups metrics so you can
 * compare multiple implementations of the same feature side-by-side.
 *
 * Example — two implementations of the same operation:
 *
 *   @Component("v1")
 *   class V1OrderService implements OrderStrategy {
 *       @Monitored("order.create.v1")
 *       public OrderResource create(...) { ... }
 *   }
 *
 *   @Component("v2")
 *   class V2OrderService implements OrderStrategy {
 *       @Monitored("order.create.v2")
 *       public OrderResource create(...) { ... }
 *   }
 *
 * Hit GET /metrics to see avg/min/max/p95 per label and compare.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Monitored {
    /**
     * Label used to group metrics. Use dot-notation for readability:
     * "feature.operation.implementation" e.g. "order.create.v1"
     *
     * Defaults to "ClassName.methodName" when left blank.
     */
    String value() default "";
}
