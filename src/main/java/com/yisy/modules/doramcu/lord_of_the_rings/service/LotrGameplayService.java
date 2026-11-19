package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.dto.SkillCheckResponse;
import com.yisy.modules.doramcu.lord_of_the_rings.dto.LotrSpellCastResponse;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacter;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrSpell;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrSpellSlot;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrCharacterRepository;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrSpellRepository;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for LOTR gameplay actions: skill checks, saving throws, spell casting, resting, and corruption checks.
 * Uses LOTR-specific mechanics: Hope/Shadow tracks, corruption, and 2d6 narrative resolution.
 */
@Service
public class LotrGameplayService {

    private static final Logger log = LoggerFactory.getLogger(LotrGameplayService.class);

    // LOTR skills mapped to ability scores
    private static final Map<String, String> SKILL_ABILITY_MAP;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("Athletics", "Str");
        map.put("Acrobatics", "Dex");
        map.put("Stealth", "Dex");
        map.put("Perception", "Wis");
        map.put("Insight", "Wis");
        map.put("Survival", "Wis");
        map.put("Lore", "Int");
        map.put("History", "Int");
        map.put("Investigation", "Int");
        map.put("Persuasion", "Cha");
        map.put("Deception", "Cha");
        map.put("Intimidation", "Cha");
        map.put("Performance", "Cha");
        map.put("Medicine", "Wis");
        map.put("Nature", "Int");
        map.put("Religion", "Int");
        map.put("Sleight of Hand", "Dex");
        SKILL_ABILITY_MAP = Map.copyOf(map);
    }

    // XP thresholds for LOTR levels 1-10
    private static final int[] XP_THRESHOLDS = {
            0, 300, 900, 2700, 6500, 14000, 23000, 34000, 48000, 64000
    };

    private final LotrCharacterRepository characterRepository;
    private final LotrSpellRepository spellRepository;
    private final LotrDiceService diceService;

    public LotrGameplayService(LotrCharacterRepository characterRepository,
                                LotrSpellRepository spellRepository,
                                LotrDiceService diceService) {
        this.characterRepository = characterRepository;
        this.spellRepository = spellRepository;
        this.diceService = diceService;
    }

    /**
     * Perform a skill check using LOTR mechanics.
     */
    @Transactional
    public SkillCheckResponse skillCheck(Long characterId, String skill, int dc, boolean hope, boolean shadow) {
        LotrCharacter character = getCharacter(characterId);

        String ability = SKILL_ABILITY_MAP.getOrDefault(skill, "Str");
        int abilityMod = getAbilityModifier(character, ability);

        boolean proficient = isProficient(character, skill);
        int modifier = abilityMod + (proficient ? character.getProficiencyBonus() : 0);

        // Apply hope/shadow modifiers
        if (hope) modifier += 1;
        if (shadow) modifier -= 1;

        int d20Roll;
        if (hope && !shadow) {
            d20Roll = Math.max(DiceUtil.rollD20(), DiceUtil.rollD20());
        } else if (shadow && !hope) {
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
        response.setAdvantage(hope);
        response.setDisadvantage(shadow);
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
    public SkillCheckResponse savingThrow(Long characterId, String ability, int dc, boolean hope, boolean shadow) {
        LotrCharacter character = getCharacter(characterId);
        int abilityMod = getAbilityModifier(character, ability);

        boolean proficient = isSavingThrowProficient(character, ability);
        int modifier = abilityMod + (proficient ? character.getProficiencyBonus() : 0);

        if (hope) modifier += 1;
        if (shadow) modifier -= 1;

        int d20Roll;
        if (hope && !shadow) {
            d20Roll = Math.max(DiceUtil.rollD20(), DiceUtil.rollD20());
        } else if (shadow && !hope) {
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
        response.setAdvantage(hope);
        response.setDisadvantage(shadow);
        response.setSuccess(success);
        response.setDc(dc);
        response.setDescription(String.format("%s Save: %d + %d = %d (%s vs DC %d)",
                ability, d20Roll, modifier, total, success ? "SUCCESS" : "FAIL", dc));

        log.info("Saving throw: {}", response.getDescription());
        return response;
    }

    /**
     * Cast a spell using LOTR magic system.
     */
    @Transactional
    public LotrSpellCastResponse castSpell(Long characterId, Long spellId) {
        LotrCharacter character = getCharacter(characterId);
        LotrSpell spell = getSpell(spellId);

        // Check if character knows the spell
        if (!character.getKnownSpellIds().contains(spellId)) {
            throw new IllegalArgumentException("Character does not know this spell");
        }

        // Check spell slots
        boolean slotAvailable = hasSpellSlot(character, spell.getLevel());
        if (!slotAvailable) {
            throw new IllegalArgumentException("No spell slots available for this spell level");
        }

        // Roll for spell effect
        int spellCheck = DiceUtil.rollD20() + getAbilityModifier(character, spell.getAbilityScore());
        boolean success = spellCheck >= spell.getDC();

        LotrSpellCastResponse response = new LotrSpellCastResponse();
        response.setSpellName(spell.getName());
        response.setSpellLevel(spell.getLevel());
        response.setSuccess(success);
        response.setSpellCheck(spellCheck);
        response.setDc(spell.getDC());
        response.setDescription(String.format("%s casts %s: %d vs DC %d (%s)",
                character.getName(), spell.getName(), spellCheck, spell.getDC(),
                success ? "SUCCESS" : "FAIL"));

        // Apply spell effects
        if (success) {
            int damage = DiceUtil.rollDice(spell.getDamageDie());
            response.setDamage(damage);
            response.setDescription(response.getDescription() + " - " + damage + " damage");
        }

        // Consume spell slot
        consumeSpellSlot(character, spell.getLevel());

        log.info("Spell cast: {}", response.getDescription());
        return response;
    }

    /**
     * Take a short rest (recover hit dice).
     */
    @Transactional
    public String shortRest(Long characterId) {
        LotrCharacter character = getCharacter(characterId);
        int hitDiceUsed = DiceUtil.rollD6();
        int healed = Math.min(hitDiceUsed, character.getMaxHitPoints() - character.getHitPoints());
        character.setHitPoints(character.getHitPoints() + healed);
        characterRepository.save(character);

        String message = String.format("%s takes a short rest and recovers %d hit points.", character.getName(), healed);
        log.info(message);
        return message;
    }

    /**
     * Take a long rest (full recovery).
     */
    @Transactional
    public String longRest(Long characterId) {
        LotrCharacter character = getCharacter(characterId);
        character.setHitPoints(character.getMaxHitPoints());
        characterRepository.save(character);

        String message = String.format("%s takes a long rest and recovers all hit points.", character.getName());
        log.info(message);
        return message;
    }

    /**
     * Check for corruption from using the Ring or dark magic.
     * Returns the new corruption level.
     */
    @Transactional
    public int corruptionCheck(Long characterId, int amount) {
        LotrCharacter character = getCharacter(characterId);
        int currentCorruption = character.getCorruption();
        int newCorruption = Math.min(10, currentCorruption + amount);
        character.setCorruption(newCorruption);
        characterRepository.save(character);

        String message;
        if (newCorruption >= 10) {
            message = String.format("%s is fully corrupted by the Ring!", character.getName());
        } else if (newCorruption >= 7) {
            message = String.format("%s's corruption deepens to %d/10. The Ring's power grows.", character.getName(), newCorruption);
        } else if (newCorruption >= 4) {
            message = String.format("%s's corruption increases to %d/10. Dark thoughts linger.", character.getName(), newCorruption);
        } else {
            message = String.format("%s's corruption is now %d/10.", character.getName(), newCorruption);
        }

        log.info(message);
        return newCorruption;
    }

    /**
     * Award XP to a character and check for level up.
     */
    @Transactional
    public String awardXP(Long characterId, int xp) {
        LotrCharacter character = getCharacter(characterId);
        int currentXP = character.getExperiencePoints();
        int newXP = currentXP + xp;
        character.setExperiencePoints(newXP);

        int level = character.getLevel();
        if (newXP >= XP_THRESHOLDS[Math.min(level, XP_THRESHOLDS.length - 1)]) {
            level++;
            character.setLevel(level);
            character.setMaxHitPoints(character.getMaxHitPoints() + DiceUtil.rollD6());
            character.setHitPoints(character.getMaxHitPoints());
            characterRepository.save(character);
            return String.format("%s gains %d XP and levels up to level %d!", character.getName(), xp, level);
        }

        characterRepository.save(character);
        return String.format("%s gains %d XP (total: %d).", character.getName(), xp, newXP);
    }

    private LotrCharacter getCharacter(Long id) {
        return characterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + id));
    }

    private LotrSpell getSpell(Long id) {
        return spellRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Spell not found: " + id));
    }

    private int getAbilityModifier(LotrCharacter character, String ability) {
        Integer score = getAbilityScore(character, ability);
        return (score - 10) / 2;
    }

    private Integer getAbilityScore(LotrCharacter character, String ability) {
        switch (ability.toLowerCase()) {
            case "str": return character.getStrScore();
            case "dex": return character.getDexScore();
            case "con": return character.getConScore();
            case "int": return character.getIntScore();
            case "wis": return character.getWisScore();
            case "cha": return character.getChaScore();
            default: return 10;
        }
    }

    private boolean isProficient(LotrCharacter character, String skill) {
        if (character.getProficiencies() == null) return false;
        return character.getProficiencies().contains(skill);
    }

    private boolean isSavingThrowProficient(LotrCharacter character, String ability) {
        if (character.getSavingThrowProficiencies() == null) return false;
        return character.getSavingThrowProficiencies().contains(ability);
    }

    private boolean hasSpellSlot(LotrCharacter character, int spellLevel) {
        if (character.getSpellSlots() == null) return true;
        LotrSpellSlot slot = character.getSpellSlots().get(spellLevel);
        return slot != null && slot.getAvailable() > 0;
    }

    private void consumeSpellSlot(LotrCharacter character, int spellLevel) {
        if (character.getSpellSlots() != null) {
            LotrSpellSlot slot = character.getSpellSlots().get(spellLevel);
            if (slot != null) {
                slot.setAvailable(slot.getAvailable() - 1);
            }
        }
    }
}
