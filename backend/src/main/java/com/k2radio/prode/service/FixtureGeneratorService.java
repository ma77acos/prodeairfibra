package com.k2radio.prode.service;

import com.k2radio.prode.entity.Match;
import com.k2radio.prode.entity.Team;
import com.k2radio.prode.exception.ConflictException;
import com.k2radio.prode.exception.NotFoundException;
import com.k2radio.prode.repository.MatchRepository;
import com.k2radio.prode.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FixtureGeneratorService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;


    private record KnockoutMatchConfig(
            int matchNumber,
            String dateTime,
            String stadium,
            String city,
            String country,
            String matchDescription
    ) {}

    private Match createKnockoutMatch(
            int matchNumber,
            Team homeTeam,
            Team awayTeam,
            LocalDateTime dateTime,
            Match.MatchPhase phase,
            String stadium,
            String city,
            String country,
            String matchDescription) {

        Match match = new Match();
        match.setMatchNumber(matchNumber);
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        match.setMatchDateTime(dateTime);
        match.setPhase(phase);
        match.setStatus(Match.MatchStatus.SCHEDULED);
        match.setStadium(stadium);
        match.setCity(city);
        match.setCountry(country);
        //match.setMatchDescription(matchDescription); // "1A vs 2B", "W73 vs W75", etc.
        return match;
    }

    /**
     * Genera el fixture completo del Mundial 2026 (48 equipos)
     *
     * Estructura:
     * - Fase de grupos: 72 partidos (12 grupos × 6 partidos)
     * - 32avos de final: 16 partidos
     * - Octavos de final: 8 partidos
     * - Cuartos de final: 4 partidos
     * - Semifinales: 2 partidos
     * - Tercer puesto: 1 partido
     * - Final: 1 partido
     * - TOTAL: 104 partidos
     */
    @Transactional
    public int generateWorldCupFixture() {
        log.info("Iniciando generación del fixture del Mundial 2026");

        Team tbd = getOrCreateTBDTeam();

        Map<String, List<Team>> teamsByGroup = teamRepository.findAll().stream()
                .filter(team -> team.getGroupName() != null)
                .collect(Collectors.groupingBy(Team::getGroupName));

        if (teamsByGroup.size() != 12) {
            throw new IllegalStateException(
                    "Se requieren exactamente 12 grupos (A-L). Actualmente hay: " + teamsByGroup.size()
            );
        }

        for (String group : List.of("A","B","C","D","E","F","G","H","I","J","K","L")) {
            List<Team> teams = teamsByGroup.get(group);
            if (teams == null || teams.size() != 4) {
                throw new IllegalStateException(
                        "El grupo " + group + " debe tener exactamente 4 equipos. Tiene: " +
                                (teams == null ? 0 : teams.size())
                );
            }
        }

        List<Match> matches = new ArrayList<>();

        log.info("Fase de grupos: 72 partidos se cargan via SQL scripts");

        // ==================== 32AVOS DE FINAL (16 partidos) ====================
        // Numeración y cruces 100% según fixture oficial FIFA
        // 28 jun: #73
        // 29 jun: #74, #75, #76
        // 30 jun: #77, #78, #79
        // 01 jul: #80, #81, #82
        // 02 jul: #83, #84
        // 03 jul: #85, #86, #87, #88

        log.info("Generando 32avos de final...");

        List<KnockoutMatchConfig> roundOf32Config = List.of(

                // 28 jun
                new KnockoutMatchConfig(73,  "2026-06-28 16:00", "Estadio Los Angeles",                  "Los Ángeles",   "USA",    "2A vs 2B"),

                // 29 jun
                new KnockoutMatchConfig(74,  "2026-06-29 17:30", "Estadio Boston",                       "Boston",        "USA",    "1E vs 3ABCDF"),
                new KnockoutMatchConfig(75,  "2026-06-29 22:00", "Estadio Monterrey",                    "Monterrey",     "México", "1F vs 2C"),
                new KnockoutMatchConfig(76,  "2026-06-29 14:00", "Estadio Houston",                      "Houston",       "USA",    "1C vs 2F"),

                // 30 jun
                new KnockoutMatchConfig(77,  "2026-06-30 18:00", "Estadio Nueva York/Nueva Jersey",      "East Rutherford","USA",   "1I vs 3CDFGH"),
                new KnockoutMatchConfig(78,  "2026-06-30 14:00", "Estadio Dallas",                       "Dallas",        "USA",    "2E vs 2I"),
                new KnockoutMatchConfig(79,  "2026-06-30 22:00", "Estadio Ciudad de México",             "Ciudad de México","México","1A vs 3CEFHI"),

                // 01 jul
                new KnockoutMatchConfig(80,  "2026-07-01 13:00", "Estadio Atlanta",                      "Atlanta",       "USA",    "1L vs 3EHIJK"),
                new KnockoutMatchConfig(81,  "2026-07-01 21:00", "Estadio de la Bahía de San Francisco", "San Francisco", "USA",    "1D vs 3BEFIJ"),
                new KnockoutMatchConfig(82,  "2026-07-01 17:00", "Estadio de Seattle",                   "Seattle",       "USA",    "1G vs 3AEHIJ"),

                // 02 jul
                new KnockoutMatchConfig(83,  "2026-07-02 20:00", "Estadio de Toronto",                   "Toronto",       "Canadá", "2K vs 2L"),
                new KnockoutMatchConfig(84,  "2026-07-02 16:00", "Estadio Los Angeles",                  "Los Ángeles",   "USA",    "1H vs 2J"),

                // 03 jul
                new KnockoutMatchConfig(85,  "2026-07-03 00:00", "Estadio BC Place Vancouver",           "Vancouver",     "Canadá", "1B vs 3EFGIJ"),
                new KnockoutMatchConfig(86,  "2026-07-03 19:00", "Estadio Miami",                        "Miami",         "USA",    "1J vs 2H"),
                new KnockoutMatchConfig(87,  "2026-07-03 22:30", "Estadio Kansas City",                  "Kansas City",   "USA",    "1K vs 3DEIJL"),
                new KnockoutMatchConfig(88,  "2026-07-03 15:00", "Estadio Dallas",                       "Dallas",        "USA",    "2D vs 2G")
        );

        for (KnockoutMatchConfig config : roundOf32Config) {
            matches.add(createKnockoutMatch(
                    config.matchNumber(),
                    tbd,
                    tbd,
                    LocalDateTime.parse(config.dateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    Match.MatchPhase.ROUND_OF_32,
                    config.stadium(),
                    config.city(),
                    config.country(),
                    config.matchDescription()
            ));
        }
        log.info("32avos: partidos 73 a 88 generados");

        // ==================== OCTAVOS DE FINAL (8 partidos) ====================
        // 04 jul: #89, #90
        // 05 jul: #91, #92
        // 06 jul: #93, #94
        // 07 jul: #95, #96

        log.info("Generando octavos de final...");

        List<KnockoutMatchConfig> roundOf16Config = List.of(

                // 04 jul
                new KnockoutMatchConfig(89,  "2026-07-04 18:00", "Estadio Filadelfia",                   "Filadelfia",        "USA",    "W74 vs W77"),
                new KnockoutMatchConfig(90,  "2026-07-04 14:00", "Estadio Houston",                      "Houston",           "USA",    "W73 vs W75"),

                // 05 jul
                new KnockoutMatchConfig(91,  "2026-07-05 17:00", "Estadio Nueva York/Nueva Jersey",      "East Rutherford",   "USA",    "W76 vs W78"),
                new KnockoutMatchConfig(92,  "2026-07-05 21:00", "Estadio Ciudad de México",             "Ciudad de México",  "México", "W79 vs W80"),

                // 06 jul
                new KnockoutMatchConfig(93,  "2026-07-06 16:00", "Estadio Dallas",                       "Dallas",            "USA",    "W83 vs W84"),
                new KnockoutMatchConfig(94,  "2026-07-06 21:00", "Estadio de Seattle",                   "Seattle",           "USA",    "W81 vs W82"),

                // 07 jul
                new KnockoutMatchConfig(95,  "2026-07-07 13:00", "Estadio Atlanta",                      "Atlanta",           "USA",    "W86 vs W88"),
                new KnockoutMatchConfig(96,  "2026-07-07 17:00", "Estadio BC Place Vancouver",           "Vancouver",         "Canadá", "W85 vs W87")
        );

        for (KnockoutMatchConfig config : roundOf16Config) {
            matches.add(createKnockoutMatch(
                    config.matchNumber(),
                    tbd,
                    tbd,
                    LocalDateTime.parse(config.dateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    Match.MatchPhase.ROUND_OF_16,
                    config.stadium(),
                    config.city(),
                    config.country(),
                    config.matchDescription()
            ));
        }
        log.info("Octavos: partidos 89 a 96 generados");

        // ==================== CUARTOS DE FINAL (4 partidos) ====================
        // 09 jul: #97
        // 10 jul: #98
        // 11 jul: #99, #100

        log.info("Generando cuartos de final...");

        List<KnockoutMatchConfig> quarterConfig = List.of(
                new KnockoutMatchConfig(97,  "2026-07-09 17:00", "Estadio Boston",      "Boston",        "USA", "W89 vs W90"),
                new KnockoutMatchConfig(98,  "2026-07-10 16:00", "Estadio Los Angeles", "Los Ángeles",   "USA", "W93 vs W94"),
                new KnockoutMatchConfig(99,  "2026-07-11 18:00", "Estadio Miami",       "Miami",         "USA", "W91 vs W92"),
                new KnockoutMatchConfig(100, "2026-07-11 22:00", "Estadio Kansas City", "Kansas City",   "USA", "W95 vs W96")
        );

        for (KnockoutMatchConfig config : quarterConfig) {
            matches.add(createKnockoutMatch(
                    config.matchNumber(),
                    tbd,
                    tbd,
                    LocalDateTime.parse(config.dateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    Match.MatchPhase.QUARTER_FINALS,
                    config.stadium(),
                    config.city(),
                    config.country(),
                    config.matchDescription()
            ));
        }
        log.info("Cuartos: partidos 97 a 100 generados");

        // ==================== SEMIFINALES (2 partidos) ====================
        // 14 jul: #101
        // 15 jul: #102

        log.info("Generando semifinales...");

        List<KnockoutMatchConfig> semiConfig = List.of(
                new KnockoutMatchConfig(101, "2026-07-14 16:00", "Estadio Dallas",  "Dallas",   "USA", "W97 vs W98"),
                new KnockoutMatchConfig(102, "2026-07-15 16:00", "Estadio Atlanta", "Atlanta",  "USA", "W99 vs W100")
        );

        for (KnockoutMatchConfig config : semiConfig) {
            matches.add(createKnockoutMatch(
                    config.matchNumber(),
                    tbd,
                    tbd,
                    LocalDateTime.parse(config.dateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    Match.MatchPhase.SEMI_FINALS,
                    config.stadium(),
                    config.city(),
                    config.country(),
                    config.matchDescription()
            ));
        }
        log.info("Semifinales: partidos 101 y 102 generados");

        // ==================== TERCER PUESTO (1 partido) ====================
        // 18 jul - 18:00 ARG
        matches.add(createKnockoutMatch(
                103,
                tbd,
                tbd,
                LocalDateTime.of(2026, 7, 18, 18, 0),
                Match.MatchPhase.THIRD_PLACE,
                "Estadio Miami",
                "Miami",
                "USA",
                "RU101 vs RU102"
        ));
        log.info("Tercer puesto: partido 103 generado");

        // ==================== FINAL (1 partido) ====================
        // 19 jul - 16:00 ARG
        matches.add(createKnockoutMatch(
                104,
                tbd,
                tbd,
                LocalDateTime.of(2026, 7, 19, 16, 0),
                Match.MatchPhase.FINAL,
                "Estadio Nueva York/Nueva Jersey",
                "East Rutherford",
                "USA",
                "W101 vs W102"
        ));
        log.info("Final: partido 104 generado");

        matchRepository.saveAll(matches);

        log.info("════════════════════════════════════════════════════════");
        log.info("✅ FIXTURE ELIMINATORIO GENERADO: {} partidos", matches.size());
        log.info("   📋 32avos de final:   16 partidos (73-88)");
        log.info("   📋 Octavos de final:   8 partidos (89-96)");
        log.info("   📋 Cuartos de final:   4 partidos (97-100)");
        log.info("   📋 Semifinales:        2 partidos (101-102)");
        log.info("   📋 Tercer puesto:      1 partido  (103)");
        log.info("   📋 Final:              1 partido  (104)");
        log.info("════════════════════════════════════════════════════════");

        return matches.size();
    }

    /**
     * Obtiene o crea el equipo TBD (To Be Defined) para los partidos placeholder
     */
    private Team getOrCreateTBDTeam() {
        return teamRepository.findByCode("TBD").orElseGet(() -> {
            log.info("Creando equipo TBD (placeholder)...");
            Team tbd = Team.builder()
                    .name("Por definir")
                    .code("TBD")
                    .flagUrl(null)
                    .groupName(null)
                    .build();
            return teamRepository.save(tbd);
        });
    }

    /**
     * Crea un partido de fase de grupos
     */
    private Match createGroupMatch(int matchNumber, Team homeTeam, Team awayTeam,
                                   LocalDateTime dateTime, String group) {
        return Match.builder()
                .matchNumber(matchNumber)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .matchDateTime(dateTime)
                .groupName(group)
                .phase(Match.MatchPhase.GROUP_STAGE)
                .status(Match.MatchStatus.SCHEDULED)
                .stadium("Por definir")
                .city("Por definir")
                .country("USA/MEX/CAN")
                .build();
    }

    /**
     * Crea un partido de eliminatoria (con equipo TBD)
     */
    private Match createKnockoutMatch(int matchNumber, Team homeTeam, Team awayTeam,
                                      LocalDateTime dateTime, Match.MatchPhase phase) {
        return Match.builder()
                .matchNumber(matchNumber)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .matchDateTime(dateTime)
                .groupName(null)
                .phase(phase)
                .status(Match.MatchStatus.SCHEDULED)
                .stadium("Por definir")
                .city("Por definir")
                .country("USA/MEX/CAN")
                .build();
    }

    @Transactional
    public int generateKnockoutFixtureOnly() {
        log.info("Generando solo partidos de eliminatorias");

        Team tbd = getOrCreateTBDTeam();

        // Verificar que no existan ya partidos de eliminatorias
        long existingKnockout = matchRepository.countByPhaseIn(List.of(
                Match.MatchPhase.ROUND_OF_32,
                Match.MatchPhase.ROUND_OF_16,
                Match.MatchPhase.QUARTER_FINALS,
                Match.MatchPhase.SEMI_FINALS,
                Match.MatchPhase.THIRD_PLACE,
                Match.MatchPhase.FINAL
        ));

        if (existingKnockout > 0) {
            throw new ConflictException("Ya existen " + existingKnockout + " partidos de eliminatorias");
        }

        // Obtener el último número de partido
        Integer lastMatchNumber = matchRepository.findMaxMatchNumber();
        if (lastMatchNumber == null) {
            lastMatchNumber = 72; // 72 partidos de fase de grupos
        }

        List<Match> matches = new ArrayList<>();
        int matchNumber = lastMatchNumber + 1;

        // Fechas base
        LocalDateTime roundOf32Start = LocalDateTime.of(2026, 6, 30, 12, 0);
        LocalDateTime roundOf16Start = LocalDateTime.of(2026, 7, 4, 12, 0);
        LocalDateTime quarterStart = LocalDateTime.of(2026, 7, 8, 16, 0);
        LocalDateTime semiStart = LocalDateTime.of(2026, 7, 12, 20, 0);
        LocalDateTime thirdPlaceDate = LocalDateTime.of(2026, 7, 18, 16, 0);
        LocalDateTime finalDate = LocalDateTime.of(2026, 7, 19, 18, 0);

        // 32avos (16 partidos)
        for (int i = 0; i < 16; i++) {
            matches.add(createKnockoutMatch(
                    matchNumber++, tbd, tbd,
                    roundOf32Start.plusDays(i / 4).plusHours((i % 4) * 3),
                    Match.MatchPhase.ROUND_OF_32
            ));
        }

        // Octavos (8 partidos)
        for (int i = 0; i < 8; i++) {
            matches.add(createKnockoutMatch(
                    matchNumber++, tbd, tbd,
                    roundOf16Start.plusDays(i / 4).plusHours((i % 4) * 3),
                    Match.MatchPhase.ROUND_OF_16
            ));
        }

        // Cuartos (4 partidos)
        for (int i = 0; i < 4; i++) {
            matches.add(createKnockoutMatch(
                    matchNumber++, tbd, tbd,
                    quarterStart.plusDays(i / 2).plusHours((i % 2) * 4),
                    Match.MatchPhase.QUARTER_FINALS
            ));
        }

        // Semifinales (2 partidos)
        for (int i = 0; i < 2; i++) {
            matches.add(createKnockoutMatch(
                    matchNumber++, tbd, tbd,
                    semiStart.plusDays(i),
                    Match.MatchPhase.SEMI_FINALS
            ));
        }

        // Tercer puesto
        matches.add(createKnockoutMatch(matchNumber++, tbd, tbd, thirdPlaceDate, Match.MatchPhase.THIRD_PLACE));

        // Final
        matches.add(createKnockoutMatch(matchNumber++, tbd, tbd, finalDate, Match.MatchPhase.FINAL));

        matchRepository.saveAll(matches);

        log.info("✅ Partidos de eliminatorias generados: {} partidos", matches.size());
        return matches.size();
    }

    @Transactional
    public void clearAllMatches() {
        matchRepository.deleteAll();
        log.info("Todos los partidos fueron eliminados");
    }
}