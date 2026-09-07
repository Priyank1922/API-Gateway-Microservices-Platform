package com.apigateway.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger("GATEWAY_ACCESS");

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalSuccesses = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final Map<String, AtomicLong> routeStats = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        totalRequests.incrementAndGet();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Long startTime = exchange.getAttribute(CorrelationTraceFilter.START_TIME_ATTR);
            long duration = (startTime != null) ? (System.currentTimeMillis() - startTime) : 0;

            String correlationId = exchange.getRequest().getHeaders().getFirst(CorrelationTraceFilter.CORRELATION_ID_HEADER);
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            String routeId = (route != null) ? route.getId() : "internal";

            routeStats.computeIfAbsent(routeId, k -> new AtomicLong(0)).incrementAndGet();

            HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
            int code = (statusCode != null) ? statusCode.value() : 200;

            if (code >= 400) {
                totalErrors.incrementAndGet();
            } else {
                totalSuccesses.incrementAndGet();
            }

            log.info("[{}] {} {} -> Status: {} ({} ms) [Route: {}]",
                    correlationId,
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI().getPath(),
                    code,
                    duration,
                    routeId
            );
        }));
    }

    public long getTotalRequests() { return totalRequests.get(); }
    public long getTotalSuccesses() { return totalSuccesses.get(); }
    public long getTotalErrors() { return totalErrors.get(); }
    public Map<String, AtomicLong> getRouteStats() { return routeStats; }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
