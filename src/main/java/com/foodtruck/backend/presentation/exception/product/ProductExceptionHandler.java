package com.foodtruck.backend.presentation.exception.product;

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
public class ProductExceptionHandler extends BaseExceptionHandler {

        @ExceptionHandler(ProductExceptions.ProductNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleProductNotFound(
                        ProductExceptions.ProductNotFoundException ex) {
                return createErrorResponse(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", ex.getMessage());
        }

        @ExceptionHandler(ProductExceptions.ProductAlreadyExistsException.class)
        public ResponseEntity<Map<String, Object>> handleProductAlreadyExists(
                        ProductExceptions.ProductAlreadyExistsException ex) {
                return createErrorResponse(HttpStatus.CONFLICT, "PRODUCT_ALREADY_EXISTS", ex.getMessage());
        }

        @ExceptionHandler(ProductExceptions.InvalidProductPriceException.class)
        public ResponseEntity<Map<String, Object>> handleInvalidProductPrice(
                        ProductExceptions.InvalidProductPriceException ex) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_PRODUCT_PRICE", ex.getMessage());
        }

        @ExceptionHandler(ProductExceptions.ProductNameTooLongException.class)
        public ResponseEntity<Map<String, Object>> handleProductNameTooLong(
                        ProductExceptions.ProductNameTooLongException ex) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "PRODUCT_NAME_TOO_LONG", ex.getMessage());
        }

        @ExceptionHandler(ProductExceptions.EmptyProductNameException.class)
        public ResponseEntity<Map<String, Object>> handleEmptyProductName(
                        ProductExceptions.EmptyProductNameException ex) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "EMPTY_PRODUCT_NAME", ex.getMessage());
        }

        @ExceptionHandler(ProductExceptions.InvalidProductImageException.class)
        public ResponseEntity<Map<String, Object>> handleInvalidProductImage(
                        ProductExceptions.InvalidProductImageException ex) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_PRODUCT_IMAGE", ex.getMessage());
        }

        @ExceptionHandler(ProductExceptions.ProductImageTooLargeException.class)
        public ResponseEntity<Map<String, Object>> handleProductImageTooLarge(
                        ProductExceptions.ProductImageTooLargeException ex) {
                return createErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, "PRODUCT_IMAGE_TOO_LARGE", ex.getMessage());
        }

        @ExceptionHandler(ProductExceptions.ProductImageProcessingException.class)
        public ResponseEntity<Map<String, Object>> handleProductImageProcessing(
                        ProductExceptions.ProductImageProcessingException ex) {
                return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "PRODUCT_IMAGE_PROCESSING_ERROR",
                                ex.getMessage());
        }

        @ExceptionHandler(ProductExceptions.CategoryNotFoundForProductException.class)
        public ResponseEntity<Map<String, Object>> handleCategoryNotFoundForProduct(
                        ProductExceptions.CategoryNotFoundForProductException ex) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "CATEGORY_NOT_FOUND_FOR_PRODUCT", ex.getMessage());
        }
}