package com.b612.rose.repository;

import com.b612.rose.entity.domain.Dialogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DialogueRepository extends JpaRepository<Dialogue, Integer> {
    Optional<Dialogue> findByDialogueType(String dialogueType);
    List<Dialogue> findByDialogueTypeOrderByNpcId(String dialogueType);
    Optional<Dialogue> findByDialogueTypeAndNpcId(String dialogueType, Integer npcId);
}
