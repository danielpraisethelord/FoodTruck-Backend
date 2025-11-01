package com.foodtruck.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.foodtruck.backend.domain.types.OrderItemType;
import com.foodtruck.backend.domain.types.OrderStatus;
import com.foodtruck.backend.infrastructure.validation.ValidOrderItemType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Conjunto de DTOs relacionados con las órdenes")
public class OrderDtos {

        @Schema(description = """
                        DTO para crear o actualizar una orden.
                        Se utiliza tanto para la creación de una nueva orden como para la actualización completa de una orden existente.
                        El identificador de la orden a actualizar se debe enviar en la URL del endpoint (por ejemplo, PUT /orders/{orderId}).
                        """)
        public record CreateOrderRequest(
                        @Schema(description = "Propina para el servicio", example = "15.00") @DecimalMin(value = "0.00", message = "La propina no puede ser negativa") BigDecimal tip,

                        @Schema(description = "Items de la orden") @NotEmpty(message = "La orden debe tener al menos un item") @Valid List<CreateOrderItemRequest> items) {
        }

        @ValidOrderItemType
        @Schema(description = "DTO para crear un item de orden")
        public record CreateOrderItemRequest(
                        @Schema(description = "Tipo de item: PRODUCT o PROMOTION", example = "PRODUCT", allowableValues = {
                                        "PRODUCT",
                                        "PROMOTION" }) @NotNull(message = "El tipo de item es obligatorio") OrderItemType type,

                        @Schema(description = "ID del producto (requerido si type es PRODUCT)", example = "1") Long productId,

                        @Schema(description = "ID de la promoción (requerido si type es PROMOTION)", example = "1") Long promotionId,

                        @Schema(description = "Cantidad de items", example = "2") @NotNull(message = "La cantidad es obligatoria") @Min(value = 1, message = "La cantidad debe ser al menos 1") Integer quantity){
        }

        @Schema(description = "DTO para actualizar el estado de una orden")
        public record UpdateOrderStatusRequest(
                        @Schema(description = "Nuevo estado de la orden", example = "EN_PREPARACION", allowableValues = {
                                        "PENDIENTE", "EN_PREPARACION", "LISTO", "ENTREGADO", "CANCELADO"
                        }) @NotNull(message = "El estado es obligatorio") OrderStatus status){
        }

        @Schema(description = "DTO para actualizar el tiempo estimado de una orden")
        public record UpdateOrderEstimatedTimeRequest(
                        @Schema(description = "Tiempo estimado en formato MM:SS", example = "45:00") @NotNull(message = "El tiempo estimado es obligatorio") @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "El tiempo estimado debe estar en formato MM:SS") String estimatedTime) {
        }

        @Schema(description = "DTO para actualizar la propina de una orden")
        public record UpdateOrderTipRequest(
                        @Schema(description = "Nueva propina", example = "20.00") @NotNull(message = "La propina es obligatoria") @DecimalMin(value = "0.00", message = "La propina no puede ser negativa") BigDecimal tip) {
        }

        @Schema(description = "DTO con información básica de una orden")
        public record OrderSummaryResponse(
                        @Schema(description = "ID único de la orden", example = "1") Long id,

                        @Schema(description = "Estado actual de la orden", example = "PENDIENTE") OrderStatus status,

                        @Schema(description = "Subtotal de la orden", example = "150.00") BigDecimal subtotal,

                        @Schema(description = "Propina", example = "15.00") BigDecimal tip,

                        @Schema(description = "Total de la orden", example = "165.00") BigDecimal total,

                        @Schema(description = "Tiempo estimado de entrega", example = "30:00") String estimatedTime,

                        @Schema(description = "Fecha y hora de creación", example = "2024-10-28T10:30:00") LocalDateTime createdAt,

                        @Schema(description = "Cantidad total de items", example = "5") int totalItems) {
        }

        @Schema(description = "DTO con información completa de una orden")
        public record OrderDetailResponse(
                        @Schema(description = "ID único de la orden", example = "1") Long id,

                        @Schema(description = "Información del usuario") UserSummaryResponse user,

                        @Schema(description = "Estado actual de la orden", example = "EN_PREPARACION") OrderStatus status,

                        @Schema(description = "Subtotal de la orden", example = "150.00") BigDecimal subtotal,

                        @Schema(description = "Propina", example = "15.00") BigDecimal tip,

                        @Schema(description = "Total de la orden", example = "165.00") BigDecimal total,

                        @Schema(description = "Tiempo estimado de entrega", example = "30:00") String estimatedTime,

                        @Schema(description = "Fecha y hora de creación", example = "2024-10-28T10:30:00") LocalDateTime createdAt,

                        @Schema(description = "Fecha y hora de entrega", example = "2024-10-28T11:00:00") LocalDateTime deliveredAt,

                        @Schema(description = "Items de la orden") List<OrderItemResponse> items) {
        }

        @Schema(description = "DTO con información de un item de orden")
        public record OrderItemResponse(
                        @Schema(description = "ID único del item", example = "1") Long id,

                        @Schema(description = "Tipo de item", example = "PRODUCT") OrderItemType type,

                        @Schema(description = "Nombre del item", example = "Taco de Pastor") String itemName,

                        @Schema(description = "URL de la imagen del item") String imageUrl,

                        @Schema(description = "Cantidad", example = "2") Integer quantity,

                        @Schema(description = "Precio unitario", example = "25.00") BigDecimal unitPrice,

                        @Schema(description = "Total de la línea", example = "50.00") BigDecimal lineTotal) {
        }

        @Schema(description = "DTO con información básica del usuario")
        public record UserSummaryResponse(
                        @Schema(description = "ID único del usuario", example = "1") Long id,

                        @Schema(description = "Nombre completo del usuario", example = "Juan Pérez") String fullName,

                        @Schema(description = "Email del usuario", example = "juan.perez@example.com") String email) {
        }

        @Schema(description = "Respuesta cuando el usuario cancela una orden")
        public record OrderCancelledResponse(
                        @Schema(description = "ID único de la orden cancelada", example = "1") Long id,
                        @Schema(description = "Estado final de la orden", example = "CANCELADO") OrderStatus status,
                        @Schema(description = "Mensaje de confirmación", example = "La orden ha sido cancelada exitosamente") String message,
                        @Schema(description = "Fecha y hora de cancelación", example = "2024-10-28T11:15:00") LocalDateTime cancelledAt) {
        }

        @Schema(description = "DTO para filtrar órdenes por estado, rango de fechas y usuario")
        public record OrderFilterRequest(
                        @Schema(description = "Estado de la orden para filtrar", example = "PENDIENTE") OrderStatus status,

                        @Schema(description = "Fecha de inicio del rango", example = "2024-10-01T00:00:00") LocalDateTime startDate,

                        @Schema(description = "Fecha de fin del rango", example = "2024-10-31T23:59:59") LocalDateTime endDate,

                        @Schema(description = "ID del usuario para filtrar", example = "1") Long userId) {
        }

        @Schema(description = "DTO con estadísticas generales de órdenes")
        public record OrderStatisticsResponse(
                        @Schema(description = "Total de órdenes registradas", example = "150") long totalOrders,

                        @Schema(description = "Órdenes pendientes", example = "20") long pendingOrders,

                        @Schema(description = "Órdenes completadas", example = "110") long completedOrders,

                        @Schema(description = "Órdenes canceladas", example = "20") long cancelledOrders,

                        @Schema(description = "Ingresos totales", example = "25000.00") BigDecimal totalRevenue) {
        }

        @Schema(description = "DTO para productos o promociones más vendidos")
        public record TopSellingItemResponse(
                        @Schema(description = "ID del producto o promoción", example = "5") Long id,

                        @Schema(description = "Nombre del producto o promoción", example = "Taco de Pastor") String name,

                        @Schema(description = "Cantidad total vendida", example = "120") long totalSold,

                        @Schema(description = "Tipo de item: PRODUCT o PROMOTION", example = "PRODUCT") OrderItemType type) {
        }
}