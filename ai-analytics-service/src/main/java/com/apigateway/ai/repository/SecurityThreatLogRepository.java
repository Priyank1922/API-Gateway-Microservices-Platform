package com.apigateway.ai.repository;

import com.apigateway.ai.entity.SecurityThreatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityThreatLogRepository extends JpaRepository<SecurityThreatLog, Long> {
    List<SecurityThreatLog> findTop50ByOrderByCreatedAtDesc();
    List<SecurityThreatLog> findByClientIpOrderByCreatedAtDesc(String clientIp);
    List<SecurityThreatLog> findByThreatTypeOrderByCreatedAtDesc(String threatType);
    long countByActionTaken(String actionTaken);
}
