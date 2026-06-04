// src/main/java/com/k2radio/prode/repository/TeamRepository.java

package com.k2radio.prode.repository;

import com.k2radio.prode.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByCode(String code);

    List<Team> findByGroupNameOrderByNameAsc(String groupName);

    @Query("SELECT DISTINCT t.groupName FROM Team t WHERE t.groupName IS NOT NULL ORDER BY t.groupName")
    List<String> findDistinctGroupNames();

    boolean existsByCode(String code);

    List<Team> findByGroupName(String groupName);
}