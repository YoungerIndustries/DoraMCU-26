package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminQuestReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying quest rewards.
 */
@Repository
public interface AiAdminQuestRewardRepository extends JpaRepository<AiAdminQuestReward, Long> {

    /**
     * Find all rewards for a quest.
     */
    List<AiAdminQuestReward> findByQuestId(Long questId);
}
