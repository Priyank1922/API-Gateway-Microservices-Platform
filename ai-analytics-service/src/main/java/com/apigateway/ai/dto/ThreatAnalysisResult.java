package com.apigateway.ai.dto;

import java.util.List;

public class ThreatAnalysisResult {
    private boolean malicious;
    private double threatScore; // 0 - 100
    private String threatType; // SAFE, SQL_INJECTION, XSS_ATTACK, PATH_TRAVERSAL, HEADER_ANOMALY, RATE_ABUSE
    private String actionRecommended; // ALLOW, FLAG, THROTTLE, BLOCK
    private List<String> detectedPatterns;
    private String explanation;

    public ThreatAnalysisResult() {}

    public ThreatAnalysisResult(boolean malicious, double threatScore, String threatType, String actionRecommended, List<String> detectedPatterns, String explanation) {
        this.malicious = malicious;
        this.threatScore = threatScore;
        this.threatType = threatType;
        this.actionRecommended = actionRecommended;
        this.detectedPatterns = detectedPatterns;
        this.explanation = explanation;
    }

    public boolean isMalicious() { return malicious; }
    public void setMalicious(boolean malicious) { this.malicious = malicious; }

    public double getThreatScore() { return threatScore; }
    public void setThreatScore(double threatScore) { this.threatScore = threatScore; }

    public String getThreatType() { return threatType; }
    public void setThreatType(String threatType) { this.threatType = threatType; }

    public String getActionRecommended() { return actionRecommended; }
    public void setActionRecommended(String actionRecommended) { this.actionRecommended = actionRecommended; }

    public List<String> getDetectedPatterns() { return detectedPatterns; }
    public void setDetectedPatterns(List<String> detectedPatterns) { this.detectedPatterns = detectedPatterns; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
