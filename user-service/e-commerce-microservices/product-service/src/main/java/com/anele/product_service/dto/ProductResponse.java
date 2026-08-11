package com.anele.product_service.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
  public class ProductResponse {
     private Long id;
     private String sku;
     private String name;
     private String description;
     private String category;
     private BigDecimal price;
     private Integer stockQuantity;


}

