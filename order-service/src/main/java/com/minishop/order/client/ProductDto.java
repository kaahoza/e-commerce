package com.minishop.order.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Mirrors the shape of ProductResponse in product-service.
 * Kept as a plain local copy rather than a shared library, to keep each
 * service independently deployable (a common tradeoff in microservices —
 * avoids a shared-model dependency coupling the services together).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
}
