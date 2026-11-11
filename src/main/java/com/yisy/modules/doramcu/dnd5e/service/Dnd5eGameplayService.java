package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.dto.SkillCheckResponse;
import com.yisy.modules.doramcu.dnd5e.dto.SpellCastResponse;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSpell;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSpellSlot;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eSpellRepository;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for D&D 5e gameplay actions: skill checks, saving throws, spell casting, resting, and death saves.
 */
@Service
public class Dnd5eGameplayService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eGameplayService.class);

    // Skill to ability score mapping
    private static final Map<String, String> SKILL_ABILITY_MAP;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("Acrobatics", "Dex");
        map.put("Animal Handling", "Wis");
        map.put("Arcana", "Int");
        map.put("Athletics", "Str");
        map.put("Deception", "Cha");
        map.put("History", "Int");
        map.put("Insight", "Wis");
        map.put("Intimidation", "Cha");
        map.put("Investigation", "Int");
        map.put("Medicine", "Wis");
        map.put("Nature", "Int");
        map.put("Perception", "Wis");
        map.put("Performance", "Cha");
        map.put("Persuasion", "Cha");
        map.put("Religion", "Int");
        map.put("Sleight of Hand", "Dex");
        map.put("Stealth", "Dex");
        map.put("Survival", "Wis");
        SKILL_ABILITY_MAP = Map.copyOf(map);
    }

    // XP thresholds for levels 1-20
    private static final int[] XP_THRESHOLDS = {
            0, 300, 900, 2700, 6500, 14000, 23000, 34000, 48000, 64000,
            85000, 100000, 120000, 140000, 165000, 195000, 225000, 265000, 305000, 355000
    };

    private final Dnd5eCharacterRepository characterRepository;
    private final Dnd5eSpellRepository spellRepository;
    private final Dnd5eDiceService diceService;

    public Dnd5eGameplayService(Dnd5eCharacterRepository characterRepository,
                                 Dnd5eSpellRepository spellRepository,
                                 Dnd5eDiceService diceService) {
        this.characterRepository = characterRepository;
        this.spellRepository = spellRepository;
        this.diceService = diceService;
    }

    /**
     * Perform a skill check.
     */
    @Transactional
    public SkillCheckResponse skillCheck(Long characterId, String skill, int dc, boolean advantage, boolean disadvantage) {
        Dnd5eCharacter character = getCharacter(characterId);

        // Get ability modifier for the skill
        String ability = SKILL_ABILITY_MAP.getOrDefault(skill, "Str");
        int abilityMod = getAbilityModifier(character, ability);

        // Check for proficiency bonus
        boolean proficient = isProficient(character, skill);
        int modifier = abilityMod + (proficient ? character.getProficiencyBonus() : 0);

        // Roll d20
        int d20Roll;
        if (advantage && !disadvantage) {
            d20Roll = Math.max(DiceUtil.rollD20(), DiceUtil.rollD20());
        } else if (disadvantage && !advantage) {
            d20Roll = Math.min(DiceUtil.rollD20(), DiceUtil.rollD20());
        } else {
            d20Roll = DiceUtil.rollD20();
        }

        int total = d20Roll + modifier;
        boolean success = total >= dc;

        SkillCheckResponse response = new SkillCheckResponse();
        response.setType("skill_check");
        response.setSkill(skill);
        response.setD20Roll(d20Roll);
        response.setModifier(modifier);
        response.setTotal(total);
        response.setAdvantage(advantage);
        response.setDisadvantage(disadvantage);
        response.setSuccess(success);
        response.setDc(dc);
        response.setDescription(String.format("%s %s: %d + %d = %d (%s vs DC %d)",
                skill, ability, d20Roll, modifier, total, success ? "SUCCESS" : "FAIL", dc));

        log.info("Skill check: {}", response.getDescription());
        return response;
    }

    /**
     * Perform a saving throw.
     */
    @Transactional
    public SkillCheckResponse savingThrow(Long characterId, String ability, int dc, boolean advantage, boolean disadvantage) {
        Dnd5eCharacter character = getCharacter(characterId);

        int abilityMod = getAbilityModifier(character, ability);
        boolean proficient = isSavingThrowProficient(character, ability);
        int modifier = abilityMod + (proficient ? character.getProficiencyBonus() : 0);

        // Roll d20
        int d20Roll;
        if (advantage && !disadvantage) {
            d20Roll = Math.max(DiceUtil.rollD20(), DiceUtil.rollD20());
        } else if (disadvantage && !advantage) {
            d20Roll = Math.min(DiceUtil.rollD20(), DiceUtil.rollD20());
        } else {
            d20Roll = DiceUtil.rollD20();
        }

        int total = d20Roll + modifier;
        boolean success = total >= dc;

        SkillCheckResponse response = new SkillCheckResponse();
        response.setType("saving_throw");
        response.setSkill(ability);
        response.setD20Roll(d20Roll);
        response.setModifier(modifier);
        response.setTotal(total);
        response.setAdvantage(advantage);
        response.setDisadvantage(disadvantage);
        response.setSuccess(success);
        response.setDc(dc);
        response.setDescription(String.format("%s Saving Throw: %d + %d = %d (%s vs DC %d)",
                ability, d20Roll, modifier, total, success ? "SUCCESS" : "FAIL", dc));

        log.info("Saving throw: {}", response.getDescription());
        return response;
    }

    /**
     * Cast a spell, consuming a spell slot.
     */
    @Transactional
    public SpellCastResponse castSpell(Long characterId, Long spellId, int slotLevelOverride) {
        Dnd5eCharacter character = getCharacter(characterId);
        Dnd5eSpell spell = spellRepository.findById(spellId)
                .orElseThrow(() -> new IllegalArgumentException("Spell not found: " + spellId));

        // Determine slot level to use
        int slotLevel = slotLevelOverride > 0 ? slotLevelOverride : spell.getLevel();
        if (slotLevel < spell.getLevel()) {
            throw new IllegalArgumentException("Cannot cast level " + spell.getLevel() + " spell using level " + slotLevel + " slot");
        }

        // Find and consume a spell slot
        Dnd5eSpellSlot spellSlot = null;
        for (Dnd5eSpellSlot s : character.getSpellSlots()) {
            if (s.getSlotLevel() == slotLevel && s.availableSlots() > 0) {
                spellSlot = s;
                break;
            }
        }

        if (spellSlot == null) {
            // Try higher level slots
            for (Dnd5eSpellSlot s : character.getSpellSlots()) {
                if (s.getSlotLevel() > slotLevel && s.availableSlots() > 0) {
                    spellSlot = s;
                    slotLevel = s.getSlotLevel();
                    break;
                }
            }
        }

        if (spellSlot == null || spellSlot.availableSlots() <= 0) {
            throw new IllegalArgumentException("No available spell slots at level " + slotLevel + " or higher");
        }

        // Consume the slot
        spellSlot.setUsedSlots(spellSlot.getUsedSlots() + 1);
        characterRepository.save(character);

        // Roll damage/healing if applicable
        int damageRoll = 0;
        int healingRoll = 0;
        if (spell.getDamageFormula() != null && !spell.getDamageFormula().isEmpty()) {
            try {
                damageRoll = rollDamageFormula(spell.getDamageFormula(), character.getLevel());
            } catch (Exception e) {
                log.warn("Failed to roll damage for {}: {}", spell.getName(), e.getMessage());
            }
        }
        if (spell.getHealingFormula() != null && !spell.getHealingFormula().isEmpty()) {
            try {
                healingRoll = rollDamageFormula(spell.getHealingFormula(), character.getLevel());
            } catch (Exception e) {
                log.warn("Failed to roll healing for {}: {}", spell.getName(), e.getMessage());
            }
        }

        SpellCastResponse response = new SpellCastResponse();
        response.setSpellName(spell.getName());
        response.setSpellLevel(spell.getLevel());
        response.setSlotLevelUsed(slotLevel);
        response.setConcentration(spell.isConcentration());
        response.setRitual(spell.isRitual());
        response.setDamageRoll(damageRoll);
        response.setHealingRoll(healingRoll);
        response.setDamageFormula(spell.getDamageFormula());
        response.setHealingFormula(spell.getHealingFormula());
        response.setSlotsRemaining(spellSlot.availableSlots());
        response.setDescription(String.format("Cast %s (level %d) using level %d slot. %d slots remaining.",
                spell.getName(), spell.getLevel(), slotLevel, spellSlot.availableSlots()));

        log.info("Spell cast: {}", response.getDescription());
        return response;
    }

    /**
     * Perform a short rest.
     */
    @Transactional
    public Dnd5eCharacter shortRest(Long characterId, int hitDiceToSpend) {
        Dnd5eCharacter character = getCharacter(characterId);

        // Roll hit dice for healing
        int hitDieValue = getHitDieValue(character.getHitDice());
        int totalHealing = DiceUtil.rollNdX(hitDiceToSpend, hitDieValue);
        totalHealing += hitDiceToSpend * character.getConMod(); // CON mod per hit die

        // Apply healing
        int newHp = Math.min(character.getMaxHitPoints(), character.getHitPoints() + totalHealing);
        character.setHitPoints(newHp);

        // Reset temp HP
        character.setTempHitPoints(0);

        // Recover some spell slots (half casters recover half their slots)
        // For simplicity, we'll recover 1 slot per 2 hit dice spent
        int slotsToRecover = hitDiceToSpend / 2;
        for (Dnd5eSpellSlot slot : character.getSpellSlots()) {
            if (slotsToRecover > 0 && slot.getUsedSlots() > 0) {
                int recover = Math.min(slotsToRecover, slot.getUsedSlots());
                slot.setUsedSlots(slot.getUsedSlots() - recover);
                slotsToRecover -= recover;
            }
        }

        characterRepository.save(character);
        log.info("Short rest: {} healed {} HP from {} hit dice", character.getName(), totalHealing, hitDiceToSpend);
        return character;
    }

    /**
     * Perform a long rest.
     */
    @Transactional
    public Dnd5eCharacter longRest(Long characterId) {
        Dnd5eCharacter character = getCharacter(characterId);

        // Full HP recovery
        character.setHitPoints(character.getMaxHitPoints());

        // Reset temp HP
        character.setTempHitPoints(0);

        // Reset exhaustion (reduce by 1)
        if (character.getExhaustionLevel() > 0) {
            character.setExhaustionLevel(character.getExhaustionLevel() - 1);
        }

        // Reset death saves
        character.setDeathSaveSuccesses(0);
        character.setDeathSaveFailures(0);

        // Recover all spell slots
        for (Dnd5eSpellSlot slot : character.getSpellSlots()) {
            slot.setUsedSlots(0);
        }

        characterRepository.save(character);
        log.info("Long rest: {} fully recovered", character.getName());
        return character;
    }

    /**
     * Perform a death saving throw.
     */
    @Transactional
    public SkillCheckResponse deathSavingThrow(Long characterId, boolean advantage, boolean disadvantage) {
        Dnd5eCharacter character = getCharacter(characterId);

        // Roll d20
        int d20Roll;
        if (advantage && !disadvantage) {
            d20Roll = Math.max(DiceUtil.rollD20(), DiceUtil.rollD20());
        } else if (disadvantage && !advantage) {
            d20Roll = Math.min(DiceUtil.rollD20(), DiceUtil.rollD20());
        } else {
            d20Roll = DiceUtil.rollD20();
        }

        boolean success;
        if (d20Roll >= 10) {
            success = true;
            character.setDeathSaveSuccesses(character.getDeathSaveSuccesses() + 1);
        } else {
            success = false;
            character.setDeathSaveFailures(character.getDeathSaveFailures() + 1);
        }

        // Check for instant death (natural 1 = 2 failures)
        if (d20Roll == 1) {
            character.setDeathSaveFailures(character.getDeathSaveFailures() + 1);
        }

        // Check for stabilization (3 successes)
        String status = "dying";
        if (character.getDeathSaveSuccesses() >= 3) {
            status = "stable";
            character.setDeathSaveSuccesses(0);
            character.setDeathSaveFailures(0);
        }

        // Check for death (3 failures)
        if (character.getDeathSaveFailures() >= 3) {
            status = "dead";
        }

        characterRepository.save(character);

        SkillCheckResponse response = new SkillCheckResponse();
        response.setType("death_save");
        response.setSkill("Death Saving Throw");
        response.setD20Roll(d20Roll);
        response.setModifier(0);
        response.setTotal(d20Roll);
        response.setSuccess(success);
        response.setDc(10);
        response.setDescription(String.format("Death Save: rolled %d (%s). Status: %s. Successes: %d, Failures: %d",
                d20Roll, success ? "success" : "failure", status,
                character.getDeathSaveSuccesses(), character.getDeathSaveFailures()));

        log.info("Death save: {}", response.getDescription());
        return response;
    }

    /**
     * Award XP to a character and check for level up.
     */
    @Transactional
    public Dnd5eCharacter awardXp(Long characterId, int xp) {
        Dnd5eCharacter character = getCharacter(characterId);
        character.setExperiencePoints(character.getExperiencePoints() + xp);

        // Check for level up
        int currentLevel = character.getLevel();
        if (currentLevel < 20 && character.getExperiencePoints() >= XP_THRESHOLDS[currentLevel]) {
            character.setLevel(currentLevel + 1);
            character.setProficiencyBonus(getProficiencyBonus(currentLevel + 1));

            // Increase HP
            int hitDieValue = getHitDieValue(character.getHitDice());
            int hpGain = Math.max(1, DiceUtil.rollDSides(hitDieValue) + character.getConMod());
            character.setMaxHitPoints(character.getMaxHitPoints() + hpGain);
            character.setHitPoints(character.getMaxHitPoints());

            log.info("Level up! {} is now level {}", character.getName(), character.getLevel());
        }

        characterRepository.save(character);
        return character;
    }

    // --- Helper Methods ---

    private Dnd5eCharacter getCharacter(Long id) {
        return characterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + id));
    }

    private int getAbilityModifier(Dnd5eCharacter character, String ability) {
        return switch (ability.toLowerCase()) {
            case "str" -> character.getStrMod();
            case "dex" -> character.getDexMod();
            case "con" -> character.getConMod();
            case "int" -> character.getIntMod();
            case "wis" -> character.getWisMod();
            case "cha" -> character.getChaMod();
            default -> 0;
        };
    }

    private boolean isProficient(Dnd5eCharacter character, String skill) {
        return character.getSkills().stream()
                .anyMatch(s -> skill.equalsIgnoreCase(s.getSkillName()));
    }

    private boolean isSavingThrowProficient(Dnd5eCharacter character, String ability) {
        // Simplified: check if class has saving throw proficiency
        String cls = character.getCharacterClass();
        return switch (cls.toLowerCase()) {
            case "barbarian" -> "str".equals(ability.toLowerCase()) || "con".equals(ability.toLowerCase());
            case "bard" -> "dex".equals(ability.toLowerCase()) || "cha".equals(ability.toLowerCase());
            case "cleric" -> "wis".equals(ability.toLowerCase()) || "cha".equals(ability.toLowerCase());
            case "druid" -> "int".equals(ability.toLowerCase()) || "wis".equals(ability.toLowerCase());
            case "fighter" -> "str".equals(ability.toLowerCase()) || "con".equals(ability.toLowerCase());
            case "monk" -> "dex".equals(ability.toLowerCase()) || "wis".equals(ability.toLowerCase());
            case "paladin" -> "wis".equals(ability.toLowerCase()) || "cha".equals(ability.toLowerCase());
            case "ranger" -> "str".equals(ability.toLowerCase()) || "dex".equals(ability.toLowerCase());
            case "rogue" -> "dex".equals(ability.toLowerCase()) || "int".equals(ability.toLowerCase());
            case "sorcerer" -> "con".equals(ability.toLowerCase()) || "cha".equals(ability.toLowerCase());
            case "warlock" -> "wis".equals(ability.toLowerCase()) || "cha".equals(ability.toLowerCase());
            case "wizard" -> "int".equals(ability.toLowerCase()) || "wis".equals(ability.toLowerCase());
            default -> false;
        };
    }

    private int getHitDieValue(String hitDice) {
        if (hitDice == null) return 8;
        try {
            return Integer.parseInt(hitDice.replace("d", ""));
        } catch (NumberFormatException e) {
            return 8;
        }
    }

    private int getProficiencyBonus(int level) {
        if (level < 1 || level > 20) return 2;
        int[] PROFICIENCY_BONUS = {0, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6};
        return PROFICIENCY_BONUS[level];
    }

    private int rollDamageFormula(String formula, int level) {
        // Parse "XdY+Z" format
        try {
            String clean = formula.replace(" ", "").toLowerCase();
            if (clean.contains("d")) {
                String[] parts = clean.split("d");
                int count = parts[0].isEmpty() ? 1 : Integer.parseInt(parts[0]);
                String rest = parts[1];
                int sides;
                int modifier = 0;

                // Handle "+Z" suffix
                if (rest.contains("+")) {
                    String[] subParts = rest.split("\\+");
                    sides = Integer.parseInt(subParts[0]);
                    modifier = Integer.parseInt(subParts[1]);
                } else if (rest.contains("-")) {
                    String[] subParts = rest.split("-");
                    sides = Integer.parseInt(subParts[0]);
                    modifier = -Integer.parseInt(subParts[1]);
                } else {
                    sides = Integer.parseInt(rest);
                }

                return DiceUtil.rollNdX(count, sides) + modifier;
            }
            return Integer.parseInt(clean);
        } catch (Exception e) {
            log.warn("Failed to parse damage formula: {}", formula);
            return 0;
        }
    }
}
