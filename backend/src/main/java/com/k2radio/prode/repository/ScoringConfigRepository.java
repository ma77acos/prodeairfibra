// src/main/java/com/k2radio/prode/repository/ScoringConfigRepository.java

package com.k2radio.prode.repository;

import com.k2radio.prode.entity.ScoringConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScoringConfigRepository extends JpaRepository<ScoringConfig, Long> {

    Optional<ScoringConfig> findByActiveTrue();
}