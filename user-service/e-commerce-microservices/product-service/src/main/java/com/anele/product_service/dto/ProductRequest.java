
package com.anele.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "SKU is mandatory")
    private String sku;

    @NotBlank(message = "Product name is mandatory")
    private String name;

    private String description;

    @NotBlank(message = "Category is mandatory")
    private String category;

    @NotNull(message = "Price is mandatory")
    @PositiveOrZero(message = "Price must be zero or a positive value")
    private BigDecimal price;

    @PositiveOrZero(message = "Stock quantity cannot be negative")
    private Integer stockQuantity;
}
