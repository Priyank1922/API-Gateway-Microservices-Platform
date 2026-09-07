package com.apigateway.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiterFilter implements GlobalFilter, Ordered {

    @Value("${rate-limiter.replenish-rate:10}")
    private double replenishRate; // tokens per second

    @Value("${rate-limiter.burst-capacity:20}")
    private double burstCapacity; // max capacity

    private static class TokenBucket {
        double tokens;
        long lastRefillTimestamp;

        TokenBucket(double maxTokens) {
            this.tokens = maxTokens;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        synchronized boolean tryConsume(double tokensRequested, double replenishRate, double maxCapacity) {
            long now = System.currentTimeMillis();
            double secondsElapsed = (now - lastRefillTimestamp) / 1000.0;
            tokens = Math.min(maxCapacity, tokens + (secondsElapsed * replenishRate));
            lastRefillTimestamp = now;

            if (tokens >= tokensRequested) {
                tokens -= tokensRequested;
                return true;
            }
            return false;
        }

        synchronized int getRemainingTokens() {
            return (int) Math.max(0, Math.floor(tokens));
        }
    }

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final AtomicInteger totalRateLimitedRequests = new AtomicInteger(0);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Bypass static assets, UI templates, fallbacks, and actuator
        if (isExemptPath(path)) {
            return chain.filter(exchange);
        }

        String clientKey = resolveClientKey(exchange.getRequest());
        TokenBucket bucket = buckets.computeIfAbsent(clientKey, k -> new TokenBucket(burstCapacity));

        boolean allowed = bucket.tryConsume(1.0, replenishRate, burstCapacity);

        if (!allowed) {
            totalRateLimitedRequests.incrementAndGet();
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            response.getHeaders().set(HttpHeaders.RETRY_AFTER, "3");
            response.getHeaders().set("X-RateLimit-Limit", String.valueOf((int) burstCapacity));
            response.getHeaders().set("X-RateLimit-Remaining", "0");

            String errorJson = String.format(
                    "{\"error\":\"Too Many Requests\",\"status\":429,\"message\":\"Rate limit exceeded. Maximum %d req/sec with burst capacity of %d.\",\"retryAfterSeconds\":3,\"clientKey\":\"%s\",\"timestamp\":\"%s\"}",
                    (int) replenishRate, (int) burstCapacity, clientKey, LocalDateTime.now()
            );

            DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }

        // Add remaining tokens header
        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().set("X-RateLimit-Limit", String.valueOf((int) burstCapacity));
            exchange.getResponse().getHeaders().set("X-RateLimit-Remaining", String.valueOf(bucket.getRemainingTokens()));
            return Mono.empty();
        });

        return chain.filter(exchange);
    }

    private String resolveClientKey(ServerHttpRequest request) {
        String apiKey = request.getHeaders().getFirst("X-API-Key");
        if (apiKey != null && !apiKey.isBlank()) {
            return "apikey:" + apiKey;
        }

        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return "ip:" + remoteAddress.getAddress().getHostAddress();
        }
        return "ip:127.0.0.1";
    }

    private boolean isExemptPath(String path) {
        return path.equals("/") ||
                path.startsWith("/sandbox") ||
                path.startsWith("/resilience") ||
                path.startsWith("/security") ||
                path.startsWith("/docs") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/favicon.ico") ||
                path.startsWith("/actuator") ||
                path.startsWith("/fallback/") ||
                path.startsWith("/gateway-api/");
    }

    public int getTotalRateLimitedCount() {
        return totalRateLimitedRequests.get();
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
