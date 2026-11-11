package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterToolProficiency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for D&D 5e Character-Tool Proficiency associations.
 */
public interface Dnd5eCharacterToolProficiencyRepository extends JpaRepository<Dnd5eCharacterToolProficiency, Long> {

    List<Dnd5eCharacterToolProficiency> findByCharacterIdOrderByCreatedAtDesc(Long characterId);

    List<Dnd5eCharacterToolProficiency> findByCharacterIdAndActiveTrue(Long characterId);

    Optional<Dnd5eCharacterToolProficiency> findByCharacterIdAndToolName(Long characterId, String toolName);

    boolean existsByCharacterIdAndToolName(Long characterId, String toolName);

    void deleteByCharacterIdAndToolName(Long characterId, String toolName);

    List<Dnd5eCharacterToolProficiency> findByToolName(String toolName);

    List<Dnd5eCharacterToolProficiency> findByExpertiseTrue();
}
