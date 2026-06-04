// src/main/java/com/k2radio/prode/entity/Prediction.java

package com.k2radio.prode.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "predictions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "match_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(nullable = false)
    private Integer predictedHomeGoals;

    @Column(nullable = false)
    private Integer predictedAwayGoals;

    @Enumerated(EnumType.STRING)
    private PredictionResult predictedResult; // Calculado automáticamente

    @Builder.Default
    private Integer pointsEarned = 0;

    @Builder.Default
    private Boolean processed = false;

    private Boolean correctWinner;

    private Boolean correctHomeGoals;

    private Boolean correctAwayGoals;

    private Boolean exactResult;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum PredictionResult {
        HOME_WIN,  // 1
        DRAW,      // X
        AWAY_WIN   // 2
    }

    @PrePersist
    @PreUpdate
    public void calculatePredictedResult() {
        if (predictedHomeGoals > predictedAwayGoals) {
            this.predictedResult = PredictionResult.HOME_WIN;
        } else if (predictedHomeGoals < predictedAwayGoals) {
            this.predictedResult = PredictionResult.AWAY_WIN;
        } else {
            this.predictedResult = PredictionResult.DRAW;
        }
    }
}