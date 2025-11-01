package com.foodtruck.backend.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OrderItemTypeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidOrderItemType {
    String message() default "Configuración inválida para el tipo de item de orden";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}