package com.foodtruck.backend.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.foodtruck.backend.application.dto.PromotionDtos.*;
import com.foodtruck.backend.application.mapper.PromotionMapper;
import com.foodtruck.backend.application.validator.PromotionValidator;
import com.foodtruck.backend.domain.model.Product;
import com.foodtruck.backend.domain.model.Promotion;
import com.foodtruck.backend.domain.model.PromotionWeeklyRule;
import com.foodtruck.backend.domain.repository.PromotionRepository;
import com.foodtruck.backend.domain.repository.PromotionWeeklyRuleRepository;
import com.foodtruck.backend.domain.types.PromotionType;
import com.foodtruck.backend.presentation.exception.promotion.PromotionExceptions.*;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de negocio para la gestión completa de promociones del sistema.
 * Se enfoca únicamente en la orquestación de la lógica de negocio,
 * delegando validaciones y transformaciones a componentes especializados.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionWeeklyRuleRepository weeklyRuleRepository;
    private final FileStorageService fileStorageService;
    private final PromotionValidator validator;
    private final PromotionMapper mapper;

    /**
     * Crea una nueva promoción sin imagen.
     * Orquesta la validación, construcción y persistencia de la promoción.
     */
    public PromotionDetailResponse createPromotion(CreatePromotionRequest request) {
        // Validar productos y reglas de negocio
        Set<Product> products = validator.validateAndGetProducts(request.productIds());
        validator.validatePromotionByType(request);

        // Construir y persistir la promoción
        Promotion promotion = mapper.toPromotion(request, products);
        Promotion savedPromotion = promotionRepository.save(promotion);

        return mapper.toPromotionDetailResponse(savedPromotion);
    }

    /**
     * Actualiza una promoción existente.
     * Aplica solo los cambios especificados manteniendo la integridad del negocio.
     */
    public PromotionDetailResponse updatePromotion(Long promotionId, UpdatePromotionRequest request) {
        Promotion promotion = findPromotionById(promotionId);

        updateBasicFields(promotion, request);
        updateDatesIfPresent(promotion, request);
        updateProductsIfPresent(promotion, request);
        updateWeeklyRulesIfPresent(promotion, request);

        Promotion savedPromotion = promotionRepository.save(promotion);
        return mapper.toPromotionDetailResponse(savedPromotion);
    }

    /**
     * Sube o actualiza la imagen de una promoción.
     * Gestiona el ciclo de vida completo de las imágenes (eliminación y creación).
     */
    public PromotionDetailResponse uploadPromotionImage(Long promotionId, MultipartFile imageFile) {
        Promotion promotion = findPromotionById(promotionId);

        try {
            // Eliminar imagen anterior si existe
            if (promotion.getImageUrl() != null) {
                fileStorageService.deleteFile(promotion.getImageUrl());
            }

            // Subir nueva imagen
            String imageUrl = fileStorageService.savePromotionImage(imageFile, promotion.getName());
            promotion.setImageUrl(imageUrl);

            Promotion savedPromotion = promotionRepository.save(promotion);
            return mapper.toPromotionDetailResponse(savedPromotion);

        } catch (Exception e) {
            throw new PromotionImageUploadException("Error al subir la imagen de la promoción", e);
        }
    }

    /**
     * Obtiene una promoción por ID con todos sus detalles.
     */
    @Transactional(readOnly = true)
    public PromotionDetailResponse getPromotionById(Long promotionId) {
        Promotion promotion = promotionRepository.findByIdWithAllRelations(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(promotionId));
        return mapper.toPromotionDetailResponse(promotion);
    }

    /**
     * Obtiene todas las promociones activas del sistema.
     */
    @Transactional(readOnly = true)
    public List<PromotionSummaryResponse> getActivePromotions() {
        List<Promotion> promotions = promotionRepository.findByIsActive(true);
        return promotions.stream()
                .map(mapper::toPromotionSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene promociones con paginación.
     */
    @Transactional(readOnly = true)
    public Page<PromotionSummaryResponse> getPromotions(Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findAllByOrderByIdDesc(pageable);
        return promotions.map(mapper::toPromotionSummaryResponse);
    }

    /**
     * Busca promociones activas por nombre.
     */
    @Transactional(readOnly = true)
    public List<PromotionSummaryResponse> searchPromotionsByName(String name) {
        List<Promotion> promotions = promotionRepository.findByNameContainingIgnoreCaseAndIsActive(name, true);
        return promotions.stream()
                .map(mapper::toPromotionSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene promociones válidas en la fecha actual.
     */
    @Transactional(readOnly = true)
    public List<PromotionSummaryResponse> getCurrentlyValidPromotions() {
        List<Promotion> promotions = promotionRepository.findCurrentlyValidPromotions(LocalDate.now());
        return promotions.stream()
                .map(mapper::toPromotionSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene promociones activas por tipo.
     */
    @Transactional(readOnly = true)
    public List<PromotionSummaryResponse> getPromotionsByType(PromotionType type) {
        List<Promotion> promotions = promotionRepository.findByTypeAndIsActive(type, true);
        return promotions.stream()
                .map(mapper::toPromotionSummaryResponse)
                .collect(Collectors.toList());
    }

    // Modificar togglePromotionStatus para validar expiración
    public PromotionDetailResponse togglePromotionStatus(Long promotionId) {
        Promotion promotion = findPromotionById(promotionId);

        // Si es temporal y está expirada, no permitir activación
        if (promotion.getType() == PromotionType.TEMPORARY &&
                promotion.getEndsAt() != null &&
                promotion.getEndsAt().isBefore(LocalDate.now())) {

            if (promotion.isActive()) {
                // Auto-desactivar si está activa pero expirada
                promotion.setActive(false);
            } else {
                throw new PromotionExpiredException(promotion.getId());
            }
        } else {
            boolean newActiveStatus = !promotion.isActive();
            if (newActiveStatus) {
                validator.validatePromotionCanBeActivated(promotion);
            }
            promotion.setActive(newActiveStatus);
        }

        Promotion savedPromotion = promotionRepository.save(promotion);
        return mapper.toPromotionDetailResponse(savedPromotion);
    }

    /**
     * Elimina una promoción del sistema.
     */
    public void deletePromotion(Long promotionId) {
        Promotion promotion = findPromotionById(promotionId);

        validator.validatePromotionCanBeDeleted(promotion);

        // Eliminar imagen si existe
        if (promotion.getImageUrl() != null) {
            fileStorageService.deleteFile(promotion.getImageUrl());
        }

        promotionRepository.delete(promotion);
    }

    /**
     * Obtiene promociones que inician en una fecha específica.
     */
    @Transactional(readOnly = true)
    public List<PromotionSummaryResponse> getPromotionsByStartDate(LocalDate startDate) {
        List<Promotion> promotions = promotionRepository.findByStartsAt(startDate);
        return promotions.stream()
                .map(mapper::toPromotionSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene promociones que terminan en una fecha específica.
     */
    @Transactional(readOnly = true)
    public List<PromotionSummaryResponse> getPromotionsByEndDate(LocalDate endDate) {
        List<Promotion> promotions = promotionRepository.findByEndsAt(endDate);
        return promotions.stream()
                .map(mapper::toPromotionSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene promociones temporales en un rango de fechas.
     */
    @Transactional(readOnly = true)
    public List<PromotionSummaryResponse> getTemporaryPromotionsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Promotion> promotions = promotionRepository.findTemporaryPromotionsByDateRange(startDate, endDate);
        return promotions.stream()
                .map(mapper::toPromotionSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene promociones temporales expiradas.
     */
    @Transactional(readOnly = true)
    public List<PromotionSummaryResponse> getExpiredPromotions() {
        List<Promotion> promotions = promotionRepository.findExpiredTemporaryPromotions(LocalDate.now());
        return promotions.stream()
                .map(mapper::toPromotionSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene promociones que expiran pronto.
     */
    @Transactional(readOnly = true)
    public List<PromotionSummaryResponse> getPromotionsExpiringBetween(LocalDate startDate, LocalDate endDate) {
        List<Promotion> promotions = promotionRepository.findPromotionsExpiringBetween(startDate, endDate);
        return promotions.stream()
                .map(mapper::toPromotionSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene promociones activas por producto específico.
     */
    @Transactional(readOnly = true)
    public List<PromotionSummaryResponse> getActivePromotionsByProduct(Long productId) {
        List<Promotion> promotions = promotionRepository.findActivePromotionsByProductId(productId);
        return promotions.stream()
                .map(mapper::toPromotionSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene promociones activas por múltiples productos.
     */
    @Transactional(readOnly = true)
    public List<PromotionSummaryResponse> getActivePromotionsByProducts(List<Long> productIds) {
        List<Promotion> promotions = promotionRepository.findActivePromotionsByProductIds(productIds);
        return promotions.stream()
                .map(mapper::toPromotionSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene promociones activas ordenadas por precio ascendente.
     */
    @Transactional(readOnly = true)
    public List<PromotionSummaryResponse> getActivePromotionsByPriceAsc() {
        List<Promotion> promotions = promotionRepository.findByIsActiveOrderByPriceAsc(true);
        return promotions.stream()
                .map(mapper::toPromotionSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene promociones activas ordenadas por precio descendente.
     */
    @Transactional(readOnly = true)
    public List<PromotionSummaryResponse> getActivePromotionsByPriceDesc() {
        List<Promotion> promotions = promotionRepository.findByIsActiveOrderByPriceDesc(true);
        return promotions.stream()
                .map(mapper::toPromotionSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cuenta promociones por tipo.
     */
    @Transactional(readOnly = true)
    public Long getPromotionsCountByType(PromotionType type) {
        return promotionRepository.countByType(type);
    }

    /**
     * Cuenta promociones activas por tipo.
     */
    @Transactional(readOnly = true)
    public Long getActivePromotionsCountByType(PromotionType type) {
        return promotionRepository.countByTypeAndIsActive(type, true);
    }

    // ========== MÉTODOS PRIVADOS DE ORQUESTACIÓN ==========

    private Promotion findPromotionById(Long promotionId) {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(promotionId));
    }

    private void updateBasicFields(Promotion promotion, UpdatePromotionRequest request) {
        if (request.name() != null) {
            validator.validateUniqueName(request.name(), promotion.getId());
            promotion.setName(request.name());
        }

        if (request.description() != null) {
            promotion.setDescription(request.description());
        }

        if (request.price() != null) {
            promotion.setPrice(request.price());
        }

        if (request.active() != null) {
            if (request.active() && !promotion.isActive()) {
                validator.validatePromotionCanBeActivated(promotion);
            }
            promotion.setActive(request.active());
        }
    }

    private void updateDatesIfPresent(Promotion promotion, UpdatePromotionRequest request) {
        if (request.startsAt() != null || request.endsAt() != null) {
            validator.validatePromotionDatesUpdate(promotion, request);

            if (request.startsAt() != null) {
                promotion.setStartsAt(request.startsAt());
            }
            if (request.endsAt() != null) {
                promotion.setEndsAt(request.endsAt());
            }
        }
    }

    private void updateProductsIfPresent(Promotion promotion, UpdatePromotionRequest request) {
        if (request.productIds() != null) {
            Set<Product> products = validator.validateAndGetProducts(request.productIds());
            promotion.setProducts(products);
        }
    }

    private void updateWeeklyRulesIfPresent(Promotion promotion, UpdatePromotionRequest request) {
        if (request.weeklyRules() != null) {
            validator.validateWeeklyRulesUpdate(promotion, request.weeklyRules());

            // Eliminar reglas existentes
            weeklyRuleRepository.deleteByPromotionId(promotion.getId());
            promotion.getWeeklyRules().clear();

            // Crear nuevas reglas
            Set<PromotionWeeklyRule> weeklyRules = request.weeklyRules().stream()
                    .map(rule -> mapper.toWeeklyRule(promotion, rule))
                    .collect(Collectors.toSet());

            promotion.setWeeklyRules(weeklyRules);
        }
    }
}