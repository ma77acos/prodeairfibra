// src/main/java/com/k2radio/prode/dto/auth/AuthResponse.java

package com.k2radio.prode.dto.auth;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserDTO user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String avatarUrl;
        private String role;
        private Integer totalPoints;
        private Integer exactResults;
    }
}