// src/main/java/com/k2radio/prode/entity/ScoringConfig.java

package com.k2radio.prode.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scoring_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    private Integer pointsCorrectWinner = 3;

    @Column(nullable = false)
    @Builder.Default
    private Integer pointsCorrectGoalsOneTeam = 2;

    @Column(nullable = false)
    @Builder.Default
    private Integer pointsExactResult = 4;

    @Column(nullable = false)
    @Builder.Default
    private Boolean exactReplacesAll = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer lockHoursBeforeMatch = 1;

    // Bonus points for knockout stages
    @Builder.Default
    private Integer bonusRoundOf16 = 0;

    @Builder.Default
    private Integer bonusQuarterFinals = 1;

    @Builder.Default
    private Integer bonusSemiFinals = 2;

    @Builder.Default
    private Integer bonusFinal = 3;

    @Builder.Default
    private Boolean active = true;
}
