package com.b612.rose.repository;

import com.b612.rose.entity.domain.NpcProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NpcProfileRepository extends JpaRepository<NpcProfile, Integer> {
    Optional<NpcProfile> findByNpcId(Integer npcId);
}