// src/main/java/com/homework/backend/config/filters/JwtAuthenticationFilter.java
package com.homework.backend.config.filters;

import com.homework.backend.services.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = null;
        final String authHeader = request.getHeader("Authorization");

        // 1. Essayer de récupérer le token depuis l'en-tête "Authorization"
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }
        // 2. Si non trouvé, et si c'est une requête pour le WebSocket, essayer depuis le paramètre de requête
        else if (request.getRequestURI().startsWith("/ws") && request.getParameter("token") != null) {
            jwt = request.getParameter("token");
        }

        // Si aucun token n'est trouvé, on continue la chaîne sans authentifier
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // On extrait l'email (sujet) du token
            final String userEmail = jwtService.extractUsername(jwt);

            // Si on a un email et que l'utilisateur n'est pas déjà authentifié
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Si le token est valide pour cet utilisateur
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // On crée l'objet d'authentification
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // On met à jour le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException e) {
            // JWT expiré - on log et on continue (l'utilisateur sera anonyme)
            logger.warn("JWT expiré pour la requête {} - {}", request.getMethod(), request.getRequestURI());
            // Le SecurityContext reste vide, l'utilisateur sera traité comme anonyme
        } catch (Exception e) {
            // Toute autre erreur JWT (signature invalide, token malformé, etc.)
            logger.error("Erreur lors de la validation du JWT pour {} {} : {}",
                    request.getMethod(), request.getRequestURI(), e.getMessage());
            // Le SecurityContext reste vide
        }

        // On passe la main au filtre suivant dans tous les cas
        filterChain.doFilter(request, response);
    }
}
