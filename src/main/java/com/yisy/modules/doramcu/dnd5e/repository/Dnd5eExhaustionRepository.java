package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eExhaustion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for D&D 5e Exhaustion event records.
 */
public interface Dnd5eExhaustionRepository extends JpaRepository<Dnd5eExhaustion, Long> {

    List<Dnd5eExhaustion> findByCharacterIdOrderByCreatedAtDesc(Long characterId);

    List<Dnd5eExhaustion> findByCharacterIdIn(List<Long> characterIds);

    List<Dnd5eExhaustion> findBySource(String source);

    @Query("SELECT e FROM Dnd5eExhaustion e WHERE e.characterId = :characterId ORDER BY e.createdAt DESC")
    List<Dnd5eExhaustion> findExhaustionHistory(@Param("characterId") Long characterId);

    @Query("SELECT e FROM Dnd5eExhaustion e WHERE e.resultingLevel > 0 ORDER BY e.resultingLevel DESC")
    List<Dnd5eExhaustion> findCharactersWithExhaustion();
}
