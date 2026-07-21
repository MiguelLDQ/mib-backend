package com.mib.backend.exception;

public class FriendRequestNotFoundException extends RuntimeException {
    public FriendRequestNotFoundException(String message) {
        super(message);
    }
}
