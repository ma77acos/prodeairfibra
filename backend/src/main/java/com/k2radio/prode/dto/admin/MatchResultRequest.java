// src/main/java/com/k2radio/prode/dto/admin/MatchResultRequest.java

package com.k2radio.prode.dto.admin;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultRequest {

    @NotNull(message = "Los goles del equipo local son requeridos")
    @Min(value = 0, message = "Los goles no pueden ser negativos")
    private Integer homeGoals;

    @NotNull(message = "Los goles del equipo visitante son requeridos")
    @Min(value = 0, message = "Los goles no pueden ser negativos")
    private Integer awayGoals;
}