package com.k2radio.prode.dto.league;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteByEmailRequest {

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email inválido")
    private String email;
}