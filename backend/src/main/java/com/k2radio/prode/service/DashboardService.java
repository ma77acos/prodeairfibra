// src/main/java/com/k2radio/prode/service/DashboardService.java

package com.k2radio.prode.service;

import com.k2radio.prode.dto.dashboard.DashboardDTO;
import com.k2radio.prode.dto.match.MatchDTO;
import com.k2radio.prode.entity.User;
import com.k2radio.prode.repository.PredictionRepository;
import com.k2radio.prode.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final MatchService matchService;
    private final PredictionService predictionService;
    private final RankingService rankingService;
    private final PredictionRepository predictionRepository;

    public DashboardDTO getDashboard(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        List<MatchDTO> upcomingMatches = matchService.getUpcomingMatches(userId, 5);
        List<MatchDTO> liveMatches = matchService.getLiveMatches(userId);
        List<MatchDTO> recentResults = matchService.getRecentResults(userId, 5);

        Integer rankingPosition = rankingService.getUserPosition(userId);
        Long pendingCount = predictionService.getPendingPredictionsCount(userId);

        Long totalProcessed = predictionRepository.countExactResultsByUserId(userId) +
                predictionRepository.countCorrectWinnersByUserId(userId);

        Integer sumPoints = predictionRepository.sumPointsByUserId(userId);
        if (sumPoints == null) sumPoints = 0;

        DashboardDTO.UserStats stats = DashboardDTO.UserStats.builder()
                .totalPoints(user.getTotalPoints())
                .exactResults(user.getExactResults())
                .correctWinners(user.getCorrectWinners())
                .totalPredictions(user.getPredictions().size())
                .matchesWithPoints(totalProcessed.intValue())
                .averagePointsPerMatch(calculateAverage(user.getTotalPoints(), totalProcessed.intValue()))
                .bestStreak(0) // TODO: Implementar
                .currentStreak(0) // TODO: Implementar
                .build();

        return DashboardDTO.builder()
                .userStats(stats)
                .upcomingMatches(upcomingMatches)
                .liveMatches(liveMatches)
                .recentResults(recentResults)
                .rankingPosition(rankingPosition)
                .totalParticipants((int) userRepository.count())
                .pendingPredictions(pendingCount.intValue())
                .build();
    }

    private Double calculateAverage(int points, int matches) {
        if (matches == 0) return 0.0;
        return Math.round((double) points / matches * 100.0) / 100.0;
    }
}