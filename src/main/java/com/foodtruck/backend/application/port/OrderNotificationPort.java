package com.foodtruck.backend.application.port;

import com.foodtruck.backend.application.dto.OrderNotificationDto;

/**
 * Puerto de salida para notificaciones de órdenes.
 * Define el contrato para enviar notificaciones en tiempo real.
 */
public interface OrderNotificationPort {

    /**
     * Notifica a todos los empleados sobre cambios en una orden.
     */
    void notifyEmployees(OrderNotificationDto notification);

    /**
     * Notifica a un usuario específico sobre cambios en su orden.
     */
    void notifyUser(Long userId, OrderNotificationDto notification);
}