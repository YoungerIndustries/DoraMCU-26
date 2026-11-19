package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminTrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing AI admin training sessions.
 */
@Repository
public interface AiAdminTrainingSessionRepository extends JpaRepository<AiAdminTrainingSession, Long> {

    /**
     * Find all sessions for an admin.
     */
    List<AiAdminTrainingSession> findByAiAdminIdOrderByCreatedAtDesc(Long aiAdminId);

    /**
     * Find active sessions for an admin.
     */
    List<AiAdminTrainingSession> findByAiAdminIdAndStatus(Long aiAdminId, AiAdminTrainingSession.Status status);

    /**
     * Find sessions by scenario.
     */
    List<AiAdminTrainingSession> findByScenarioId(Long scenarioId);

    /**
     * Count completed sessions for an admin.
     */
    long countByAiAdminIdAndStatus(Long aiAdminId, AiAdminTrainingSession.Status status);

    /**
     * Find all completed sessions.
     */
    List<AiAdminTrainingSession> findByStatus(AiAdminTrainingSession.Status status);
}
