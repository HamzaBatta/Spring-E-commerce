package com.codewithmosh.store.exceptions;

public class StorageNotFoundException extends RuntimeException {
    public StorageNotFoundException() {
        super("storage not found");
    }
}
