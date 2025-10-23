package com.foodtruck.backend.infrastructure.validation;

import java.time.LocalDate;
import java.util.Set;
import com.foodtruck.backend.application.dto.PromotionDtos.CreateWeeklyRuleRequest;
import com.foodtruck.backend.domain.types.PromotionType;

/**
 * Interfaz para objetos que pueden ser validados por fechas de promoción
 */
public interface PromotionDatesValidation {
    PromotionType type();

    LocalDate startsAt();

    LocalDate endsAt();

    Set<CreateWeeklyRuleRequest> weeklyRules();
}