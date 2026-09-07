package com.apigateway.ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_threat_logs")
public class SecurityThreatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_ip", nullable = false, length = 50)
    private String clientIp;

    @Column(nullable = false, length = 255)
    private String endpoint;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "threat_type", nullable = false, length = 50)
    private String threatType; // SQL_INJECTION, XSS_ATTACK, PATH_TRAVERSAL, RATE_ABUSE, ANOMALOUS_SPIKE

    @Column(name = "threat_score", nullable = false)
    private Double threatScore; // 0.0 - 100.0

    @Column(name = "payload_snippet", length = 500)
    private String payloadSnippet;

    @Column(name = "action_taken", length = 50)
    private String actionTaken; // BLOCKED, FLAGGED, THROTTLED, MONITORED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public SecurityThreatLog() {}

    public SecurityThreatLog(String clientIp, String endpoint, String httpMethod, String threatType, Double threatScore, String payloadSnippet, String actionTaken) {
        this.clientIp = clientIp;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.threatType = threatType;
        this.threatScore = threatScore;
        this.payloadSnippet = payloadSnippet;
        this.actionTaken = actionTaken;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void onPrePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getThreatType() { return threatType; }
    public void setThreatType(String threatType) { this.threatType = threatType; }

    public Double getThreatScore() { return threatScore; }
    public void setThreatScore(Double threatScore) { this.threatScore = threatScore; }

    public String getPayloadSnippet() { return payloadSnippet; }
    public void setPayloadSnippet(String payloadSnippet) { this.payloadSnippet = payloadSnippet; }

    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
