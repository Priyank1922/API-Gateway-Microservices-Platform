package com.apigateway.ai.service;

import com.apigateway.ai.dto.ThreatAnalysisRequest;
import com.apigateway.ai.dto.ThreatAnalysisResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AnomalyDetectionEngine {

    private static final List<Pattern> SQLI_PATTERNS = List.of(
            Pattern.compile("(?i)(union(\\s+all)?\\s+select)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)('\\s*or\\s*['\"\\d]+\\s*=\\s*['\"\\d]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(drop\\s+table|alter\\s+table|delete\\s+from|insert\\s+into|update\\s+\\w+\\s+set)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(exec(\\s|\\+)+(s|x)p\\w+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(benchmark\\s*\\(|sleep\\s*\\()", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(--|;|/\\*|\\*/)", Pattern.CASE_INSENSITIVE)
    );

    private static final List<Pattern> XSS_PATTERNS = List.of(
            Pattern.compile("(?i)<script.*?>.*?</script.*?>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(onerror|onload|onclick|onmouseover|eval)\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)<img.*?src=.*?onerror=.*?>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)<iframe.*?>", Pattern.CASE_INSENSITIVE)
    );

    private static final List<Pattern> PATH_TRAVERSAL_PATTERNS = List.of(
            Pattern.compile("(\\.\\./|\\.\\.\\\\|%2e%2e%2f|%2e%2e/)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(/etc/passwd|/etc/shadow|c:\\\\windows\\\\system32|win\\.ini)", Pattern.CASE_INSENSITIVE)
    );

    public ThreatAnalysisResult analyze(ThreatAnalysisRequest req) {
        List<String> detected = new ArrayList<>();
        double score = 0.0;
        String threatType = "SAFE";

        StringBuilder fullInspectionText = new StringBuilder();
        if (req.getEndpoint() != null) fullInspectionText.append(" ").append(req.getEndpoint());
        if (req.getPayload() != null) fullInspectionText.append(" ").append(req.getPayload());
        if (req.getQueryParams() != null) {
            req.getQueryParams().forEach((k, v) -> fullInspectionText.append(" ").append(k).append("=").append(v));
        }

        String content = fullInspectionText.toString();

        // 1. SQL Injection Inspection
        int sqliMatches = 0;
        for (Pattern p : SQLI_PATTERNS) {
            if (p.matcher(content).find()) {
                sqliMatches++;
                detected.add("SQL Injection pattern: " + p.pattern());
            }
        }
        if (sqliMatches > 0) {
            score += Math.min(85.0 + (sqliMatches * 5), 100.0);
            threatType = "SQL_INJECTION";
        }

        // 2. XSS Inspection
        int xssMatches = 0;
        for (Pattern p : XSS_PATTERNS) {
            if (p.matcher(content).find()) {
                xssMatches++;
                detected.add("Cross-Site Scripting (XSS) pattern: " + p.pattern());
            }
        }
        if (xssMatches > 0 && score < 80.0) {
            score = Math.max(score, 75.0 + (xssMatches * 7));
            threatType = "XSS_ATTACK";
        }

        // 3. Path Traversal Inspection
        int pathMatches = 0;
        for (Pattern p : PATH_TRAVERSAL_PATTERNS) {
            if (p.matcher(content).find()) {
                pathMatches++;
                detected.add("Path Traversal / Local File Inclusion pattern: " + p.pattern());
            }
        }
        if (pathMatches > 0 && score < 85.0) {
            score = Math.max(score, 80.0 + (pathMatches * 6));
            threatType = "PATH_TRAVERSAL";
        }

        // 4. Header / Entropy Anomaly inspection
        if (req.getHeaders() != null) {
            String userAgent = req.getHeaders().getOrDefault("user-agent", "");
            if (userAgent.toLowerCase().contains("sqlmap") || userAgent.toLowerCase().contains("nikto") || userAgent.toLowerCase().contains("nmap")) {
                score = Math.max(score, 90.0);
                threatType = "SECURITY_SCANNER_BOT";
                detected.add("Malicious Security Scanner User-Agent: " + userAgent);
            }
        }

        score = Math.min(score, 100.0);
        boolean malicious = score >= 50.0;
        String action = "ALLOW";
        if (score >= 80.0) {
            action = "BLOCK";
        } else if (score >= 50.0) {
            action = "FLAG";
        } else if (score > 20.0) {
            action = "THROTTLE";
        }

        String explanation;
        if (malicious) {
            explanation = "AI security engine detected high-probability " + threatType + " threat score (" + String.format("%.1f", score) + "/100). Recommended Action: " + action;
        } else {
            explanation = "Request payload verified clean. Anomaly heuristic score is nominal (" + String.format("%.1f", score) + "/100).";
        }

        return new ThreatAnalysisResult(malicious, score, threatType, action, detected, explanation);
    }
}
