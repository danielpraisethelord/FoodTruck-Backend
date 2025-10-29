package com.foodtruck.backend.presentation.exception.order;

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
public class OrderExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(OrderExceptions.OrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOrderNotFound(
            OrderExceptions.OrderNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(OrderExceptions.EmptyOrderException.class)
    public ResponseEntity<Map<String, Object>> handleEmptyOrder(
            OrderExceptions.EmptyOrderException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "EMPTY_ORDER", ex.getMessage());
    }

    @ExceptionHandler(OrderExceptions.InvalidOrderItemException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOrderItem(
            OrderExceptions.InvalidOrderItemException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_ORDER_ITEM", ex.getMessage());
    }

    @ExceptionHandler(OrderExceptions.OrderCannotBeModifiedException.class)
    public ResponseEntity<Map<String, Object>> handleOrderCannotBeModified(
            OrderExceptions.OrderCannotBeModifiedException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "ORDER_CANNOT_BE_MODIFIED", ex.getMessage());
    }

    @ExceptionHandler(OrderExceptions.InvalidOrderStatusTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOrderStatusTransition(
            OrderExceptions.InvalidOrderStatusTransitionException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_ORDER_STATUS_TRANSITION", ex.getMessage());
    }

    @ExceptionHandler(OrderExceptions.OrderAlreadyCancelledException.class)
    public ResponseEntity<Map<String, Object>> handleOrderAlreadyCancelled(
            OrderExceptions.OrderAlreadyCancelledException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "ORDER_ALREADY_CANCELLED", ex.getMessage());
    }

    @ExceptionHandler(OrderExceptions.OrderAlreadyDeliveredException.class)
    public ResponseEntity<Map<String, Object>> handleOrderAlreadyDelivered(
            OrderExceptions.OrderAlreadyDeliveredException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "ORDER_ALREADY_DELIVERED", ex.getMessage());
    }

    @ExceptionHandler(OrderExceptions.ProductNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotAvailable(
            OrderExceptions.ProductNotAvailableException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "PRODUCT_NOT_AVAILABLE", ex.getMessage());
    }

    @ExceptionHandler(OrderExceptions.PromotionNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handlePromotionNotAvailable(
            OrderExceptions.PromotionNotAvailableException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "PROMOTION_NOT_AVAILABLE", ex.getMessage());
    }

    @ExceptionHandler(OrderExceptions.InvalidEstimatedTimeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidEstimatedTime(
            OrderExceptions.InvalidEstimatedTimeException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_ESTIMATED_TIME", ex.getMessage());
    }

    @ExceptionHandler(OrderExceptions.InvalidTipException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTip(
            OrderExceptions.InvalidTipException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_TIP", ex.getMessage());
    }

    @ExceptionHandler(OrderExceptions.OrderAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleOrderAccessDenied(
            OrderExceptions.OrderAccessDeniedException ex) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "ORDER_ACCESS_DENIED", ex.getMessage());
    }
}