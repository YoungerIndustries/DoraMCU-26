package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Companion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Earth 2026 Companion entities.
 */
@Repository
public interface Earth2026CompanionRepository extends JpaRepository<Earth2026Companion, Long> {

    /**
     * Find companions by owner character ID.
     */
    List<Earth2026Companion> findByOwnerCharacterId(Long ownerCharacterId);

    /**
     * Find companions by owner character ID and active status.
     */
    List<Earth2026Companion> findByOwnerCharacterIdAndActive(Long ownerCharacterId, boolean active);

    /**
     * Find companions by species ID.
     */
    List<Earth2026Companion> findBySpeciesId(String speciesId);

    /**
     * Find companions by companion type.
     */
    List<Earth2026Companion> findByCompanionType(Earth2026Companion.CompanionType companionType);

    /**
     * Find companions by species ID and active status.
     */
    List<Earth2026Companion> findBySpeciesIdAndActive(String speciesId, boolean active);

    /**
     * Find companions by AI character ID.
     */
    List<Earth2026Companion> findByAiCharacterId(Long aiCharacterId);

    /**
     * Find companions by health status.
     */
    List<Earth2026Companion> findByHealth(Earth2026Companion.HealthStatus health);

    /**
     * Find active companions by training level.
     */
    @Query("SELECT c FROM Earth2026Companion c WHERE c.trainingLevel = :trainingLevel AND c.active = true")
    List<Earth2026Companion> findByTrainingLevelActive(@Param("trainingLevel") Earth2026Companion.TrainingLevel trainingLevel);

    /**
     * Find companions by owner and companion type.
     */
    List<Earth2026Companion> findByOwnerCharacterIdAndCompanionType(Long ownerCharacterId, Earth2026Companion.CompanionType companionType);
}
