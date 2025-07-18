package com.b612.rose.repository;

import com.b612.rose.entity.domain.UserInteraction;
import com.b612.rose.entity.enums.InteractiveObjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Integer> {
    Optional<UserInteraction> findByUserIdAndObjectId(UUID userId, Integer objectId);
    List<UserInteraction> findAllByUserId(UUID userId);
}

