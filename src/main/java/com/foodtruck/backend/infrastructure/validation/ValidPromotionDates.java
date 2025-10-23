package com.foodtruck.backend.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PromotionDatesValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPromotionDates {
    String message() default "Las fechas solo son válidas para promociones temporales";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}