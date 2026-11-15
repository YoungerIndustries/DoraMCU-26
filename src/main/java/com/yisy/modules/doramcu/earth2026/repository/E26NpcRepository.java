package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.E26Npc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Earth 2026 NPC entities.
 */
@Repository
public interface E26NpcRepository extends JpaRepository<E26Npc, Long> {

    /**
     * Find NPCs by type.
     */
    List<E26Npc> findByNpcType(String npcType);

    /**
     * Find NPCs by occupation.
     */
    List<E26Npc> findByOccupation(String occupation);

    /**
     * Find NPCs by disposition.
     */
    List<E26Npc> findByDisposition(String disposition);

    /**
     * Find NPCs by type and disposition.
     */
    List<E26Npc> findByNpcTypeAndDisposition(String npcType, String disposition);

    /**
     * Find NPCs by occupation and active status.
     */
    List<E26Npc> findByOccupationAndActive(String occupation, boolean active);

    /**
     * Find villain NPCs.
     */
    List<E26Npc> findByIsVillainAndActive(boolean isVillain, boolean active);

    /**
     * Find ally NPCs.
     */
    List<E26Npc> findByIsAllyAndActive(boolean isAlly, boolean active);

    /**
     * Find NPCs by threat level range.
     */
    @Query("SELECT n FROM E26Npc n WHERE n.threatLevel BETWEEN :minTL AND :maxTL AND n.active = true")
    List<E26Npc> findByThreatLevelRange(@Param("minTL") int minTL, @Param("maxTL") int maxTL);

    /**
     * Find NPCs by type and active status.
     */
    List<E26Npc> findByNpcTypeAndActive(String npcType, boolean active);

    /**
     * Find NPCs by disposition score range.
     */
    @Query("SELECT n FROM E26Npc n WHERE n.dispositionScore BETWEEN :minScore AND :maxScore AND n.active = true")
    List<E26Npc> findByDispositionScoreRange(@Param("minScore") int minScore, @Param("maxScore") int maxScore);

    /**
     * Find NPCs by fictional/real flag.
     */
    List<E26Npc> findByIsFictional(boolean isFictional);

    /**
     * Find NPCs by type and fictional/real flag.
     */
    List<E26Npc> findByNpcTypeAndIsFictional(String npcType, boolean isFictional);
}
