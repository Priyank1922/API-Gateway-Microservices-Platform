package com.apigateway.gateway.filter;

import com.apigateway.gateway.service.AiAnomalyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class AiThreatDetectionFilter implements GlobalFilter, Ordered {

    private final AiAnomalyService anomalyService;

    @Value("${security.ai-threat-detection.enabled:true}")
    private boolean enabled;

    public AiThreatDetectionFilter(AiAnomalyService anomalyService) {
        this.anomalyService = anomalyService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        if (isExemptPath(path)) {
            return chain.filter(exchange);
        }

        String query = exchange.getRequest().getURI().getQuery();
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");

        AiAnomalyService.InspectionResult result = anomalyService.evaluateRequest(path, query, userAgent);

        if (result.isBlocked()) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            response.getHeaders().set("X-Threat-Detected", result.getThreatType());
            response.getHeaders().set("X-Threat-Score", String.valueOf(result.getScore()));

            String responseBody = String.format(
                    "{\"error\":\"Security Threat Intercepted\",\"status\":403,\"threatType\":\"%s\",\"threatScore\":%.1f,\"details\":\"%s\",\"timestamp\":\"%s\",\"action\":\"BLOCKED_BY_GATEWAY_AI\"}",
                    result.getThreatType(), result.getScore(), result.getDetails(), LocalDateTime.now()
            );

            DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }

        // Attach threat score header for telemetry
        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().set("X-Threat-Score", String.valueOf(result.getScore()));
            return Mono.empty();
        });

        return chain.filter(exchange);
    }

    private boolean isExemptPath(String path) {
        return path.equals("/") ||
                path.startsWith("/sandbox") ||
                path.startsWith("/resilience") ||
                path.startsWith("/security") ||
                path.startsWith("/docs") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/actuator") ||
                path.startsWith("/fallback/") ||
                path.startsWith("/gateway-api/");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
