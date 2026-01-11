package com.orderservice.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
    String message,
    int statusCode,
    String timestamp,
    String path
) {
    public ErrorResponse(String message, int statusCode, String path) {
        this(message, statusCode, LocalDateTime.now().toString(), path);
    }
}

