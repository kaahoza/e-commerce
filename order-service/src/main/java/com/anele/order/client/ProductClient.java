package com.anele.order.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductClient {

    private final WebClient productServiceWebClient;

    /**
     * Fetches a product by id from Product Service.
     * Wrapped in a circuit breaker + retry: if Product Service is slow or
     * down, we fail fast after a few retries instead of hanging the whole
     * order-placement flow, and fall back to a clear error via
     * fallbackGetProduct rather than a raw connection exception.
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetProduct")
    @Retry(name = "productService")
    public ProductDto getProduct(Long productId) {
        return productServiceWebClient.get()
                .uri("/api/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .block();
    }

    /**
     * Decrements stock on Product Service after an order is confirmed.
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackDecrementStock")
    @Retry(name = "productService")
    public ProductDto decrementStock(Long productId, int quantity) {
        return productServiceWebClient.patch()
                .uri("/api/products/{id}/decrement-stock?quantity={quantity}", productId, quantity)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .block();
    }

    private ProductDto fallbackGetProduct(Long productId, Throwable throwable) {
        log.error("Product Service unavailable while fetching product {}: {}", productId, throwable.getMessage());
        throw new ProductServiceUnavailableException(
                "Could not reach Product Service to verify product " + productId);
    }

    private ProductDto fallbackDecrementStock(Long productId, int quantity, Throwable throwable) {
        log.error("Product Service unavailable while decrementing stock for {}: {}", productId, throwable.getMessage());
        throw new ProductServiceUnavailableException(
                "Could not reach Product Service to reserve stock for product " + productId);
    }
}
