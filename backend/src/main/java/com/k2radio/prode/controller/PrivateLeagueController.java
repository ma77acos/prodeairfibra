// src/main/java/com/k2radio/prode/controller/PrivateLeagueController.java

package com.k2radio.prode.controller;

import com.k2radio.prode.dto.league.*;
import com.k2radio.prode.service.PrivateLeagueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leagues")
@RequiredArgsConstructor
public class PrivateLeagueController {

    private final PrivateLeagueService leagueService;

    // ==================== CREAR LIGA ====================

    @PostMapping
    public ResponseEntity<LeagueResponse> createLeague(
            @Valid @RequestBody CreateLeagueRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leagueService.createLeague(request, userDetails.getUsername()));
    }

    // ==================== MIS LIGAS ====================

    @GetMapping("/my")
    public ResponseEntity<List<LeagueResponse>> getMyLeagues(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(leagueService.getMyLeagues(userDetails.getUsername()));
    }

    // ==================== RANKING PÚBLICO DE LIGAS ====================

    @GetMapping("/public/ranking")
    public ResponseEntity<List<LeaguePublicRankingEntry>> getPublicLeagueRanking() {
        return ResponseEntity.ok(leagueService.getPublicLeagueRanking());
    }

    // ==================== DETALLE DE LIGA ====================

    @GetMapping("/{leagueId}")
    public ResponseEntity<LeagueResponse> getLeague(
            @PathVariable Long leagueId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(leagueService.getLeague(leagueId, userDetails.getUsername()));
    }

    // ==================== INFO POR CÓDIGO (para join page) ====================

    @GetMapping("/code/{code}")
    public ResponseEntity<LeagueResponse> getLeagueByCode(
            @PathVariable String code,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(leagueService.getLeagueByCode(code, userDetails.getUsername()));
    }

    // ==================== UNIRSE POR CÓDIGO ====================

    @PostMapping("/join/{code}")
    public ResponseEntity<LeagueResponse> joinByCode(
            @PathVariable String code,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(leagueService.joinByCode(code, userDetails.getUsername()));
    }

    // ==================== SALIR DE LIGA ====================

    @DeleteMapping("/{leagueId}/leave")
    public ResponseEntity<Map<String, String>> leaveLeague(
            @PathVariable Long leagueId,
            @AuthenticationPrincipal UserDetails userDetails) {

        leagueService.leaveLeague(leagueId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Saliste de la liga correctamente"));
    }

    // ==================== EXPULSAR MIEMBRO ====================

    @DeleteMapping("/{leagueId}/members/{memberId}")
    public ResponseEntity<Map<String, String>> kickMember(
            @PathVariable Long leagueId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails) {

        leagueService.kickMember(leagueId, memberId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Miembro expulsado correctamente"));
    }

    // ==================== ELIMINAR LIGA ====================

    @DeleteMapping("/{leagueId}")
    public ResponseEntity<Map<String, String>> deleteLeague(
            @PathVariable Long leagueId,
            @AuthenticationPrincipal UserDetails userDetails) {

        leagueService.deleteLeague(leagueId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Liga eliminada correctamente"));
    }

    // ==================== RANKING DE LIGA ====================

    @GetMapping("/{leagueId}/ranking")
    public ResponseEntity<List<LeagueRankingEntry>> getLeagueRanking(
            @PathVariable Long leagueId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(leagueService.getLeagueRanking(leagueId, userDetails.getUsername()));
    }

    // ==================== INVITAR POR EMAIL ====================

    @PostMapping("/{leagueId}/invite")
    public ResponseEntity<Map<String, String>> inviteByEmail(
            @PathVariable Long leagueId,
            @Valid @RequestBody InviteByEmailRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        leagueService.inviteByEmail(leagueId, request, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Invitación enviada correctamente"));
    }
}