// src/main/java/com/k2radio/prode/service/RankingService.java

package com.k2radio.prode.service;

import com.k2radio.prode.dto.ranking.RankingDTO;
import com.k2radio.prode.dto.ranking.WeekOption;
import com.k2radio.prode.entity.Match;
import com.k2radio.prode.entity.User;
import com.k2radio.prode.repository.PredictionRepository;
import com.k2radio.prode.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RankingService {

    private final UserRepository userRepository;
    private final PredictionRepository predictionRepository;

    @Cacheable(value = "ranking", key = "'global'")
    public RankingDTO getGlobalRanking() {
        List<User> rankedUsers = userRepository.findAllRanked();
        return buildRankingDTO(rankedUsers, null);
    }

    public RankingDTO getGlobalRankingPaginated(int page, int size, Long currentUserId) {
        Page<User> rankedUsersPage = userRepository.findAllRankedPaginated(PageRequest.of(page, size));

        List<RankingDTO.RankingEntry> entries = buildEntriesFromUsers(
                rankedUsersPage.getContent(),
                (page * size) + 1
        );

        RankingDTO.RankingEntry currentUserEntry = findEntryByUserId(entries, currentUserId);

        // Si el usuario no está en la página actual, buscarlo en el ranking completo
        if (currentUserEntry == null && currentUserId != null) {
            currentUserEntry = findUserEntryInUserList(userRepository.findAllRanked(), currentUserId);
        }

        return RankingDTO.builder()
                .entries(entries)
                .totalParticipants((int) rankedUsersPage.getTotalElements())
                .currentUserRanking(currentUserEntry)
                .currentPage(rankedUsersPage.getNumber())
                .totalPages(rankedUsersPage.getTotalPages())
                .pageSize(rankedUsersPage.getSize())
                .hasNext(rankedUsersPage.hasNext())
                .hasPrevious(rankedUsersPage.hasPrevious())
                .build();
    }

    public RankingDTO getRankingForUser(Long userId) {
        List<User> rankedUsers = userRepository.findAllRanked();
        return buildRankingDTO(rankedUsers, userId);
    }

    public RankingDTO getLeagueRanking(Long leagueId, Long userId) {
        List<User> rankedUsers = userRepository.findByLeagueIdRanked(leagueId);
        return buildRankingDTO(rankedUsers, userId);
    }

    public Integer getUserPosition(Long userId) {
        List<User> rankedUsers = userRepository.findAllRanked();

        for (int i = 0; i < rankedUsers.size(); i++) {
            if (rankedUsers.get(i).getId().equals(userId)) {
                return i + 1;
            }
        }

        return null;
    }

    private RankingDTO buildRankingDTO(List<User> users, Long currentUserId) {
        List<RankingDTO.RankingEntry> entries = buildEntriesFromUsers(users, 1);
        RankingDTO.RankingEntry currentUserEntry = findEntryByUserId(entries, currentUserId);

        return RankingDTO.builder()
                .entries(entries)
                .totalParticipants(users.size())
                .currentUserRanking(currentUserEntry)
                .currentPage(0)
                .totalPages(1)
                .pageSize(users.size())
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    private List<RankingDTO.RankingEntry> buildEntriesFromUsers(List<User> users, int startPosition) {
        List<RankingDTO.RankingEntry> entries = new ArrayList<>();
        AtomicInteger position = new AtomicInteger(startPosition);

        for (User user : users) {
            entries.add(buildUserEntry(user, position.getAndIncrement()));
        }

        return entries;
    }

    private RankingDTO.RankingEntry buildUserEntry(User user, int position) {
        return RankingDTO.RankingEntry.builder()
                .position(position)
                .userId(user.getId())
                .userName(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .totalPoints(user.getTotalPoints())
                .exactResults(user.getExactResults())
                .correctWinners(user.getCorrectWinners())
                .predictionsCount(user.getPredictions().size())
                .matchesPlayed(countProcessedPredictions(user))
                .averagePoints(calculateAveragePoints(user))
                .positionChange(0)
                .build();
    }

    private RankingDTO.RankingEntry findEntryByUserId(List<RankingDTO.RankingEntry> entries, Long userId) {
        if (userId == null) return null;

        return entries.stream()
                .filter(entry -> entry.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    private RankingDTO.RankingEntry findUserEntryInUserList(List<User> users, Long userId) {
        if (userId == null) return null;

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getId().equals(userId)) {
                return buildUserEntry(user, i + 1);
            }
        }

        return null;
    }

    private int countProcessedPredictions(User user) {
        return (int) user.getPredictions().stream()
                .filter(p -> Boolean.TRUE.equals(p.getProcessed()))
                .count();
    }

    private Double calculateAveragePoints(User user) {
        int processed = countProcessedPredictions(user);
        if (processed == 0) return 0.0;
        return (double) user.getTotalPoints() / processed;
    }

    // ==================== RANKING POR FASE ====================

    public RankingDTO getRankingByPhase(String phase, Long currentUserId) {
        Match.MatchPhase phaseEnum;
        try {
            phaseEnum = Match.MatchPhase.valueOf(phase.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Si mandan una fase que no existe, devolvemos un error 400 Bad Request
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fase proporcionada no es válida: " + phase);
        }
        List<Object[]> results = predictionRepository.findRankingByPhase(phaseEnum);
        return buildRankingFromProjection(results, currentUserId);
    }

    public RankingDTO getRankingByPhase(String phase, Long currentUserId, int page, int size) {
        Match.MatchPhase phaseEnum;
        try {
            phaseEnum = Match.MatchPhase.valueOf(phase.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Si mandan una fase que no existe, devolvemos un error 400 Bad Request
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fase proporcionada no es válida: " + phase);
        }
        Page<Object[]> resultsPage = predictionRepository.findRankingByPhasePaginated(
                phaseEnum,
                PageRequest.of(page, size)
        );

        List<RankingDTO.RankingEntry> entries = buildProjectionEntries(
                resultsPage.getContent(),
                (page * size) + 1
        );

        RankingDTO.RankingEntry currentUserEntry = null;
        if (currentUserId != null) {
            currentUserEntry = findEntryByUserId(entries, currentUserId);

            if (currentUserEntry == null) {
                List<Object[]> allResults = predictionRepository.findRankingByPhase(phaseEnum);
                currentUserEntry = findCurrentUserInProjection(allResults, currentUserId);
            }
        }

        return RankingDTO.builder()
                .entries(entries)
                .totalParticipants((int) resultsPage.getTotalElements())
                .currentUserRanking(currentUserEntry)
                .currentPage(resultsPage.getNumber())
                .totalPages(resultsPage.getTotalPages())
                .pageSize(resultsPage.getSize())
                .hasNext(resultsPage.hasNext())
                .hasPrevious(resultsPage.hasPrevious())
                .build();
    }

    // ==================== RANKING POR SEMANA ====================

    public RankingDTO getRankingByWeek(LocalDateTime weekStart, Long currentUserId) {
        LocalDateTime weekEnd = weekStart.plusDays(7);
        List<Object[]> results = predictionRepository.findRankingByWeek(weekStart, weekEnd);
        return buildRankingFromProjection(results, currentUserId);
    }

    public RankingDTO getRankingByWeek(LocalDateTime weekStart, Long currentUserId, int page, int size) {
        LocalDateTime weekEnd = weekStart.plusDays(7);

        Page<Object[]> resultsPage = predictionRepository.findRankingByWeekPaginated(
                weekStart,
                weekEnd,
                PageRequest.of(page, size)
        );

        List<RankingDTO.RankingEntry> entries = buildProjectionEntries(
                resultsPage.getContent(),
                (page * size) + 1
        );

        RankingDTO.RankingEntry currentUserEntry = null;
        if (currentUserId != null) {
            currentUserEntry = findEntryByUserId(entries, currentUserId);

            if (currentUserEntry == null) {
                List<Object[]> allResults = predictionRepository.findRankingByWeek(weekStart, weekEnd);
                currentUserEntry = findCurrentUserInProjection(allResults, currentUserId);
            }
        }

        return RankingDTO.builder()
                .entries(entries)
                .totalParticipants((int) resultsPage.getTotalElements())
                .currentUserRanking(currentUserEntry)
                .currentPage(resultsPage.getNumber())
                .totalPages(resultsPage.getTotalPages())
                .pageSize(resultsPage.getSize())
                .hasNext(resultsPage.hasNext())
                .hasPrevious(resultsPage.hasPrevious())
                .build();
    }

    // ==================== SEMANAS DISPONIBLES ====================

    public List<WeekOption> getAvailableWeeks() {
        List<java.sql.Date> dates = predictionRepository.findFinishedMatchDates();

        return dates.stream()
                .map(date -> {
                    LocalDateTime day = date.toLocalDate().atStartOfDay();
                    return day.minusDays(day.getDayOfWeek().getValue() - 1);
                })
                .distinct()
                .sorted()
                .map(monday -> WeekOption.builder()
                        .weekStart(monday)
                        .weekEnd(monday.plusDays(7))
                        .label(formatWeekLabel(monday))
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== HELPERS PROJECTION ====================

    private RankingDTO buildRankingFromProjection(List<Object[]> results, Long currentUserId) {
        List<RankingDTO.RankingEntry> entries = buildProjectionEntries(results, 1);
        RankingDTO.RankingEntry currentUserEntry = findCurrentUserInProjection(results, currentUserId);

        return RankingDTO.builder()
                .entries(entries)
                .totalParticipants(entries.size())
                .currentUserRanking(currentUserEntry)
                .currentPage(0)
                .totalPages(1)
                .pageSize(entries.size())
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    private List<RankingDTO.RankingEntry> buildProjectionEntries(List<Object[]> results, int startPosition) {
        List<RankingDTO.RankingEntry> entries = new ArrayList<>();
        int position = startPosition;

        for (Object[] row : results) {
            RankingDTO.RankingEntry entry = buildProjectionEntry(row, position);
            if (entry != null) {
                entries.add(entry);
                position++;
            }
        }

        return entries;
    }

    private RankingDTO.RankingEntry findCurrentUserInProjection(List<Object[]> results, Long currentUserId) {
        if (currentUserId == null) return null;

        int position = 1;
        for (Object[] row : results) {
            Long userId = numberToLong(row[0]);

            if (currentUserId.equals(userId)) {
                return buildProjectionEntry(row, position);
            }
            position++;
        }

        return null;
    }

    private RankingDTO.RankingEntry buildProjectionEntry(Object[] row, int position) {
        Long userId = numberToLong(row[0]);
        int points = numberToInt(row[1]);
        int exactResults = numberToInt(row[2]);
        int correctWinners = numberToInt(row[3]);
        int predictionsCount = row.length > 4 ? numberToInt(row[4]) : 0;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        double averagePoints = predictionsCount == 0 ? 0.0 : (double) points / predictionsCount;

        return RankingDTO.RankingEntry.builder()
                .position(position)
                .userId(user.getId())
                .userName(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .totalPoints(points)
                .exactResults(exactResults)
                .correctWinners(correctWinners)
                .predictionsCount(predictionsCount)
                .matchesPlayed(predictionsCount)
                .averagePoints(averagePoints)
                .positionChange(0)
                .build();
    }

    private int numberToInt(Object value) {
        if (value == null) return 0;
        return ((Number) value).intValue();
    }

    private Long numberToLong(Object value) {
        if (value == null) return 0L;
        return ((Number) value).longValue();
    }

    private String formatWeekLabel(LocalDateTime monday) {
        LocalDateTime sunday = monday.plusDays(6);
        return String.format("%d/%d - %d/%d",
                monday.getDayOfMonth(), monday.getMonthValue(),
                sunday.getDayOfMonth(), sunday.getMonthValue());
    }
}