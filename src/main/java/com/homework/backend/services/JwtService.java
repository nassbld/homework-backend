// src/main/java/com/homework/backend/services/JwtService.java
package com.homework.backend.services;

import com.homework.backend.config.props.JwtProperties;
import com.homework.backend.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .claim("firstName", user.getFirstName())
                .claim("role", user.getRole().name())
                .claim("id", user.getId())
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
                .signWith(getSignInKey())
                .compact();
    }

    private String buildToken(Map<String, Object> extraClaims, String subject) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public org.springframework.security.core.Authentication getAuthentication(String token) {
        if (token != null && isTokenValidForWebSocket(token)) {
            Claims claims = extractAllClaims(token);
            String username = claims.getSubject();

            String role = claims.get("role", String.class);
            if (role == null) {
                return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(username, null, java.util.Collections.emptyList());
            }

            java.util.List<org.springframework.security.core.GrantedAuthority> authorities = java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(role));

            return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(username, null, authorities);
        }
        return null;
    }

    private boolean isTokenValidForWebSocket(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
