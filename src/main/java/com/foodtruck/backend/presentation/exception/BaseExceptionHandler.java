package com.foodtruck.backend.presentation.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseExceptionHandler {

    protected ResponseEntity<Map<String, Object>> createErrorResponse(
            HttpStatus status,
            String errorCode,
            String message) {

        Map<String, Object> errorResponse = Map.of(
                "error", errorCode,
                "message", message,
                "timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(status).body(errorResponse);
    }
}