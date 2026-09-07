package com.apigateway.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/auth")
    public ResponseEntity<?> authFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "Auth Service Degraded",
                "circuitBreaker", "OPEN",
                "status", 503,
                "message", "The Authentication Microservice is currently unavailable or experiencing high latency. Circuit breaker activated fallback.",
                "action", "Please retry in a few moments.",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @RequestMapping("/products")
    public ResponseEntity<?> productFallback() {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "status", "FALLBACK_CACHED_RESPONSE",
                "circuitBreaker", "OPEN",
                "message", "Product Service is currently taking longer than expected. Serving static fallback catalog.",
                "data", List.of(
                        Map.of("id", 0, "name", "Cached Product Backup (Emergency Cache)", "sku", "CACHE-BACKUP-01", "price", 99.99, "stockQuantity", 50, "category", "Fallback")
                ),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @RequestMapping("/orders")
    public ResponseEntity<?> orderFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "Order Service Degraded",
                "circuitBreaker", "OPEN",
                "status", 503,
                "message", "Order Processing Service is currently unreachable. Your order request was safely queued or rejected to protect downstream integrity.",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @RequestMapping("/ai")
    public ResponseEntity<?> aiFallback() {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "status", "AI_STANDALONE_FALLBACK",
                "circuitBreaker", "OPEN",
                "message", "AI Analytics Service offline. Gateway local anomaly heuristic engine active.",
                "currentGlobalAnomalyScore", 15.0,
                "threatsBlocked", 0,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
