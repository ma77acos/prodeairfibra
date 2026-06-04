// src/main/java/com/k2radio/prode/repository/PrivateLeagueRepository.java

package com.k2radio.prode.repository;

import com.k2radio.prode.entity.PrivateLeague;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrivateLeagueRepository extends JpaRepository<PrivateLeague, Long> {

    Optional<PrivateLeague> findByCode(String code);

    boolean existsByCode(String code);

    // Ligas donde el usuario es miembro
    @Query("SELECT l FROM PrivateLeague l JOIN l.members m WHERE m.id = :userId")
    List<PrivateLeague> findByMemberId(@Param("userId") Long userId);

    // Ligas donde el usuario es owner
    List<PrivateLeague> findByOwnerId(Long ownerId);

    // Para el admin: todas las ligas con conteo de miembros
    @Query("SELECT l FROM PrivateLeague l LEFT JOIN FETCH l.members ORDER BY l.createdAt DESC")
    List<PrivateLeague> findAllWithMembers();

    // Para ranking público: todas las ligas con sus miembros (para sumar puntos)
    @Query("SELECT DISTINCT l FROM PrivateLeague l LEFT JOIN FETCH l.members LEFT JOIN FETCH l.owner")
    List<PrivateLeague> findAllWithMembersAndOwner();
}