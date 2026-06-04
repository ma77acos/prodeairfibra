package com.k2radio.prode.dto.admin;

import com.k2radio.prode.entity.Match;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMatchRequest {
    @NotNull
    private Long homeTeamId;

    @NotNull
    private Long awayTeamId;

    @NotNull
    private LocalDateTime matchDateTime;

    @NotNull
    private Match.MatchPhase phase;

    private String groupName;
    private Integer matchNumber;
    private String stadium;
    private String city;
    private String country;
}