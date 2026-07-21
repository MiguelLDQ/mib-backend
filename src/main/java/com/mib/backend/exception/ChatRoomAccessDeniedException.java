package com.mib.backend.exception;

public class ChatRoomAccessDeniedException extends RuntimeException {
    public ChatRoomAccessDeniedException(String message) {
        super(message);
    }
}
