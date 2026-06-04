package com.k2radio.prode.controller;

import com.k2radio.prode.dto.admin.*;
import com.k2radio.prode.dto.league.LeagueResponse;
import com.k2radio.prode.entity.*;
import com.k2radio.prode.service.AdminService;
import com.k2radio.prode.service.FixtureGeneratorService;
import com.k2radio.prode.service.GroupStandingsService;
import com.k2radio.prode.service.KnockoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final FixtureGeneratorService fixtureGeneratorService;
    private final KnockoutService knockoutService;
    private final GroupStandingsService groupStandingsService;

    // ==================== KNOCKOUT ====================

    @PostMapping("/fixture/generate-knockout-only")
    public ResponseEntity<MessageResponse> generateKnockoutOnly() {
        int matchesCreated = fixtureGeneratorService.generateKnockoutFixtureOnly();
        return ResponseEntity.ok(new MessageResponse(
                matchesCreated + " partidos de eliminatorias generados correctamente"
        ));
    }

    // Obtener tabla de posiciones de todos los grupos
    @GetMapping("/standings")
    public ResponseEntity<Map<String, List<GroupStandingDTO>>> getAllStandings() {
        return ResponseEntity.ok(groupStandingsService.getAllGroupStandings());
    }

    // Obtener tabla de un grupo específico
    @GetMapping("/standings/{group}")
    public ResponseEntity<List<GroupStandingDTO>> getGroupStandings(@PathVariable String group) {
        return ResponseEntity.ok(groupStandingsService.getGroupStandings(group.toUpperCase()));
    }

    // Generar cruces de 32avos (después de fase de grupos)
    @PostMapping("/knockout/generate-round-of-32")
    public ResponseEntity<MessageResponse> generateRoundOf32() {
        knockoutService.generateRoundOf32();
        return ResponseEntity.ok(new MessageResponse("Cruces de 32avos generados correctamente"));
    }

    // Avanzar ganador de un partido a la siguiente fase
    @PostMapping("/knockout/advance/{matchId}")
    public ResponseEntity<MessageResponse> advanceWinner(@PathVariable Long matchId) {
        knockoutService.advanceWinner(matchId);
        return ResponseEntity.ok(new MessageResponse("Ganador avanzado a la siguiente fase"));
    }

    @PostMapping("/knockout/{matchId}/penalty-winner")
    public ResponseEntity<MessageResponse> setPenaltyWinner(
            @PathVariable Long matchId,
            @Valid @RequestBody PenaltyWinnerRequest request) {

        knockoutService.setPenaltyWinner(matchId, request.getPenaltyWinnerTeamId());
        return ResponseEntity.ok(new MessageResponse("Ganador por penales registrado correctamente"));
    }

    // ==================== FIXTURE ====================

    @PostMapping("/fixture/generate")
    public ResponseEntity<MessageResponse> generateFixture() {
        int matchesCreated = fixtureGeneratorService.generateWorldCupFixture();
        return ResponseEntity.ok(new MessageResponse(
                matchesCreated + " partidos generados correctamente"
        ));
    }

    @DeleteMapping("/fixture/clear")
    public ResponseEntity<MessageResponse> clearFixture() {
        fixtureGeneratorService.clearAllMatches();
        return ResponseEntity.ok(new MessageResponse("Fixture eliminado correctamente"));
    }

    @DeleteMapping("/matches/{matchId}/result")
    public ResponseEntity<MessageResponse> revertMatchResult(@PathVariable Long matchId) {
        adminService.revertMatchResult(matchId);
        return ResponseEntity.ok(new MessageResponse("Resultado revertido correctamente. Podés cargar el resultado correcto."));
    }

    // ==================== ESTADÍSTICAS ====================

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getAdminStats());
    }

    // ==================== PARTIDOS ====================

    @PostMapping("/matches/{matchId}/result")
    public ResponseEntity<MatchResultResponse> setMatchResult(
            @PathVariable Long matchId,
            @Valid @RequestBody MatchResultRequest request) {

        adminService.setMatchResult(matchId, request);

        return ResponseEntity.ok(MatchResultResponse.builder()
                .message("Resultado establecido correctamente")
                .matchId(matchId)
                .homeGoals(request.getHomeGoals())
                .awayGoals(request.getAwayGoals())
                .build());
    }

    @PatchMapping("/matches/{matchId}/status")
    public ResponseEntity<MessageResponse> updateMatchStatus(
            @PathVariable Long matchId,
            @RequestBody UpdateMatchStatusRequest request) {

        adminService.updateMatchStatus(matchId, request.getStatus());

        return ResponseEntity.ok(new MessageResponse("Estado actualizado correctamente"));
    }

    @PostMapping("/matches")
    public ResponseEntity<MatchDTO> createMatch(@Valid @RequestBody CreateMatchRequest request) {
        Match match = adminService.createMatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toMatchDTO(match));
    }

    @PutMapping("/matches/{matchId}")
    public ResponseEntity<MatchDTO> updateMatch(
            @PathVariable Long matchId,
            @Valid @RequestBody UpdateMatchRequest request) {

        Match match = adminService.updateMatch(matchId, request);
        return ResponseEntity.ok(toMatchDTO(match));
    }

    // ==================== USUARIOS ====================

    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{userId}/toggle")
    public ResponseEntity<UserStatusResponse> toggleUserStatus(@PathVariable Long userId) {
        UserSummaryDTO user = adminService.toggleUserStatus(userId);

        return ResponseEntity.ok(UserStatusResponse.builder()
                .message("Estado del usuario actualizado")
                .userId(userId)
                .active(user.getActive())
                .build());
    }

    // ==================== CONFIGURACIÓN DE PUNTAJE ====================

    @GetMapping("/config/scoring")
    public ResponseEntity<ScoringConfigDTO> getScoringConfig() {
        return ResponseEntity.ok(adminService.getScoringConfig());
    }

    @PutMapping("/config/scoring")
    public ResponseEntity<ScoringConfigDTO> updateScoringConfig(
            @Valid @RequestBody ScoringConfigRequest request) {

        return ResponseEntity.ok(adminService.updateScoringConfig(request));
    }

    // ==================== EQUIPOS ====================

    @GetMapping("/teams")
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        return ResponseEntity.ok(adminService.getAllTeams());
    }

    @PostMapping("/teams")
    public ResponseEntity<TeamDTO> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        TeamDTO team = adminService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(team);
    }

    // ==================== LIGAS PRIVADAS ====================

    @GetMapping("/leagues")
    public ResponseEntity<List<LeagueResponse>> getAllLeagues() {
        return ResponseEntity.ok(adminService.getAllLeagues());
    }

    @DeleteMapping("/leagues/{leagueId}")
    public ResponseEntity<MessageResponse> deleteLeague(@PathVariable Long leagueId) {
        adminService.adminDeleteLeague(leagueId);
        return ResponseEntity.ok(new MessageResponse("Liga eliminada correctamente"));
    }

    // ==================== HELPER METHODS ====================

    private MatchDTO toMatchDTO(Match match) {
        return MatchDTO.builder()
                .id(match.getId())
                .matchNumber(match.getMatchNumber())
                .homeTeamId(match.getHomeTeam().getId())
                .homeTeamName(match.getHomeTeam().getName())
                .awayTeamId(match.getAwayTeam().getId())
                .awayTeamName(match.getAwayTeam().getName())
                .matchDateTime(match.getMatchDateTime())
                .status(match.getStatus())
                .phase(match.getPhase())
                .groupName(match.getGroupName())
                .stadium(match.getStadium())
                .city(match.getCity())
                .country(match.getCountry())
                .homeGoals(match.getHomeGoals())
                .awayGoals(match.getAwayGoals())
                .build();
    }
}