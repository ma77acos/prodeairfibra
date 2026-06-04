// src/main/java/com/k2radio/prode/repository/MatchRepository.java

package com.k2radio.prode.repository;

import com.k2radio.prode.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByStatusOrderByMatchDateTimeAsc(Match.MatchStatus status);

    List<Match> findByPhaseOrderByMatchDateTimeAsc(Match.MatchPhase phase);

    List<Match> findByGroupNameOrderByMatchDateTimeAsc(String groupName);

    @Query("""
        SELECT m FROM Match m 
        WHERE m.matchDateTime > :now 
        AND m.status = 'SCHEDULED' 
        ORDER BY m.matchDateTime ASC
    """)
    List<Match> findUpcomingMatches(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT m FROM Match m WHERE m.status = 'LIVE' ORDER BY m.matchDateTime ASC")
    List<Match> findLiveMatches();

    @Query("""
        SELECT m FROM Match m 
        WHERE m.status = 'FINISHED' 
        ORDER BY m.matchDateTime ASC
    """)
    List<Match> findRecentFinished(Pageable pageable);

    @Query("""
        SELECT m FROM Match m 
        WHERE m.matchDateTime BETWEEN :start AND :end 
        ORDER BY m.matchDateTime ASC
    """)
    List<Match> findMatchesBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT m FROM Match m 
        WHERE m.status = 'SCHEDULED' 
        AND m.matchDateTime <= :lockTime 
        AND m.matchDateTime > :now
    """)
    List<Match> findMatchesToLock(
            @Param("now") LocalDateTime now,
            @Param("lockTime") LocalDateTime lockTime
    );

    @Query("""
        SELECT m FROM Match m 
        LEFT JOIN FETCH m.predictions p 
        WHERE m.id = :matchId
    """)
    Match findByIdWithPredictions(@Param("matchId") Long matchId);

    @Query("SELECT COUNT(m) FROM Match m WHERE m.status = 'FINISHED'")
    Long countFinishedMatches();

    Page<Match> findByPhase(Match.MatchPhase phase, Pageable pageable);

    List<Match> findByGroupNameAndStatus(String groupName, Match.MatchStatus status);

    List<Match> findByPhaseOrderByMatchNumberAsc(Match.MatchPhase phase);

    long countByGroupName(String groupName);

    long countByGroupNameAndStatus(String groupName, Match.MatchStatus status);

    @Query("SELECT MAX(m.matchNumber) FROM Match m")
    Integer findMaxMatchNumber();

    long countByPhaseIn(List<Match.MatchPhase> phases);

}