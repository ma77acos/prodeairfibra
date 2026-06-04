package com.k2radio.prode.dto.league;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueRankingEntry {
    private Integer position;
    private Long userId;
    private String userName;
    private String avatarUrl;
    private Integer totalPoints;
    private Integer exactResults;
    private Integer correctWinners;
    private Boolean isOwner;
}