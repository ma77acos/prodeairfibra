// src/main/java/com/k2radio/prode/dto/ranking/RankingDTO.java

package com.k2radio.prode.dto.ranking;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingDTO {
    private List<RankingEntry> entries;
    private Integer totalParticipants;
    private RankingEntry currentUserRanking;

    // Paginación
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankingEntry {
        private Integer position;
        private Long userId;
        private String userName;
        private String avatarUrl;
        private Integer totalPoints;
        private Integer exactResults;
        private Integer correctWinners;
        private Integer predictionsCount;
        private Integer matchesPlayed;
        private Double averagePoints;
        private Integer positionChange; // +/- desde el último partido
    }
}