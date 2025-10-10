package com.homework.backend.config.filters;

import com.homework.backend.services.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtChannelInterceptor.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtChannelInterceptor(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

            if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
                throw new AccessDeniedException("Missing or invalid Authorization header");
            }

            try {
                String jwt = authorizationHeader.substring(7);
                String userEmail = jwtService.extractUsername(jwt);

                if (userEmail != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        accessor.setUser(authentication);
                    } else {
                        throw new AccessDeniedException("Invalid or expired JWT token");
                    }
                } else {
                    throw new AccessDeniedException("Unable to extract username from JWT");
                }
            } catch (Exception e) {
                throw new AccessDeniedException("JWT authentication failed: " + e.getMessage());
            }
        }

        return message;
    }

}
