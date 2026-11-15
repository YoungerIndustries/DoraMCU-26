package com.yisy.modules.doramcu.earth2026.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Companion;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CompanionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * Service for Earth 2026 Companions (pets, familiars, working animals).
 * Provides CRUD operations, bond management, and training progression.
 */
@Service
public class Earth2026CompanionService {

    private final Earth2026CompanionRepository repository;

    @Autowired
    public Earth2026CompanionService(Earth2026CompanionRepository repository) {
        this.repository = repository;
    }

    // --- CRUD Operations ---

    /**
     * Find all companions.
     */
    public List<Earth2026Companion> findAll() {
        return repository.findAll();
    }

    /**
     * Find companion by ID.
     */
    public Optional<Earth2026Companion> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * Save a companion.
     */
    public Earth2026Companion save(Earth2026Companion companion) {
        return repository.save(companion);
    }

    /**
     * Delete a companion by ID.
     */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    // --- Query Operations ---

    /**
     * Find companions by owner character ID.
     */
    public List<Earth2026Companion> findByOwnerCharacterId(Long ownerCharacterId) {
        return repository.findByOwnerCharacterId(ownerCharacterId);
    }

    /**
     * Find active companions by owner character ID.
     */
    public List<Earth2026Companion> findActiveByOwnerCharacterId(Long ownerCharacterId) {
        return repository.findByOwnerCharacterIdAndActive(ownerCharacterId, true);
    }

    /**
     * Find companions by species ID.
     */
    public List<Earth2026Companion> findBySpeciesId(String speciesId) {
        return repository.findBySpeciesId(speciesId);
    }

    /**
     * Find companions by companion type.
     */
    public List<Earth2026Companion> findByCompanionType(Earth2026Companion.CompanionType companionType) {
        return repository.findByCompanionType(companionType);
    }

    /**
     * Find companions by AI character ID.
     */
    public List<Earth2026Companion> findByAiCharacterId(Long aiCharacterId) {
        return repository.findByAiCharacterId(aiCharacterId);
    }

    /**
     * Find companions by health status.
     */
    public List<Earth2026Companion> findByHealth(Earth2026Companion.HealthStatus health) {
        return repository.findByHealth(health);
    }

    /**
     * Find companions by owner and companion type.
     */
    public List<Earth2026Companion> findByOwnerAndType(Long ownerCharacterId, Earth2026Companion.CompanionType companionType) {
        return repository.findByOwnerCharacterIdAndCompanionType(ownerCharacterId, companionType);
    }

    // --- Bond Management ---

    /**
     * Increase the bond score between a companion and its owner.
     * @param companionId the companion ID
     * @param amount the amount to increase (clamped to 0-100)
     * @return the updated companion
     */
    public Optional<Earth2026Companion> increaseBond(Long companionId, int amount) {
        return repository.findById(companionId).map(companion -> {
            companion.increaseBond(amount);
            return repository.save(companion);
        });
    }

    /**
     * Decrease the bond score between a companion and its owner.
     * @param companionId the companion ID
     * @param amount the amount to decrease (clamped to 0-100)
     * @return the updated companion
     */
    public Optional<Earth2026Companion> decreaseBond(Long companionId, int amount) {
        return repository.findById(companionId).map(companion -> {
            companion.decreaseBond(amount);
            return repository.save(companion);
        });
    }

    // --- Training Progression ---

    /**
     * Advance the training level of a companion.
     * Progression: UNTRAINED -> BASIC -> ADVANCED -> MASTER
     * @param companionId the companion ID
     * @return the updated companion, or empty if not found or already at MASTER
     */
    public Optional<Earth2026Companion> advanceTraining(Long companionId) {
        return repository.findById(companionId).map(companion -> {
            Earth2026Companion.TrainingLevel current = companion.getTrainingLevel();
            if (current != Earth2026Companion.TrainingLevel.MASTER) {
                Earth2026Companion.TrainingLevel[] levels = Earth2026Companion.TrainingLevel.values();
                int nextIndex = current.ordinal() + 1;
                if (nextIndex < levels.length) {
                    companion.setTrainingLevel(levels[nextIndex]);
                }
            }
            return repository.save(companion);
        });
    }

    // --- Health Management ---

    /**
     * Heal a companion by the specified amount.
     * @param companionId the companion ID
     * @param amount the amount to heal
     * @return the updated companion
     */
    public Optional<Earth2026Companion> heal(Long companionId, int amount) {
        return repository.findById(companionId).map(companion -> {
            companion.heal(amount);
            return repository.save(companion);
        });
    }

    /**
     * Damage a companion by the specified amount.
     * @param companionId the companion ID
     * @param amount the amount of damage
     * @return the updated companion
     */
    public Optional<Earth2026Companion> damage(Long companionId, int amount) {
        return repository.findById(companionId).map(companion -> {
            companion.takeDamage(amount);
            return repository.save(companion);
        });
    }
}
