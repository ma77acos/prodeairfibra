// src/main/java/com/k2radio/prode/dto/prediction/PredictionResponse.java

package com.k2radio.prode.dto.prediction;

import com.k2radio.prode.entity.Prediction;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {
    private Long id;
    private Long matchId;
    private String homeTeamName;
    private String homeTeamCode;
    private String awayTeamName;
    private String awayTeamCode;
    private LocalDateTime matchDateTime;
    private Integer predictedHomeGoals;
    private Integer predictedAwayGoals;
    private Prediction.PredictionResult predictedResult;
    private Integer actualHomeGoals;
    private Integer actualAwayGoals;
    private Integer pointsEarned;
    private Boolean processed;
    private Boolean correctWinner;
    private Boolean correctHomeGoals;
    private Boolean correctAwayGoals;
    private Boolean exactResult;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}