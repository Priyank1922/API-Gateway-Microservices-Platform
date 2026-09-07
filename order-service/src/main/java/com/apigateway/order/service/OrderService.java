package com.apigateway.order.service;

import com.apigateway.order.dto.*;
import com.apigateway.order.entity.Order;
import com.apigateway.order.entity.OrderItem;
import com.apigateway.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<OrderResponse> getOrderById(Long id) {
        return orderRepository.findById(id).map(this::toResponse);
    }

    public Optional<OrderResponse> getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber).map(this::toResponse);
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByUsername(String username) {
        return orderRepository.findByUsernameOrderByCreatedAtDesc(username).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String username = (request.getUsername() != null && !request.getUsername().isBlank()) ? request.getUsername() : "User-" + request.getUserId();
        String paymentMethod = (request.getPaymentMethod() != null) ? request.getPaymentMethod() : "CREDIT_CARD";

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : request.getItems()) {
            BigDecimal subtotal = itemReq.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(subtotal);
        }

        Order order = new Order(
                orderNumber,
                request.getUserId(),
                username,
                total,
                "PENDING",
                paymentMethod
        );

        for (OrderItemRequest itemReq : request.getItems()) {
            OrderItem item = new OrderItem(
                    itemReq.getProductId(),
                    itemReq.getProductName(),
                    itemReq.getSku(),
                    itemReq.getPrice(),
                    itemReq.getQuantity()
            );
            order.addItem(item);
        }

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse markOrderPaid(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Only PENDING orders can be marked as PAID. Current status: " + order.getStatus());
        }

        order.setStatus("PAID");
        Order updated = orderRepository.save(order);
        return toResponse(updated);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        if ("COMPLETED".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Completed orders cannot be cancelled.");
        }

        order.setStatus("CANCELLED");
        Order updated = orderRepository.save(order);
        return toResponse(updated);
    }

    private OrderResponse toResponse(Order o) {
        List<OrderItemDto> items = o.getItems().stream()
                .map(i -> new OrderItemDto(
                        i.getId(),
                        i.getProductId(),
                        i.getProductName(),
                        i.getSku(),
                        i.getPrice(),
                        i.getQuantity(),
                        i.getSubtotal()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                o.getId(),
                o.getOrderNumber(),
                o.getUserId(),
                o.getUsername(),
                o.getTotalAmount(),
                o.getStatus(),
                o.getPaymentMethod(),
                o.getCreatedAt(),
                items
        );
    }
}
