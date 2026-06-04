package com.k2radio.prode.service;

import com.k2radio.prode.dto.admin.GroupStandingDTO;
import com.k2radio.prode.entity.Match;
import com.k2radio.prode.entity.Team;
import com.k2radio.prode.exception.BadRequestException;
import com.k2radio.prode.exception.NotFoundException;
import com.k2radio.prode.repository.MatchRepository;
import com.k2radio.prode.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnockoutService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final GroupStandingsService groupStandingsService;


    /**
     * Genera los cruces de 32avos basado en los clasificados de grupos
     *
     * Fixture oficial FIFA Mundial 2026:
     * P73: 2A vs 2B
     * P74: 1E vs 3ABCDF
     * P75: 1F vs 2C
     * P76: 1C vs 2F
     * P77: 1I vs 3CDFGH
     * P78: 2E vs 2I
     * P79: 1A vs 3CEFHI
     * P80: 1L vs 3EHIJK
     * P81: 1D vs 3BEFIJ
     * P82: 1G vs 3AEHIJ
     * P83: 2K vs 2L
     * P84: 1H vs 2J
     * P85: 1B vs 3EFGIJ
     * P86: 1J vs 2H
     * P87: 1K vs 3DEIJL
     * P88: 2D vs 2G
     */
    @Transactional
    public void generateRoundOf32() {
        log.info("Generando cruces de 32avos de final - Fixture oficial FIFA 2026");

        // 1. Verificar que todos los grupos estén completos
        List<String> groups = List.of("A","B","C","D","E","F","G","H","I","J","K","L");
        for (String group : groups) {
            if (!groupStandingsService.isGroupComplete(group)) {
                throw new BadRequestException("El grupo " + group + " aún no ha terminado");
            }
        }

        // 2. Obtener clasificados de cada grupo usando getTeamByPosition
        Team first_A  = groupStandingsService.getTeamByPosition("A", 1);
        Team second_A = groupStandingsService.getTeamByPosition("A", 2);
        Team third_A  = groupStandingsService.getTeamByPosition("A", 3);

        Team first_B  = groupStandingsService.getTeamByPosition("B", 1);
        Team second_B = groupStandingsService.getTeamByPosition("B", 2);
        Team third_B  = groupStandingsService.getTeamByPosition("B", 3);

        Team first_C  = groupStandingsService.getTeamByPosition("C", 1);
        Team second_C = groupStandingsService.getTeamByPosition("C", 2);
        Team third_C  = groupStandingsService.getTeamByPosition("C", 3);

        Team first_D  = groupStandingsService.getTeamByPosition("D", 1);
        Team second_D = groupStandingsService.getTeamByPosition("D", 2);
        Team third_D  = groupStandingsService.getTeamByPosition("D", 3);

        Team first_E  = groupStandingsService.getTeamByPosition("E", 1);
        Team second_E = groupStandingsService.getTeamByPosition("E", 2);
        Team third_E  = groupStandingsService.getTeamByPosition("E", 3);

        Team first_F  = groupStandingsService.getTeamByPosition("F", 1);
        Team second_F = groupStandingsService.getTeamByPosition("F", 2);
        Team third_F  = groupStandingsService.getTeamByPosition("F", 3);

        Team first_G  = groupStandingsService.getTeamByPosition("G", 1);
        Team second_G = groupStandingsService.getTeamByPosition("G", 2);
        Team third_G  = groupStandingsService.getTeamByPosition("G", 3);

        Team first_H  = groupStandingsService.getTeamByPosition("H", 1);
        Team second_H = groupStandingsService.getTeamByPosition("H", 2);
        Team third_H  = groupStandingsService.getTeamByPosition("H", 3);

        Team first_I  = groupStandingsService.getTeamByPosition("I", 1);
        Team second_I = groupStandingsService.getTeamByPosition("I", 2);
        Team third_I  = groupStandingsService.getTeamByPosition("I", 3);

        Team first_J  = groupStandingsService.getTeamByPosition("J", 1);
        Team second_J = groupStandingsService.getTeamByPosition("J", 2);
        Team third_J  = groupStandingsService.getTeamByPosition("J", 3);

        Team first_K  = groupStandingsService.getTeamByPosition("K", 1);
        Team second_K = groupStandingsService.getTeamByPosition("K", 2);
        Team third_K  = groupStandingsService.getTeamByPosition("K", 3);

        Team first_L  = groupStandingsService.getTeamByPosition("L", 1);
        Team second_L = groupStandingsService.getTeamByPosition("L", 2);
        Team third_L  = groupStandingsService.getTeamByPosition("L", 3);

        // 3. Resolver mejores terceros para cada cruce
        // Usamos un Set para evitar que el mismo equipo aparezca en dos cruces
        Set<Long> assignedThirdIds = new HashSet<>();

        // P74: 1E vs mejor 3° entre grupos A, B, C, D, F
        Team third_74 = getBestThirdFromCandidates(
                List.of(third_A, third_B, third_C, third_D, third_F),
                assignedThirdIds
        );

        // P77: 1I vs mejor 3° entre grupos C, D, F, G, H
        Team third_77 = getBestThirdFromCandidates(
                List.of(third_C, third_D, third_F, third_G, third_H),
                assignedThirdIds
        );

        // P79: 1A vs mejor 3° entre grupos C, E, F, H, I
        Team third_79 = getBestThirdFromCandidates(
                List.of(third_C, third_E, third_F, third_H, third_I),
                assignedThirdIds
        );

        // P80: 1L vs mejor 3° entre grupos E, H, I, J, K
        Team third_80 = getBestThirdFromCandidates(
                List.of(third_E, third_H, third_I, third_J, third_K),
                assignedThirdIds
        );

        // P81: 1D vs mejor 3° entre grupos B, E, F, I, J
        Team third_81 = getBestThirdFromCandidates(
                List.of(third_B, third_E, third_F, third_I, third_J),
                assignedThirdIds
        );

        // P82: 1G vs mejor 3° entre grupos A, E, H, I, J
        Team third_82 = getBestThirdFromCandidates(
                List.of(third_A, third_E, third_H, third_I, third_J),
                assignedThirdIds
        );

        // P85: 1B vs mejor 3° entre grupos E, F, G, I, J
        Team third_85 = getBestThirdFromCandidates(
                List.of(third_E, third_F, third_G, third_I, third_J),
                assignedThirdIds
        );

        // P87: 1K vs mejor 3° entre grupos D, E, I, J, L
        Team third_87 = getBestThirdFromCandidates(
                List.of(third_D, third_E, third_I, third_J, third_L),
                assignedThirdIds
        );

        // 4. Obtener partidos de 32avos ordenados por match_number
        List<Match> roundOf32Matches = matchRepository
                .findByPhaseOrderByMatchNumberAsc(Match.MatchPhase.ROUND_OF_32);

        if (roundOf32Matches.size() < 16) {
            throw new NotFoundException(
                    "Faltan partidos de 32avos. Se necesitan 16, hay: " + roundOf32Matches.size()
            );
        }

        // 5. Asignar cruces según fixture oficial FIFA
        assignMatch(roundOf32Matches, 73, second_A,  second_B);
        assignMatch(roundOf32Matches, 74, first_E,   third_74);
        assignMatch(roundOf32Matches, 75, first_F,   second_C);
        assignMatch(roundOf32Matches, 76, first_C,   second_F);
        assignMatch(roundOf32Matches, 77, first_I,   third_77);
        assignMatch(roundOf32Matches, 78, second_E,  second_I);
        assignMatch(roundOf32Matches, 79, first_A,   third_79);
        assignMatch(roundOf32Matches, 80, first_L,   third_80);
        assignMatch(roundOf32Matches, 81, first_D,   third_81);
        assignMatch(roundOf32Matches, 82, first_G,   third_82);
        assignMatch(roundOf32Matches, 83, second_K,  second_L);
        assignMatch(roundOf32Matches, 84, first_H,   second_J);
        assignMatch(roundOf32Matches, 85, first_B,   third_85);
        assignMatch(roundOf32Matches, 86, first_J,   second_H);
        assignMatch(roundOf32Matches, 87, first_K,   third_87);
        assignMatch(roundOf32Matches, 88, second_D,  second_G);

        log.info("════════════════════════════════════════════════");
        log.info("✅ 16 cruces de 32avos generados según fixture FIFA 2026");
        log.info("  P73: {} vs {}", second_A.getName(),  second_B.getName());
        log.info("  P74: {} vs {}", first_E.getName(),   third_74.getName());
        log.info("  P75: {} vs {}", first_F.getName(),   second_C.getName());
        log.info("  P76: {} vs {}", first_C.getName(),   second_F.getName());
        log.info("  P77: {} vs {}", first_I.getName(),   third_77.getName());
        log.info("  P78: {} vs {}", second_E.getName(),  second_I.getName());
        log.info("  P79: {} vs {}", first_A.getName(),   third_79.getName());
        log.info("  P80: {} vs {}", first_L.getName(),   third_80.getName());
        log.info("  P81: {} vs {}", first_D.getName(),   third_81.getName());
        log.info("  P82: {} vs {}", first_G.getName(),   third_82.getName());
        log.info("  P83: {} vs {}", second_K.getName(),  second_L.getName());
        log.info("  P84: {} vs {}", first_H.getName(),   second_J.getName());
        log.info("  P85: {} vs {}", first_B.getName(),   third_85.getName());
        log.info("  P86: {} vs {}", first_J.getName(),   second_H.getName());
        log.info("  P87: {} vs {}", first_K.getName(),   third_87.getName());
        log.info("  P88: {} vs {}", second_D.getName(),  second_G.getName());
        log.info("════════════════════════════════════════════════");
    }

    /**
     * Obtiene el mejor tercero disponible entre los candidatos recibidos.
     * Usa assignedThirdIds para evitar asignar el mismo equipo dos veces.
     *
     * Criterios FIFA:
     * 1. Mayor cantidad de puntos
     * 2. Mayor diferencia de goles
     * 3. Mayor cantidad de goles a favor
     */
    private Team getBestThirdFromCandidates(List<Team> candidates, Set<Long> assignedThirdIds) {
        return candidates.stream()
                .filter(team -> !assignedThirdIds.contains(team.getId()))
                .map(team -> {
                    // Obtener el standing del equipo para comparar
                    List<GroupStandingDTO> standings = groupStandingsService
                            .getGroupStandings(team.getGroupName());

                    return standings.stream()
                            .filter(s -> s.getTeamId().equals(team.getId()))
                            .findFirst()
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .max(Comparator
                        .comparingInt(GroupStandingDTO::getPoints)
                        .thenComparingInt(GroupStandingDTO::getGoalDiff)
                        .thenComparingInt(GroupStandingDTO::getGoalsFor))
                .map(standing -> {
                    assignedThirdIds.add(standing.getTeamId());
                    return teamRepository.findById(standing.getTeamId())
                            .orElseThrow(() -> new NotFoundException(
                                    "Equipo no encontrado: " + standing.getTeamId()
                            ));
                })
                .orElseThrow(() -> new NotFoundException(
                        "No se pudo determinar el mejor tercero entre los candidatos"
                ));
    }

    /**
     * Asigna home y away team al partido con el match_number indicado
     */
    private void assignMatch(List<Match> matches, int matchNumber, Team home, Team away) {
        matches.stream()
                .filter(m -> m.getMatchNumber() == matchNumber)
                .findFirst()
                .ifPresentOrElse(
                        match -> {
                            match.setHomeTeam(home);
                            match.setAwayTeam(away);
                            matchRepository.save(match);
                        },
                        () -> { throw new NotFoundException(
                                "No se encontró el partido número: " + matchNumber
                        );}
                );
    }

    /**
     * Avanza al ganador de un partido a la siguiente fase
     * En semifinales, también avanza al perdedor al partido por el 3er puesto
     */
    @Transactional
    public void advanceWinner(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partido no encontrado"));

        if (match.getStatus() != Match.MatchStatus.FINISHED) {
            throw new BadRequestException("El partido aún no ha finalizado");
        }

        if (match.getHomeGoals() == null || match.getAwayGoals() == null) {
            throw new BadRequestException("El partido no tiene resultado");
        }

        Team winner;
        Team loser;

        if (match.getHomeGoals() > match.getAwayGoals()) {
            // Ganó local en 90 minutos
            winner = match.getHomeTeam();
            loser  = match.getAwayTeam();

        } else if (match.getAwayGoals() > match.getHomeGoals()) {
            // Ganó visitante en 90 minutos
            winner = match.getAwayTeam();
            loser  = match.getHomeTeam();

        } else {
            // Empate en 90 min → se resuelve por penales
            // Verificar que tenga definido el ganador por penales
            if (match.getPenaltyWinner() == null) {
                throw new BadRequestException(
                        "El partido terminó empatado. Debe definirse el ganador por penales antes de avanzar."
                );
            }

            winner = match.getPenaltyWinner();
            loser  = winner.getId().equals(match.getHomeTeam().getId())
                    ? match.getAwayTeam()
                    : match.getHomeTeam();
        }

        // Avanzar ganador a la siguiente fase
        Match nextMatch = findNextStageMatch(match);
        if (nextMatch != null) {
            if (isHomeSlot(match.getMatchNumber())) {
                nextMatch.setHomeTeam(winner);
            } else {
                nextMatch.setAwayTeam(winner);
            }
            matchRepository.save(nextMatch);
            log.info("✅ {} avanza a {} (partido #{})",
                    winner.getName(), nextMatch.getPhase(), nextMatch.getMatchNumber());
        }

        // Si es semifinal, el perdedor va al 3er puesto
        if (match.getPhase() == Match.MatchPhase.SEMI_FINALS) {
            advanceLoserToThirdPlace(match, loser);
        }
    }

    @Transactional
    public void setPenaltyWinner(Long matchId, Long penaltyWinnerTeamId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partido no encontrado"));

        if (match.getStatus() != Match.MatchStatus.FINISHED) {
            throw new BadRequestException("El partido aún no ha finalizado");
        }

        if (match.getHomeGoals() == null || match.getAwayGoals() == null) {
            throw new BadRequestException("El partido no tiene resultado cargado");
        }

        if (!match.getHomeGoals().equals(match.getAwayGoals())) {
            throw new BadRequestException(
                    "El partido no terminó empatado, no aplica ganador por penales"
            );
        }

        Long homeId = match.getHomeTeam().getId();
        Long awayId = match.getAwayTeam().getId();

        if (!penaltyWinnerTeamId.equals(homeId) && !penaltyWinnerTeamId.equals(awayId)) {
            throw new BadRequestException(
                    "El equipo indicado no participa en este partido"
            );
        }

        Team penaltyWinner = teamRepository.findById(penaltyWinnerTeamId)
                .orElseThrow(() -> new NotFoundException("Equipo no encontrado"));

        match.setPenaltyWinner(penaltyWinner);
        matchRepository.save(match);

        log.info("✅ Ganador por penales del partido #{}: {}",
                match.getMatchNumber(), penaltyWinner.getName());

        // ✅ Ahora sí avanzar automáticamente
        if (match.getPhase() != Match.MatchPhase.THIRD_PLACE &&
                match.getPhase() != Match.MatchPhase.FINAL) {
            advanceWinner(matchId);
            log.info("✅ Ganador por penales avanzado a la siguiente fase");
        }
    }

    /**
     * Avanza al perdedor de una semifinal al partido por el 3er puesto
     */
    private void advanceLoserToThirdPlace(Match semiMatch, Team loser) {
        // Usar el método que ya existe
        List<Match> thirdPlaceMatches = matchRepository.findByPhaseOrderByMatchNumberAsc(
                Match.MatchPhase.THIRD_PLACE
        );

        if (thirdPlaceMatches.isEmpty()) {
            log.warn("No se encontró partido por el 3er puesto");
            return;
        }

        Match thirdPlaceMatch = thirdPlaceMatches.get(0);

        if (isFirstSemifinal(semiMatch)) {
            thirdPlaceMatch.setHomeTeam(loser);
            log.info("✅ {} (perdedor SF1) va al 3er puesto como LOCAL", loser.getName());
        } else {
            thirdPlaceMatch.setAwayTeam(loser);
            log.info("✅ {} (perdedor SF2) va al 3er puesto como VISITANTE", loser.getName());
        }

        matchRepository.save(thirdPlaceMatch);
    }

    /**
     * Determina si es la primera o segunda semifinal
     */
    private boolean isFirstSemifinal(Match match) {
        // Opción 1: Por número de partido
        List<Match> semis = matchRepository.findByPhaseOrderByMatchNumberAsc(Match.MatchPhase.SEMI_FINALS);
        if (!semis.isEmpty()) {
            return match.getMatchNumber() == semis.get(0).getMatchNumber();
        }

        // Opción 2: Por fecha/hora (la primera en jugarse)
        return true; // fallback
    }

    /**
     * Busca el siguiente partido donde debe avanzar el ganador
     */
    private Match findNextStageMatch(Match currentMatch) {
        Match.MatchPhase nextPhase = getNextPhase(currentMatch.getPhase());

        if (nextPhase == null) {
            return null; // Es la final, no hay siguiente
        }

        List<Match> nextPhaseMatches = matchRepository.findByPhaseOrderByMatchNumberAsc(nextPhase);

        // Calcular el índice del próximo partido basado en el número de partido actual
        int nextMatchIndex = (currentMatch.getMatchNumber() - getPhaseStartNumber(currentMatch.getPhase())) / 2;

        if (nextMatchIndex < nextPhaseMatches.size()) {
            return nextPhaseMatches.get(nextMatchIndex);
        }

        return null;
    }

    private Match.MatchPhase getNextPhase(Match.MatchPhase currentPhase) {
        return switch (currentPhase) {
            case ROUND_OF_32 -> Match.MatchPhase.ROUND_OF_16;
            case ROUND_OF_16 -> Match.MatchPhase.QUARTER_FINALS;
            case QUARTER_FINALS -> Match.MatchPhase.SEMI_FINALS;
            case SEMI_FINALS -> Match.MatchPhase.FINAL; // o THIRD_PLACE
            default -> null;
        };
    }

    private int getPhaseStartNumber(Match.MatchPhase phase) {
        return switch (phase) {
            case GROUP_STAGE -> 1;
            case ROUND_OF_32 -> 73;
            case ROUND_OF_16 -> 89;
            case QUARTER_FINALS -> 97;
            case SEMI_FINALS -> 101;
            case THIRD_PLACE -> 103;
            case FINAL -> 104;
        };
    }

    private boolean isHomeSlot(int matchNumber) {
        // Los partidos impares van como local en la siguiente fase
        return matchNumber % 2 == 1;
    }

    private Team getTeamByPosition(String key) {
        // key = "1A" significa 1ro del grupo A
        int position = Character.getNumericValue(key.charAt(0));
        String group = key.substring(1);

        List<Team> qualifiers = groupStandingsService.getGroupQualifiers(group);

        if (position <= qualifiers.size()) {
            return qualifiers.get(position - 1);
        }

        return null;
    }
}