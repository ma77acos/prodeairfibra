package com.k2radio.prode.service;

import com.k2radio.prode.dto.admin.GroupStandingDTO;
import com.k2radio.prode.dto.group.GroupDTO;
import com.k2radio.prode.entity.Match;
import com.k2radio.prode.entity.Team;
import com.k2radio.prode.repository.MatchRepository;
import com.k2radio.prode.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    public List<GroupDTO> getAllGroups() {
        // Obtener todos los grupos únicos
        List<String> groupNames = teamRepository.findDistinctGroupNames();

        return groupNames.stream()
                .sorted()
                .map(this::getGroupStandings)
                .collect(Collectors.toList());
    }

    public GroupDTO getGroupStandings(String groupName) {
        // Obtener equipos del grupo
        List<Team> teams = teamRepository.findByGroupNameOrderByNameAsc(groupName);

        // Obtener partidos del grupo
        List<Match> matches = matchRepository.findByGroupNameOrderByMatchDateTimeAsc(groupName);

        // Calcular standings
        Map<Long, GroupStandingDTO> standingsMap = new HashMap<>();

        // Inicializar standings para cada equipo
        for (Team team : teams) {
            standingsMap.put(team.getId(), GroupStandingDTO.builder()
                    .teamId(team.getId())
                    .teamName(team.getName())
                    .teamCode(team.getCode())
                    .flagUrl(team.getFlagUrl())
                    .played(0)
                    .won(0)
                    .drawn(0)
                    .lost(0)
                    .goalsFor(0)
                    .goalsAgainst(0)
                    .goalDiff(0)
                    .points(0)
                    .qualified(false)
                    .build());
        }

        int matchesPlayed = 0;

        // Procesar partidos finalizados
        for (Match match : matches) {
            if (match.getStatus() == Match.MatchStatus.FINISHED &&
                    match.getHomeGoals() != null && match.getAwayGoals() != null) {

                matchesPlayed++;

                Long homeId = match.getHomeTeam().getId();
                Long awayId = match.getAwayTeam().getId();
                int homeGoals = match.getHomeGoals();
                int awayGoals = match.getAwayGoals();

                GroupStandingDTO homeStanding = standingsMap.get(homeId);
                GroupStandingDTO awayStanding = standingsMap.get(awayId);

                if (homeStanding != null && awayStanding != null) {
                    // Actualizar partidos jugados
                    homeStanding.setPlayed(homeStanding.getPlayed() + 1);
                    awayStanding.setPlayed(awayStanding.getPlayed() + 1);

                    // Actualizar goles
                    homeStanding.setGoalsFor(homeStanding.getGoalsFor() + homeGoals);
                    homeStanding.setGoalsAgainst(homeStanding.getGoalsAgainst() + awayGoals);
                    awayStanding.setGoalsFor(awayStanding.getGoalsFor() + awayGoals);
                    awayStanding.setGoalsAgainst(awayStanding.getGoalsAgainst() + homeGoals);

                    // Actualizar resultados y puntos
                    if (homeGoals > awayGoals) {
                        // Victoria local
                        homeStanding.setWon(homeStanding.getWon() + 1);
                        homeStanding.setPoints(homeStanding.getPoints() + 3);
                        awayStanding.setLost(awayStanding.getLost() + 1);
                    } else if (homeGoals < awayGoals) {
                        // Victoria visitante
                        awayStanding.setWon(awayStanding.getWon() + 1);
                        awayStanding.setPoints(awayStanding.getPoints() + 3);
                        homeStanding.setLost(homeStanding.getLost() + 1);
                    } else {
                        // Empate
                        homeStanding.setDrawn(homeStanding.getDrawn() + 1);
                        homeStanding.setPoints(homeStanding.getPoints() + 1);
                        awayStanding.setDrawn(awayStanding.getDrawn() + 1);
                        awayStanding.setPoints(awayStanding.getPoints() + 1);
                    }
                }
            }
        }

        // Calcular diferencia de goles y ordenar
        List<GroupStandingDTO> standings = standingsMap.values().stream()
                .peek(s -> s.setGoalDiff(s.getGoalsFor() - s.getGoalsAgainst()))
                .sorted(this::compareStandings)
                .collect(Collectors.toList());

        // Asignar posiciones y marcar clasificados
        for (int i = 0; i < standings.size(); i++) {
            standings.get(i).setPosition(i + 1);
            // Los primeros 2 clasifican (ajustar según formato)
            standings.get(i).setQualified(i < 2);
        }

        // Calcular total de partidos del grupo (n equipos = n*(n-1)/2 partidos)
        int totalMatches = teams.size() * (teams.size() - 1) / 2;
        boolean completed = matchesPlayed == totalMatches;

        return GroupDTO.builder()
                .groupName(groupName)
                .standings(standings)
                .matchesPlayed(matchesPlayed)
                .totalMatches(totalMatches)
                .completed(completed)
                .build();
    }

    /**
     * Comparador para ordenar standings
     * Criterios: 1) Puntos, 2) Diferencia de goles, 3) Goles a favor, 4) Nombre
     */
    private int compareStandings(GroupStandingDTO a, GroupStandingDTO b) {
        // 1. Por puntos (descendente)
        int pointsDiff = b.getPoints().compareTo(a.getPoints());
        if (pointsDiff != 0) return pointsDiff;

        // 2. Por diferencia de goles (descendente)
        int goalDiffDiff = b.getGoalDiff().compareTo(a.getGoalDiff());
        if (goalDiffDiff != 0) return goalDiffDiff;

        // 3. Por goles a favor (descendente)
        int goalsForDiff = b.getGoalsFor().compareTo(a.getGoalsFor());
        if (goalsForDiff != 0) return goalsForDiff;

        // 4. Por nombre (alfabético)
        return a.getTeamName().compareTo(b.getTeamName());
    }

    /**
     * Obtener los clasificados de un grupo (primero y segundo)
     */
    public List<Team> getQualifiedTeams(String groupName) {
        GroupDTO group = getGroupStandings(groupName);

        if (!group.getCompleted()) {
            return Collections.emptyList();
        }

        return group.getStandings().stream()
                .filter(GroupStandingDTO::getQualified)
                .map(s -> teamRepository.findById(s.getTeamId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}