package com.apigateway.ai.repository;

import com.apigateway.ai.entity.TrafficMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrafficMetricRepository extends JpaRepository<TrafficMetric, Long> {
    List<TrafficMetric> findTop30ByOrderByRecordedAtDesc();
    List<TrafficMetric> findTop20ByServiceNameOrderByRecordedAtDesc(String serviceName);
}
