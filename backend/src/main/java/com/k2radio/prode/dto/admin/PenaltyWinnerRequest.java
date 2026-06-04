// com/k2radio/prode/dto/admin/PenaltyWinnerRequest.java
package com.k2radio.prode.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PenaltyWinnerRequest {

    @NotNull(message = "Debe indicar el ID del equipo ganador por penales")
    private Long penaltyWinnerTeamId;
}