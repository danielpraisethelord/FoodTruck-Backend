package com.foodtruck.backend.application.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.foodtruck.backend.application.dto.OrderDtos.*;
import com.foodtruck.backend.domain.model.Order;
import com.foodtruck.backend.domain.model.OrderItem;
import com.foodtruck.backend.domain.model.Product;
import com.foodtruck.backend.domain.model.Promotion;
import com.foodtruck.backend.domain.types.OrderItemType;

/**
 * Mapper para conversiones entre entidades de dominio y DTOs de órdenes.
 * Centraliza todas las transformaciones de datos evitando duplicación de
 * código.
 */
@Component
public class OrderMapper {

    /**
     * Convierte una entidad Order a DTO de respuesta detallada.
     */
    public OrderDetailResponse toOrderDetailResponse(Order order) {
        return new OrderDetailResponse(
                order.getId(),
                toUserSummaryResponse(order),
                order.getStatus(),
                order.getSubtotal(),
                order.getTip(),
                order.getTotal(),
                order.getEstimatedTime(),
                order.getCreatedAt(),
                order.getDeliveredAt(),
                toOrderItemResponses(order.getItems()));
    }

    /**
     * Convierte una entidad Order a DTO de respuesta resumida.
     */
    public OrderSummaryResponse toOrderSummaryResponse(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getStatus(),
                order.getSubtotal(),
                order.getTip(),
                order.getTotal(),
                order.getEstimatedTime(),
                order.getCreatedAt(),
                order.getItems().size());
    }

    /**
     * Convierte una lista de órdenes a DTOs de respuesta resumida.
     */
    public List<OrderSummaryResponse> toOrderSummaryResponses(List<Order> orders) {
        return orders.stream()
                .map(this::toOrderSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una página de órdenes a DTOs de respuesta resumida.
     */
    public Page<OrderSummaryResponse> toOrderSummaryResponsePage(Page<Order> orderPage) {
        return orderPage.map(this::toOrderSummaryResponse);
    }

    /**
     * Convierte una lista de items de orden a DTOs de respuesta.
     */
    public List<OrderItemResponse> toOrderItemResponses(List<OrderItem> items) {
        return items.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un item de orden a DTO de respuesta.
     */
    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        String itemName;
        String imageUrl;

        if (item.getType() == OrderItemType.PRODUCT) {
            Product product = item.getProduct();
            itemName = product.getName();
            imageUrl = product.getImage();
        } else {
            Promotion promotion = item.getPromotion();
            itemName = promotion.getName();
            imageUrl = promotion.getImageUrl();
        }

        return new OrderItemResponse(
                item.getId(),
                item.getType(),
                itemName,
                imageUrl,
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal());
    }

    /**
     * Convierte información del usuario de una orden a DTO resumido.
     */
    public UserSummaryResponse toUserSummaryResponse(Order order) {
        return new UserSummaryResponse(
                order.getUser().getId(),
                order.getUser().getName(),
                order.getUser().getEmail());
    }

    /**
     * Construye una entidad Order a partir de los datos de creación.
     */
    public Order toOrder(CreateOrderRequest request) {
        return Order.builder()
                .tip(request.tip() != null ? request.tip() : BigDecimal.ZERO)
                .status(com.foodtruck.backend.domain.types.OrderStatus.PENDIENTE)
                .estimatedTime("30:00")
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Construye un OrderItem a partir del request.
     */
    public OrderItem toOrderItem(CreateOrderItemRequest request, Order order, Product product, Promotion promotion) {
        BigDecimal unitPrice = product != null ? product.getPrice() : promotion.getPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(request.quantity()));

        return OrderItem.builder()
                .order(order)
                .type(request.type())
                .product(product)
                .promotion(promotion)
                .quantity(request.quantity())
                .unitPrice(unitPrice)
                .lineTotal(lineTotal)
                .itemName(product != null ? product.getName() : promotion.getName())
                .build();
    }

    /**
     * Convierte una orden cancelada a DTO de respuesta.
     */
    public OrderCancelledResponse toOrderCancelledResponse(Order order) {
        return new OrderCancelledResponse(
                order.getId(),
                order.getStatus(),
                "La orden ha sido cancelada exitosamente",
                order.getCanceledAt());
    }

    /**
     * Crea un DTO de estadísticas de órdenes.
     */
    public OrderStatisticsResponse toOrderStatisticsResponse(
            long totalOrders,
            long pendingOrders,
            long completedOrders,
            long cancelledOrders,
            BigDecimal totalRevenue) {

        return new OrderStatisticsResponse(
                totalOrders,
                pendingOrders,
                completedOrders,
                cancelledOrders,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
    }

    /**
     * Crea un DTO de item más vendido a partir de datos de consulta.
     */
    public TopSellingItemResponse toTopSellingItemResponse(Object[] data, OrderItemType type) {
        return new TopSellingItemResponse(
                ((Number) data[0]).longValue(), // id
                (String) data[1], // name
                ((Number) data[2]).longValue(), // totalSold
                type);
    }
}