package com.codewithmosh.store.exceptions;

public class InsufficientInventoryException extends RuntimeException {
    public InsufficientInventoryException() {
        super("insufficient inventory");
    }
}

