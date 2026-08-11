package com.anele.product_service.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String message,
        String details,
        LocalDateTime timestamp
) {
}
