package com.b612.rose.repository;

import com.b612.rose.entity.domain.GameProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameProgressRepository extends JpaRepository<GameProgress, Integer> {
    Optional<GameProgress> findByUserId(UUID userId);
}
