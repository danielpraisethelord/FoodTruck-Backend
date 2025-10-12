package com.foodtruck.backend.presentation.exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> validationErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> errorResponse = Map.of(
                "error", "Datos de entrada inválidos",
                "validationErrors", validationErrors,
                "timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "El usuario o email ya existe";

        String causeMessage = ex.getMostSpecificCause().getMessage().toLowerCase();
        if (causeMessage.contains("username")) {
            message = "El nombre de usuario ya está en uso";
        } else if (causeMessage.contains("email")) {
            message = "El email ya está registrado";
        }

        Map<String, Object> errorResponse = Map.of(
                "error", message,
                "timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler({ UsernameNotFoundException.class, BadCredentialsException.class })
    public ResponseEntity<Map<String, Object>> handleAuthErrors(RuntimeException ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", "Credenciales inválidas",
                "message", "El nombre de usuario o contraseña son incorrectos",
                "timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabledAccount(DisabledException ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", "Cuenta deshabilitada",
                "message", "Su cuenta ha sido deshabilitada. Contacte al administrador",
                "timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> handleLockedAccount(LockedException ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", "Cuenta bloqueada",
                "message", "Su cuenta ha sido bloqueada temporalmente",
                "timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.LOCKED).body(errorResponse);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtErrors(JwtException ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", "Token inválido",
                "message", "El token JWT es inválido o ha expirado",
                "timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", "Error interno del servidor",
                "message", "Ha ocurrido un error inesperado",
                "timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(FileExceptions.EmptyFileException.class)
    public ResponseEntity<Map<String, Object>> handleEmptyFile(FileExceptions.EmptyFileException ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", "Archivo vacío",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(FileExceptions.InvalidFileFormatException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFileFormat(FileExceptions.InvalidFileFormatException ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", "Formato de archivo inválido",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(FileExceptions.FileSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleFileSizeExceeded(FileExceptions.FileSizeExceededException ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", "Archivo demasiado grande",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    @ExceptionHandler(FileExceptions.UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(FileExceptions.UserNotFoundException ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", "Usuario no encontrado",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", "Archivo demasiado grande",
                "message", "El archivo excede el tamaño máximo permitido de 5MB",
                "timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(IOException ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", "Error procesando archivo",
                "message", "No se pudo procesar el archivo. Intente nuevamente",
                "timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(UserExceptions.InvalidCurrentPasswordException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCurrentPassword(
            UserExceptions.InvalidCurrentPasswordException ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", "Contraseña actual incorrecta",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(UserExceptions.PasswordMismatchException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordMismatch(UserExceptions.PasswordMismatchException ex) {
        Map<String, Object> errorResponse = Map.of(
                "error", "Contraseñas no coinciden",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}