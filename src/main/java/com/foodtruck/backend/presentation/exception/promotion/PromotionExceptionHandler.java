package com.foodtruck.backend.presentation.exception.promotion;

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
public class PromotionExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(PromotionExceptions.PromotionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePromotionNotFound(
            PromotionExceptions.PromotionNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "PROMOTION_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(PromotionExceptions.InvalidTemporaryPromotionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTemporaryPromotion(
            PromotionExceptions.InvalidTemporaryPromotionException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_TEMPORARY_PROMOTION", ex.getMessage());
    }

    @ExceptionHandler(PromotionExceptions.InvalidRecurringPromotionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRecurringPromotion(
            PromotionExceptions.InvalidRecurringPromotionException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_RECURRING_PROMOTION", ex.getMessage());
    }

    @ExceptionHandler(PromotionExceptions.PromotionExpiredException.class)
    public ResponseEntity<Map<String, Object>> handlePromotionExpired(
            PromotionExceptions.PromotionExpiredException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "PROMOTION_EXPIRED", ex.getMessage());
    }

    @ExceptionHandler(PromotionExceptions.PromotionCannotBeActivatedException.class)
    public ResponseEntity<Map<String, Object>> handlePromotionCannotBeActivated(
            PromotionExceptions.PromotionCannotBeActivatedException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "PROMOTION_CANNOT_BE_ACTIVATED", ex.getMessage());
    }

    @ExceptionHandler(PromotionExceptions.ProductsNotFoundForPromotionException.class)
    public ResponseEntity<Map<String, Object>> handleProductsNotFoundForPromotion(
            PromotionExceptions.ProductsNotFoundForPromotionException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "PRODUCTS_NOT_FOUND_FOR_PROMOTION", ex.getMessage());
    }

    @ExceptionHandler(PromotionExceptions.PromotionMustHaveProductsException.class)
    public ResponseEntity<Map<String, Object>> handlePromotionMustHaveProducts(
            PromotionExceptions.PromotionMustHaveProductsException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "PROMOTION_MUST_HAVE_PRODUCTS", ex.getMessage());
    }

    @ExceptionHandler(PromotionExceptions.ConflictingWeeklyRulesException.class)
    public ResponseEntity<Map<String, Object>> handleConflictingWeeklyRules(
            PromotionExceptions.ConflictingWeeklyRulesException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "CONFLICTING_WEEKLY_RULES", ex.getMessage());
    }

    @ExceptionHandler(PromotionExceptions.InvalidPromotionDatesException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPromotionDates(
            PromotionExceptions.InvalidPromotionDatesException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_PROMOTION_DATES", ex.getMessage());
    }

    @ExceptionHandler(PromotionExceptions.PromotionImageUploadException.class)
    public ResponseEntity<Map<String, Object>> handlePromotionImageUpload(
            PromotionExceptions.PromotionImageUploadException ex) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "PROMOTION_IMAGE_UPLOAD_ERROR", ex.getMessage());
    }

    @ExceptionHandler(PromotionExceptions.PromotionTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handlePromotionTypeMismatch(
            PromotionExceptions.PromotionTypeMismatchException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "PROMOTION_TYPE_MISMATCH", ex.getMessage());
    }

    @ExceptionHandler(PromotionExceptions.PromotionCannotBeDeletedException.class)
    public ResponseEntity<Map<String, Object>> handlePromotionCannotBeDeleted(
            PromotionExceptions.PromotionCannotBeDeletedException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "PROMOTION_CANNOT_BE_DELETED", ex.getMessage());
    }
}