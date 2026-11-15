package com.yisy.modules.doramcu.earth2026.survival.base;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for managing base structures in Earth2026 survival mode.
 * <p>
 * Provides structure placement, upgrades, and management. Each character
 * can build multiple structures at their base location.
 * </p>
 */
@Service
public class BaseBuildingService {

    private static final Logger log = LoggerFactory.getLogger(BaseBuildingService.class);

    // Maximum structures per type per character
    private static final Map<String, Integer> MAX_STRUCTURES_PER_TYPE = Map.of(
            "SHELTER", 1,
            "STORAGE", 2,
            "WORKSHOP", 1,
            "DEFENSE", 3,
            "GARDEN", 2
    );

    // Default structure definitions
    private static final Map<String, StructureDefinition> STRUCTURE_DEFINITIONS = Map.of(
            "SHELTER", new StructureDefinition("Wooden Cabin", "A basic wooden shelter.", 3, 100, 0, 15, 0, false, false, 0),
            "STORAGE", new StructureDefinition("Storage Shed", "A shed for storing supplies.", 3, 80, 0, 0, 50, false, false, 0),
            "WORKSHOP", new StructureDefinition("Workshop", "A workshop for crafting.", 2, 90, 0, 0, 0, true, false, 0),
            "DEFENSE", new StructureDefinition("Wooden Wall", "A defensive wall.", 2, 120, 0, 0, 0, false, true, 20),
            "GARDEN", new StructureDefinition("Garden Plot", "A plot for growing food.", 2, 60, 5, 0, 0, false, false, 0)
    );

    private final BaseStructureRepository structureRepository;
    private final Earth2026CharacterRepository characterRepository;

    public BaseBuildingService(BaseStructureRepository structureRepository,
                                Earth2026CharacterRepository characterRepository) {
        this.structureRepository = structureRepository;
        this.characterRepository = characterRepository;
    }

    // ==================== Structure Queries ====================

    @Transactional(readOnly = true)
    public List<BaseStructure> getStructures(Long characterId) {
        verifyCharacterExists(characterId);
        return structureRepository.findByCharacterId(characterId);
    }

    @Transactional(readOnly = true)
    public List<BaseStructure> getStructuresByType(Long characterId, String type) {
        verifyCharacterExists(characterId);
        return structureRepository.findByCharacterIdAndStructureType(characterId, type);
    }

    // ==================== Building ====================

    /**
     * Build a new structure at the character's base.
     *
     * @param characterId the character ID
     * @param structureType the structure type (SHELTER, STORAGE, WORKSHOP, DEFENSE, GARDEN)
     * @return the built structure
     */
    @Transactional
    public BaseStructure buildStructure(Long characterId, String structureType) {
        verifyCharacterExists(characterId);

        StructureDefinition definition = STRUCTURE_DEFINITIONS.get(structureType);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown structure type: " + structureType);
        }

        // Check max structures per type
        int currentCount = structureRepository.findByCharacterIdAndStructureType(characterId, structureType).size();
        int maxCount = MAX_STRUCTURES_PER_TYPE.getOrDefault(structureType, 1);
        if (currentCount >= maxCount) {
            throw new IllegalStateException("Maximum number of " + structureType + " structures reached (" + maxCount + ")");
        }

        // Create structure
        BaseStructure structure = new BaseStructure(characterId, structureType, definition.name);
        structure.setDescription(definition.description);
        structure.setLevel(1);
        structure.setMaxLevel(definition.maxLevel);
        structure.setHealth(definition.maxHealth);
        structure.setMaxHealth(definition.maxHealth);
        structure.setHungerBonus(definition.hungerBonus);
        structure.setExposureReduction(definition.exposureReduction);
        structure.setStorageBonus(definition.storageBonus);
        structure.setWorkshop(definition.isWorkshop);
        structure.setDefense(definition.isDefense);
        structure.setDefenseValue(definition.defenseValue);

        BaseStructure saved = structureRepository.save(structure);
        log.info("Character {} built {} (level 1)", characterId, definition.name);
        return saved;
    }

    // ==================== Upgrades ====================

    /**
     * Upgrade a structure to the next level.
     *
     * @param structureId the structure ID
     * @return the upgraded structure
     */
    @Transactional
    public BaseStructure upgradeStructure(Long structureId) {
        BaseStructure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new IllegalArgumentException("Structure not found: " + structureId));

        if (structure.isMaxLevel()) {
            throw new IllegalStateException("Structure is already at maximum level (" + structure.getMaxLevel() + ")");
        }

        int newLevel = structure.getLevel() + 1;
        structure.setLevel(newLevel);
        structure.setUpgradedAt(LocalDateTime.now());

        // Apply level bonuses
        applyLevelBonuses(structure, newLevel);

        BaseStructure saved = structureRepository.save(structure);
        log.info("Upgraded {} to level {}", saved.getName(), newLevel);
        return saved;
    }

    /**
     * Repair a structure.
     *
     * @param structureId the structure ID
     * @param amount the amount to repair
     * @return the repaired structure
     */
    @Transactional
    public BaseStructure repairStructure(Long structureId, int amount) {
        BaseStructure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new IllegalArgumentException("Structure not found: " + structureId));

        if (structure.isDestroyed()) {
            throw new IllegalStateException("Structure is destroyed and cannot be repaired");
        }

        structure.setHealth(structure.getHealth() + amount);
        structureRepository.save(structure);
        log.info("Repaired {} by {} health", structure.getName(), amount);
        return structure;
    }

    /**
     * Demolish a structure.
     *
     * @param structureId the structure ID
     */
    @Transactional
    public void demolishStructure(Long structureId) {
        BaseStructure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new IllegalArgumentException("Structure not found: " + structureId));

        structureRepository.delete(structure);
        log.info("Demolished {}", structure.getName());
    }

    // ==================== Helpers ====================

    private void applyLevelBonuses(BaseStructure structure, int level) {
        String type = structure.getStructureType();

        switch (type) {
            case "SHELTER" -> {
                structure.setExposureReduction(15 * level);
                structure.setMaxHealth(100 + 50 * (level - 1));
                structure.setHealth(structure.getMaxHealth());
            }
            case "STORAGE" -> {
                structure.setStorageBonus(50 * level);
                structure.setMaxHealth(80 + 30 * (level - 1));
                structure.setHealth(structure.getMaxHealth());
            }
            case "WORKSHOP" -> {
                structure.setMaxHealth(90 + 40 * (level - 1));
                structure.setHealth(structure.getMaxHealth());
            }
            case "DEFENSE" -> {
                structure.setDefenseValue(20 * level);
                structure.setMaxHealth(120 + 60 * (level - 1));
                structure.setHealth(structure.getMaxHealth());
            }
            case "GARDEN" -> {
                structure.setHungerBonus(5 * level);
                structure.setMaxHealth(60 + 20 * (level - 1));
                structure.setHealth(structure.getMaxHealth());
            }
        }
    }

    private void verifyCharacterExists(Long characterId) {
        characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
    }

    // ==================== Inner Classes ====================

    private static class StructureDefinition {
        final String name;
        final String description;
        final int maxLevel;
        final int maxHealth;
        final int hungerBonus;
        final int exposureReduction;
        final int storageBonus;
        final boolean isWorkshop;
        final boolean isDefense;
        final int defenseValue;

        StructureDefinition(String name, String description, int maxLevel, int maxHealth,
                             int hungerBonus, int exposureReduction, int storageBonus,
                             boolean isWorkshop, boolean isDefense, int defenseValue) {
            this.name = name;
            this.description = description;
            this.maxLevel = maxLevel;
            this.maxHealth = maxHealth;
            this.hungerBonus = hungerBonus;
            this.exposureReduction = exposureReduction;
            this.storageBonus = storageBonus;
            this.isWorkshop = isWorkshop;
            this.isDefense = isDefense;
            this.defenseValue = defenseValue;
        }
    }
}
