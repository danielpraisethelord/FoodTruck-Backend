package com.foodtruck.backend.infrastructure.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.foodtruck.backend.application.dto.OrderNotificationDto;
import com.foodtruck.backend.application.port.OrderNotificationPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador de infraestructura para notificaciones de órdenes por WebSocket.
 * Implementa el puerto de salida usando SimpMessagingTemplate de Spring.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketOrderNotificationAdapter implements OrderNotificationPort {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifyEmployees(OrderNotificationDto notification) {
        try {
            log.info("Enviando notificación a empleados: {}", notification.getMessage());
            messagingTemplate.convertAndSend("/topic/orders/employees", notification);
        } catch (Exception e) {
            log.error("Error al enviar notificación a empleados: {}", e.getMessage());
        }
    }

    @Override
    public void notifyUser(Long userId, OrderNotificationDto notification) {
        try {
            log.info("Enviando notificación al usuario {}: {}", userId, notification.getMessage());
            messagingTemplate.convertAndSend("/topic/orders/user/" + userId, notification);
        } catch (Exception e) {
            log.error("Error al enviar notificación al usuario {}: {}", userId, e.getMessage());
        }
    }
}