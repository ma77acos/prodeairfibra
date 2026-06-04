package com.k2radio.prode.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultResponse {
    private String message;
    private Long matchId;
    private Integer homeGoals;
    private Integer awayGoals;
}