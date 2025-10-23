package com.foodtruck.backend.application.mapper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.foodtruck.backend.application.dto.PromotionDtos.*;
import com.foodtruck.backend.domain.model.Product;
import com.foodtruck.backend.domain.model.Promotion;
import com.foodtruck.backend.domain.model.PromotionWeeklyRule;

/**
 * Mapper para conversiones entre entidades de dominio y DTOs de promociones.
 * Centraliza todas las transformaciones de datos evitando duplicación de
 * código.
 */
@Component
public class PromotionMapper {

    /**
     * Convierte una entidad Promotion a DTO de respuesta detallada.
     * 
     * @param promotion Entidad a convertir
     * @return DTO con información completa de la promoción
     */
    public PromotionDetailResponse toPromotionDetailResponse(Promotion promotion) {
        return new PromotionDetailResponse(
                promotion.getId(),
                promotion.getName(),
                promotion.getDescription(),
                promotion.getImageUrl(),
                promotion.getPrice(),
                promotion.getType(),
                promotion.getStartsAt(),
                promotion.getEndsAt(),
                promotion.isActive(),
                isCurrentlyValid(promotion),
                toProductSummaryResponses(promotion.getProducts()),
                toWeeklyRuleResponses(promotion.getWeeklyRules()));
    }

    /**
     * Convierte una entidad Promotion a DTO de respuesta resumida.
     * 
     * @param promotion Entidad a convertir
     * @return DTO con información resumida de la promoción
     */
    public PromotionSummaryResponse toPromotionSummaryResponse(Promotion promotion) {
        return new PromotionSummaryResponse(
                promotion.getId(),
                promotion.getName(),
                promotion.getImageUrl(),
                promotion.getPrice(),
                promotion.getType(),
                promotion.isActive(),
                isCurrentlyValid(promotion));
    }

    /**
     * Convierte un set de productos a DTOs de respuesta resumida.
     * 
     * @param products Set de productos a convertir
     * @return Set de DTOs de productos
     */
    public Set<ProductSummaryResponse> toProductSummaryResponses(Set<Product> products) {
        return products.stream()
                .map(product -> new ProductSummaryResponse(
                        product.getId(),
                        product.getName(),
                        product.getImage(),
                        product.getPrice()))
                .collect(Collectors.toSet());
    }

    /**
     * Convierte un set de reglas semanales a DTOs de respuesta.
     * 
     * @param rules Set de reglas semanales a convertir
     * @return Set de DTOs de reglas semanales
     */
    public Set<WeeklyRuleResponse> toWeeklyRuleResponses(Set<PromotionWeeklyRule> rules) {
        LocalTime currentTime = LocalTime.now();
        DayOfWeek currentDay = LocalDate.now().getDayOfWeek();

        return rules.stream()
                .map(rule -> new WeeklyRuleResponse(
                        rule.getId(),
                        rule.getDayOfWeek(),
                        rule.getStartTime(),
                        rule.getEndTime(),
                        isRuleActiveNow(rule, currentDay, currentTime)))
                .collect(Collectors.toSet());
    }

    /**
     * Construye una entidad Promotion a partir de los datos de creación.
     * 
     * @param request  Datos de creación
     * @param products Productos validados
     * @return Nueva entidad Promotion
     */
    public Promotion toPromotion(CreatePromotionRequest request, Set<Product> products) {
        Promotion promotion = Promotion.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .type(request.type())
                .startsAt(request.startsAt())
                .endsAt(request.endsAt())
                .isActive(true)
                .products(products)
                .build();

        // Agregar reglas semanales si es recurrente
        if (request.weeklyRules() != null && !request.weeklyRules().isEmpty()) {
            Set<PromotionWeeklyRule> weeklyRules = request.weeklyRules().stream()
                    .map(rule -> toWeeklyRule(promotion, rule))
                    .collect(Collectors.toSet());
            promotion.setWeeklyRules(weeklyRules);
        }

        return promotion;
    }

    /**
     * Construye una regla semanal a partir de los datos de creación.
     * 
     * @param promotion Promoción padre
     * @param request   Datos de la regla
     * @return Nueva regla semanal
     */
    public PromotionWeeklyRule toWeeklyRule(Promotion promotion, CreateWeeklyRuleRequest request) {
        return PromotionWeeklyRule.builder()
                .promotion(promotion)
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();
    }

    // ========== MÉTODOS UTILITARIOS ==========

    /**
     * Determina si una promoción está actualmente válida.
     */
    private boolean isCurrentlyValid(Promotion promotion) {
        if (!promotion.isActive()) {
            return false;
        }

        if (promotion.getType() == com.foodtruck.backend.domain.types.PromotionType.RECURRING) {
            return true;
        }

        // Para temporales, verificar fechas
        LocalDate today = LocalDate.now();
        if (promotion.getStartsAt() != null && promotion.getStartsAt().isAfter(today)) {
            return false;
        }
        if (promotion.getEndsAt() != null && promotion.getEndsAt().isBefore(today)) {
            return false;
        }

        return true;
    }

    /**
     * Determina si una regla semanal está activa en este momento.
     */
    private boolean isRuleActiveNow(PromotionWeeklyRule rule, DayOfWeek currentDay, LocalTime currentTime) {
        return rule.getDayOfWeek() == currentDay &&
                !rule.getStartTime().isAfter(currentTime) &&
                !rule.getEndTime().isBefore(currentTime) &&
                rule.getPromotion().isActive();
    }
}