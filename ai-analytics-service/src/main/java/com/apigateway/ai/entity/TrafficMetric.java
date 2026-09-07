package com.apigateway.ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "traffic_metrics")
public class TrafficMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false, length = 60)
    private String serviceName;

    @Column(nullable = false)
    private Double rps;

    @Column(name = "avg_latency_ms", nullable = false)
    private Double avgLatencyMs;

    @Column(name = "error_rate_percent", nullable = false)
    private Double errorRatePercent;

    @Column(name = "anomaly_score", nullable = false)
    private Double anomalyScore;

    @Column(name = "predicted_rps_next_5min", nullable = false)
    private Double predictedRpsNext5Min;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    public TrafficMetric() {}

    public TrafficMetric(String serviceName, Double rps, Double avgLatencyMs, Double errorRatePercent, Double anomalyScore, Double predictedRpsNext5Min) {
        this.serviceName = serviceName;
        this.rps = rps;
        this.avgLatencyMs = avgLatencyMs;
        this.errorRatePercent = errorRatePercent;
        this.anomalyScore = anomalyScore;
        this.predictedRpsNext5Min = predictedRpsNext5Min;
        this.recordedAt = LocalDateTime.now();
    }

    @PrePersist
    public void onPrePersist() {
        if (this.recordedAt == null) {
            this.recordedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public Double getRps() { return rps; }
    public void setRps(Double rps) { this.rps = rps; }

    public Double getAvgLatencyMs() { return avgLatencyMs; }
    public void setAvgLatencyMs(Double avgLatencyMs) { this.avgLatencyMs = avgLatencyMs; }

    public Double getErrorRatePercent() { return errorRatePercent; }
    public void setErrorRatePercent(Double errorRatePercent) { this.errorRatePercent = errorRatePercent; }

    public Double getAnomalyScore() { return anomalyScore; }
    public void setAnomalyScore(Double anomalyScore) { this.anomalyScore = anomalyScore; }

    public Double getPredictedRpsNext5Min() { return predictedRpsNext5Min; }
    public void setPredictedRpsNext5Min(Double predictedRpsNext5Min) { this.predictedRpsNext5Min = predictedRpsNext5Min; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
