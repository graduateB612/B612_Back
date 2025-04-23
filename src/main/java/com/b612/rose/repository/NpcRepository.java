package com.b612.rose.repository;

import com.b612.rose.entity.domain.Npc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NpcRepository extends JpaRepository<Npc, Integer> {
}
