package com.foodtruck.backend.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ImageValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImage {
    String message() default "El archivo debe ser una imagen válida";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean required() default false;

    long maxSize() default 5 * 1024 * 1024; // 5MB por defecto

    String[] allowedTypes() default { "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp" };
}