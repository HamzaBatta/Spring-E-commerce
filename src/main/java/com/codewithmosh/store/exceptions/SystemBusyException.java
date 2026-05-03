package com.codewithmosh.store.exceptions;

public class SystemBusyException extends RuntimeException {
    public SystemBusyException() {
        super("System is busy. Please try again.");
    }
}

