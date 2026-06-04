// src/main/java/com/k2radio/prode/security/JwtTokenProvider.java
package com.k2radio.prode.security;

import com.k2radio.prode.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("id", user.getId())
                .claim("name", user.getName())
                .claim("role", user.getRole().name())
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return parseClaims(token).get("id", Long.class);
    }

    /**
     * Valida el token. Solo devuelve true si es válido Y no expiró.
     * Para token expirado, lanza ExpiredJwtException en vez de retornar false,
     * así el filtro puede distinguir entre "inválido" y "expirado".
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // ⭐ Re-lanzamos para que el filtro pueda distinguirla
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Token JWT malformado");
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT no soportado");
        } catch (IllegalArgumentException e) {
            log.error("Token JWT vacío o nulo");
        } catch (JwtException e) {
            log.error("Error JWT: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Valida sin lanzar excepción - útil para el refresh token
     * donde queremos saber si expiró vs si es inválido
     */
    public TokenValidationResult validateTokenWithResult(String token) {
        try {
            parseClaims(token);
            return TokenValidationResult.VALID;
        } catch (ExpiredJwtException e) {
            return TokenValidationResult.EXPIRED;
        } catch (JwtException | IllegalArgumentException e) {
            return TokenValidationResult.INVALID;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Obtiene el email incluso de un token expirado.
     * Necesario para el refresh: el token expiró pero queremos saber de quién era.
     */
    public String getEmailFromExpiredToken(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            // El payload sigue siendo accesible aunque esté expirado
            return e.getClaims().getSubject();
        }
    }

    public enum TokenValidationResult {
        VALID, EXPIRED, INVALID
    }
}