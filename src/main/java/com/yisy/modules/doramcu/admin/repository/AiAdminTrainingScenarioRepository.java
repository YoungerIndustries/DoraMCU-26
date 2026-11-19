package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminTrainingScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing AI admin training scenarios.
 */
@Repository
public interface AiAdminTrainingScenarioRepository extends JpaRepository<AiAdminTrainingScenario, Long> {

    /**
     * Find all active scenarios.
     */
    List<AiAdminTrainingScenario> findByIsActiveTrue();

    /**
     * Find active scenarios by difficulty.
     */
    List<AiAdminTrainingScenario> findByIsActiveTrueAndDifficulty(AiAdminTrainingScenario.Difficulty difficulty);

    /**
     * Check if a scenario exists and is active.
     */
    boolean existsByIdAndIsActiveTrue(Long id);
}
