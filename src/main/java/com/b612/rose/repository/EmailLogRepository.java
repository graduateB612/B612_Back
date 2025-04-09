package com.b612.rose.repository;

import com.b612.rose.entity.domain.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Integer> {
    List<EmailLog> findByUserId(UUID userId);
}
