package com.k2radio.prode.service;

import com.k2radio.prode.dto.admin.GroupStandingDTO;
import com.k2radio.prode.entity.Match;
import com.k2radio.prode.entity.Team;
import com.k2radio.prode.exception.NotFoundException;
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
public class GroupStandingsService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    /**
     * Calcula la tabla de posiciones de un grupo
     */
    public List<GroupStandingDTO> getGroupStandings(String groupName) {
        List<Team> teams = teamRepository.findByGroupName(groupName);
        List<Match> matches = matchRepository.findByGroupNameAndStatus(groupName, Match.MatchStatus.FINISHED);

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
                    .build());
        }

        // Procesar cada partido finalizado
        for (Match match : matches) {
            Long homeId = match.getHomeTeam().getId();
            Long awayId = match.getAwayTeam().getId();
            int homeGoals = match.getHomeGoals();
            int awayGoals = match.getAwayGoals();

            GroupStandingDTO homeStanding = standingsMap.get(homeId);
            GroupStandingDTO awayStanding = standingsMap.get(awayId);

            if (homeStanding == null || awayStanding == null) continue;

            // Actualizar partidos jugados
            homeStanding.setPlayed(homeStanding.getPlayed() + 1);
            awayStanding.setPlayed(awayStanding.getPlayed() + 1);

            // Actualizar goles
            homeStanding.setGoalsFor(homeStanding.getGoalsFor() + homeGoals);
            homeStanding.setGoalsAgainst(homeStanding.getGoalsAgainst() + awayGoals);
            awayStanding.setGoalsFor(awayStanding.getGoalsFor() + awayGoals);
            awayStanding.setGoalsAgainst(awayStanding.getGoalsAgainst() + homeGoals);

            // Actualizar victorias/empates/derrotas y puntos
            if (homeGoals > awayGoals) {
                homeStanding.setWon(homeStanding.getWon() + 1);
                homeStanding.setPoints(homeStanding.getPoints() + 3);
                awayStanding.setLost(awayStanding.getLost() + 1);
            } else if (homeGoals < awayGoals) {
                awayStanding.setWon(awayStanding.getWon() + 1);
                awayStanding.setPoints(awayStanding.getPoints() + 3);
                homeStanding.setLost(homeStanding.getLost() + 1);
            } else {
                homeStanding.setDrawn(homeStanding.getDrawn() + 1);
                awayStanding.setDrawn(awayStanding.getDrawn() + 1);
                homeStanding.setPoints(homeStanding.getPoints() + 1);
                awayStanding.setPoints(awayStanding.getPoints() + 1);
            }
        }

        // Calcular diferencia de goles y ordenar
        List<GroupStandingDTO> standings = new ArrayList<>(standingsMap.values());
        for (GroupStandingDTO s : standings) {
            s.setGoalDiff(s.getGoalsFor() - s.getGoalsAgainst());
        }

        // Ordenar: Puntos > Diferencia de gol > Goles a favor
        standings.sort((a, b) -> {
            if (!a.getPoints().equals(b.getPoints())) {
                return b.getPoints() - a.getPoints();
            }
            if (!a.getGoalDiff().equals(b.getGoalDiff())) {
                return b.getGoalDiff() - a.getGoalDiff();
            }
            return b.getGoalsFor() - a.getGoalsFor();
        });

        // Asignar posición
        for (int i = 0; i < standings.size(); i++) {
            standings.get(i).setPosition(i + 1);
        }

        return standings;
    }

    /**
     * Obtiene las tablas de todos los grupos
     */
    public Map<String, List<GroupStandingDTO>> getAllGroupStandings() {
        Map<String, List<GroupStandingDTO>> allStandings = new LinkedHashMap<>();

        for (String group : List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L")) {
            allStandings.put(group, getGroupStandings(group));
        }

        return allStandings;
    }

    /**
     * Verifica si un grupo está completo (todos los partidos jugados)
     */
    public boolean isGroupComplete(String groupName) {
        long totalMatches = matchRepository.countByGroupName(groupName);
        long finishedMatches = matchRepository.countByGroupNameAndStatus(groupName, Match.MatchStatus.FINISHED);
        return totalMatches == finishedMatches && totalMatches == 6; // 6 partidos por grupo
    }

    /**
     * Obtiene los clasificados de un grupo (1ro y 2do)
     */
    public List<Team> getGroupQualifiers(String groupName) {
        List<GroupStandingDTO> standings = getGroupStandings(groupName);

        if (standings.size() < 2) {
            return Collections.emptyList();
        }

        List<Team> qualifiers = new ArrayList<>();
        qualifiers.add(teamRepository.findById(standings.get(0).getTeamId()).orElse(null));
        qualifiers.add(teamRepository.findById(standings.get(1).getTeamId()).orElse(null));

        return qualifiers.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Obtiene el equipo en una posición específica del grupo (1, 2 o 3)
     */
    public Team getTeamByPosition(String groupName, int position) {
        List<GroupStandingDTO> standings = getGroupStandings(groupName);

        if (standings.size() < position) {
            throw new NotFoundException(
                    "No se pudo obtener la posición " + position + " del grupo " + groupName
            );
        }

        return teamRepository.findById(standings.get(position - 1).getTeamId())
                .orElseThrow(() -> new NotFoundException(
                        "Equipo no encontrado para posición " + position + " del grupo " + groupName
                ));
    }
}