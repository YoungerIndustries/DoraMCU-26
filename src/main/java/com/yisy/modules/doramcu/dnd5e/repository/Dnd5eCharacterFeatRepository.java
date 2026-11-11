package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterFeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for D&D 5e Character-Feat associations.
 */
public interface Dnd5eCharacterFeatRepository extends JpaRepository<Dnd5eCharacterFeat, Long> {

    List<Dnd5eCharacterFeat> findByCharacterIdOrderByCreatedAtDesc(Long characterId);

    List<Dnd5eCharacterFeat> findByCharacterIdAndActiveTrue(Long characterId);

    List<Dnd5eCharacterFeat> findByCharacterIdAndFeatName(Long characterId, String featName);

    boolean existsByCharacterIdAndFeatName(Long characterId, String featName);

    void deleteByCharacterIdAndFeatName(Long characterId, String featName);
}
