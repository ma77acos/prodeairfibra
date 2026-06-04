// src/main/java/com/k2radio/prode/security/JwtAuthenticationFilter.java
package com.k2radio.prode.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k2radio.prode.exception.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String jwt = getJwtFromRequest(request);

        // Sin token → continuar (rutas públicas lo manejan)
        if (!StringUtils.hasText(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // validateToken() lanza ExpiredJwtException si expiró
            if (tokenProvider.validateToken(jwt)) {
                String email = tokenProvider.getEmailFromToken(jwt);
                log.debug("JWT válido para usuario: {}", email);

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (userDetails instanceof CustomUserDetails customUserDetails) {
                    log.debug("CustomUserDetails cargado para userId: {}", customUserDetails.getId());
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (ExpiredJwtException e) {
            // ⭐ Token expirado → 401 con mensaje claro
            // NO seguimos el filtro, cortamos acá
            log.debug("Access token expirado para request: {}", request.getRequestURI());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED",
                    "El token de acceso expiró. Por favor, renovalo.");
            return;

        } catch (JwtException e) {
            // Token malformado o inválido → 401
            log.error("Token JWT inválido: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "TOKEN_INVALID",
                    "Token inválido.");
            return;

        } catch (Exception e) {
            // Cualquier otro error inesperado
            log.error("Error procesando JWT: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_ERROR",
                    "Error procesando la autenticación.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response,
                                   HttpStatus status,
                                   String error,
                                   String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}