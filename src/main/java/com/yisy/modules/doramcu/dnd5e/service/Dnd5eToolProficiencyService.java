package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterToolProficiency;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingTool;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterToolProficiencyRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCraftingToolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for managing D&D 5e Tool Proficiencies (2024 PHB).
 * <p>
 * In the 2024 PHB, tool proficiencies grant concrete mechanical effects:
 * - Proficiency bonus to checks using the tool
 * - Combat uses (improvised weapons, field repairs)
 * - Exploration uses (navigation, foraging, crafting)
 * - "Use" action that makes tools actively useful in any situation
 * - Expertise doubles the proficiency bonus for certain classes
 * </p>
 * <p>
 * This service manages character-tool proficiency associations and resolves
 * tool-based skill checks with the full 2024 PHB mechanics.
 * </p>
 */
@Service
public class Dnd5eToolProficiencyService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eToolProficiencyService.class);
    private static final Random random = new Random();

    private final Dnd5eCharacterToolProficiencyRepository proficiencyRepository;
    private final Dnd5eCharacterRepository characterRepository;
    private final Dnd5eCraftingToolRepository toolRepository;

    public Dnd5eToolProficiencyService(Dnd5eCharacterToolProficiencyRepository proficiencyRepository,
                                        Dnd5eCharacterRepository characterRepository,
                                        Dnd5eCraftingToolRepository toolRepository) {
        this.proficiencyRepository = proficiencyRepository;
        this.characterRepository = characterRepository;
        this.toolRepository = toolRepository;
    }

    // ==================== Tool Lookup ====================

    /**
     * List all available tools with their mechanical effects.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eCraftingTool> listAllTools() {
        return toolRepository.findAll();
    }

    /**
     * Get a tool by name.
     */
    @Transactional(readOnly = true)
    public Dnd5eCraftingTool getToolByName(String name) {
        return toolRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Tool not found: " + name));
    }

    /**
     * List tools by type (e.g., "Artisan", "Musical", "Gaming").
     */
    @Transactional(readOnly = true)
    public List<Dnd5eCraftingTool> listToolsByType(String toolType) {
        return toolRepository.findByToolType(toolType);
    }

    /**
     * List tools by associated ability score.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eCraftingTool> listToolsByAbility(String ability) {
        return toolRepository.findByAssociatedAbility(ability);
    }

    // ==================== Character Proficiency Management ====================

    /**
     * Add a tool proficiency to a character.
     */
    @Transactional
    public Dnd5eCharacterToolProficiency addProficiency(Long characterId, String toolName, int levelAcquired) {
        // Verify character exists
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        // Verify tool exists
        Dnd5eCraftingTool tool = getToolByName(toolName);

        // Check for existing proficiency
        if (proficiencyRepository.existsByCharacterIdAndToolName(characterId, toolName)) {
            throw new IllegalStateException("Character already proficient with: " + toolName);
        }

        Dnd5eCharacterToolProficiency proficiency = new Dnd5eCharacterToolProficiency();
        proficiency.setCharacterId(characterId);
        proficiency.setCharacterName(character.getName());
        proficiency.setToolName(toolName);
        proficiency.setLevelAcquired(levelAcquired);
        proficiency.setActive(true);
        proficiency.setExpertise(false);

        Dnd5eCharacterToolProficiency saved = proficiencyRepository.save(proficiency);
        log.info("Added tool proficiency '{}' to character {}", toolName, character.getName());
        return saved;
    }

    /**
     * Remove a tool proficiency from a character.
     */
    @Transactional
    public void removeProficiency(Long characterId, String toolName) {
        boolean exists = proficiencyRepository.existsByCharacterIdAndToolName(characterId, toolName);
        if (!exists) {
            throw new IllegalArgumentException("Proficiency not found: " + toolName + " for character " + characterId);
        }
        proficiencyRepository.deleteByCharacterIdAndToolName(characterId, toolName);
        log.info("Removed tool proficiency '{}' from character {}", toolName, characterId);
    }

    /**
     * List all tool proficiencies for a character.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eCharacterToolProficiency> listProficiencies(Long characterId) {
        verifyCharacterExists(characterId);
        return proficiencyRepository.findByCharacterIdOrderByCreatedAtDesc(characterId);
    }

    /**
     * List active tool proficiencies for a character.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eCharacterToolProficiency> listActiveProficiencies(Long characterId) {
        verifyCharacterExists(characterId);
        return proficiencyRepository.findByCharacterIdAndActiveTrue(characterId);
    }

    /**
     * Check if a character is proficient with a specific tool.
     */
    @Transactional(readOnly = true)
    public boolean isProficient(Long characterId, String toolName) {
        return proficiencyRepository.findByCharacterIdAndToolName(characterId, toolName)
                .filter(Dnd5eCharacterToolProficiency::isActive)
                .isPresent();
    }

    /**
     * Check if a character has expertise with a specific tool.
     */
    @Transactional(readOnly = true)
    public boolean hasExpertise(Long characterId, String toolName) {
        return proficiencyRepository.findByCharacterIdAndToolName(characterId, toolName)
                .filter(Dnd5eCharacterToolProficiency::isExpertise)
                .isPresent();
    }

    /**
     * Set expertise on a tool proficiency (doubles proficiency bonus).
     */
    @Transactional
    public Dnd5eCharacterToolProficiency setExpertise(Long characterId, String toolName, boolean expertise) {
        Dnd5eCharacterToolProficiency proficiency = proficiencyRepository
                .findByCharacterIdAndToolName(characterId, toolName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Proficiency not found: " + toolName + " for character " + characterId));
        proficiency.setExpertise(expertise);
        Dnd5eCharacterToolProficiency saved = proficiencyRepository.save(proficiency);
        log.info("{} expertise on '{}' for character {}",
                expertise ? "Granted" : "Removed", toolName, characterId);
        return saved;
    }

    /**
     * Toggle the active state of a tool proficiency.
     */
    @Transactional
    public Dnd5eCharacterToolProficiency toggleActive(Long characterId, String toolName) {
        Dnd5eCharacterToolProficiency proficiency = proficiencyRepository
                .findByCharacterIdAndToolName(characterId, toolName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Proficiency not found: " + toolName + " for character " + characterId));
        proficiency.setActive(!proficiency.isActive());
        Dnd5eCharacterToolProficiency saved = proficiencyRepository.save(proficiency);
        log.info("Toggled proficiency '{}' for character {} to active={}",
                toolName, characterId, saved.isActive());
        return saved;
    }

    // ==================== Tool Check Resolution ====================

    /**
     * Resolve a tool-based skill check using 2024 PHB mechanics.
     *
     * @param characterId the character making the check
     * @param toolName    the tool being used
     * @param dc          the difficulty class
     * @return the check result with roll details
     */
    @Transactional(readOnly = true)
    public ToolCheckResult makeToolCheck(Long characterId, String toolName, int dc) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        Dnd5eCraftingTool tool = getToolByName(toolName);
        boolean proficient = isProficient(characterId, toolName);
        boolean expertise = hasExpertise(characterId, toolName);

        // Calculate ability modifier
        int abilityModifier = getAbilityModifier(character, tool.getAssociatedAbility());

        // Calculate proficiency bonus contribution
        int profBonus = 0;
        if (proficient) {
            profBonus = expertise ? character.getProficiencyBonus() * 2 : character.getProficiencyBonus();
        }

        int totalModifier = abilityModifier + profBonus;

        // Roll d20
        int roll = random.nextInt(20) + 1;
        int total = roll + totalModifier;
        boolean success = total >= dc;

        // Determine result description
        String result;
        if (roll == 20) {
            result = "Critical Success! Exceptional tool mastery.";
        } else if (roll == 1) {
            result = "Critical Failure! Tool malfunction or mishap.";
        } else if (success) {
            result = "Success! The tool serves its purpose effectively.";
        } else {
            result = "Failure. The tool check does not succeed.";
        }

        return new ToolCheckResult(
                roll, abilityModifier, profBonus, totalModifier, total, dc,
                success, proficient, expertise, result,
                tool.getName(), tool.getAssociatedAbility(),
                tool.getCombatUse(), tool.getExplorationUse(), tool.getUseAction()
        );
    }

    // ==================== Helper Methods ====================

    private void verifyCharacterExists(Long characterId) {
        if (!characterRepository.existsById(characterId)) {
            throw new IllegalArgumentException("Character not found: " + characterId);
        }
    }

    private int getAbilityModifier(Dnd5eCharacter character, String ability) {
        int score = switch (ability.toUpperCase()) {
            case "STR" -> character.getStrScore();
            case "DEX" -> character.getDexScore();
            case "CON" -> character.getConScore();
            case "INT" -> character.getIntScore();
            case "WIS" -> character.getWisScore();
            case "CHA" -> character.getChaScore();
            default -> 10;
        };
        return (score - 10) / 2;
    }

    // ==================== Tool Check Result DTO ====================

    /**
     * Result of a tool-based skill check.
     */
    public static class ToolCheckResult {
        private final int roll;
        private final int abilityModifier;
        private final int proficiencyBonus;
        private final int totalModifier;
        private final int total;
        private final int dc;
        private final boolean success;
        private final boolean proficient;
        private final boolean expertise;
        private final String result;
        private final String toolName;
        private final String associatedAbility;
        private final String combatUse;
        private final String explorationUse;
        private final String useAction;

        public ToolCheckResult(int roll, int abilityModifier, int proficiencyBonus,
                               int totalModifier, int total, int dc, boolean success,
                               boolean proficient, boolean expertise, String result,
                               String toolName, String associatedAbility,
                               String combatUse, String explorationUse, String useAction) {
            this.roll = roll;
            this.abilityModifier = abilityModifier;
            this.proficiencyBonus = proficiencyBonus;
            this.totalModifier = totalModifier;
            this.total = total;
            this.dc = dc;
            this.success = success;
            this.proficient = proficient;
            this.expertise = expertise;
            this.result = result;
            this.toolName = toolName;
            this.associatedAbility = associatedAbility;
            this.combatUse = combatUse;
            this.explorationUse = explorationUse;
            this.useAction = useAction;
        }

        public int getRoll() { return roll; }
        public int getAbilityModifier() { return abilityModifier; }
        public int getProficiencyBonus() { return proficiencyBonus; }
        public int getTotalModifier() { return totalModifier; }
        public int getTotal() { return total; }
        public int getDc() { return dc; }
        public boolean isSuccess() { return success; }
        public boolean isProficient() { return proficient; }
        public boolean isExpertise() { return expertise; }
        public String getResult() { return result; }
        public String getToolName() { return toolName; }
        public String getAssociatedAbility() { return associatedAbility; }
        public String getCombatUse() { return combatUse; }
        public String getExplorationUse() { return explorationUse; }
        public String getUseAction() { return useAction; }
    }
}
