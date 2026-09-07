package com.apigateway.ai.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TrafficPredictionDto {
    private String serviceName;
    private double currentRps;
    private double predictedRpsNext5Min;
    private double predictedRpsNext15Min;
    private double predictedRpsNext1Hour;
    private String loadTrend; // SURGE, STABLE, DECREASING
    private String recommendedScaleAction; // SCALE_UP, MAINTAIN, SCALE_DOWN
    private double confidenceScore;
    private List<Map<String, Object>> forecastSeries;

    public TrafficPredictionDto() {}

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public double getCurrentRps() { return currentRps; }
    public void setCurrentRps(double currentRps) { this.currentRps = currentRps; }

    public double getPredictedRpsNext5Min() { return predictedRpsNext5Min; }
    public void setPredictedRpsNext5Min(double predictedRpsNext5Min) { this.predictedRpsNext5Min = predictedRpsNext5Min; }

    public double getPredictedRpsNext15Min() { return predictedRpsNext15Min; }
    public void setPredictedRpsNext15Min(double predictedRpsNext15Min) { this.predictedRpsNext15Min = predictedRpsNext15Min; }

    public double getPredictedRpsNext1Hour() { return predictedRpsNext1Hour; }
    public void setPredictedRpsNext1Hour(double predictedRpsNext1Hour) { this.predictedRpsNext1Hour = predictedRpsNext1Hour; }

    public String getLoadTrend() { return loadTrend; }
    public void setLoadTrend(String loadTrend) { this.loadTrend = loadTrend; }

    public String getRecommendedScaleAction() { return recommendedScaleAction; }
    public void setRecommendedScaleAction(String recommendedScaleAction) { this.recommendedScaleAction = recommendedScaleAction; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public List<Map<String, Object>> getForecastSeries() { return forecastSeries; }
    public void setForecastSeries(List<Map<String, Object>> forecastSeries) { this.forecastSeries = forecastSeries; }
}
