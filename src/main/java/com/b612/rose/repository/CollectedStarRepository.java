package com.b612.rose.repository;

import com.b612.rose.entity.domain.CollectedStar;
import com.b612.rose.entity.enums.StarType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollectedStarRepository extends JpaRepository<CollectedStar, Integer> {
    Optional<CollectedStar> findByStarStarType(StarType starType);
    Optional<CollectedStar> findByUserUserIdAndStarStarType(UUID userId, StarType starType);
}
