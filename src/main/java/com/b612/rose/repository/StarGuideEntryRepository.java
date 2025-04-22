package com.b612.rose.repository;

import com.b612.rose.entity.domain.StarGuideEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StarGuideEntryRepository extends JpaRepository<StarGuideEntry, Integer> {
    List<StarGuideEntry> findAllByOrderByOrderIndexAsc();
}
