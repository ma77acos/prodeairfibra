package com.k2radio.prode.dto.league;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaguePublicRankingEntry {
    private Long id;
    private String name;
    private String description;
    private String ownerName;
    private Integer memberCount;
    private Integer totalPoints;
    private Integer position;
}