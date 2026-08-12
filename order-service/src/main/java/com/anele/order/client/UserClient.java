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
public class UserClient {

    private final WebClient userServiceWebClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUser")
    @Retry(name = "userService")
    public UserDto getUser(Long userId) {
        return userServiceWebClient.get()
                .uri("/api/users/{id}", userId)
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
    }

    private UserDto fallbackGetUser(Long userId, Throwable throwable) {
        log.error("User Service unavailable while fetching user {}: {}", userId, throwable.getMessage());
        throw new UserServiceUnavailableException(
                "Could not reach User Service to verify user " + userId);
    }
}
