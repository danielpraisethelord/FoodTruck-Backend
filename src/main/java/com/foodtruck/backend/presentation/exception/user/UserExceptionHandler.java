package com.foodtruck.backend.presentation.exception.user;

import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.foodtruck.backend.presentation.exception.BaseExceptionHandler;

import io.swagger.v3.oas.annotations.Hidden;

@RestControllerAdvice
@Order(1)
@Hidden
public class UserExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(UserExceptions.InvalidCurrentPasswordException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCurrentPassword(
            UserExceptions.InvalidCurrentPasswordException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_CURRENT_PASSWORD", ex.getMessage());
    }

    @ExceptionHandler(UserExceptions.PasswordMismatchException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordMismatch(UserExceptions.PasswordMismatchException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", ex.getMessage());
    }
}