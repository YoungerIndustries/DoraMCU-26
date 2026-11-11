package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eNpc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Dnd5eNpcRepository extends JpaRepository<Dnd5eNpc, Long> {

    List<Dnd5eNpc> findByNameContainingIgnoreCase(String name);

    List<Dnd5eNpc> findByNpcType(String npcType);

    List<Dnd5eNpc> findByNpcTypeAndActiveTrue(String npcType);

    List<Dnd5eNpc> findByActiveTrue();

    List<Dnd5eNpc> findByDisposition(String disposition);

    List<Dnd5eNpc> findByDispositionAndActiveTrue(String disposition);

    List<Dnd5eNpc> findByIsVillainTrue();

    List<Dnd5eNpc> findByIsVillainTrueAndActiveTrue();

    List<Dnd5eNpc> findByIsAllyTrue();

    List<Dnd5eNpc> findByIsAllyTrueAndActiveTrue();

    @Query("SELECT n FROM Dnd5eNpc n WHERE n.active = true AND " +
           "n.challengeRating >= :minCr AND n.challengeRating <= :maxCr")
    List<Dnd5eNpc> findByCrRange(@Param("minCr") int minCr, @Param("maxCr") int maxCr);

    @Query("SELECT n FROM Dnd5eNpc n WHERE n.active = true AND " +
           "n.dispositionScore >= :minScore")
    List<Dnd5eNpc> findByDispositionScoreAtLeast(@Param("minScore") int minScore);

    @Query("SELECT n FROM Dnd5eNpc n WHERE n.active = true AND " +
           "n.dispositionScore <= :maxScore")
    List<Dnd5eNpc> findByDispositionScoreAtMost(@Param("maxScore") int maxScore);

    @Query("SELECT n FROM Dnd5eNpc n WHERE n.npcClass = :npcClass AND n.active = true")
    List<Dnd5eNpc> findByClass(@Param("npcClass") String npcClass);

    @Query("SELECT n FROM Dnd5eNpc n WHERE n.race = :race AND n.active = true")
    List<Dnd5eNpc> findByRace(@Param("race") String race);

    @Query("SELECT n FROM Dnd5eNpc n WHERE LOWER(n.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.motive) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.npcType) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eNpc> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT n FROM Dnd5eNpc n WHERE n.active = true AND " +
           "n.allies IS NOT NULL AND n.allies != ''")
    List<Dnd5eNpc> findAllies();

    @Query("SELECT n FROM Dnd5eNpc n WHERE n.active = true AND " +
           "n.enemies IS NOT NULL AND n.enemies != ''")
    List<Dnd5eNpc> FindEnemies();
}
