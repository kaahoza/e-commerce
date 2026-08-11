package com.anele.product_service.controller.service;


import com.anele.product_service.dto.ProductRequest;
import com.anele.product_service.dto.ProductResponse;
import com.anele.product_service.exception.ResourceNotFoundException;
import com.anele.product_service.entity.Product;
import com.anele.product_service.repository.ProductRepository;
import com.anele.product_service.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private ProductRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new ProductRequest("SKU-123", "Gaming Laptop", "High performance laptop", "Electronics", new BigDecimal("15000.00"), 50);

        sampleProduct = Product.builder()
                .id(1L)
                .sku("SKU-123")
                .name("Gaming Laptop")
                .description("High performance laptop")
                .category("Electronics")
                .price(new BigDecimal("15000.00"))
                .stockQuantity(50)
                .build();
    }

    @Test
    @DisplayName("Create Product - Should successfully save and return response")
    void createProduct_Success() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        // Act
        ProductResponse response = productService.createProduct(sampleRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSku()).isEqualTo("SKU-123");
        assertThat(response.getName()).isEqualTo("Gaming Laptop");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Get All Products - Should return list of mapped product responses")
    void getAllProducts_Success() {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct));

        // Act
        List<ProductResponse> responses = productService.getAllProducts();

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getSku()).isEqualTo("SKU-123");
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Get Product By Sku - Should return target product response when found")
    void getProductBySku_Success() {
        // Arrange
        String targetSku = "SKU-123";
        when(productRepository.findBySku(targetSku)).thenReturn(Optional.of(sampleProduct));

        // Act
        ProductResponse response = productService.getProductBySku(targetSku);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSku()).isEqualTo(targetSku);
        verify(productRepository, times(1)).findBySku(targetSku);
    }

    @Test
    @DisplayName("Get Product By Sku - Should throw ResourceNotFoundException when missing")
    void getProductBySku_NotFound_ThrowsException() {
        // Arrange
        String missingSku = "SKU-999";
        when(productRepository.findBySku(missingSku)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductBySku(missingSku))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");

        verify(productRepository, times(1)).findBySku(missingSku);
    }

    @Test
    @DisplayName("Update Product By Sku - Should merge fields and save updated entity")
    void updateProductBySku_Success() {
        // Arrange
        String targetSku = "SKU-123";
        ProductRequest updatedRequest = new ProductRequest("SKU-123", "New Name", "New Desc", "Electronics", new BigDecimal("16000.00"), 40);

        Product updatedProduct = Product.builder()
                .id(1L)
                .sku("SKU-123")
                .name("New Name")
                .description("New Desc")
                .category("Electronics")
                .price(new BigDecimal("16000.00"))
                .stockQuantity(40)
                .build();

        when(productRepository.findBySku(targetSku)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        ProductResponse response = productService.updateProductBySku(targetSku, updatedRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("New Name");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("16000.00"));
        verify(productRepository, times(1)).findBySku(targetSku);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Delete Product By Sku - Should locate entity and perform delete mapping")
    void deleteProductBySku_Success() {
        // Arrange
        String targetSku = "SKU-123";
        when(productRepository.findBySku(targetSku)).thenReturn(Optional.of(sampleProduct));
        doNothing().when(productRepository).delete(sampleProduct); // Or deleteBySku(targetSku) depending on implementation

        // Act
        productService.deleteProductBySku(targetSku);

        // Assert
        verify(productRepository, times(1)).findBySku(targetSku);
        verify(productRepository, times(1)).delete(sampleProduct);
    }
}






