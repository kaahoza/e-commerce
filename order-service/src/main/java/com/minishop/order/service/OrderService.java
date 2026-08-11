package com.minishop.order.service;

import com.minishop.order.client.ProductClient;
import com.minishop.order.client.ProductDto;
import com.minishop.order.client.UserClient;
import com.minishop.order.dto.OrderItemRequest;
import com.minishop.order.dto.OrderRequest;
import com.minishop.order.dto.OrderResponse;
import com.minishop.order.exception.InsufficientStockException;
import com.minishop.order.exception.ResourceNotFoundException;
import com.minishop.order.model.Order;
import com.minishop.order.model.OrderItem;
import com.minishop.order.model.OrderStatus;
import com.minishop.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final UserClient userClient;

    /**
     * Places an order:
     *  1. Validate the user exists (call User Service)
     *  2. For each line item, validate the product exists and has stock (call Product Service)
     *  3. Persist the order as PENDING, then CONFIRMED once all checks pass
     *  4. Decrement stock on Product Service for each item
     *
     * If any downstream call fails after retries, the circuit breaker's
     * fallback throws a *ServiceUnavailableException, which bubbles up here
     * and results in a 503 to the caller rather than a half-created order.
     */
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        // 1. Validate user
        userClient.getUser(request.getUserId());

        // 2. Validate products + build order items
        Order order = Order.builder()
                .userId(request.getUserId())
                .status(OrderStatus.PENDING)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductDto product = productClient.getProduct(itemRequest.getProductId());

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        "Not enough stock for product: " + product.getName());
            }

            OrderItem item = OrderItem.builder()
                    .productId(product.getId())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            order.addItem(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.CONFIRMED);
        Order savedOrder = orderRepository.save(order);

        // 3. Decrement stock now that the order is confirmed
        for (OrderItem item : savedOrder.getItems()) {
            productClient.decrementStock(item.getProductId(), item.getQuantity());
        }

        log.info("Order {} placed for user {} with {} item(s), total {}",
                savedOrder.getId(), savedOrder.getUserId(), savedOrder.getItems().size(), total);

        return OrderResponse.fromEntity(savedOrder);
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return OrderResponse.fromEntity(order);
    }

    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }
}
