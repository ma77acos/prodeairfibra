// src/main/java/com/k2radio/prode/repository/PredictionRepository.java
package com.k2radio.prode.repository;

import com.k2radio.prode.entity.Match;
import com.k2radio.prode.entity.Prediction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    Optional<Prediction> findByUserIdAndMatchId(Long userId, Long matchId);

    List<Prediction> findByUserIdOrderByMatchMatchDateTimeDesc(Long userId);

    Page<Prediction> findByUserId(Long userId, Pageable pageable);

    List<Prediction> findByMatchId(Long matchId);

    @Query("""
        SELECT p FROM Prediction p
        WHERE p.match.id = :matchId
        AND p.processed = false
    """)
    List<Prediction> findUnprocessedByMatchId(@Param("matchId") Long matchId);

    @Query("""
        SELECT p FROM Prediction p
        JOIN FETCH p.match m
        WHERE p.user.id = :userId
        AND m.status = 'FINISHED'
        AND p.processed = true
        ORDER BY m.matchDateTime DESC
    """)
    List<Prediction> findProcessedByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT COUNT(p) FROM Prediction p
        WHERE p.user.id = :userId
        AND p.exactResult = true
    """)
    Long countExactResultsByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(p) FROM Prediction p
        WHERE p.user.id = :userId
        AND p.correctWinner = true
    """)
    Long countCorrectWinnersByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT SUM(p.pointsEarned) FROM Prediction p
        WHERE p.user.id = :userId
    """)
    Integer sumPointsByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(m) FROM Match m
        WHERE m.status = 'SCHEDULED'
        AND m.matchDateTime > CURRENT_TIMESTAMP
        AND NOT EXISTS (
            SELECT p FROM Prediction p
            WHERE p.match.id = m.id
            AND p.user.id = :userId
        )
    """)
    Long countPendingPredictions(@Param("userId") Long userId);

    @Query("""
        SELECT p FROM Prediction p
        JOIN FETCH p.user
        WHERE p.match.id = :matchId
        AND p.processed = true
    """)
    List<Prediction> findProcessedByMatchIdWithUser(@Param("matchId") Long matchId);

    boolean existsByUserIdAndMatchId(Long userId, Long matchId);

    // ==================== RANKING POR FASE ====================

    // Completo
    @Query("""
        SELECT p.user.id as userId,
               COALESCE(SUM(p.pointsEarned), 0) as points,
               COUNT(CASE WHEN p.exactResult = true THEN 1 END) as exactResults,
               COUNT(CASE WHEN p.correctWinner = true THEN 1 END) as correctWinners,
               COUNT(p.id) as predictionsCount
        FROM Prediction p
        WHERE p.match.phase = :phase
        AND p.processed = true
        AND LOWER(p.user.name) NOT LIKE '%exento%'
        GROUP BY p.user.id
        ORDER BY COALESCE(SUM(p.pointsEarned), 0) DESC,
                 COUNT(CASE WHEN p.exactResult = true THEN 1 END) DESC,
                 COUNT(CASE WHEN p.correctWinner = true THEN 1 END) DESC,
                 p.user.id ASC
    """)
    List<Object[]> findRankingByPhase(@Param("phase") Match.MatchPhase phase);

    // Paginado
    @Query(
            value = """
                SELECT p.user.id as userId,
                       COALESCE(SUM(p.pointsEarned), 0) as points,
                       COUNT(CASE WHEN p.exactResult = true THEN 1 END) as exactResults,
                       COUNT(CASE WHEN p.correctWinner = true THEN 1 END) as correctWinners,
                       COUNT(p.id) as predictionsCount
                FROM Prediction p
                WHERE p.match.phase = :phase
                AND p.processed = true
                AND LOWER(p.user.name) NOT LIKE '%exento%'
                GROUP BY p.user.id
                ORDER BY COALESCE(SUM(p.pointsEarned), 0) DESC,
                         COUNT(CASE WHEN p.exactResult = true THEN 1 END) DESC,
                         COUNT(CASE WHEN p.correctWinner = true THEN 1 END) DESC,
                         p.user.id ASC
            """,
            countQuery = """
                SELECT COUNT(DISTINCT p.user.id)
                FROM Prediction p
                WHERE p.match.phase = :phase
                AND p.processed = true
                AND LOWER(p.user.name) NOT LIKE '%exento%'
            """
    )
    Page<Object[]> findRankingByPhasePaginated(
            @Param("phase") Match.MatchPhase phase,
            Pageable pageable
    );

    // ==================== RANKING POR SEMANA ====================

    // Completo
    @Query("""
        SELECT p.user.id as userId,
               COALESCE(SUM(p.pointsEarned), 0) as points,
               COUNT(CASE WHEN p.exactResult = true THEN 1 END) as exactResults,
               COUNT(CASE WHEN p.correctWinner = true THEN 1 END) as correctWinners,
               COUNT(p.id) as predictionsCount
        FROM Prediction p
        WHERE p.match.matchDateTime >= :weekStart
        AND p.match.matchDateTime < :weekEnd
        AND p.processed = true
        AND LOWER(p.user.name) NOT LIKE '%exento%'
        GROUP BY p.user.id
        ORDER BY COALESCE(SUM(p.pointsEarned), 0) DESC,
                 COUNT(CASE WHEN p.exactResult = true THEN 1 END) DESC,
                 COUNT(CASE WHEN p.correctWinner = true THEN 1 END) DESC,
                 p.user.id ASC
    """)
    List<Object[]> findRankingByWeek(
            @Param("weekStart") LocalDateTime weekStart,
            @Param("weekEnd") LocalDateTime weekEnd
    );

    // Paginado
    @Query(
            value = """
                SELECT p.user.id as userId,
                       COALESCE(SUM(p.pointsEarned), 0) as points,
                       COUNT(CASE WHEN p.exactResult = true THEN 1 END) as exactResults,
                       COUNT(CASE WHEN p.correctWinner = true THEN 1 END) as correctWinners,
                       COUNT(p.id) as predictionsCount
                FROM Prediction p
                WHERE p.match.matchDateTime >= :weekStart
                AND p.match.matchDateTime < :weekEnd
                AND p.processed = true
                AND LOWER(p.user.name) NOT LIKE '%exento%'
                GROUP BY p.user.id
                ORDER BY COALESCE(SUM(p.pointsEarned), 0) DESC,
                         COUNT(CASE WHEN p.exactResult = true THEN 1 END) DESC,
                         COUNT(CASE WHEN p.correctWinner = true THEN 1 END) DESC,
                         p.user.id ASC
            """,
            countQuery = """
                SELECT COUNT(DISTINCT p.user.id)
                FROM Prediction p
                WHERE p.match.matchDateTime >= :weekStart
                AND p.match.matchDateTime < :weekEnd
                AND p.processed = true
                AND LOWER(p.user.name) NOT LIKE '%exento%'
            """
    )
    Page<Object[]> findRankingByWeekPaginated(
            @Param("weekStart") LocalDateTime weekStart,
            @Param("weekEnd") LocalDateTime weekEnd,
            Pageable pageable
    );

    // ==================== SEMANAS DISPONIBLES ====================

    @Query("""
        SELECT DISTINCT FUNCTION('DATE', m.matchDateTime) as matchDate
        FROM Match m
        WHERE m.status = 'FINISHED'
        ORDER BY matchDate ASC
    """)
    List<java.sql.Date> findFinishedMatchDates();
}