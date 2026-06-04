// src/main/java/com/k2radio/prode/controller/MatchController.java

package com.k2radio.prode.controller;

import com.k2radio.prode.dto.match.MatchDTO;
import com.k2radio.prode.entity.Match;
import com.k2radio.prode.entity.Team;
import com.k2radio.prode.repository.MatchRepository;
import com.k2radio.prode.repository.TeamRepository;
import com.k2radio.prode.security.CustomUserDetails;
import com.k2radio.prode.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    @GetMapping
    public ResponseEntity<List<MatchDTO>> getAllMatches(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(matchService.getAllMatches(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDTO> getMatchById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(matchService.getMatchById(id, userId));
    }

    @GetMapping("/phase/{phase}")
    public ResponseEntity<List<MatchDTO>> getMatchesByPhase(
            @PathVariable Match.MatchPhase phase,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(matchService.getMatchesByPhase(phase, userId));
    }

    @GetMapping("/group/{groupName}")
    public ResponseEntity<List<MatchDTO>> getMatchesByGroup(
            @PathVariable String groupName,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(matchService.getMatchesByGroup(groupName, userId));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<MatchDTO>> getUpcomingMatches(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(matchService.getUpcomingMatches(userId, limit));
    }

    @GetMapping("/live")
    public ResponseEntity<List<MatchDTO>> getLiveMatches(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(matchService.getLiveMatches(userId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<MatchDTO>> getRecentResults(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(matchService.getRecentResults(userId, limit));
    }

    @GetMapping("/knockout-started")
    public ResponseEntity<Map<String, Boolean>> isKnockoutStarted() {
        // Verificar si hay al menos un partido de eliminatorias con equipos reales (no TBD)
        Team tbdTeam = teamRepository.findByCode("TBD").orElse(null);

        if (tbdTeam == null) {
            return ResponseEntity.ok(Map.of("started", false));
        }

        List<Match> knockoutMatches = matchRepository.findByPhaseOrderByMatchNumberAsc(Match.MatchPhase.ROUND_OF_16);

        boolean hasRealTeams = knockoutMatches.stream()
                .anyMatch(match ->
                        !match.getHomeTeam().getId().equals(tbdTeam.getId()) &&
                                !match.getAwayTeam().getId().equals(tbdTeam.getId())
                );

        return ResponseEntity.ok(Map.of("started", hasRealTeams));
    }
}
