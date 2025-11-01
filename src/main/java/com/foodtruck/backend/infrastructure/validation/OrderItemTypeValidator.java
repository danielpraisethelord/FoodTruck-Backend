package com.foodtruck.backend.infrastructure.validation;

import com.foodtruck.backend.application.dto.OrderDtos.CreateOrderItemRequest;
import com.foodtruck.backend.domain.types.OrderItemType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OrderItemTypeValidator implements ConstraintValidator<ValidOrderItemType, CreateOrderItemRequest> {

    @Override
    public boolean isValid(CreateOrderItemRequest value, ConstraintValidatorContext context) {
        if (value == null)
            return true;

        boolean valid = true;
        String message = null;

        if (value.type() == OrderItemType.PRODUCT) {
            if (value.productId() == null) {
                valid = false;
                message = "El campo productId es obligatorio cuando el tipo es PRODUCT.";
            } else if (value.promotionId() != null) {
                valid = false;
                message = "El campo promotionId debe ser nulo cuando el tipo es PRODUCT.";
            }
        } else if (value.type() == OrderItemType.PROMOTION) {
            if (value.promotionId() == null) {
                valid = false;
                message = "El campo promotionId es obligatorio cuando el tipo es PROMOTION.";
            } else if (value.productId() != null) {
                valid = false;
                message = "El campo productId debe ser nulo cuando el tipo es PROMOTION.";
            }
        }

        if (!valid && message != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }

        return valid;
    }
}