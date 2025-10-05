package com.homework.backend.config.handlers;

import com.homework.backend.models.Role;
import com.homework.backend.models.User;
import com.homework.backend.repositories.UserRepository;
import com.homework.backend.services.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final String frontendRedirectUrl;

    public OAuth2AuthenticationSuccessHandler(JwtService jwtService, UserRepository userRepository, @Value("${frontend.url}") String frontendRedirectUrl) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.frontendRedirectUrl = frontendRedirectUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String email = oidcUser.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFirstName(oidcUser.getAttribute("given_name"));
                    newUser.setLastName(oidcUser.getAttribute("family_name"));
                    newUser.setRole(Role.STUDENT);
                    newUser.setPassword(UUID.randomUUID().toString());

                    return userRepository.save(newUser);
                });

        String token = jwtService.generateToken(user);

        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl + "/oauth/redirect")
                .queryParam("token", token)
                .build().toUriString();

        response.sendRedirect(targetUrl);
    }
}
