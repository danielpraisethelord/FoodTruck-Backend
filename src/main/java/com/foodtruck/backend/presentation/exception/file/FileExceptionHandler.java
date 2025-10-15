package com.foodtruck.backend.presentation.exception.file;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.foodtruck.backend.presentation.exception.BaseExceptionHandler;

import io.swagger.v3.oas.annotations.Hidden;

import java.util.Map;

@RestControllerAdvice
@Hidden
public class FileExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(FileExceptions.EmptyFileException.class)
    public ResponseEntity<Map<String, Object>> handleEmptyFile(FileExceptions.EmptyFileException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "EMPTY_FILE", ex.getMessage());
    }

    @ExceptionHandler(FileExceptions.InvalidFileFormatException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFileFormat(
            FileExceptions.InvalidFileFormatException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_FILE_FORMAT", ex.getMessage());
    }

    @ExceptionHandler(FileExceptions.FileSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleFileSizeExceeded(FileExceptions.FileSizeExceededException ex) {
        return createErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_SIZE_EXCEEDED", ex.getMessage());
    }

    @ExceptionHandler(FileExceptions.UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(FileExceptions.UserNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return createErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE",
                "El archivo excede el tamaño máximo permitido de 5MB");
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(IOException ex) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_PROCESSING_ERROR",
                "No se pudo procesar el archivo. Intente nuevamente");
    }
}