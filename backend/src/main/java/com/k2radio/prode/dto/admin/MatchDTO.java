package com.k2radio.prode.dto.admin;

import com.k2radio.prode.entity.Match;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchDTO {
    private Long id;
    private Integer matchNumber;
    private Long homeTeamId;
    private String homeTeamName;
    private Long awayTeamId;
    private String awayTeamName;
    private LocalDateTime matchDateTime;
    private Match.MatchStatus status;
    private Match.MatchPhase phase;
    private String groupName;
    private String stadium;
    private String city;
    private String country;
    private Integer homeGoals;
    private Integer awayGoals;
}