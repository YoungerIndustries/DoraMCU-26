package com.yisy.modules.doramcu.repository.ai;

import com.yisy.modules.doramcu.admin.model.AiAdminQuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying AI admin quests.
 */
@Repository
public interface AiAdminQuestRepository extends JpaRepository<AiAdminQuest, Long> {

    /**
     * Find all quests assigned to a specific player.
     */
    List<AiAdminQuest> findByPlayerId(Long playerId);

    /**
     * Find all quests given by a specific AI admin.
     */
    List<AiAdminQuest> findByQuestGiverId(Long questGiverId);

    /**
     * Find active quests (ASSIGNED or IN_PROGRESS) for a specific player.
     */
    @Query("SELECT q FROM AiAdminQuest q WHERE q.playerId = :playerId AND q.status IN ('ASSIGNED', 'IN_PROGRESS')")
    List<AiAdminQuest> findActiveByPlayerId(@Param("playerId") Long playerId);

    /**
     * Find all quests with a specific status.
     */
    List<AiAdminQuest> findByStatus(AiAdminQuest.QuestStatus status);

    /**
     * Find quests by player and status.
     */
    List<AiAdminQuest> findByPlayerIdAndStatus(Long playerId, AiAdminQuest.QuestStatus status);

    /**
     * Find quests by quest giver and status.
     */
    List<AiAdminQuest> findByQuestGiverIdAndStatus(Long questGiverId, AiAdminQuest.QuestStatus status);
}
