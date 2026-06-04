// src/main/java/com/k2radio/prode/entity/Match.java

package com.k2radio.prode.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Column(nullable = false)
    private LocalDateTime matchDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MatchStatus status = MatchStatus.SCHEDULED;

    private Integer homeGoals;

    private Integer awayGoals;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "penalty_winner_id")
    private Team penaltyWinner; // null si se definió en 90 min o alargue, equipo ganador si fue por penales

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchPhase phase;

    @Column(length = 10)
    private String groupName; // A, B, C... (null for knockout)

    @Column(nullable = false)
    private Integer matchNumber; // 1-64

    @Column(length = 100)
    private String stadium;

    @Column(length = 100)
    private String city;

    @Column(length = 50)
    private String country;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Prediction> predictions = new HashSet<>();

    public enum MatchStatus {
        SCHEDULED,    // Programado
        LOCKED,       // Bloqueado (menos de 1 hora)
        LIVE,         // En juego
        FINISHED,     // Finalizado
        POSTPONED,    // Pospuesto
        CANCELLED     // Cancelado
    }

    public enum MatchPhase {
        GROUP_STAGE,
        ROUND_OF_32,
        ROUND_OF_16,
        QUARTER_FINALS,
        SEMI_FINALS,
        THIRD_PLACE,
        FINAL
    }

    public boolean isLocked() {
        return LocalDateTime.now().isAfter(matchDateTime.minusHours(1));
    }

    public boolean canPredict() {
        return status == MatchStatus.SCHEDULED && !isLocked();
    }
}