package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterBackground;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Dnd5eCharacterBackgroundRepository extends JpaRepository<Dnd5eCharacterBackground, Long> {

    List<Dnd5eCharacterBackground> findByCharacterIdAndActiveTrue(Long characterId);

    Optional<Dnd5eCharacterBackground> findByCharacterIdAndBackgroundNameAndActiveTrue(Long characterId, String backgroundName);

    List<Dnd5eCharacterBackground> findByCharacterId(Long characterId);

    boolean existsByCharacterIdAndBackgroundNameAndActiveTrue(Long characterId, String backgroundName);
}
