package com.foodtruck.backend.application.validator;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.foodtruck.backend.domain.model.Order;
import com.foodtruck.backend.domain.model.Product;
import com.foodtruck.backend.domain.model.Promotion;
import com.foodtruck.backend.domain.model.User;
import com.foodtruck.backend.domain.repository.ProductRepository;
import com.foodtruck.backend.domain.repository.PromotionRepository;
import com.foodtruck.backend.domain.types.OrderStatus;
import com.foodtruck.backend.presentation.exception.order.OrderExceptions.*;

import lombok.RequiredArgsConstructor;

/**
 * Validador de reglas de negocio para órdenes.
 * Centraliza toda la lógica de validación compleja del dominio.
 */
@Component
@RequiredArgsConstructor
public class OrderValidator {

    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;

    // Tiempo máximo permitido para modificar una orden después de estar
    // EN_PREPARACION (en minutos)
    private static final int MAX_MINUTES_TO_MODIFY_IN_PREPARATION = 5;

    /**
     * Valida que un producto exista y esté disponible.
     */
    public Product validateAndGetProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotAvailableException(productId));

        if (!product.getActive()) {
            throw new ProductNotAvailableException(
                    "El producto '" + product.getName() + "' no está disponible");
        }

        return product;
    }

    /**
     * Valida que una promoción exista y esté disponible.
     */
    public Promotion validateAndGetPromotion(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotAvailableException(promotionId));

        if (!promotion.isActive()) {
            throw new PromotionNotAvailableException(
                    "La promoción '" + promotion.getName() + "' no está activa");
        }

        // Validar si es temporal y está dentro del rango de fechas
        if (promotion.getType() == com.foodtruck.backend.domain.types.PromotionType.TEMPORARY) {
            validateTemporaryPromotionAvailability(promotion);
        }

        return promotion;
    }

    /**
     * Valida que una promoción temporal esté disponible en este momento.
     */
    private void validateTemporaryPromotionAvailability(Promotion promotion) {
        LocalDateTime now = LocalDateTime.now();

        if (promotion.getStartsAt() != null && now.toLocalDate().isBefore(promotion.getStartsAt())) {
            throw new PromotionNotAvailableException(
                    "La promoción '" + promotion.getName() + "' aún no ha iniciado");
        }

        if (promotion.getEndsAt() != null && now.toLocalDate().isAfter(promotion.getEndsAt())) {
            throw new PromotionNotAvailableException(
                    "La promoción '" + promotion.getName() + "' ya ha finalizado");
        }
    }

    /**
     * Valida que una orden pueda ser modificada según su estado.
     */
    public void validateOrderCanBeModified(Order order) {
        if (order.getStatus() == OrderStatus.CANCELADO) {
            throw new OrderAlreadyCancelledException(order.getId());
        }

        if (order.getStatus() == OrderStatus.ENTREGADO) {
            throw new OrderAlreadyDeliveredException(order.getId());
        }

        if (order.getStatus() == OrderStatus.LISTO) {
            throw new OrderCannotBeModifiedException(
                    "No se puede modificar una orden que ya está lista");
        }

        // Si está EN_PREPARACION, validar tiempo transcurrido
        if (order.getStatus() == OrderStatus.EN_PREPARACION) {
            validateModificationTimeWindow(order);
        }
    }

    /**
     * Valida que no haya pasado demasiado tiempo desde que la orden entró en
     * preparación.
     */
    private void validateModificationTimeWindow(Order order) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = order.getCreatedAt();

        Duration timeSinceCreation = Duration.between(createdAt, now);

        if (timeSinceCreation.toMinutes() > MAX_MINUTES_TO_MODIFY_IN_PREPARATION) {
            throw new OrderCannotBeModifiedException(
                    "No se puede modificar la orden después de " +
                            MAX_MINUTES_TO_MODIFY_IN_PREPARATION + " minutos de estar en preparación");
        }
    }

    /**
     * Valida que una orden pueda ser cancelada.
     */
    public void validateOrderCanBeCancelled(Order order) {
        if (order.getStatus() == OrderStatus.CANCELADO) {
            throw new OrderAlreadyCancelledException(order.getId());
        }

        if (order.getStatus() == OrderStatus.ENTREGADO) {
            throw new OrderAlreadyDeliveredException(order.getId());
        }

        if (order.getStatus() == OrderStatus.LISTO) {
            throw new OrderCannotBeModifiedException(
                    "No se puede cancelar una orden que ya está lista para entregar");
        }
    }

    /**
     * Valida que el cambio de estado de una orden sea válido.
     */
    public void validateOrderStatusTransition(Order order, OrderStatus newStatus) {
        OrderStatus currentStatus = order.getStatus();

        // No se puede cambiar el estado si ya está cancelada o entregada
        if (currentStatus == OrderStatus.CANCELADO) {
            throw new InvalidOrderStatusTransitionException(
                    "No se puede cambiar el estado de una orden cancelada");
        }

        if (currentStatus == OrderStatus.ENTREGADO) {
            throw new InvalidOrderStatusTransitionException(
                    "No se puede cambiar el estado de una orden ya entregada");
        }

        // Validar transiciones permitidas
        switch (currentStatus) {
            case PENDIENTE:
                if (newStatus != OrderStatus.EN_PREPARACION && newStatus != OrderStatus.CANCELADO) {
                    throw new InvalidOrderStatusTransitionException(
                            "Una orden PENDIENTE solo puede pasar a EN_PREPARACION o CANCELADO");
                }
                break;

            case EN_PREPARACION:
                if (newStatus != OrderStatus.LISTO && newStatus != OrderStatus.CANCELADO) {
                    throw new InvalidOrderStatusTransitionException(
                            "Una orden EN_PREPARACION solo puede pasar a LISTO o CANCELADO");
                }
                break;

            case LISTO:
                if (newStatus != OrderStatus.ENTREGADO) {
                    throw new InvalidOrderStatusTransitionException(
                            "Una orden LISTO solo puede pasar a ENTREGADO");
                }
                break;

            default:
                throw new InvalidOrderStatusTransitionException(
                        "Transición de estado no permitida: " + currentStatus + " -> " + newStatus);
        }
    }

    /**
     * Valida que un usuario tenga permiso para acceder a una orden.
     */
    public void validateUserOwnsOrder(Order order, User user) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw new OrderAccessDeniedException(order.getId());
        }
    }

    /**
     * Valida que el tiempo estimado tenga un formato válido.
     */
    public void validateEstimatedTime(String estimatedTime) {
        if (estimatedTime == null || estimatedTime.isBlank()) {
            throw new InvalidEstimatedTimeException("El tiempo estimado no puede estar vacío");
        }

        if (!estimatedTime.matches("^\\d{2}:\\d{2}$")) {
            throw new InvalidEstimatedTimeException(
                    "El tiempo estimado debe tener el formato MM:SS");
        }

        // Validar que los valores sean lógicos
        String[] parts = estimatedTime.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);

        if (seconds >= 60) {
            throw new InvalidEstimatedTimeException(
                    "Los segundos deben estar entre 00 y 59");
        }

        if (minutes < 0 || seconds < 0) {
            throw new InvalidEstimatedTimeException(
                    "El tiempo estimado no puede tener valores negativos");
        }
    }

    /**
     * Valida que una orden no esté vacía.
     */
    public void validateOrderNotEmpty(int itemCount) {
        if (itemCount == 0) {
            throw new EmptyOrderException();
        }
    }

    /**
     * Valida que un usuario no tenga demasiadas órdenes activas simultáneamente.
     * (Opcional, para evitar spam)
     */
    public void validateUserActiveOrdersLimit(User user, int activeOrdersCount) {
        final int MAX_ACTIVE_ORDERS = 3;

        if (activeOrdersCount >= MAX_ACTIVE_ORDERS) {
            throw new OrderCannotBeModifiedException(
                    "No puedes tener más de " + MAX_ACTIVE_ORDERS + " órdenes activas al mismo tiempo");
        }
    }
}