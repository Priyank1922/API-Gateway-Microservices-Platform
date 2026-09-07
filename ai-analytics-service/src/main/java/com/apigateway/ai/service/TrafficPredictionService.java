package com.apigateway.ai.service;

import com.apigateway.ai.dto.TrafficPredictionDto;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class TrafficPredictionService {

    private final ConcurrentLinkedDeque<Double> recentRpsObservations = new ConcurrentLinkedDeque<>();

    public TrafficPredictionService() {
        // initialize with baseline series
        for (int i = 0; i < 20; i++) {
            recentRpsObservations.add(15.0 + (Math.sin(i * 0.4) * 5.0) + (Math.random() * 2.0));
        }
    }

    public synchronized void recordRps(double rps) {
        recentRpsObservations.add(rps);
        if (recentRpsObservations.size() > 50) {
            recentRpsObservations.poll();
        }
    }

    public TrafficPredictionDto predictTraffic(String serviceName) {
        List<Double> rpsList = new ArrayList<>(recentRpsObservations);
        if (rpsList.isEmpty()) {
            rpsList.add(10.0);
        }

        double currentRps = rpsList.get(rpsList.size() - 1);
        
        // Compute moving average & slope (trend)
        double sum = 0;
        for (double r : rpsList) sum += r;
        double movingAvg = sum / rpsList.size();

        // Calculate short-term slope over last 5 readings
        int window = Math.min(5, rpsList.size());
        double firstRecent = rpsList.get(rpsList.size() - window);
        double slope = (currentRps - firstRecent) / window;

        double predicted5m = Math.max(1.0, currentRps + (slope * 2.5) + (Math.random() * 1.5));
        double predicted15m = Math.max(1.0, movingAvg + (slope * 5.0) + (Math.random() * 3.0));
        double predicted1h = Math.max(1.0, movingAvg * 1.15 + (Math.random() * 4.0));

        String trend = "STABLE";
        String action = "MAINTAIN";

        if (slope > 1.5 || predicted5m > currentRps * 1.3) {
            trend = "SURGE";
            action = "SCALE_UP";
        } else if (slope < -1.5) {
            trend = "DECREASING";
            action = "SCALE_DOWN";
        }

        double confidence = Math.min(98.5, 85.0 + (Math.min(rpsList.size(), 40) * 0.3));

        // Generate forecast series for charts
        List<Map<String, Object>> series = new ArrayList<>();
        LocalTime now = LocalTime.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        for (int i = -10; i <= 10; i++) {
            Map<String, Object> point = new HashMap<>();
            LocalTime time = now.plusMinutes(i * 2);
            point.put("time", time.format(fmt));
            if (i <= 0) {
                int idx = Math.max(0, rpsList.size() + i - 1);
                point.put("actual", Math.round(rpsList.get(Math.min(idx, rpsList.size() - 1)) * 10.0) / 10.0);
                point.put("predicted", null);
            } else {
                point.put("actual", null);
                double forecastVal = Math.max(0, currentRps + (slope * i * 0.8) + (Math.sin(i) * 2.0));
                point.put("predicted", Math.round(forecastVal * 10.0) / 10.0);
            }
            series.add(point);
        }

        TrafficPredictionDto dto = new TrafficPredictionDto();
        dto.setServiceName(serviceName != null ? serviceName : "GLOBAL_GATEWAY_CLUSTER");
        dto.setCurrentRps(Math.round(currentRps * 10.0) / 10.0);
        dto.setPredictedRpsNext5Min(Math.round(predicted5m * 10.0) / 10.0);
        dto.setPredictedRpsNext15Min(Math.round(predicted15m * 10.0) / 10.0);
        dto.setPredictedRpsNext1Hour(Math.round(predicted1h * 10.0) / 10.0);
        dto.setLoadTrend(trend);
        dto.setRecommendedScaleAction(action);
        dto.setConfidenceScore(Math.round(confidence * 10.0) / 10.0);
        dto.setForecastSeries(series);

        return dto;
    }
}
