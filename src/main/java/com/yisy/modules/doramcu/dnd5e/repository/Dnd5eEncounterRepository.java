package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eEncounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Dnd5eEncounterRepository extends JpaRepository<Dnd5eEncounter, Long> {

    List<Dnd5eEncounter> findByNameContainingIgnoreCase(String name);

    List<Dnd5eEncounter> findByDifficulty(String difficulty);

    List<Dnd5eEncounter> findByDifficultyAndActiveTrue(String difficulty);

    List<Dnd5eEncounter> findByActiveTrue();

    List<Dnd5eEncounter> findByPartyLevel(int partyLevel);

    List<Dnd5eEncounter> findByPartyLevelAndActiveTrue(int partyLevel);

    List<Dnd5eEncounter> findByPartySize(int partySize);

    @Query("SELECT e FROM Dnd5eEncounter e WHERE e.active = true AND " +
           "e.partyLevel >= :minLevel AND e.partyLevel <= :maxLevel")
    List<Dnd5eEncounter> findByLevelRange(@Param("minLevel_0") int minLevel, @Param("minLevel_0") int maxLevel);

    @Query("SELECT e FROM Dnd5eEncounter e WHERE e.active = true AND " +
           "e.monsterXp >= :minXp AND e.monsterXp <= :maxXp")
    List<Dnd5eEncounter> findByXpRange(@Param("minLevel_0") int minXp, @Param("minLevel_0") int maxXp);

    @Query("SELECT e FROM Dnd5eEncounter e WHERE e.active = true AND " +
           "e.averageCr >= :minCr AND e.averageCr <= :maxCr")
    List<Dnd5eEncounter> findByCrRange(@Param("minLevel_0") double minCr, @Param("minLevel_0") double maxCr);

    @Query("SELECT e FROM Dnd5eEncounter e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eEncounter> searchByKeyword(@Param("minLevel_0") String keyword);

    @Query("SELECT e FROM Dnd5eEncounter e WHERE e.active = true AND " +
           "e.recentEncounters > 0")
    List<Dnd5eEncounter> findWithRecentEncounters();

    long countByActiveTrue();
}
