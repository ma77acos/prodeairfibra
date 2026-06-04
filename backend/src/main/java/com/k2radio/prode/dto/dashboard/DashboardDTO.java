// src/main/java/com/k2radio/prode/dto/dashboard/DashboardDTO.java

package com.k2radio.prode.dto.dashboard;

import com.k2radio.prode.dto.match.MatchDTO;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private UserStats userStats;
    private List<MatchDTO> upcomingMatches;
    private List<MatchDTO> liveMatches;
    private List<MatchDTO> recentResults;
    private Integer rankingPosition;
    private Integer totalParticipants;
    private Integer pendingPredictions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStats {
        private Integer totalPoints;
        private Integer exactResults;
        private Integer correctWinners;
        private Integer totalPredictions;
        private Integer matchesWithPoints;
        private Double averagePointsPerMatch;
        private Integer bestStreak;
        private Integer currentStreak;
    }
}