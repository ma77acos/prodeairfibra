// src/main/java/com/k2radio/prode/dto/match/MatchDTO.java

package com.k2radio.prode.dto.match;

import com.k2radio.prode.entity.Match;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchDTO {
    private Long id;
    private TeamDTO homeTeam;
    private TeamDTO awayTeam;
    private LocalDateTime matchDateTime;
    private Match.MatchStatus status;
    private Integer homeGoals;
    private Integer awayGoals;
    private Match.MatchPhase phase;
    private String groupName;
    private Integer matchNumber;
    private String stadium;
    private String city;
    private String country;
    private Boolean canPredict;
    private Boolean isLocked;
    private Long secondsUntilLock;
    private PredictionDTO userPrediction;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamDTO {
        private Long id;
        private String name;
        private String code;
        private String flagUrl;
        private String group;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictionDTO {
        private Long id;
        private Integer predictedHomeGoals;
        private Integer predictedAwayGoals;
        private Integer pointsEarned;
        private Boolean processed;
    }
}