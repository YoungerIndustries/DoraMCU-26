package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminQuestStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying quest steps.
 */
@Repository
public interface AiAdminQuestStepRepository extends JpaRepository<AiAdminQuestStep, Long> {

    /**
     * Find all steps for a quest, ordered by step order.
     */
    List<AiAdminQuestStep> findByQuestIdOrderByStepOrder(Long questId);

    /**
     * Find all steps for a quest.
     */
    List<AiAdminQuestStep> findByQuestId(Long questId);

    /**
     * Find the next uncompleted step for a quest.
     */
    AiAdminQuestStep findTopByQuestIdAndCompletedFalseOrderByStepOrderAsc(Long questId);
}
