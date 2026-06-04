package com.k2radio.prode.dto.admin;

import com.k2radio.prode.entity.Match;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMatchStatusRequest {
    @NotNull(message = "El estado es requerido")
    private Match.MatchStatus status;
}
