package com.apigateway.gateway.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AiAnomalyService {

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
            Pattern.compile("(?i)(onerror|onload|onclick|eval)\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)<iframe.*?>", Pattern.CASE_INSENSITIVE)
    );

    private static final List<Pattern> PATH_TRAVERSAL_PATTERNS = List.of(
            Pattern.compile("(\\.\\./|\\.\\.\\\\|%2e%2e%2f|%2e%2e/)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(/etc/passwd|/etc/shadow|c:\\\\windows\\\\system32|win\\.ini)", Pattern.CASE_INSENSITIVE)
    );

    public static class InspectionResult {
        private final boolean blocked;
        private final double score;
        private final String threatType;
        private final List<String> patterns;
        private final String details;

        public InspectionResult(boolean blocked, double score, String threatType, List<String> patterns, String details) {
            this.blocked = blocked;
            this.score = score;
            this.threatType = threatType;
            this.patterns = patterns;
            this.details = details;
        }

        public boolean isBlocked() { return blocked; }
        public double getScore() { return score; }
        public String getThreatType() { return threatType; }
        public List<String> getPatterns() { return patterns; }
        public String getDetails() { return details; }
    }

    public InspectionResult evaluateRequest(String path, String query, String userAgent) {
        String target = (path != null ? path : "") + " " + (query != null ? query : "");
        List<String> matched = new ArrayList<>();
        double score = 0.0;
        String threatType = "CLEAN";

        // SQLi check
        for (Pattern p : SQLI_PATTERNS) {
            if (p.matcher(target).find()) {
                score += 85.0;
                threatType = "SQL_INJECTION";
                matched.add("SQLi pattern matched: " + p.pattern());
                break;
            }
        }

        // XSS check
        if (score < 80.0) {
            for (Pattern p : XSS_PATTERNS) {
                if (p.matcher(target).find()) {
                    score = 80.0;
                    threatType = "XSS_ATTACK";
                    matched.add("XSS pattern matched: " + p.pattern());
                    break;
                }
            }
        }

        // Path Traversal check
        if (score < 80.0) {
            for (Pattern p : PATH_TRAVERSAL_PATTERNS) {
                if (p.matcher(target).find()) {
                    score = 88.0;
                    threatType = "PATH_TRAVERSAL";
                    matched.add("Path Traversal pattern matched: " + p.pattern());
                    break;
                }
            }
        }

        // Bot Scanner check
        if (userAgent != null) {
            String lowerUa = userAgent.toLowerCase();
            if (lowerUa.contains("sqlmap") || lowerUa.contains("nikto") || lowerUa.contains("acunetix")) {
                score = 95.0;
                threatType = "SECURITY_SCANNER_BOT";
                matched.add("Malicious User-Agent: " + userAgent);
            }
        }

        boolean shouldBlock = score >= 80.0;
        String details = shouldBlock
                ? "AI Threat Shield blocked suspicious payload (" + threatType + ", score: " + score + ")"
                : "Payload analyzed safe (score: " + score + ")";

        return new InspectionResult(shouldBlock, score, threatType, matched, details);
    }
}
