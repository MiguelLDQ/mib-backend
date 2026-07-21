package com.mib.backend.exception;

public class ItemNotOwnedException extends RuntimeException {
    public ItemNotOwnedException(String message) {
        super(message);
    }
}
