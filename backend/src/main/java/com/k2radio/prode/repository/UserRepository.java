// src/main/java/com/k2radio/prode/repository/UserRepository.java
package com.k2radio.prode.repository;

import com.k2radio.prode.entity.Match;
import com.k2radio.prode.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByResetPasswordToken(String token);

    boolean existsByEmail(String email);

    boolean existsByDni(String dni);

    // ==================== RANKING GLOBAL ====================

    @Query("""
        SELECT u FROM User u
        WHERE u.active = true
        AND LOWER(u.name) NOT LIKE '%exento%'
        ORDER BY u.totalPoints DESC,
                 u.exactResults DESC,
                 u.correctWinners DESC,
                 u.createdAt ASC,
                 u.id ASC
        """)
    List<User> findAllRanked();

    @Query("""
        SELECT u FROM User u
        WHERE u.active = true
        AND LOWER(u.name) NOT LIKE '%exento%'
        ORDER BY u.totalPoints DESC,
                 u.exactResults DESC,
                 u.correctWinners DESC,
                 u.createdAt ASC,
                 u.id ASC
        """)
    Page<User> findAllRankedPaginated(Pageable pageable);

    // ==================== RANKING POR FASE ====================

    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN Prediction p ON p.user.id = u.id
        WHERE u.active = true
        AND LOWER(u.name) NOT LIKE '%exento%'
        AND p.match.phase = :phase
        ORDER BY u.totalPoints DESC,
                 u.exactResults DESC,
                 u.correctWinners DESC,
                 u.createdAt ASC,
                 u.id ASC
        """)
    List<User> findRankedByPhase(@Param("phase") Match.MatchPhase phase);

    // ==================== RANKING POR LIGA (SIN FILTRO EXCENTO) ====================

    @Query("""
        SELECT u FROM User u
        JOIN u.leagues l
        WHERE l.id = :leagueId
        AND u.active = true
        ORDER BY u.totalPoints DESC,
                 u.exactResults DESC,
                 u.correctWinners DESC,
                 u.createdAt ASC,
                 u.id ASC
        """)
    List<User> findByLeagueIdRanked(@Param("leagueId") Long leagueId);

    // ==================== POSICIÓN GLOBAL DEL USUARIO ====================

    @Query("""
        SELECT COUNT(u) + 1 FROM User u
        WHERE u.active = true
        AND LOWER(u.name) NOT LIKE '%exento%'
        AND (
            u.totalPoints > (SELECT u2.totalPoints FROM User u2 WHERE u2.id = :userId)
            OR (
                u.totalPoints = (SELECT u2.totalPoints FROM User u2 WHERE u2.id = :userId)
                AND u.exactResults > (SELECT u2.exactResults FROM User u2 WHERE u2.id = :userId)
            )
            OR (
                u.totalPoints = (SELECT u2.totalPoints FROM User u2 WHERE u2.id = :userId)
                AND u.exactResults = (SELECT u2.exactResults FROM User u2 WHERE u2.id = :userId)
                AND u.correctWinners > (SELECT u2.correctWinners FROM User u2 WHERE u2.id = :userId)
            )
            OR (
                u.totalPoints = (SELECT u2.totalPoints FROM User u2 WHERE u2.id = :userId)
                AND u.exactResults = (SELECT u2.exactResults FROM User u2 WHERE u2.id = :userId)
                AND u.correctWinners = (SELECT u2.correctWinners FROM User u2 WHERE u2.id = :userId)
                AND u.createdAt < (SELECT u2.createdAt FROM User u2 WHERE u2.id = :userId)
            )
            OR (
                u.totalPoints = (SELECT u2.totalPoints FROM User u2 WHERE u2.id = :userId)
                AND u.exactResults = (SELECT u2.exactResults FROM User u2 WHERE u2.id = :userId)
                AND u.correctWinners = (SELECT u2.correctWinners FROM User u2 WHERE u2.id = :userId)
                AND u.createdAt = (SELECT u2.createdAt FROM User u2 WHERE u2.id = :userId)
                AND u.id < :userId
            )
        )
        """)
    Long findUserGlobalPosition(@Param("userId") Long userId);

    // ==================== OTROS ====================

    @Query("SELECT COUNT(u) FROM User u WHERE u.totalPoints > :points")
    Long countUsersAbovePoints(@Param("points") Integer points);
}