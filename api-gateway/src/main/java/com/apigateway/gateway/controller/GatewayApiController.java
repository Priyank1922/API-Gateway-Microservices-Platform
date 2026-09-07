package com.apigateway.gateway.controller;

import com.apigateway.gateway.filter.LoggingFilter;
import com.apigateway.gateway.filter.RateLimiterFilter;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/gateway-api")
public class GatewayApiController {

    private final LoggingFilter loggingFilter;
    private final RateLimiterFilter rateLimiterFilter;
    private final DiscoveryClient discoveryClient;
    private final RouteLocator routeLocator;
    private final WebClient webClient;

    public GatewayApiController(LoggingFilter loggingFilter,
                                RateLimiterFilter rateLimiterFilter,
                                DiscoveryClient discoveryClient,
                                RouteLocator routeLocator) {
        this.loggingFilter = loggingFilter;
        this.rateLimiterFilter = rateLimiterFilter;
        this.discoveryClient = discoveryClient;
        this.routeLocator = routeLocator;
        this.webClient = WebClient.builder().build();
    }

    @GetMapping("/stats")
    public Mono<ResponseEntity<Map<String, Object>>> getStats() {
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        long uptimeSec = uptimeMs / 1000;

        Map<String, Object> stats = new HashMap<>();
        stats.put("uptimeSeconds", uptimeSec);
        stats.put("uptimeFormatted", formatUptime(uptimeSec));
        stats.put("totalRequests", loggingFilter.getTotalRequests());
        stats.put("totalSuccesses", loggingFilter.getTotalSuccesses());
        stats.put("totalErrors", loggingFilter.getTotalErrors());
        stats.put("rateLimitedCount", rateLimiterFilter.getTotalRateLimitedCount());

        Map<String, Long> routeCallMap = new HashMap<>();
        loggingFilter.getRouteStats().forEach((k, v) -> routeCallMap.put(k, v.get()));
        stats.put("routeStats", routeCallMap);

        List<String> registeredServices = discoveryClient.getServices();
        stats.put("registeredServicesCount", registeredServices.size());
        stats.put("registeredServices", registeredServices);
        stats.put("timestamp", LocalDateTime.now().toString());

        return Mono.just(ResponseEntity.ok(stats));
    }

    @GetMapping("/routes")
    public Mono<ResponseEntity<List<Map<String, Object>>>> getRoutes() {
        return routeLocator.getRoutes()
                .map(r -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", r.getId());
                    item.put("uri", r.getUri().toString());
                    item.put("order", r.getOrder());
                    item.put("predicate", r.getPredicate().toString());
                    return item;
                })
                .collectList()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/services-health")
    public Mono<ResponseEntity<List<Map<String, Object>>>> getServicesHealth() {
        List<ServiceHealthCheck> targets = List.of(
                new ServiceHealthCheck("EUREKA-SERVER", "Service Discovery", "http://localhost:8761", 8761),
                new ServiceHealthCheck("AUTH-SERVICE", "Auth & Users", "http://localhost:8081/api/auth/health", 8081),
                new ServiceHealthCheck("PRODUCT-SERVICE", "Product Catalog", "http://localhost:8082/api/products/health", 8082),
                new ServiceHealthCheck("ORDER-SERVICE", "Order & Billing", "http://localhost:8083/api/orders/health", 8083),
                new ServiceHealthCheck("AI-ANALYTICS-SERVICE", "AI Threat & Load", "http://localhost:8084/api/ai/health", 8084)
        );

        List<Mono<Map<String, Object>>> checks = new ArrayList<>();
        for (ServiceHealthCheck target : targets) {
            Mono<Map<String, Object>> check = webClient.get()
                    .uri(target.url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(1200))
                    .map(body -> {
                        Map<String, Object> res = new HashMap<>();
                        res.put("serviceName", target.name);
                        res.put("description", target.description);
                        res.put("port", target.port);
                        res.put("status", "UP");
                        res.put("url", target.url);
                        return res;
                    })
                    .onErrorResume(e -> {
                        Map<String, Object> res = new HashMap<>();
                        res.put("serviceName", target.name);
                        res.put("description", target.description);
                        res.put("port", target.port);
                        res.put("status", "DOWN");
                        res.put("url", target.url);
                        res.put("error", e.getMessage());
                        return Mono.just(res);
                    });
            checks.add(check);
        }

        return Mono.zip(checks, results -> {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Object obj : results) {
                if (obj instanceof Map) {
                    list.add((Map<String, Object>) obj);
                }
            }
            return ResponseEntity.ok(list);
        });
    }

    private static class ServiceHealthCheck {
        String name;
        String description;
        String url;
        int port;

        ServiceHealthCheck(String name, String description, String url, int port) {
            this.name = name;
            this.description = description;
            this.url = url;
            this.port = port;
        }
    }

    private String formatUptime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02dh %02dm %02ds", hours, minutes, secs);
    }
}
