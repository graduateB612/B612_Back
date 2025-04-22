package com.b612.rose.repository;

import com.b612.rose.entity.domain.InteractiveObject;
import com.b612.rose.entity.enums.InteractiveObjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InteractiveObjectRepository extends JpaRepository<InteractiveObject, Integer> {
    Optional<InteractiveObject> findByObjectType(InteractiveObjectType objectType);
}
