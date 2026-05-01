package com.codewithmosh.store.exceptions;

public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException() {
        super("invalid order status transition");
    }
}

