package com.foodtruck.backend.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodtruck.backend.application.dto.OrderNotificationDto;
import com.foodtruck.backend.application.dto.OrderNotificationDto.NotificationType;
import com.foodtruck.backend.domain.types.OrderStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Controlador informativo sobre la configuración y uso de WebSocket.
 * No maneja conexiones reales, solo proporciona documentación.
 */
@RestController
@RequestMapping("/api/websocket")
@Tag(name = "WebSocket Documentation", description = "Información sobre conexión y uso de WebSocket para notificaciones en tiempo real")
public class WebSocketDocumentationController {

  @Operation(summary = "Información de conexión WebSocket", description = """
      # Conexión WebSocket para Notificaciones en Tiempo Real

      ## Endpoint de Conexión:
      **URL:** `ws://localhost:8081/ws` (o `wss://` en producción con HTTPS)

      ## Protocolo:
      Se utiliza **STOMP** sobre WebSocket con **SockJS** como fallback.

      ## Canales de Suscripción:

      ### 1. Canal para Empleados (reciben notificaciones de cambios hechos por usuarios):
      - **Topic:** `/topic/orders/employees`
      - **Quién se suscribe:** Empleados y administradores
      - **Notificaciones que reciben:**
        - Órdenes creadas por usuarios
        - Órdenes modificadas por usuarios (cambios en productos/cantidades)

      ### 2. Canal para Usuarios (reciben notificaciones de cambios hechos por empleados):
      - **Topic:** `/topic/orders/user/{userId}`
      - **Quién se suscribe:** Cada usuario se suscribe a su propio canal (reemplazar {userId} con su ID)
      - **Notificaciones que reciben:**
        - Cambios de estado en sus órdenes
        - Cambios en tiempo estimado de preparación

      ---

      ## Implementación en Ionic/Angular

      ### 1. Instalar dependencias:
      ```bash
      npm install sockjs-client @stomp/stompjs rxjs
      ```

      ### 2. Crear el servicio WebSocket (websocket.service.ts):

      ```typescript
      import { Injectable } from '@angular/core';
      import { Client, StompSubscription } from '@stomp/stompjs';
      import * as SockJS from 'sockjs-client';
      import { BehaviorSubject, Observable } from 'rxjs';

      export interface OrderNotification {
        orderId: number;
        userId: number;
        userName: string;
        status: string;
        total: number;
        estimatedTime: string;
        createdAt: string;
        updatedAt: string;
        notificationType: 'ORDER_CREATED' | 'ORDER_UPDATED' | 'ORDER_STATUS_CHANGED' |
                         'ORDER_ESTIMATED_TIME_CHANGED' | 'ORDER_CANCELLED';
        message: string;
      }

      @Injectable({
        providedIn: 'root'
      })
      export class WebSocketService {
        private client: Client;
        private connected = new BehaviorSubject<boolean>(false);
        private employeeNotifications = new BehaviorSubject<OrderNotification | null>(null);
        private userNotifications = new BehaviorSubject<OrderNotification | null>(null);
        private subscriptions: StompSubscription[] = [];

        constructor() {
          this.client = new Client();
        }

        connect(serverUrl: string = 'http://localhost:8081/ws'): void {
          this.client.webSocketFactory = () => new SockJS(serverUrl);

          this.client.onConnect = (frame) => {
            console.log('Conectado a WebSocket:', frame);
            this.connected.next(true);
          };

          this.client.onStompError = (frame) => {
            console.error('Error en WebSocket:', frame);
            this.connected.next(false);
          };

          this.client.onWebSocketClose = () => {
            console.log('WebSocket desconectado');
            this.connected.next(false);
          };

          this.client.reconnectDelay = 5000;
          this.client.activate();
        }

        disconnect(): void {
          this.subscriptions.forEach(sub => sub.unsubscribe());
          this.subscriptions = [];
          this.client.deactivate();
          this.connected.next(false);
        }

        subscribeToEmployeeNotifications(): Observable<OrderNotification | null> {
          if (this.client.connected) {
            const subscription = this.client.subscribe('/topic/orders/employees', (message) => {
              const notification: OrderNotification = JSON.parse(message.body);
              console.log('Notificación para empleados:', notification);
              this.employeeNotifications.next(notification);
            });
            this.subscriptions.push(subscription);
          }
          return this.employeeNotifications.asObservable();
        }

        subscribeToUserNotifications(userId: number): Observable<OrderNotification | null> {
          if (this.client.connected) {
            const topic = '/topic/orders/user/' + userId;
            const subscription = this.client.subscribe(topic, (message) => {
              const notification: OrderNotification = JSON.parse(message.body);
              console.log('Notificación para usuario:', notification);
              this.userNotifications.next(notification);
            });
            this.subscriptions.push(subscription);
          }
          return this.userNotifications.asObservable();
        }

        isConnected(): Observable<boolean> {
          return this.connected.asObservable();
        }
      }
      ```

      ### 3. Usar en componente de usuario:

      Ver endpoint /api/websocket/notification-example para ver la estructura completa de las notificaciones.

      ## Notas Importantes:
      - Conecta solo cuando el usuario entre a la pantalla relevante
      - Desconecta al salir del componente
      - En producción usa wss:// con HTTPS
      - Las notificaciones se reciben en tiempo real
      """, responses = {
      @ApiResponse(responseCode = "200", description = "Información de configuración WebSocket", content = @Content(mediaType = "application/json", schema = @Schema(implementation = WebSocketInfo.class)))
  })
  @GetMapping("/info")
  public ResponseEntity<WebSocketInfo> getWebSocketInfo() {
    WebSocketInfo info = new WebSocketInfo(
        "ws://localhost:8081/ws",
        "STOMP over WebSocket with SockJS fallback",
        "/topic/orders/employees",
        "/topic/orders/user/{userId}",
        "Employees receive notifications when users create/modify orders. " +
            "Users receive notifications when employees change order status or estimated time.",
        "Install: npm install sockjs-client @stomp/stompjs rxjs. " +
            "See full implementation example in the operation description above.");
    return ResponseEntity.ok(info);
  }

  @Operation(summary = "Ejemplo de estructura de notificación WebSocket", description = """
      Este endpoint muestra la estructura exacta del JSON que recibirás por WebSocket.

      **Nota:** Este es solo un endpoint de ejemplo para documentación.
      Las notificaciones reales se envían automáticamente por WebSocket cuando ocurren eventos en las órdenes.

      ## Tipos de notificación:
      - **ORDER_CREATED**: Cuando un usuario crea una orden
      - **ORDER_UPDATED**: Cuando un usuario modifica su orden
      - **ORDER_STATUS_CHANGED**: Cuando un empleado cambia el estado de una orden
      - **ORDER_ESTIMATED_TIME_CHANGED**: Cuando un empleado actualiza el tiempo estimado
      - **ORDER_CANCELLED**: Cuando una orden es cancelada
      """, responses = {
      @ApiResponse(responseCode = "200", description = "Ejemplo de notificación WebSocket", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderNotificationDto.class), examples = {
          @ExampleObject(name = "Notificación para Empleados (Orden Creada)", value = """
              {
                "orderId": 1,
                "userId": 48,
                "userName": "Juan Pérez",
                "status": "PENDIENTE",
                "total": 250.50,
                "estimatedTime": null,
                "createdAt": "2024-10-30T14:30:00",
                "updatedAt": "2024-10-30T14:30:00",
                "notificationType": "ORDER_CREATED",
                "message": "¡Nueva orden! El usuario Juan Pérez creó la orden #1"
              }
              """),
          @ExampleObject(name = "Notificación para Usuario (Estado Cambiado)", value = """
              {
                "orderId": 1,
                "userId": 48,
                "userName": "Juan Pérez",
                "status": "EN_PREPARACION",
                "total": 250.50,
                "estimatedTime": "00:30",
                "createdAt": "2024-10-30T14:30:00",
                "updatedAt": "2024-10-30T14:35:00",
                "notificationType": "ORDER_STATUS_CHANGED",
                "message": "Tu orden #1 cambió a estado: EN_PREPARACION"
              }
              """),
          @ExampleObject(name = "Notificación para Usuario (Tiempo Estimado)", value = """
              {
                "orderId": 1,
                "userId": 48,
                "userName": "Juan Pérez",
                "status": "EN_PREPARACION",
                "total": 250.50,
                "estimatedTime": "00:45",
                "createdAt": "2024-10-30T14:30:00",
                "updatedAt": "2024-10-30T14:40:00",
                "notificationType": "ORDER_ESTIMATED_TIME_CHANGED",
                "message": "El tiempo estimado de tu orden #1 cambió a: 00:45"
              }
              """)
      }))
  })
  @GetMapping("/notification-example")
  public ResponseEntity<OrderNotificationDto> getNotificationExample() {
    // Este es solo un ejemplo para documentación
    OrderNotificationDto example = OrderNotificationDto.builder()
        .orderId(1L)
        .userId(48L)
        .userName("Juan Pérez")
        .status(OrderStatus.EN_PREPARACION)
        .total(new BigDecimal("250.50"))
        .estimatedTime("00:30")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .notificationType(NotificationType.ORDER_STATUS_CHANGED)
        .message("Tu orden #1 cambió a estado: EN_PREPARACION")
        .build();

    return ResponseEntity.ok(example);
  }

  @Data
  @AllArgsConstructor
  @Schema(description = "Información de configuración WebSocket")
  public static class WebSocketInfo {
    @Schema(description = "Endpoint de conexión WebSocket", example = "ws://localhost:8081/ws")
    private String endpoint;

    @Schema(description = "Protocolo utilizado", example = "STOMP over WebSocket with SockJS fallback")
    private String protocol;

    @Schema(description = "Topic para empleados", example = "/topic/orders/employees")
    private String employeesTopic;

    @Schema(description = "Patrón del topic para usuarios", example = "/topic/orders/user/{userId}")
    private String userTopicPattern;

    @Schema(description = "Descripción del funcionamiento")
    private String documentation;

    @Schema(description = "Ejemplo de implementación en Ionic/Angular")
    private String ionicAngularExample;
  }
}