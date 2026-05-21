package com.riwi.librotech.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuracion de STOMP sobre WebSocket.
 *
 * - "/tema/**"  → prefijo de los TOPICS a los que el cliente se SUSCRIBE.
 *   (cuando el bot/usuario emiten un mensaje, todos los suscritos a /tema/mensajes lo reciben).
 *
 * - "/app/**"   → prefijo de los DESTINOS a los que el cliente ENVIA.
 *   (el cliente publica a /app/enviar; lo recibe @MessageMapping("/enviar")).
 *
 * - "/chat-websocket" → endpoint HTTP que el cliente usa para abrir el WS.
 *   withSockJS() habilita fallback (long-polling) para navegadores sin WS.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Broker en memoria: difunde a los suscritos a /tema/**
        config.enableSimpleBroker("/tema");
        // Prefijo para mensajes que el cliente envia hacia los @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat-websocket")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
