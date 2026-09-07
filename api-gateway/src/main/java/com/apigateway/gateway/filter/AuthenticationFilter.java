package com.apigateway.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // 1. Allow public routes
        if (isPublicRoute(path, method)) {
            return chain.filter(exchange);
        }

        // 2. Check for Bearer token or API Key
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String apiKeyHeader = request.getHeaders().getFirst("X-API-Key");

        if ((authHeader == null || !authHeader.startsWith("Bearer ")) && (apiKeyHeader == null || apiKeyHeader.isBlank())) {
            return unauthorizedResponse(exchange, "Missing or invalid authorization header (Bearer JWT or X-API-Key required)");
        }

        // 3. If API Key provided, validate format (or downstream lookup)
        if (apiKeyHeader != null && !apiKeyHeader.isBlank()) {
            if (apiKeyHeader.startsWith("ak_") && apiKeyHeader.length() >= 10) {
                // Pass valid API key downstream with injected identity
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-Auth-Type", "API_KEY")
                        .build();
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            } else {
                return unauthorizedResponse(exchange, "Invalid API Key format");
            }
        }

        // 4. Validate JWT Bearer token
        String token = authHeader.substring(7);
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            Object userId = claims.get("userId");

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Name", username != null ? username : "")
                    .header("X-User-Role", role != null ? role : "ROLE_USER")
                    .header("X-User-Id", userId != null ? userId.toString() : "")
                    .header("X-Auth-Type", "JWT")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            return unauthorizedResponse(exchange, "JWT token validation failed: " + e.getMessage());
        }
    }

    private boolean isPublicRoute(String path, HttpMethod method) {
        // Static and UI
        if (path.equals("/") ||
                path.startsWith("/sandbox") ||
                path.startsWith("/resilience") ||
                path.startsWith("/security") ||
                path.startsWith("/docs") ||
                path.startsWith("/routes") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/actuator") ||
                path.startsWith("/fallback/") ||
                path.startsWith("/gateway-api/")) {
            return true;
        }

        // Public Auth endpoints
        if (path.equals("/api/auth/login") ||
                path.equals("/api/auth/register") ||
                path.equals("/api/auth/validate") ||
                path.equals("/api/auth/health") ||
                path.equals("/api/auth/verify-key")) {
            return true;
        }

        // Public Product GET endpoints
        if (path.startsWith("/api/products") && HttpMethod.GET.equals(method)) {
            return true;
        }

        // Public AI endpoints for telemetry
        if (path.startsWith("/api/ai")) {
            return true;
        }

        return false;
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = String.format(
                "{\"error\":\"Unauthorized\",\"status\":401,\"message\":\"%s\",\"timestamp\":\"%s\"}",
                message, LocalDateTime.now()
        );

        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
