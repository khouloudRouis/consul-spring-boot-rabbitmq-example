package com.metricservice.mq.config.model;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(String label, Long orderId, BigDecimal amount, Long customerId, Instant createdAt) {
}
