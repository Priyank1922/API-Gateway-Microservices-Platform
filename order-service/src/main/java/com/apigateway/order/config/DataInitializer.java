package com.apigateway.order.config;

import com.apigateway.order.entity.Order;
import com.apigateway.order.entity.OrderItem;
import com.apigateway.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final OrderRepository orderRepository;

    public DataInitializer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        try {
            if (orderRepository.count() == 0) {
                log.info("Seeding initial orders into Order database...");

                Order sampleOrder = new Order(
                        "ORD-INIT-882910-SAMPLE",
                        1L,
                        "admin",
                        new BigDecimal("2198.99"),
                        "PAID",
                        "CREDIT_CARD"
                );

                sampleOrder.addItem(new OrderItem(1L, "Edge API Gateway Accelerator X1", "GATEWAY-X1-PRO", new BigDecimal("1299.99"), 1));
                sampleOrder.addItem(new OrderItem(3L, "AI Inference Accelerator Card", "AI-INF-ACC-V2", new BigDecimal("899.00"), 1));

                orderRepository.save(sampleOrder);
                log.info("Seeded 1 sample order successfully.");
            }
        } catch (Exception e) {
            log.warn("Order database initialization notice: {}", e.getMessage());
        }
    }
}
