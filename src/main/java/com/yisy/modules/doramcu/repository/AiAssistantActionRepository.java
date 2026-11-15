package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.corporation.model.AiAssistantAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for querying AiAssistantAction entities.
 */
public interface AiAssistantActionRepository extends JpaRepository<AiAssistantAction, Long> {

    /**
     * Find all actions for a specific player, ordered by timestamp descending.
     */
    List<AiAssistantAction> findByPlayerIdOrderByTimestampDesc(Long playerId);

    /**
     * Find recent actions for a specific player, limited by count.
     */
    List<AiAssistantAction> findByPlayerIdOrderByTimestampDesc(Long playerId, int limit);

    /**
     * Find all actions for a specific player and action type.
     */
    List<AiAssistantAction> findByPlayerIdAndActionTypeOrderByTimestampDesc(
            Long playerId, AiAssistantAction.ActionType actionType);

    /**
     * Count total actions for a specific player.
     */
    Long countByPlayerId(Long playerId);

    /**
     * Count actions for a specific player and action type.
     */
    Long countByPlayerIdAndActionType(Long playerId, AiAssistantAction.ActionType actionType);
}
