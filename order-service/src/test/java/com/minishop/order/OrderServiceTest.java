package com.minishop.order;

import com.minishop.order.client.ProductClient;
import com.minishop.order.client.ProductDto;
import com.minishop.order.client.UserClient;
import com.minishop.order.client.UserDto;
import com.minishop.order.dto.OrderItemRequest;
import com.minishop.order.dto.OrderRequest;
import com.minishop.order.dto.OrderResponse;
import com.minishop.order.exception.InsufficientStockException;
import com.minishop.order.model.Order;
import com.minishop.order.model.OrderStatus;
import com.minishop.order.repository.OrderRepository;
import com.minishop.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_succeeds_whenUserAndProductValid() {
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setUserId(10L);
        request.setItems(List.of(itemRequest));

        when(userClient.getUser(10L)).thenReturn(new UserDto(10L, "jane", "jane@example.com", "ROLE_USER"));
        when(productClient.getProduct(1L))
                .thenReturn(new ProductDto(1L, "Mouse", "desc", new BigDecimal("100.00"), 50, "Electronics"));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(99L);
            return o;
        });

        OrderResponse response = orderService.placeOrder(request);

        assertEquals(99L, response.getId());
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        assertEquals(new BigDecimal("200.00"), response.getTotalAmount());
        verify(productClient).decrementStock(1L, 2);
    }

    @Test
    void placeOrder_throwsInsufficientStock_whenNotEnoughStock() {
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(100);

        OrderRequest request = new OrderRequest();
        request.setUserId(10L);
        request.setItems(List.of(itemRequest));

        when(userClient.getUser(10L)).thenReturn(new UserDto(10L, "jane", "jane@example.com", "ROLE_USER"));
        when(productClient.getProduct(1L))
                .thenReturn(new ProductDto(1L, "Mouse", "desc", new BigDecimal("100.00"), 5, "Electronics"));

        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder(request));
    }
}
