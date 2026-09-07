package com.apigateway.ai.config;

import com.apigateway.ai.entity.SecurityThreatLog;
import com.apigateway.ai.entity.TrafficMetric;
import com.apigateway.ai.repository.SecurityThreatLogRepository;
import com.apigateway.ai.repository.TrafficMetricRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final SecurityThreatLogRepository threatRepo;
    private final TrafficMetricRepository trafficRepo;

    public DataInitializer(SecurityThreatLogRepository threatRepo, TrafficMetricRepository trafficRepo) {
        this.threatRepo = threatRepo;
        this.trafficRepo = trafficRepo;
    }

    @Override
    public void run(String... args) {
        try {
            if (threatRepo.count() == 0) {
                log.info("Seeding initial security threat logs into AI database...");

                List<SecurityThreatLog> sampleThreats = List.of(
                        new SecurityThreatLog("198.51.100.42", "/api/products?search=' UNION SELECT username,password FROM users--", "GET", "SQL_INJECTION", 95.0, "' UNION SELECT username,password FROM users--", "BLOCKED"),
                        new SecurityThreatLog("203.0.113.19", "/api/auth/register", "POST", "XSS_ATTACK", 85.0, "{\"username\":\"<script>alert(1)</script>\"}", "BLOCKED"),
                        new SecurityThreatLog("192.0.2.78", "/api/orders/../../etc/passwd", "GET", "PATH_TRAVERSAL", 90.0, "/../../etc/passwd", "BLOCKED"),
                        new SecurityThreatLog("198.51.100.99", "/api/products", "GET", "SECURITY_SCANNER_BOT", 88.0, "User-Agent: sqlmap/1.7.2#stable", "BLOCKED"),
                        new SecurityThreatLog("203.0.113.55", "/api/auth/login", "POST", "RATE_ABUSE", 65.0, "Rapid 120 req/sec burst detected", "THROTTLED")
                );

                threatRepo.saveAll(sampleThreats);
                log.info("Seeded {} sample security threats.", sampleThreats.size());
            }

            if (trafficRepo.count() == 0) {
                log.info("Seeding initial traffic metrics...");
                for (int i = 1; i <= 10; i++) {
                    TrafficMetric metric = new TrafficMetric(
                            "GLOBAL_GATEWAY",
                            12.0 + (i * 1.5),
                            18.0 + (Math.random() * 8.0),
                            0.2,
                            5.0 + (i * 0.8),
                            15.0 + (i * 1.6)
                    );
                    trafficRepo.save(metric);
                }
            }
        } catch (Exception e) {
            log.warn("AI database initialization notice: {}", e.getMessage());
        }
    }
}
