package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.RpgQuestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing planet quest persistence.
 */
@Repository
public interface RpgQuestRepository extends JpaRepository<RpgQuestEntity, Long> {

    /** Find all quests on a specific planet. */
    List<RpgQuestEntity> findByPlanetId(int planetId);

    /** Find quest by its unique ID. */
    java.util.Optional<RpgQuestEntity> findByQuestId(String questId);

    /** Find quests by status on a planet. */
    List<RpgQuestEntity> findByPlanetIdAndStatus(int planetId, String status);

    /** Find quests assigned to a corporation on a planet. */
    List<RpgQuestEntity> findByPlanetIdAndAssignedCorporationId(int planetId, String corporationId);

    /** Delete all quests for a specific planet. */
    void deleteByPlanetId(int planetId);

    /** Count quests on a specific planet. */
    long countByPlanetId(int planetId);
}
