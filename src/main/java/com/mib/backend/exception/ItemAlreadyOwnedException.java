package com.mib.backend.exception;

public class ItemAlreadyOwnedException extends RuntimeException {
    public ItemAlreadyOwnedException(String message) {
        super(message);
    }
}
