package com.example.PrepaidSolution.config;

// Marks this class as configuration class (Spring will load it at startup)
import org.springframework.context.annotation.Configuration;

// Enables WebSocket message handling in Spring Boot
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

// Used to configure message broker (message routing system)
import org.springframework.messaging.simp.config.MessageBrokerRegistry;

// Used to register WebSocket endpoints
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

// Interface to customize WebSocket settings
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration // Tells Spring: this class contains configuration
@EnableWebSocketMessageBroker // Enables WebSocket + messaging system
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        // Enables simple in-memory message broker
        // It routes messages from backend → frontend
        config.enableSimpleBroker("/topic");

        // Prefix for messages coming from frontend → backend
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // This is the URL where frontend will connect
        // First HTTP handshake happens, then upgraded to WebSocket
        registry.addEndpoint("/ws").withSockJS();
    }
}
