package com.orderservice.dto;

import java.time.Instant;
import java.time.LocalDate;

public record SearchRequest(String status, Instant createdAt, LocalDate fromDate, LocalDate toDate) {
}
