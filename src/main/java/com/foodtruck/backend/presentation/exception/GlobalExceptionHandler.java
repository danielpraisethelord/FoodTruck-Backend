package com.foodtruck.backend.presentation.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Order(10)
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
                Map<String, String> validationErrors = new HashMap<>();

                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));

                Map<String, Object> errorResponse = Map.of(
                                "error", "VALIDATION_ERROR",
                                "message", "Datos de entrada inválidos",
                                "validationErrors", validationErrors,
                                "timestamp", LocalDateTime.now().toString());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
                String message = "El usuario o email ya existe";
                String errorCode = "DATA_INTEGRITY_VIOLATION";

                String causeMessage = ex.getMostSpecificCause().getMessage().toLowerCase();
                if (causeMessage.contains("username")) {
                        message = "El nombre de usuario ya está en uso";
                        errorCode = "USERNAME_ALREADY_EXISTS";
                } else if (causeMessage.contains("email")) {
                        message = "El email ya está registrado";
                        errorCode = "EMAIL_ALREADY_EXISTS";
                }

                return createErrorResponse(HttpStatus.CONFLICT, errorCode, message);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "ILLEGAL_ARGUMENT", ex.getMessage());
        }

        @ExceptionHandler({ UsernameNotFoundException.class, BadCredentialsException.class })
        public ResponseEntity<Map<String, Object>> handleAuthErrors(RuntimeException ex) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                                "El nombre de usuario o contraseña son incorrectos");
        }

        @ExceptionHandler(DisabledException.class)
        public ResponseEntity<Map<String, Object>> handleDisabledAccount(DisabledException ex) {
                return createErrorResponse(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED",
                                "Su cuenta ha sido deshabilitada. Contacte al administrador");
        }

        @ExceptionHandler(LockedException.class)
        public ResponseEntity<Map<String, Object>> handleLockedAccount(LockedException ex) {
                return createErrorResponse(HttpStatus.LOCKED, "ACCOUNT_LOCKED",
                                "Su cuenta ha sido bloqueada temporalmente");
        }

        @ExceptionHandler(JwtException.class)
        public ResponseEntity<Map<String, Object>> handleJwtErrors(JwtException ex) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_JWT_TOKEN",
                                "El token JWT es inválido o ha expirado");
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
                return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                                "Ha ocurrido un error inesperado");
        }
}