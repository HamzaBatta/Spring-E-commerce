package com.codewithmosh.store.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Labels a strategy interface with a short key used in the X-Strategy header.
 *
 * Example:
 *   @StrategyType("order")
 *   public interface OrderCreationStrategy { ... }
 *
 * Then in the request header:
 *   X-Strategy: order=naive,invoice=async
 *
 * StrategySelector reads this annotation to know which part of the header
 * applies to which strategy interface.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StrategyType {
    String value();
}
