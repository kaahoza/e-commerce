package com.anele.order.client;

public class ProductServiceUnavailableException extends RuntimeException {
    public ProductServiceUnavailableException(String message) {
        super(message);
    }
}
