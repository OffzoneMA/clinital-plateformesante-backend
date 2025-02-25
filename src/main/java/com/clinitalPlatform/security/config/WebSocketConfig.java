package com.clinitalPlatform.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.ArrayList;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue" , "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Autorise l'origine du frontend
                .withSockJS()
                .setSessionCookieNeeded(true);
    }

    /**
     * Ajoute un intercepteur pour gérer l'authentification.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Récupère le token JWT depuis les headers
                    String jwt = accessor.getFirstNativeHeader("Authorization");
                    if (jwt != null && jwt.startsWith("Bearer ")) {
                        jwt = jwt.substring(7); // Retire "Bearer " du début

                        // Validez le token JWT ici (exemple simplifié)
                        UsernamePasswordAuthenticationToken authentication = getAuthentication(jwt);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }

                return message;
            }
        });
    }

    /**
     * Configure les autorisations pour les destinations STOMP.
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                // Vous pouvez ajouter des contrôles supplémentaires ici si nécessaire
                return message;
            }
        });
    }

    /**
     * Exemple de validation d'un token JWT.
     */
    private UsernamePasswordAuthenticationToken getAuthentication(String jwt) {
        // Implémentez ici la logique de validation du JWT
        // Par exemple, extrayez le nom d'utilisateur du token
        String user = "exampleUser"; // Remplacez par la vraie logique de décodage JWT
        return user != null ?
                new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>()) :
                null;
    }
}