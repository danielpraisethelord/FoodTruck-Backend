package com.foodtruck.backend.application.validator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.foodtruck.backend.application.dto.PromotionDtos.*;
import com.foodtruck.backend.domain.model.Product;
import com.foodtruck.backend.domain.model.Promotion;
import com.foodtruck.backend.domain.repository.ProductRepository;
import com.foodtruck.backend.domain.repository.PromotionRepository;
import com.foodtruck.backend.domain.types.PromotionType;
import com.foodtruck.backend.presentation.exception.promotion.PromotionExceptions.*;

import lombok.RequiredArgsConstructor;

/**
 * Validador de reglas de negocio para promociones.
 * Centraliza toda la lógica de validación compleja del dominio.
 */
@Component
@RequiredArgsConstructor
public class PromotionValidator {

    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;

    /**
     * Valida que todos los productos especificados existan en el sistema.
     */
    public Set<Product> validateAndGetProducts(Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new PromotionMustHaveProductsException();
        }

        Set<Product> products = new HashSet<>(productRepository.findAllById(productIds));

        if (products.size() != productIds.size()) {
            Set<Long> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());
            Set<Long> missingIds = productIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());
            throw new ProductsNotFoundForPromotionException(
                    "Productos no encontrados: " + missingIds);
        }

        return products;
    }

    /**
     * Valida una promoción según su tipo específico.
     */
    public void validatePromotionByType(CreatePromotionRequest request) {
        if (request.type() == PromotionType.TEMPORARY) {
            validateTemporaryPromotion(request);
        } else if (request.type() == PromotionType.RECURRING) {
            validateRecurringPromotion(request);
        }
    }

    /**
     * Valida reglas específicas para promociones temporales.
     */
    public void validateTemporaryPromotion(CreatePromotionRequest request) {
        // Las fechas son opcionales, pero si están presentes deben ser válidas
        if (request.startsAt() != null && request.endsAt() != null) {
            if (request.startsAt().isAfter(request.endsAt())) {
                throw new InvalidTemporaryPromotionException(
                        "La fecha de inicio debe ser anterior a la fecha de fin");
            }
        }

        // Si tiene fecha de fin y ya pasó, no se puede crear
        if (request.endsAt() != null && request.endsAt().isBefore(LocalDate.now())) {
            throw new InvalidTemporaryPromotionException(
                    "No se puede crear una promoción con fecha de fin en el pasado");
        }
    }

    /**
     * Valida reglas específicas para promociones recurrentes.
     */
    public void validateRecurringPromotion(CreatePromotionRequest request) {
        if (request.weeklyRules() == null || request.weeklyRules().isEmpty()) {
            throw new InvalidRecurringPromotionException(
                    "Las promociones recurrentes deben tener al menos una regla semanal");
        }

        // Validar conflictos de horarios
        validateWeeklyRulesConflicts(request.weeklyRules());

        // Validar que las horas de inicio sean menores que las de fin
        for (CreateWeeklyRuleRequest rule : request.weeklyRules()) {
            if (rule.startTime().isAfter(rule.endTime()) || rule.startTime().equals(rule.endTime())) {
                throw new InvalidRecurringPromotionException(
                        "La hora de inicio debe ser anterior a la hora de fin en día " + rule.dayOfWeek());
            }
        }
    }

    /**
     * Valida que no haya conflictos de horarios entre reglas semanales.
     * Optimizado con ordenamiento para reducir la complejidad a O(n log n).
     */
    public void validateWeeklyRulesConflicts(Set<CreateWeeklyRuleRequest> rules) {
        rules.stream()
                .collect(Collectors.groupingBy(CreateWeeklyRuleRequest::dayOfWeek))
                .forEach(this::validateDayRulesConflicts);
    }

    /**
     * Valida conflictos usando fusión de intervalos - O(n log n).
     * Más eficiente para muchas reglas por día.
     */
    private void validateDayRulesConflicts(DayOfWeek day, List<CreateWeeklyRuleRequest> dayRules) {
        if (dayRules.size() <= 1) {
            return;
        }

        // Filtrar solo reglas con tiempos válidos (protección contra nulls)
        List<CreateWeeklyRuleRequest> validRules = dayRules.stream()
                .filter(rule -> rule.startTime() != null && rule.endTime() != null)
                .collect(Collectors.toList());

        if (validRules.size() <= 1) {
            return;
        }

        // Crear lista de intervalos ordenados
        List<TimeInterval> intervals = validRules.stream()
                .map(rule -> new TimeInterval(rule.startTime(), rule.endTime(), rule))
                .sorted(Comparator.comparing(TimeInterval::start))
                .collect(Collectors.toList());

        // Detectar solapamientos en una sola pasada
        for (int i = 0; i < intervals.size() - 1; i++) {
            TimeInterval current = intervals.get(i);
            TimeInterval next = intervals.get(i + 1);

            if (current.end().isAfter(next.start())) {
                throw new ConflictingWeeklyRulesException(
                        "Conflicto de horarios en " + day + ": " +
                                current.rule().startTime() + "-" + current.rule().endTime() + " vs " +
                                next.rule().startTime() + "-" + next.rule().endTime());
            }
        }
    }

    /**
     * Record auxiliar para representar intervalos de tiempo.
     */
    private record TimeInterval(
            LocalTime start,
            LocalTime end,
            CreateWeeklyRuleRequest rule) {
    }

    /**
     * Valida que no exista otra promoción activa con el mismo nombre.
     */
    public void validateUniqueName(String name, Long excludeId) {
        boolean exists = promotionRepository.findAll().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name) &&
                        p.isActive() &&
                        !p.getId().equals(excludeId));

        if (exists) {
            throw new InvalidTemporaryPromotionException(
                    "Ya existe una promoción activa con el nombre: " + name);
        }
    }

    /**
     * Valida que una promoción pueda ser activada.
     */
    public void validatePromotionCanBeActivated(Promotion promotion) {
        if (promotion.getType() == PromotionType.TEMPORARY) {
            if (promotion.getEndsAt() != null && promotion.getEndsAt().isBefore(LocalDate.now())) {
                throw new PromotionExpiredException(promotion.getId());
            }
        }
    }

    /**
     * Valida que las fechas de actualización sean coherentes.
     */
    public void validatePromotionDatesUpdate(Promotion promotion, UpdatePromotionRequest request) {
        if (request.startsAt() != null || request.endsAt() != null) {
            if (promotion.getType() != PromotionType.TEMPORARY) {
                throw new PromotionTypeMismatchException(
                        "Solo las promociones temporales pueden tener fechas");
            }

            LocalDate newStartsAt = request.startsAt() != null ? request.startsAt() : promotion.getStartsAt();
            LocalDate newEndsAt = request.endsAt() != null ? request.endsAt() : promotion.getEndsAt();

            if (newStartsAt != null && newEndsAt != null && newStartsAt.isAfter(newEndsAt)) {
                throw new InvalidPromotionDatesException(
                        "La fecha de inicio debe ser anterior a la fecha de fin");
            }
        }
    }

    /**
     * Valida que se puedan actualizar las reglas semanales.
     */
    public void validateWeeklyRulesUpdate(Promotion promotion, Set<CreateWeeklyRuleRequest> newRules) {
        if (promotion.getType() != PromotionType.RECURRING) {
            throw new PromotionTypeMismatchException(
                    "Solo las promociones recurrentes pueden tener reglas semanales");
        }

        // Validar las nuevas reglas
        validateWeeklyRulesConflicts(newRules);
    }

    /**
     * Valida que una promoción se pueda eliminar.
     */
    public void validatePromotionCanBeDeleted(Promotion promotion) {
        if (promotion.isActive()) {
            throw new PromotionCannotBeDeletedException("No se puede eliminar una promoción activa");
        }
    }

    // ========== MÉTODOS UTILITARIOS ==========

    /**
     * Verifica si dos rangos de tiempo se solapan.
     */
    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}