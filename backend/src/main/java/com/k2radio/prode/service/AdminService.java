package com.k2radio.prode.service;

import com.k2radio.prode.dto.admin.*;
import com.k2radio.prode.dto.league.LeagueResponse;
import com.k2radio.prode.entity.*;
import com.k2radio.prode.exception.*;
import com.k2radio.prode.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ScoringConfigRepository scoringConfigRepository;
    private final PredictionRepository predictionRepository;
    private final PredictionService predictionService;
    private final KnockoutService knockoutService;
    private final GroupStandingsService groupStandingsService;
    private final PrivateLeagueService privateLeagueService;

    // ==================== PARTIDOS ====================

    @Transactional
    @CacheEvict(value = {"matches", "ranking"}, allEntries = true)
    public void setMatchResult(Long matchId, MatchResultRequest request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partido no encontrado"));

        match.setHomeGoals(request.getHomeGoals());
        match.setAwayGoals(request.getAwayGoals());
        match.setStatus(Match.MatchStatus.FINISHED);

        matchRepository.save(match);
        log.info("Resultado establecido para partido {}: {}-{}",
                matchId, request.getHomeGoals(), request.getAwayGoals());

        // Procesar pronósticos
        predictionService.processMatchPredictions(matchId);

        // ==================== AUTO-GENERAR CRUCES ====================

        // Si es el último partido de fase de grupos, generar 32avos automáticamente
        if (match.getPhase() == Match.MatchPhase.GROUP_STAGE) {
            // Llamar a método separado que se ejecuta DESPUÉS del commit
            tryGenerateKnockoutAsync();  // ← CAMBIO
        }

        // Si es partido de eliminatoria, avanzar al ganador automáticamente
        // (Excluir fase de grupos, 3er puesto y final - no tienen "siguiente fase")
        if (match.getPhase() != Match.MatchPhase.GROUP_STAGE &&
                match.getPhase() != Match.MatchPhase.THIRD_PLACE &&
                match.getPhase() != Match.MatchPhase.FINAL) {

            try {
                // ✅ Si hay empate, no intentar avanzar todavía
                // El admin debe indicar el ganador por penales primero
                if (request.getHomeGoals().equals(request.getAwayGoals())) {
                    log.info("⚽ Partido {} terminó empatado. Esperando ganador por penales.", matchId);
                } else {
                    knockoutService.advanceWinner(matchId);
                    log.info("✅ Ganador avanzado automáticamente a la siguiente fase");
                }
            } catch (Exception e) {
                log.warn("No se pudo avanzar al ganador automáticamente: {}", e.getMessage());
            }
        }
    }

    // ← NUEVO MÉTODO
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void tryGenerateKnockoutAfterCommit() {
        try {
            // Verificar si TODOS los grupos terminaron
            boolean allGroupsComplete = true;
            for (String group : List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L")) {
                if (!groupStandingsService.isGroupComplete(group)) {
                    allGroupsComplete = false;
                    break;
                }
            }

            // Si todos los grupos terminaron, generar cruces
            if (allGroupsComplete) {
                log.info("🏆 ¡Todos los grupos completados! Generando cruces de 32avos automáticamente...");
                knockoutService.generateRoundOf32();
                log.info("✅ Cruces de 32avos generados automáticamente");
            }
        } catch (Exception e) {
            // Si falla, solo loguear (NO romper el guardado del resultado)
            log.error("❌ Error al intentar generar cruces automáticamente: {}", e.getMessage());
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void tryGenerateKnockoutAsync() {
        try {
            boolean allGroupsComplete = true;
            for (String group : List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L")) {
                if (!groupStandingsService.isGroupComplete(group)) {
                    allGroupsComplete = false;
                    break;
                }
            }

            if (allGroupsComplete) {
                log.info("🏆 ¡Todos los grupos completados! Generando cruces de 32avos automáticamente...");
                knockoutService.generateRoundOf32();
                log.info("✅ Cruces de 32avos generados automáticamente");
            }
        } catch (Exception e) {
            log.error("❌ Error al intentar generar cruces: {}", e.getMessage());
        }
    }

    @Transactional
    @CacheEvict(value = "matches", allEntries = true)
    public void updateMatchStatus(Long matchId, Match.MatchStatus status) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partido no encontrado"));

        match.setStatus(status);
        matchRepository.save(match);

        log.info("Estado del partido {} actualizado a: {}", matchId, status);
    }

    @Transactional
    @CacheEvict(value = "matches", allEntries = true)
    public Match createMatch(CreateMatchRequest request) {
        Team homeTeam = teamRepository.findById(request.getHomeTeamId())
                .orElseThrow(() -> new NotFoundException("Equipo local no encontrado"));

        Team awayTeam = teamRepository.findById(request.getAwayTeamId())
                .orElseThrow(() -> new NotFoundException("Equipo visitante no encontrado"));

        Match match = Match.builder()
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .matchDateTime(request.getMatchDateTime())
                .phase(request.getPhase())
                .groupName(request.getGroupName())
                .matchNumber(request.getMatchNumber())
                .stadium(request.getStadium())
                .city(request.getCity())
                .country(request.getCountry())
                .status(Match.MatchStatus.SCHEDULED)
                .build();

        match = matchRepository.save(match);
        log.info("Partido creado: {} vs {}", homeTeam.getName(), awayTeam.getName());

        return match;
    }

    @Transactional
    @CacheEvict(value = "matches", allEntries = true)
    public Match updateMatch(Long matchId, UpdateMatchRequest request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partido no encontrado"));

        match.setMatchDateTime(request.getMatchDateTime());
        match.setStadium(request.getStadium());
        match.setCity(request.getCity());
        match.setCountry(request.getCountry());

        match = matchRepository.save(match);
        log.info("Partido {} actualizado", matchId);

        return match;
    }

    // ==================== USUARIOS ====================

    public List<UserSummaryDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserSummaryDTO toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        user.setActive(!user.getActive());
        userRepository.save(user);

        log.info("Usuario {} ahora está {}", userId, user.getActive() ? "activo" : "inactivo");

        return toUserSummaryDTO(user);
    }

    // ==================== CONFIGURACIÓN DE PUNTAJE ====================

    public ScoringConfigDTO getScoringConfig() {
        return scoringConfigRepository.findByActiveTrue()
                .map(this::toScoringConfigDTO)
                .orElseGet(() -> ScoringConfigDTO.builder()
                        .pointsCorrectWinner(3)
                        .pointsCorrectGoalsOneTeam(2)
                        .pointsExactResult(4)
                        .exactReplacesAll(true)
                        .bonusRoundOf16(0)
                        .bonusQuarterFinals(1)
                        .bonusSemiFinals(2)
                        .bonusFinal(3)
                        .lockHoursBeforeMatch(1)
                        .active(false)
                        .build());
    }

    @Transactional
    public ScoringConfigDTO updateScoringConfig(ScoringConfigRequest request) {
        // Desactivar configuración anterior
        scoringConfigRepository.findByActiveTrue()
                .ifPresent(existing -> {
                    existing.setActive(false);
                    scoringConfigRepository.save(existing);
                    log.info("Configuración anterior desactivada");
                });

        // Crear nueva configuración
        ScoringConfig config = ScoringConfig.builder()
                .pointsCorrectWinner(request.getPointsCorrectWinner())
                .pointsCorrectGoalsOneTeam(request.getPointsCorrectGoalsOneTeam())
                .pointsExactResult(request.getPointsExactResult())
                .exactReplacesAll(request.getExactReplacesAll())
                .bonusRoundOf16(request.getBonusRoundOf16())
                .bonusQuarterFinals(request.getBonusQuarterFinals())
                .bonusSemiFinals(request.getBonusSemiFinals())
                .bonusFinal(request.getBonusFinal())
                .lockHoursBeforeMatch(request.getLockHoursBeforeMatch())
                .active(true)
                .build();

        config = scoringConfigRepository.save(config);
        log.info("Nueva configuración de puntaje guardada");

        return toScoringConfigDTO(config);
    }

    // ==================== EQUIPOS ====================

    public List<TeamDTO> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::toTeamDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TeamDTO createTeam(CreateTeamRequest request) {
        if (teamRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Ya existe un equipo con el código: " + request.getCode());
        }

        Team team = Team.builder()
                .name(request.getName())
                .code(request.getCode())
                .flagUrl(request.getFlagUrl())
                .groupName(request.getGroupName())
                .build();

        team = teamRepository.save(team);
        log.info("Equipo creado: {} ({})", team.getName(), team.getCode());

        return toTeamDTO(team);
    }

    // ==================== ESTADÍSTICAS ====================

    public AdminStatsDTO getAdminStats() {
        long totalUsers = userRepository.count();
        long totalMatches = matchRepository.count();
        long finishedMatches = matchRepository.countFinishedMatches();

        return AdminStatsDTO.builder()
                .totalUsers((int) totalUsers)
                .totalMatches((int) totalMatches)
                .finishedMatches((int) finishedMatches)
                .pendingMatches((int) (totalMatches - finishedMatches))
                .build();
    }

    @Transactional
    @CacheEvict(value = {"matches", "ranking"}, allEntries = true)
    public void revertMatchResult(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partido no encontrado"));

        if (match.getStatus() != Match.MatchStatus.FINISHED) {
            throw new BadRequestException("Solo se pueden revertir partidos finalizados");
        }

        // Obtener predictions procesadas de este partido
        List<Prediction> predictions = predictionRepository.findProcessedByMatchIdWithUser(matchId);

        for (Prediction prediction : predictions) {
            User user = prediction.getUser();

            // Restar puntos del usuario
            user.setTotalPoints(user.getTotalPoints() - prediction.getPointsEarned());

            // Restar exactResults si correspondía
            if (Boolean.TRUE.equals(prediction.getExactResult())) {
                user.setExactResults(user.getExactResults() - 1);
            }

            // Restar correctWinners si correspondía
            if (Boolean.TRUE.equals(prediction.getCorrectWinner())) {
                user.setCorrectWinners(user.getCorrectWinners() - 1);
            }

            // Resetear la prediction
            prediction.setPointsEarned(0);
            prediction.setProcessed(false);
            prediction.setExactResult(null);
            prediction.setCorrectWinner(null);
            prediction.setCorrectHomeGoals(null);
            prediction.setCorrectAwayGoals(null);

            userRepository.save(user);
        }

        predictionRepository.saveAll(predictions);

        // Limpiar resultado del partido
        match.setHomeGoals(null);
        match.setAwayGoals(null);
        match.setStatus(Match.MatchStatus.SCHEDULED);
        matchRepository.save(match);

        log.info("⏪ Resultado revertido para partido {}. {} predictions reseteadas.",
                matchId, predictions.size());
    }

    public List<LeagueResponse> getAllLeagues() {
        return privateLeagueService.getAllLeagues();
    }

    public void adminDeleteLeague(Long leagueId) {
        privateLeagueService.adminDeleteLeague(leagueId);
    }

    // ==================== MAPPERS PRIVADOS ====================

    private UserSummaryDTO toUserSummaryDTO(User user) {
        return UserSummaryDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .active(user.getActive())
                .totalPoints(user.getTotalPoints())
                .exactResults(user.getExactResults())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private ScoringConfigDTO toScoringConfigDTO(ScoringConfig config) {
        return ScoringConfigDTO.builder()
                .id(config.getId())
                .pointsCorrectWinner(config.getPointsCorrectWinner())
                .pointsCorrectGoalsOneTeam(config.getPointsCorrectGoalsOneTeam())
                .pointsExactResult(config.getPointsExactResult())
                .exactReplacesAll(config.getExactReplacesAll())
                .bonusRoundOf16(config.getBonusRoundOf16())
                .bonusQuarterFinals(config.getBonusQuarterFinals())
                .bonusSemiFinals(config.getBonusSemiFinals())
                .bonusFinal(config.getBonusFinal())
                .lockHoursBeforeMatch(config.getLockHoursBeforeMatch())
                .active(config.getActive())
                .build();
    }

    private TeamDTO toTeamDTO(Team team) {
        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .code(team.getCode())
                .flagUrl(team.getFlagUrl())
                .groupName(team.getGroupName())
                .build();
    }
}
