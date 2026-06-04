// src/main/java/com/k2radio/prode/controller/RankingController.java

package com.k2radio.prode.controller;

import com.k2radio.prode.dto.ranking.RankingDTO;
import com.k2radio.prode.security.CustomUserDetails;
import com.k2radio.prode.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.k2radio.prode.dto.ranking.WeekOption;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/paginated")
    public ResponseEntity<RankingDTO> getGlobalRankingPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal(errorOnInvalidType = false) CustomUserDetails userDetails) {

        Long currentUserId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(rankingService.getGlobalRankingPaginated(page, size, currentUserId));
    }

    @GetMapping("/my-position")
    public ResponseEntity<RankingDTO> getMyRanking(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(rankingService.getRankingForUser(userDetails.getId()));
    }

    @GetMapping("/league/{leagueId}")
    public ResponseEntity<RankingDTO> getLeagueRanking(
            @PathVariable Long leagueId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(rankingService.getLeagueRanking(leagueId, userDetails.getId()));
    }

    @GetMapping("/by-phase")
    public ResponseEntity<RankingDTO> getRankingByPhase(
            @RequestParam String phase,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal(errorOnInvalidType = false) CustomUserDetails userDetails) {

        Long currentUserId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(rankingService.getRankingByPhase(phase, currentUserId, page, size));
    }

    @GetMapping("/by-week")
    public ResponseEntity<RankingDTO> getRankingByWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime weekStart,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal(errorOnInvalidType = false) CustomUserDetails userDetails) {

        Long currentUserId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(rankingService.getRankingByWeek(weekStart, currentUserId, page, size));
    }

    @GetMapping("/available-weeks")
    public ResponseEntity<List<WeekOption>> getAvailableWeeks() {
        return ResponseEntity.ok(rankingService.getAvailableWeeks());
    }
}