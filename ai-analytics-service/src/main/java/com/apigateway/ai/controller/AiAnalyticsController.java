package com.apigateway.ai.controller;

import com.apigateway.ai.dto.*;
import com.apigateway.ai.entity.SecurityThreatLog;
import com.apigateway.ai.service.AiAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiAnalyticsController {

    private final AiAnalyticsService aiAnalyticsService;

    public AiAnalyticsController(AiAnalyticsService aiAnalyticsService) {
        this.aiAnalyticsService = aiAnalyticsService;
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsDashboardDto> getAnalytics(
            @RequestParam(required = false, defaultValue = "GLOBAL_GATEWAY_CLUSTER") String serviceName) {
        return ResponseEntity.ok(aiAnalyticsService.getDashboardData(serviceName));
    }

    @PostMapping("/analyze-request")
    public ResponseEntity<ThreatAnalysisResult> analyzeRequest(@RequestBody ThreatAnalysisRequest request) {
        ThreatAnalysisResult result = aiAnalyticsService.inspectRequest(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/log-threat")
    public ResponseEntity<SecurityThreatLog> logThreat(@RequestBody SecurityThreatLog threat) {
        return ResponseEntity.ok(aiAnalyticsService.logThreat(threat));
    }

    @GetMapping("/threats")
    public ResponseEntity<List<SecurityThreatLog>> getRecentThreats() {
        return ResponseEntity.ok(aiAnalyticsService.getRecentThreats());
    }

    @GetMapping("/prediction")
    public ResponseEntity<TrafficPredictionDto> getTrafficPrediction(
            @RequestParam(required = false, defaultValue = "GLOBAL_GATEWAY_CLUSTER") String serviceName) {
        return ResponseEntity.ok(aiAnalyticsService.getPrediction(serviceName));
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "service", "ai-analytics-service",
                "status", "UP",
                "port", 8084,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
