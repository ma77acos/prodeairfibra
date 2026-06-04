package com.k2radio.prode.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringConfigDTO {
    private Long id;
    private Integer pointsCorrectWinner;
    private Integer pointsCorrectGoalsOneTeam;
    private Integer pointsExactResult;
    private Boolean exactReplacesAll;
    private Integer bonusRoundOf16;
    private Integer bonusQuarterFinals;
    private Integer bonusSemiFinals;
    private Integer bonusFinal;
    private Integer lockHoursBeforeMatch;
    private Boolean active;
}