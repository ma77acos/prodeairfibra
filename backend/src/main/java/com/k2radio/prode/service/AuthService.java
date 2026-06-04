// src/main/java/com/k2radio/prode/service/AuthService.java

package com.k2radio.prode.service;

import com.k2radio.prode.dto.auth.*;
import com.k2radio.prode.dto.user.UserDTO;
import com.k2radio.prode.entity.User;
import com.k2radio.prode.exception.*;
import com.k2radio.prode.repository.UserRepository;
import com.k2radio.prode.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public Map<String, String> register(RegisterRequest request) {
        log.info("Registrando nuevo usuario: {}", request.getEmail());

        // Validaciones
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Las contraseñas no coinciden");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email ya está registrado");
        }

        if (userRepository.existsByDni(request.getDni())) {
            throw new ConflictException("El DNI ya está registrado");
        }

        LocalDate birthDate;

        try {
            birthDate = LocalDate.parse(request.getBirthDate());
        } catch (DateTimeParseException e) {
            throw new BadRequestException("La fecha de nacimiento debe tener formato YYYY-MM-DD");
        }

        if (birthDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de nacimiento no puede ser futura");
        }

        int age = Period.between(birthDate, LocalDate.now()).getYears();

        if (age < 18) {
            throw new BadRequestException("Debés ser mayor de 18 años para registrarte");
        }

        // Crear usuario (emailVerified = false por defecto)
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase())
                .phone(request.getPhone())
                .dni(request.getDni())
                .birthDate(birthDate)
                .password(passwordEncoder.encode(request.getPassword()))
                .provider("LOCAL")
                .verificationToken(UUID.randomUUID().toString())
                .build();

        user = userRepository.save(user);

        // Enviar email de verificación
        try {
            emailService.sendVerificationEmail(user);
        } catch (Exception e) {
            log.error("Error enviando email de verificación", e);
        }

        // NO devolvemos tokens, el usuario debe verificar el email primero
        return Map.of(
                "message", "Registro exitoso. Revisá tu email para verificar tu cuenta.",
                "email", user.getEmail()
        );
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login de usuario: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                    .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

            if (!user.getActive()) {
                throw new UnauthorizedException("Cuenta desactivada");
            }

            if (!user.getEmailVerified()) {
                throw new UnauthorizedException("Debés verificar tu email antes de ingresar. Revisá tu casilla.");
            }

            return generateAuthResponse(user);

        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Credenciales inválidas");
        }
    }

    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        user.setResetPasswordToken(UUID.randomUUID().toString());
        user.setResetPasswordExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new BadRequestException("Token inválido"));

        if (user.getResetPasswordExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expirado");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Token inválido o ya utilizado"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user = userRepository.save(user);

        log.info("Email verificado para usuario: {}", user.getEmail());

        // Devolvemos tokens para que el frontend lo loguee automáticamente
        return generateAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token requerido");
        }

        // ⭐ Usamos validateTokenWithResult para distinguir expirado de inválido
        JwtTokenProvider.TokenValidationResult result =
                jwtTokenProvider.validateTokenWithResult(refreshToken);

        switch (result) {
            case INVALID -> throw new UnauthorizedException("Refresh token inválido");
            case EXPIRED -> throw new UnauthorizedException("La sesión expiró. Por favor, iniciá sesión nuevamente.");
            case VALID -> { /* continuar */ }
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

        if (!user.getActive()) {
            throw new UnauthorizedException("Cuenta desactivada");
        }

        if (!user.getEmailVerified()) {
            throw new UnauthorizedException("Email no verificado");
        }

        log.info("Refresh token válido para usuario: {}", email);

        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .user(AuthResponse.UserDTO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .avatarUrl(user.getAvatarUrl())
                        .role(user.getRole().name())
                        .totalPoints(user.getTotalPoints())
                        .exactResults(user.getExactResults())
                        .build())
                .build();
    }
}
