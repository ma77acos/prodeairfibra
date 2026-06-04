package com.k2radio.prode.dto.league;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeagueRequest {

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String description;

    @Min(value = 2, message = "La liga debe tener al menos 2 miembros")
    @Max(value = 200, message = "La liga no puede superar los 200 miembros")
    private Integer maxMembers = 50;
}