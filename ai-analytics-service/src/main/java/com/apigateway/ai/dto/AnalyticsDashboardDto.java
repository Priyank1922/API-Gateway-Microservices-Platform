package com.apigateway.ai.dto;

import com.apigateway.ai.entity.SecurityThreatLog;
import com.apigateway.ai.entity.TrafficMetric;

import java.util.List;
import java.util.Map;

public class AnalyticsDashboardDto {
    private long totalRequestsAnalyzed;
    private long threatsBlocked;
    private long threatsFlagged;
    private double currentGlobalAnomalyScore;
    private double currentGlobalRps;
    private double avgLatencyMs;
    private List<SecurityThreatLog> recentThreats;
    private List<TrafficMetric> trafficHistory;
    private Map<String, Double> threatDistribution;
    private TrafficPredictionDto prediction;

    public AnalyticsDashboardDto() {}

    public long getTotalRequestsAnalyzed() { return totalRequestsAnalyzed; }
    public void setTotalRequestsAnalyzed(long totalRequestsAnalyzed) { this.totalRequestsAnalyzed = totalRequestsAnalyzed; }

    public long getThreatsBlocked() { return threatsBlocked; }
    public void setThreatsBlocked(long threatsBlocked) { this.threatsBlocked = threatsBlocked; }

    public long getThreatsFlagged() { return threatsFlagged; }
    public void setThreatsFlagged(long threatsFlagged) { this.threatsFlagged = threatsFlagged; }

    public double getCurrentGlobalAnomalyScore() { return currentGlobalAnomalyScore; }
    public void setCurrentGlobalAnomalyScore(double currentGlobalAnomalyScore) { this.currentGlobalAnomalyScore = currentGlobalAnomalyScore; }

    public double getCurrentGlobalRps() { return currentGlobalRps; }
    public void setCurrentGlobalRps(double currentGlobalRps) { this.currentGlobalRps = currentGlobalRps; }

    public double getAvgLatencyMs() { return avgLatencyMs; }
    public void setAvgLatencyMs(double avgLatencyMs) { this.avgLatencyMs = avgLatencyMs; }

    public List<SecurityThreatLog> getRecentThreats() { return recentThreats; }
    public void setRecentThreats(List<SecurityThreatLog> recentThreats) { this.recentThreats = recentThreats; }

    public List<TrafficMetric> getTrafficHistory() { return trafficHistory; }
    public void setTrafficHistory(List<TrafficMetric> trafficHistory) { this.trafficHistory = trafficHistory; }

    public Map<String, Double> getThreatDistribution() { return threatDistribution; }
    public void setThreatDistribution(Map<String, Double> threatDistribution) { this.threatDistribution = threatDistribution; }

    public TrafficPredictionDto getPrediction() { return prediction; }
    public void setPrediction(TrafficPredictionDto prediction) { this.prediction = prediction; }
}
