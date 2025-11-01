package com.foodtruck.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.foodtruck.backend.domain.types.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para notificaciones de órdenes por WebSocket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notificación de orden enviada por WebSocket")
public class OrderNotificationDto {

  @Schema(description = "ID de la orden", example = "1")
  private Long orderId;

  @Schema(description = "ID del usuario dueño de la orden", example = "42")
  private Long userId;

  @Schema(description = "Nombre del usuario", example = "Juan Pérez")
  private String userName;

  @Schema(description = "Estado actual de la orden", example = "EN_PREPARACION")
  private OrderStatus status;

  @Schema(description = "Total de la orden", example = "250.50")
  private BigDecimal total;

  @Schema(description = "Tiempo estimado de preparación", example = "00:30")
  private String estimatedTime;

  @Schema(description = "Fecha de creación de la orden")
  private LocalDateTime createdAt;

  @Schema(description = "Fecha de última actualización")
  private LocalDateTime updatedAt;

  @Schema(description = "Tipo de notificación", example = "ORDER_STATUS_CHANGED")
  private NotificationType notificationType;

  @Schema(description = "Mensaje descriptivo de la notificación", example = "Tu orden #1 cambió a estado: EN_PREPARACION")
  private String message;

  @Schema(description = "Tipo de notificación WebSocket")
  public enum NotificationType {
    @Schema(description = "Orden creada por un usuario")
    ORDER_CREATED,

    @Schema(description = "Orden modificada por el usuario (productos, cantidades)")
    ORDER_UPDATED,

    @Schema(description = "Estado de la orden cambió")
    ORDER_STATUS_CHANGED,

    @Schema(description = "Tiempo estimado de la orden cambió")
    ORDER_ESTIMATED_TIME_CHANGED,

    @Schema(description = "Orden cancelada")
    ORDER_CANCELLED
  }
}