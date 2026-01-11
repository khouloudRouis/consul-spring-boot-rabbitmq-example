package com.metricservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.metricservice.entity.Metric;

@Repository
public interface MetricsRepository extends JpaRepository<Metric, Long> {

}
