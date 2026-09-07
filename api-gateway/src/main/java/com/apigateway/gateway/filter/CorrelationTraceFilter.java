package com.apigateway.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationTraceFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String START_TIME_ATTR = "gatewayRequestStartTime";
    public static final String RESPONSE_TIME_HEADER = "X-Response-Time-Millis";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(START_TIME_ATTR, startTime);

        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = "trace-" + UUID.randomUUID().toString().substring(0, 13);
        }

        // Mutate request with correlation ID so downstream microservices receive it
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build();

        final String finalCorrelationId = correlationId;

        // Add response header after downstream execution
        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, finalCorrelationId);
            Long start = exchange.getAttribute(START_TIME_ATTR);
            if (start != null) {
                long duration = System.currentTimeMillis() - start;
                exchange.getResponse().getHeaders().set(RESPONSE_TIME_HEADER, String.valueOf(duration));
            }
            return Mono.empty();
        });

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
