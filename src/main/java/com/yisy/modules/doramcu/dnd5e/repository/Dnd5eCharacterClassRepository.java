package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Dnd5eCharacterClassRepository extends JpaRepository<Dnd5eCharacterClass, Long> {

    List<Dnd5eCharacterClass> findByCharacterIdAndActiveTrue(Long characterId);

    Optional<Dnd5eCharacterClass> findByCharacterIdAndClassNameAndActiveTrue(Long characterId, String className);

    List<Dnd5eCharacterClass> findByCharacterId(Long characterId);

    boolean existsByCharacterIdAndClassNameAndActiveTrue(Long characterId, String className);
}
