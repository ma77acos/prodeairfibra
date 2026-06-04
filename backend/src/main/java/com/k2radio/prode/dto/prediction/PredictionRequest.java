// src/main/java/com/k2radio/prode/dto/prediction/PredictionRequest.java

package com.k2radio.prode.dto.prediction;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {

    @NotNull(message = "El ID del partido es requerido")
    private Long matchId;

    @NotNull(message = "Los goles del equipo local son requeridos")
    @Min(value = 0, message = "Los goles no pueden ser negativos")
    @Max(value = 20, message = "Máximo 20 goles")
    private Integer predictedHomeGoals;

    @NotNull(message = "Los goles del equipo visitante son requeridos")
    @Min(value = 0, message = "Los goles no pueden ser negativos")
    @Max(value = 20, message = "Máximo 20 goles")
    private Integer predictedAwayGoals;
}