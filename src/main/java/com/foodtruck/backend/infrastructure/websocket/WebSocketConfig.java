package com.foodtruck.backend.infrastructure.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket usando STOMP sobre WebSocket.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita un broker simple en memoria para enviar mensajes a los clientes
        config.enableSimpleBroker("/topic", "/queue");

        // Define el prefijo para mensajes que vienen del cliente (si se necesita en el
        // futuro)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registra el endpoint WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // En producción, especifica los orígenes permitidos
                .withSockJS(); // Fallback para navegadores que no soportan WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // En producción, especifica los orígenes permitidos
    }
}