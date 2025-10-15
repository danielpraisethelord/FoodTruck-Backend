package com.foodtruck.backend.presentation.exception.category;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.foodtruck.backend.presentation.exception.BaseExceptionHandler;

import io.swagger.v3.oas.annotations.Hidden;

@RestControllerAdvice
@Hidden
public class CategoryExceptionHandler extends BaseExceptionHandler {
        @ExceptionHandler(CategoryExceptions.CategoryNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleCategoryNotFound(
                        CategoryExceptions.CategoryNotFoundException ex) {
                return createErrorResponse(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", ex.getMessage());
        }

        @ExceptionHandler(CategoryExceptions.CategoryAlreadyExistsException.class)
        public ResponseEntity<Map<String, Object>> handleCategoryAlreadyExists(
                        CategoryExceptions.CategoryAlreadyExistsException ex) {
                return createErrorResponse(HttpStatus.CONFLICT, "CATEGORY_ALREADY_EXISTS", ex.getMessage());
        }

        @ExceptionHandler(CategoryExceptions.CategoryHasProductsException.class)
        public ResponseEntity<Map<String, Object>> handleCategoryHasProducts(
                        CategoryExceptions.CategoryHasProductsException ex) {
                return createErrorResponse(HttpStatus.CONFLICT, "CATEGORY_HAS_PRODUCTS", ex.getMessage());
        }
}
