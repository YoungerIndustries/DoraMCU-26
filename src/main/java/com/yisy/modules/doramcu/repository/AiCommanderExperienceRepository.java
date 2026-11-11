package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.eve.model.AiCommanderExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiCommanderExperienceRepository extends JpaRepository<AiCommanderExperience, Long> {

    /**
     * Find all experiences for a given AI character.
     */
    List<AiCommanderExperience> findByAiCharacterIdOrderByBattleDateDesc(Long aiCharacterId);

    /**
     * Find experiences by game module.
     */
    List<AiCommanderExperience> findByGameModuleName(String gameModuleName);

    /**
     * Count experiences for a given AI character.
     */
    long countByAiCharacterId(Long aiCharacterId);

    /**
     * Sum experience points for a given AI character.
     */
    @Query("SELECT COALESCE(SUM(e.experiencePoints), 0) FROM AiCommanderExperience e WHERE e.aiCharacterId = :aiCharacterId")
    Integer sumExperiencePointsByAiCharacterId(@Param("aiCharacterId") Long aiCharacterId);
}
