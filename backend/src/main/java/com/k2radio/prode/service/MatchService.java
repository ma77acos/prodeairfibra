// src/main/java/com/k2radio/prode/service/MatchService.java

package com.k2radio.prode.service;

import com.k2radio.prode.dto.match.MatchDTO;
import com.k2radio.prode.entity.*;
import com.k2radio.prode.exception.*;
import com.k2radio.prode.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final ScoringConfigRepository scoringConfigRepository;

    @Cacheable(value = "matches", key = "'all'")
    public List<MatchDTO> getAllMatches(Long userId) {
        List<Match> matches = matchRepository.findAll();
        return matches.stream()
                .map(match -> toDTO(match, userId))
                .collect(Collectors.toList());
    }

    public List<MatchDTO> getMatchesByPhase(Match.MatchPhase phase, Long userId) {
        List<Match> matches = matchRepository.findByPhaseOrderByMatchDateTimeAsc(phase);
        return matches.stream()
                .map(match -> toDTO(match, userId))
                .collect(Collectors.toList());
    }

    public List<MatchDTO> getMatchesByGroup(String groupName, Long userId) {
        List<Match> matches = matchRepository.findByGroupNameOrderByMatchDateTimeAsc(groupName);
        return matches.stream()
                .map(match -> toDTO(match, userId))
                .collect(Collectors.toList());
    }

    public List<MatchDTO> getUpcomingMatches(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Match> matches = matchRepository.findUpcomingMatches(LocalDateTime.now(), pageable);
        return matches.stream()
                .map(match -> toDTO(match, userId))
                .collect(Collectors.toList());
    }

    public List<MatchDTO> getLiveMatches(Long userId) {
        List<Match> matches = matchRepository.findLiveMatches();
        return matches.stream()
                .map(match -> toDTO(match, userId))
                .collect(Collectors.toList());
    }

    public List<MatchDTO> getRecentResults(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Match> matches = matchRepository.findRecentFinished(pageable);
        return matches.stream()
                .map(match -> toDTO(match, userId))
                .collect(Collectors.toList());
    }

    public MatchDTO getMatchById(Long matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partido no encontrado"));
        return toDTO(match, userId);
    }

    @Transactional
    @CacheEvict(value = "matches", allEntries = true)
    public void updateMatchStatus(Long matchId, Match.MatchStatus status) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partido no encontrado"));
        match.setStatus(status);
        matchRepository.save(match);
    }

    // Scheduled task para bloquear partidos automáticamente
    @Scheduled(fixedRate = 60000) // Cada minuto
    @Transactional
    @CacheEvict(value = "matches", allEntries = true)
    public void lockMatchesAutomatically() {
        ScoringConfig config = scoringConfigRepository.findByActiveTrue()
                .orElse(ScoringConfig.builder().lockHoursBeforeMatch(1).build());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lockTime = now.plusHours(config.getLockHoursBeforeMatch());

        List<Match> matchesToLock = matchRepository.findMatchesToLock(now, lockTime);

        for (Match match : matchesToLock) {
            match.setStatus(Match.MatchStatus.LOCKED);
            log.info("Partido {} bloqueado automáticamente", match.getId());
        }

        if (!matchesToLock.isEmpty()) {
            matchRepository.saveAll(matchesToLock);
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    @CacheEvict(value = "matches", allEntries = true)
    public void updateLiveMatchesAutomatically() {
        LocalDateTime now = LocalDateTime.now();

        List<Match> matches = matchRepository.findAll();
        boolean changed = false;

        for (Match match : matches) {
            LocalDateTime start = match.getMatchDateTime();
            LocalDateTime finishTime = start.plusHours(2).plusMinutes(30);

            // Si ya pasaron 2h30 desde el inicio → FINISHED
            if ((match.getStatus() == Match.MatchStatus.SCHEDULED
                    || match.getStatus() == Match.MatchStatus.LOCKED
                    || match.getStatus() == Match.MatchStatus.LIVE)
                    && !now.isBefore(finishTime)) {

                if (match.getStatus() != Match.MatchStatus.FINISHED) {
                    match.setStatus(Match.MatchStatus.FINISHED);
                    changed = true;
                }
            }
            // Si ya empezó pero todavía no llegaron las 2h30 → LIVE
            else if ((match.getStatus() == Match.MatchStatus.SCHEDULED
                    || match.getStatus() == Match.MatchStatus.LOCKED)
                    && !now.isBefore(start)) {

                match.setStatus(Match.MatchStatus.LIVE);
                changed = true;
            }
        }

        if (changed) {
            matchRepository.saveAll(matches);
        }
    }

    private MatchDTO toDTO(Match match, Long userId) {
        MatchDTO.PredictionDTO predictionDTO = null;

        if (userId != null) {
            predictionRepository.findByUserIdAndMatchId(userId, match.getId())
                    .ifPresent(p -> {
                        // Se procesa en el builder siguiente
                    });

            var predOpt = predictionRepository.findByUserIdAndMatchId(userId, match.getId());
            if (predOpt.isPresent()) {
                Prediction p = predOpt.get();
                predictionDTO = MatchDTO.PredictionDTO.builder()
                        .id(p.getId())
                        .predictedHomeGoals(p.getPredictedHomeGoals())
                        .predictedAwayGoals(p.getPredictedAwayGoals())
                        .pointsEarned(p.getPointsEarned())
                        .processed(p.getProcessed())
                        .build();
            }
        }

        long secondsUntilLock = ChronoUnit.SECONDS.between(
                LocalDateTime.now(),
                match.getMatchDateTime().minusHours(1)
        );

        return MatchDTO.builder()
                .id(match.getId())
                .homeTeam(MatchDTO.TeamDTO.builder()
                        .id(match.getHomeTeam().getId())
                        .name(match.getHomeTeam().getName())
                        .code(match.getHomeTeam().getCode())
                        .flagUrl(match.getHomeTeam().getFlagUrl())
                        .group(match.getHomeTeam().getGroupName())
                        .build())
                .awayTeam(MatchDTO.TeamDTO.builder()
                        .id(match.getAwayTeam().getId())
                        .name(match.getAwayTeam().getName())
                        .code(match.getAwayTeam().getCode())
                        .flagUrl(match.getAwayTeam().getFlagUrl())
                        .group(match.getAwayTeam().getGroupName())
                        .build())
                .matchDateTime(match.getMatchDateTime())
                .status(match.getStatus())
                .homeGoals(match.getHomeGoals())
                .awayGoals(match.getAwayGoals())
                .phase(match.getPhase())
                .groupName(match.getGroupName())
                .matchNumber(match.getMatchNumber())
                .stadium(match.getStadium())
                .city(match.getCity())
                .country(match.getCountry())
                .canPredict(match.canPredict())
                .isLocked(match.isLocked())
                .secondsUntilLock(Math.max(0, secondsUntilLock))
                .userPrediction(predictionDTO)
                .build();
    }
}