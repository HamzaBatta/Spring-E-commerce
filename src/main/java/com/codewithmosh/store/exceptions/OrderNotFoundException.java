package com.codewithmosh.store.exceptions;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException() {
        super("order not found");
    }
}

