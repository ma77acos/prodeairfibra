// src/main/java/com/k2radio/prode/service/PredictionService.java

package com.k2radio.prode.service;

import com.k2radio.prode.dto.prediction.*;
import com.k2radio.prode.entity.*;
import com.k2radio.prode.exception.*;
import com.k2radio.prode.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ScoringConfigRepository scoringConfigRepository;

    @Transactional
    public PredictionResponse createOrUpdatePrediction(Long userId, PredictionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new NotFoundException("Partido no encontrado"));

        // Validar que el partido no esté bloqueado
        if (!match.canPredict()) {
            throw new BadRequestException("No se puede pronosticar este partido. Ya está bloqueado o finalizado.");
        }

        // Buscar pronóstico existente o crear uno nuevo
        Prediction prediction = predictionRepository
                .findByUserIdAndMatchId(userId, request.getMatchId())
                .orElse(Prediction.builder()
                        .user(user)
                        .match(match)
                        .build());

        prediction.setPredictedHomeGoals(request.getPredictedHomeGoals());
        prediction.setPredictedAwayGoals(request.getPredictedAwayGoals());

        prediction = predictionRepository.save(prediction);

        log.info("Pronóstico guardado - Usuario: {}, Partido: {}, Resultado: {}-{}",
                userId, match.getId(), request.getPredictedHomeGoals(), request.getPredictedAwayGoals());

        return toResponse(prediction);
    }

    public List<PredictionResponse> getUserPredictions(Long userId) {
        List<Prediction> predictions = predictionRepository
                .findByUserIdOrderByMatchMatchDateTimeDesc(userId);

        return predictions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<PredictionResponse> getUserPredictionsPaginated(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return predictionRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    public List<PredictionResponse> getUserProcessedPredictions(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Prediction> predictions = predictionRepository
                .findProcessedByUserId(userId, pageable);

        return predictions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void processMatchPredictions(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partido no encontrado"));

        if (match.getStatus() != Match.MatchStatus.FINISHED) {
            throw new BadRequestException("El partido no ha finalizado");
        }

        if (match.getHomeGoals() == null || match.getAwayGoals() == null) {
            throw new BadRequestException("El resultado del partido no está completo");
        }

        ScoringConfig config = scoringConfigRepository.findByActiveTrue()
                .orElse(getDefaultConfig());

        List<Prediction> predictions = predictionRepository.findUnprocessedByMatchId(matchId);

        for (Prediction prediction : predictions) {
            calculateAndAssignPoints(prediction, match, config);
            prediction.setProcessed(true);

            // Actualizar estadísticas del usuario
            User user = prediction.getUser();
            user.setTotalPoints(user.getTotalPoints() + prediction.getPointsEarned());

            if (Boolean.TRUE.equals(prediction.getExactResult())) {
                user.setExactResults(user.getExactResults() + 1);
            }

            if (Boolean.TRUE.equals(prediction.getCorrectWinner())) {
                user.setCorrectWinners(user.getCorrectWinners() + 1);
            }

            userRepository.save(user);
        }

        predictionRepository.saveAll(predictions);
        log.info("Procesados {} pronósticos para el partido {}", predictions.size(), matchId);
    }

    private void calculateAndAssignPoints(Prediction prediction, Match match, ScoringConfig config) {
        int points = 0;

        Integer predHome = prediction.getPredictedHomeGoals();
        Integer predAway = prediction.getPredictedAwayGoals();
        Integer actualHome = match.getHomeGoals();
        Integer actualAway = match.getAwayGoals();

        // Verificar resultado exacto
        boolean exactResult = predHome.equals(actualHome) && predAway.equals(actualAway);
        prediction.setExactResult(exactResult);
        prediction.setCorrectHomeGoals(predHome.equals(actualHome));
        prediction.setCorrectAwayGoals(predAway.equals(actualAway));

        // Verificar ganador
        Prediction.PredictionResult actualResult = getMatchResult(actualHome, actualAway);
        boolean correctWinner = prediction.getPredictedResult() == actualResult;
        prediction.setCorrectWinner(correctWinner);

        if (exactResult) {
            // Resultado exacto
            if (config.getExactReplacesAll()) {
                points = config.getPointsExactResult();
            } else {
                points = config.getPointsExactResult() + config.getPointsCorrectWinner();
            }
        } else {
            // Acertar ganador
            if (correctWinner) {
                points += config.getPointsCorrectWinner();
            }

            // Acertar goles de un equipo
            if (predHome.equals(actualHome)) {
                points += config.getPointsCorrectGoalsOneTeam();
            }
            if (predAway.equals(actualAway)) {
                points += config.getPointsCorrectGoalsOneTeam();
            }
        }

        // Bonus por fase eliminatoria
        points += getPhaseBonus(match.getPhase(), config);

        prediction.setPointsEarned(points);
    }

    private Prediction.PredictionResult getMatchResult(int homeGoals, int awayGoals) {
        if (homeGoals > awayGoals) return Prediction.PredictionResult.HOME_WIN;
        if (homeGoals < awayGoals) return Prediction.PredictionResult.AWAY_WIN;
        return Prediction.PredictionResult.DRAW;
    }

    private int getPhaseBonus(Match.MatchPhase phase, ScoringConfig config) {
        return switch (phase) {
            case ROUND_OF_16 -> config.getBonusRoundOf16();
            case QUARTER_FINALS -> config.getBonusQuarterFinals();
            case SEMI_FINALS -> config.getBonusSemiFinals();
            case FINAL, THIRD_PLACE -> config.getBonusFinal();
            default -> 0;
        };
    }

    private ScoringConfig getDefaultConfig() {
        return ScoringConfig.builder()
                .pointsCorrectWinner(3)
                .pointsCorrectGoalsOneTeam(2)
                .pointsExactResult(4)
                .exactReplacesAll(true)
                .bonusRoundOf16(0)
                .bonusQuarterFinals(1)
                .bonusSemiFinals(2)
                .bonusFinal(3)
                .build();
    }

    public Long getPendingPredictionsCount(Long userId) {
        return predictionRepository.countPendingPredictions(userId);
    }

    private PredictionResponse toResponse(Prediction prediction) {
        Match match = prediction.getMatch();
        return PredictionResponse.builder()
                .id(prediction.getId())
                .matchId(match.getId())
                .homeTeamName(match.getHomeTeam().getName())
                .homeTeamCode(match.getHomeTeam().getCode())
                .awayTeamName(match.getAwayTeam().getName())
                .awayTeamCode(match.getAwayTeam().getCode())
                .matchDateTime(match.getMatchDateTime())
                .predictedHomeGoals(prediction.getPredictedHomeGoals())
                .predictedAwayGoals(prediction.getPredictedAwayGoals())
                .predictedResult(prediction.getPredictedResult())
                .actualHomeGoals(match.getHomeGoals())
                .actualAwayGoals(match.getAwayGoals())
                .pointsEarned(prediction.getPointsEarned())
                .processed(prediction.getProcessed())
                .correctWinner(prediction.getCorrectWinner())
                .correctHomeGoals(prediction.getCorrectHomeGoals())
                .correctAwayGoals(prediction.getCorrectAwayGoals())
                .exactResult(prediction.getExactResult())
                .createdAt(prediction.getCreatedAt())
                .updatedAt(prediction.getUpdatedAt())
                .build();
    }
}