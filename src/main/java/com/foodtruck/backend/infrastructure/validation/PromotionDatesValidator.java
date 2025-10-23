package com.foodtruck.backend.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.foodtruck.backend.domain.types.PromotionType;

public class PromotionDatesValidator implements ConstraintValidator<ValidPromotionDates, PromotionDatesValidation> {

    @Override
    public boolean isValid(PromotionDatesValidation request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        // Para UpdatePromotionRequest, el tipo puede ser null, validamos solo si está
        // presente
        PromotionType type = request.type();
        if (type == null) {
            return validateUpdateRequest(request, context);
        }

        return validateByType(request, context, type);
    }

    /**
     * Validación específica para UpdatePromotionRequest (tipo puede ser null)
     */
    private boolean validateUpdateRequest(PromotionDatesValidation request, ConstraintValidatorContext context) {
        boolean isValid = true;

        // Solo validar coherencia de fechas si ambas están presentes
        if (request.startsAt() != null && request.endsAt() != null) {
            if (request.startsAt().isAfter(request.endsAt())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "La fecha de inicio debe ser anterior a la fecha de fin")
                        .addPropertyNode("endsAt")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }

    /**
     * Validación completa cuando el tipo está presente (CreatePromotionRequest)
     */
    private boolean validateByType(PromotionDatesValidation request, ConstraintValidatorContext context,
            PromotionType type) {
        boolean isValid = true;

        // VALIDACIONES PARA PROMOCIONES TEMPORALES
        if (type == PromotionType.TEMPORARY) {
            isValid &= validateTemporaryPromotion(request, context);
        }

        // VALIDACIONES PARA PROMOCIONES RECURRENTES
        if (type == PromotionType.RECURRING) {
            isValid &= validateRecurringPromotion(request, context);
        }

        return isValid;
    }

    private boolean validateTemporaryPromotion(PromotionDatesValidation request, ConstraintValidatorContext context) {
        boolean isValid = true;

        // No debe tener reglas semanales
        if (request.weeklyRules() != null && !request.weeklyRules().isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Las promociones temporales no deben tener reglas semanales")
                    .addPropertyNode("weeklyRules")
                    .addConstraintViolation();
            isValid = false;
        }

        // Debe tener fecha de inicio obligatoriamente
        if (request.startsAt() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Las promociones temporales deben tener fecha de inicio")
                    .addPropertyNode("startsAt")
                    .addConstraintViolation();
            isValid = false;
        }

        // Debe tener fecha de fin obligatoriamente
        if (request.endsAt() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Las promociones temporales deben tener fecha de fin")
                    .addPropertyNode("endsAt")
                    .addConstraintViolation();
            isValid = false;
        }

        // Si tiene ambas fechas, validar que startsAt sea antes que endsAt
        if (request.startsAt() != null && request.endsAt() != null) {
            if (request.startsAt().isAfter(request.endsAt())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "La fecha de inicio debe ser anterior a la fecha de fin")
                        .addPropertyNode("endsAt")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean validateRecurringPromotion(PromotionDatesValidation request, ConstraintValidatorContext context) {
        boolean isValid = true;

        // No debe tener fechas
        if (request.startsAt() != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Las promociones recurrentes no deben tener fecha de inicio")
                    .addPropertyNode("startsAt")
                    .addConstraintViolation();
            isValid = false;
        }

        if (request.endsAt() != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Las promociones recurrentes no deben tener fecha de fin")
                    .addPropertyNode("endsAt")
                    .addConstraintViolation();
            isValid = false;
        }

        // Debe tener al menos una regla semanal
        if (request.weeklyRules() == null || request.weeklyRules().isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Las promociones recurrentes deben tener al menos una regla semanal")
                    .addPropertyNode("weeklyRules")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}