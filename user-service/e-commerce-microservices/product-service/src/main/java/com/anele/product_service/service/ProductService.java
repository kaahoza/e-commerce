package com.anele.product_service.service;


import com.anele.product_service.dto.ProductRequest;
import com.anele.product_service.dto.ProductResponse;
import com.anele.product_service.entity.Product;
import com.anele.product_service.exception.ResourceNotFoundException;
import com.anele.product_service.repository.ProductRepository;
import lombok.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    public final ProductRepository productRepository;

    /**
     * Retrieve all items inside products catalog
     */

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        log.info("fetching all products from data layer");
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {
        log.info("creating product for sku");

        Product savedProduct = Product.builder()
                .sku(productRequest.getSku())
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .category(productRequest.getCategory())
                .price(productRequest.getPrice())
                .stockQuantity(productRequest.getStockQuantity())
                .build();

        //Save the product to database
        Product productToSave = productRepository.save(savedProduct);

        return this.mapToResponse(savedProduct);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();

    }

/**
 * Retrieve a single product by its unique sku
 * throw ResourceNotFoundException if missing data
 */

    public ProductResponse getProductBySku(String sku) {
        Product product =  productRepository.findBySku(sku)
                .orElseThrow(() -> {
                    log.warn("Product Sku resolution failed to locate metadata for: {}", sku);

                    return new ResourceNotFoundException("Product metadata with sku " + sku + " was not found");
                });
        return mapToResponse(product);
    }

    public ProductResponse updateProductBySku(String sku, ProductRequest productRequest) {
        log.info("Updating product with sku: {}", sku);

        Product existingProduct = productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("product not found with sku " + sku));
        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setCategory(productRequest.getCategory());
        existingProduct.setPrice(productRequest.getPrice());
        existingProduct.setStockQuantity(productRequest.getStockQuantity());

        //Save updated product
        productRepository.save(existingProduct);

        return this.mapToResponse(existingProduct);

    }

    public void deleteProductBySku(String sku) {
        log.info("deleting product for sku");
        Product deleteProduct = productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("product not found with sku " + sku));

        // Delete the record
        productRepository.delete(deleteProduct);
    }

}