package com.mib.backend.exception;

public class ShopItemNotFoundException extends RuntimeException {
    public ShopItemNotFoundException(String message) {
        super(message);
    }
}
