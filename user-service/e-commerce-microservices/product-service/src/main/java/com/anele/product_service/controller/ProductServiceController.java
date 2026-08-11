package com.anele.product_service.controller;

import com.anele.product_service.dto.ProductRequest;
import com.anele.product_service.service.ProductService;
import com.anele.product_service.dto.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductServiceController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return  ResponseEntity.ok(productService.getAllProducts());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        ProductResponse response = productService.createProduct(productRequest);
        return new  ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.getProductBySku(sku));
    }

    @PutMapping("/{sku}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String sku, @Valid @RequestBody ProductRequest productRequest) {
        ProductResponse response = productService.updateProductBySku(sku, productRequest);
        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/{sku}")
    public ResponseEntity<ProductResponse> deleteProductBySku(@PathVariable String sku) {
        productService.deleteProductBySku(sku);
        return ResponseEntity.noContent().build();
    }

}
