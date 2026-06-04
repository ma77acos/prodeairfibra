package com.k2radio.prode.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoringConfigRequest {

    @NotNull
    @Min(0)
    private Integer pointsCorrectWinner;

    @NotNull
    @Min(0)
    private Integer pointsCorrectGoalsOneTeam;

    @NotNull
    @Min(0)
    private Integer pointsExactResult;

    @NotNull
    private Boolean exactReplacesAll;

    @NotNull
    @Min(0)
    private Integer bonusRoundOf16;

    @NotNull
    @Min(0)
    private Integer bonusQuarterFinals;

    @NotNull
    @Min(0)
    private Integer bonusSemiFinals;

    @NotNull
    @Min(0)
    private Integer bonusFinal;

    @NotNull
    @Min(1)
    private Integer lockHoursBeforeMatch;
}