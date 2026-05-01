package com.codewithmosh.store.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("user not found");
    }
}

