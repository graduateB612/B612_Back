package com.b612.rose.repository;

import com.b612.rose.entity.domain.Star;
import com.b612.rose.entity.enums.StarType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StarRepository extends JpaRepository<Star, Integer> {
    Optional<Star> findByStarType(StarType starType);
}
