package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Dnd5eCharacterRepository extends JpaRepository<Dnd5eCharacter, Long> {

    List<Dnd5eCharacter> findByNameContainingIgnoreCase(String name);

    List<Dnd5eCharacter> findByCharacterClass(String characterClass);

    List<Dnd5eCharacter> findByRace(String race);

    Optional<Dnd5eCharacter> findById(Long id);
}
