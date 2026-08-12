package com.anele.order.client;

public class UserServiceUnavailableException extends RuntimeException {
    public UserServiceUnavailableException(String message) {
        super(message);
    }
}
