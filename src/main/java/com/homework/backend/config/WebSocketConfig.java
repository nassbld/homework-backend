package com.homework.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocketSecurity // On garde la sécurité moderne de Spring
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Note : Nous n'avons plus besoin d'injecter JwtService ou UserDetailsService ici.

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/user", "/topic");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    // Le bean d'autorisation est la manière moderne de sécuriser les messages.
    // Spring s'occupera de trouver l'utilisateur authentifié par le handshake HTTP.
    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages
                .simpDestMatchers("/app/**").authenticated() // Seuls les utilisateurs authentifiés peuvent envoyer des messages
                .simpSubscribeDestMatchers("/user/**").authenticated() // Seuls les utilisateurs authentifiés peuvent s'abonner à des files privées
                .anyMessage().denyAll(); // On refuse tout le reste par défaut

        return messages.build();
    }
}
