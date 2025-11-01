package com.foodtruck.backend.application.service;

import org.springframework.stereotype.Service;

import com.foodtruck.backend.application.dto.OrderDtos.OrderDetailResponse;
import com.foodtruck.backend.application.dto.OrderNotificationDto;
import com.foodtruck.backend.application.dto.OrderNotificationDto.NotificationType;
import com.foodtruck.backend.application.port.OrderNotificationPort;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación para gestionar notificaciones de órdenes.
 * Orquesta la creación de notificaciones y delega el envío al puerto de salida.
 */
@Service
@RequiredArgsConstructor
public class OrderNotificationService {

    private final OrderNotificationPort notificationPort;

    /**
     * Notifica a empleados que un usuario modificó su orden.
     */
    public void notifyOrderUpdatedByUser(OrderDetailResponse order) {
        OrderNotificationDto notification = OrderNotificationDto.builder()
                .orderId(order.id())
                .userId(order.user().id())
                .userName(order.user().fullName())
                .status(order.status())
                .total(order.total())
                .estimatedTime(order.estimatedTime())
                .createdAt(order.createdAt())
                // .updatedAt(order.updatedAt())
                .notificationType(NotificationType.ORDER_UPDATED)
                .message("El usuario " + order.user().fullName() + " modificó su orden #" + order.id())
                .build();

        notificationPort.notifyEmployees(notification);
    }

    /**
     * Notifica al usuario que el estado de su orden cambió.
     */
    public void notifyOrderStatusChanged(OrderDetailResponse order) {
        OrderNotificationDto notification = OrderNotificationDto.builder()
                .orderId(order.id())
                .userId(order.user().id())
                .userName(order.user().fullName())
                .status(order.status())
                .total(order.total())
                .estimatedTime(order.estimatedTime())
                .createdAt(order.createdAt())
                // .updatedAt(order.updatedAt())
                .notificationType(NotificationType.ORDER_STATUS_CHANGED)
                .message("Tu orden #" + order.id() + " cambió a estado: " + order.status())
                .build();

        notificationPort.notifyUser(order.user().id(), notification);
    }

    /**
     * Notifica al usuario que el tiempo estimado de su orden cambió.
     */
    public void notifyOrderEstimatedTimeChanged(OrderDetailResponse order) {
        OrderNotificationDto notification = OrderNotificationDto.builder()
                .orderId(order.id())
                .userId(order.user().id())
                .userName(order.user().fullName())
                .status(order.status())
                .total(order.total())
                .estimatedTime(order.estimatedTime())
                .createdAt(order.createdAt())
                // .updatedAt(order.updatedAt())
                .notificationType(NotificationType.ORDER_ESTIMATED_TIME_CHANGED)
                .message("El tiempo estimado de tu orden #" + order.id() + " cambió a: " + order.estimatedTime())
                .build();

        notificationPort.notifyUser(order.user().id(), notification);
    }

    public void notifyOrderCreatedByUser(OrderDetailResponse order) {
        OrderNotificationDto notification = OrderNotificationDto.builder()
                .orderId(order.id())
                .userId(order.user().id())
                .userName(order.user().fullName())
                .status(order.status())
                .total(order.total())
                .estimatedTime(order.estimatedTime())
                .createdAt(order.createdAt())
                // .updatedAt(order.updatedAt())
                .notificationType(NotificationType.ORDER_CREATED)
                .message("¡Nueva orden! El usuario " + order.user().fullName() + " creó la orden #" + order.id())
                .build();

        notificationPort.notifyEmployees(notification);
    }
}