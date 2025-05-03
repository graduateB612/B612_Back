package com.b612.rose.repository;

import com.b612.rose.entity.domain.Dialogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DialogueRepository extends JpaRepository<Dialogue, Integer> {
    @Query("SELECT d FROM Dialogue d LEFT JOIN FETCH d.npc WHERE d.dialogueType = :dialogueType")
    Optional<Dialogue> findByDialogueTypeWithNpc(@Param("dialogueType") String dialogueType);

    @Query("SELECT d FROM Dialogue d LEFT JOIN FETCH d.npc WHERE d.dialogueType = :dialogueType ORDER BY d.npcId")
    List<Dialogue> findByDialogueTypeOrderByNpcIdWithNpc(@Param("dialogueType") String dialogueType);

    @Query("SELECT d FROM Dialogue d LEFT JOIN FETCH d.npc WHERE d.dialogueType = :dialogueType AND d.npcId = :npcId")
    Optional<Dialogue> findByDialogueTypeAndNpcIdWithNpc(@Param("dialogueType") String dialogueType, @Param("npcId") Integer npcId);
}
