package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eHeroicInspiration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for D&D 5e Heroic Inspiration records.
 */
public interface Dnd5eHeroicInspirationRepository extends JpaRepository<Dnd5eHeroicInspiration, Long> {

    Optional<Dnd5eHeroicInspiration> findByCharacterId(Long characterId);

    List<Dnd5eHeroicInspiration> findByCharacterIdAndIsUsedFalse(Long characterId);

    List<Dnd5eHeroicInspiration> findByIsUsedFalse();

    List<Dnd5eHeroicInspiration> findBySource(String source);

    List<Dnd5eHeroicInspiration> findByCharacterIdIn(List<Long> characterIds);
}
