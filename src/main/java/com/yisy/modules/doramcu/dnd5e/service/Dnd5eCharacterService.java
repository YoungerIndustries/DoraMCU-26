package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSpellSlot;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Service for D&D 5e character CRUD and ability score generation.
 */
@Service
public class Dnd5eCharacterService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eCharacterService.class);
    private static final Random random = new Random();

    // Hit dice by class
    private static final Map<String, String> CLASS_HIT_DICE;
    static {
        Map<String, String> map = new java.util.HashMap<>();
        map.put("Barbarian", "d12");
        map.put("Bard", "d6");
        map.put("Cleric", "d8");
        map.put("Druid", "d8");
        map.put("Fighter", "d10");
        map.put("Monk", "d8");
        map.put("Paladin", "d12");
        map.put("Ranger", "d10");
        map.put("Rogue", "d8");
        map.put("Sorcerer", "d6");
        map.put("Warlock", "d8");
        map.put("Wizard", "d4");
        CLASS_HIT_DICE = Map.copyOf(map);
    }

    // Proficiency bonus by level
    private static final int[] PROFICIENCY_BONUS = {0, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6};

    // Spell slot table for full casters (Wizard/Cleric/Druid/Sorcerer) by level
    // Format: [characterLevel][slotLevel] = number of slots
    private static final int[][] FULL_CASTER_SLOTS = {
            {}, // 0
            {0, 2, 0, 0, 0, 0, 0, 0, 0, 0}, // 1
            {0, 3, 0, 0, 0, 0, 0, 0, 0, 0}, // 2
            {0, 4, 2, 0, 0, 0, 0, 0, 0, 0}, // 3
            {0, 4, 3, 0, 0, 0, 0, 0, 0, 0}, // 4
            {0, 4, 3, 2, 0, 0, 0, 0, 0, 0}, // 5
            {0, 4, 3, 3, 0, 0, 0, 0, 0, 0}, // 6
            {0, 4, 3, 3, 1, 0, 0, 0, 0, 0}, // 7
            {0, 4, 3, 3, 2, 0, 0, 0, 0, 0}, // 8
            {0, 4, 3, 3, 3, 1, 0, 0, 0, 0}, // 9
            {0, 4, 3, 3, 3, 2, 0, 0, 0, 0}, // 10
            {0, 4, 3, 3, 3, 2, 1, 0, 0, 0}, // 11
            {0, 4, 3, 3, 3, 2, 1, 0, 0, 0}, // 12
            {0, 4, 3, 3, 3, 2, 1, 1, 0, 0}, // 13
            {0, 4, 3, 3, 3, 2, 1, 1, 0, 0}, // 14
            {0, 4, 3, 3, 3, 2, 1, 1, 1, 0}, // 15
            {0, 4, 3, 3, 3, 2, 1, 1, 1, 0}, // 16
            {0, 4, 3, 3, 3, 2, 1, 1, 1, 1}, // 17
            {0, 4, 3, 3, 3, 3, 1, 1, 1, 1}, // 18
            {0, 4, 3, 3, 3, 3, 2, 1, 1, 1}, // 19
            {0, 4, 3, 3, 3, 3, 2, 2, 1, 1}  // 20
    };

    private final Dnd5eCharacterRepository characterRepository;

    public Dnd5eCharacterService(Dnd5eCharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    /**
     * Create a new D&D 5e character.
     */
    @Transactional
    public Dnd5eCharacter createCharacter(CreateDnd5eCharacterRequest request) {
        Dnd5eCharacter character = new Dnd5eCharacter();
        character.setName(request.getName());
        character.setRace(request.getRace());
        character.setCharacterClass(request.getCharacterClass());
        character.setBackground(request.getBackground());
        character.setLevel(1);

        // Generate or use provided ability scores
        if (request.getStrScore() != null) {
            character.setStrScore(request.getStrScore());
            character.setDexScore(request.getDexScore());
            character.setConScore(request.getConScore());
            character.setIntScore(request.getIntScore());
            character.setWisScore(request.getWisScore());
            character.setChaScore(request.getChaScore());
        } else {
            int[] scores = generateAbilityScores(request.getScoreMethod());
            character.setStrScore(scores[0]);
            character.setDexScore(scores[1]);
            character.setConScore(scores[2]);
            character.setIntScore(scores[3]);
            character.setWisScore(scores[4]);
            character.setChaScore(scores[5]);
        }

        // Calculate derived stats
        int level = 1;
        character.setProficiencyBonus(PROFICIENCY_BONUS[level]);

        // Hit dice
        String hitDie = CLASS_HIT_DICE.getOrDefault(request.getCharacterClass(), "d8");
        character.setHitDice(hitDie);

        // HP = hit die average + CON mod at level 1
        int hitDieValue = switch (hitDie) {
            case "d4" -> 4;
            case "d6" -> 4;
            case "d8" -> 5;
            case "d10" -> 6;
            case "d12" -> 7;
            default -> 5;
        };
        int maxHp = hitDieValue + character.getConMod();
        character.setMaxHitPoints(Math.max(1, maxHp));
        character.setHitPoints(character.getMaxHitPoints());

        // AC = 10 + DEX mod (base, no armor)
        character.setArmorClass(10 + character.getDexMod());

        // Initialize spell slots for casters
        String cls = request.getCharacterClass();
        if (isSpellCaster(cls)) {
            initializeSpellSlots(character, level);
        }

        Dnd5eCharacter saved = characterRepository.save(character);
        log.info("Created D&D 5e character: {}", saved.getName());
        return saved;
    }

    /**
     * Get a character by ID.
     */
    public Dnd5eCharacter getCharacter(Long id) {
        return characterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + id));
    }

    /**
     * List all characters.
     */
    public List<Dnd5eCharacter> listCharacters() {
        return characterRepository.findAll();
    }

    /**
     * Delete a character.
     */
    @Transactional
    public void deleteCharacter(Long id) {
        characterRepository.deleteById(id);
        log.info("Deleted character: {}", id);
    }

    /**
     * Update a character's stats. Only provided fields will be updated.
     */
    @Transactional
    public Dnd5eCharacter updateCharacter(Long id, UpdateDnd5eCharacterRequest request) {
        Dnd5eCharacter character = getCharacter(id);

        if (request.getHitPoints() != null) {
            character.setHitPoints(Math.max(0, request.getHitPoints()));
        }
        if (request.getMaxHitPoints() != null) {
            character.setMaxHitPoints(Math.max(1, request.getMaxHitPoints()));
        }
        if (request.getArmorClass() != null) {
            character.setArmorClass(request.getArmorClass());
        }
        if (request.getSpeed() != null) {
            character.setSpeed(request.getSpeed());
        }
        if (request.getExperiencePoints() != null) {
            character.setExperiencePoints(Math.max(0, request.getExperiencePoints()));
        }
        if (request.getTempHitPoints() != null) {
            character.setTempHitPoints(Math.max(0, request.getTempHitPoints()));
        }
        if (request.getExhaustionLevel() != null) {
            character.setExhaustionLevel(Math.max(0, Math.min(6, request.getExhaustionLevel())));
        }
        if (request.getDeathSaveSuccesses() != null) {
            character.setDeathSaveSuccesses(Math.max(0, Math.min(3, request.getDeathSaveSuccesses())));
        }
        if (request.getDeathSaveFailures() != null) {
            character.setDeathSaveFailures(Math.max(0, Math.min(3, request.getDeathSaveFailures())));
        }
        if (request.getStrScore() != null) {
            character.setStrScore(request.getStrScore());
        }
        if (request.getDexScore() != null) {
            character.setDexScore(request.getDexScore());
        }
        if (request.getConScore() != null) {
            character.setConScore(request.getConScore());
        }
        if (request.getIntScore() != null) {
            character.setIntScore(request.getIntScore());
        }
        if (request.getWisScore() != null) {
            character.setWisScore(request.getWisScore());
        }
        if (request.getChaScore() != null) {
            character.setChaScore(request.getChaScore());
        }
        if (request.getEquipment() != null) {
            character.setEquipment(request.getEquipment());
        }
        if (request.getConditions() != null) {
            character.setConditions(request.getConditions());
        }
        if (request.getInspiration() != null) {
            character.setInspiration(request.getInspiration());
        }

        Dnd5eCharacter saved = characterRepository.save(character);
        log.info("Updated character: {}", saved.getName());
        return saved;
    }

    /**
     * Generate ability scores using the specified method.
     * Methods: "standard" (standard array), "roll" (4d6 drop lowest), "pointbuy" (same as standard for now).
     */
    public int[] generateAbilityScores(String method) {
        return switch (method != null ? method.toLowerCase() : "standard") {
            case "roll" -> roll4d6DropLowest();
            case "pointbuy" -> pointBuyScores();
            default -> standardArray();
        };
    }

    /**
     * Standard array: 15, 14, 13, 12, 10, 8.
     */
    private int[] standardArray() {
        return new int[]{15, 14, 13, 12, 10, 8};
    }

    /**
     * Roll 4d6 drop lowest for each of 6 scores.
     */
    private int[] roll4d6DropLowest() {
        int[] scores = new int[6];
        for (int i = 0; i < 6; i++) {
            scores[i] = roll4d6DropLowestOnce();
        }
        return scores;
    }

    private int roll4d6DropLowestOnce() {
        int[] rolls = new int[4];
        int minIndex = 0;
        int sum = 0;
        for (int i = 0; i < 4; i++) {
            rolls[i] = random.nextInt(6) + 1;
            sum += rolls[i];
            if (rolls[i] < rolls[minIndex]) {
                minIndex = i;
            }
        }
        return sum - rolls[minIndex];
    }

    /**
     * Point buy default: same as standard array for simplicity.
     */
    private int[] pointBuyScores() {
        return new int[]{15, 14, 13, 12, 10, 8};
    }

    /**
     * Check if a class is a spellcaster.
     */
    private boolean isSpellCaster(String characterClass) {
        return switch (characterClass) {
            case "Bard", "Cleric", "Druid", "Sorcerer", "Wizard",
                 "Paladin", "Ranger", "Warlock" -> true;
            default -> false;
        };
    }

    /**
     * Initialize spell slots for a character.
     */
    private void initializeSpellSlots(Dnd5eCharacter character, int level) {
        String cls = character.getCharacterClass();
        int[][] slotTable;

        // Half casters (Paladin, Ranger)
        if ("Paladin".equals(cls) || "Ranger".equals(cls)) {
            slotTable = getHalfCasterSlots(level);
        }
        // Pact caster (Warlock)
        else if ("Warlock".equals(cls)) {
            slotTable = getWarlockSlots(level);
        }
        // Third casters (Bard)
        else if ("Bard".equals(cls)) {
            slotTable = getThirdCasterSlots(level);
        }
        // Full casters
        else {
            slotTable = FULL_CASTER_SLOTS;
        }

        int[] slots = slotTable[Math.min(level, slotTable.length - 1)];
        for (int i = 1; i <= 9; i++) {
            if (slots[i] > 0) {
                Dnd5eSpellSlot spellSlot = new Dnd5eSpellSlot(character, i, slots[i]);
                character.getSpellSlots().add(spellSlot);
            }
        }
    }

    private int[][] getHalfCasterSlots(int level) {
        // Paladin/Ranger: gain spell slots at levels 2, 5, 8, 11, 14, 17
        int[][] slots = new int[21][10];
        if (level >= 2) { slots[2] = new int[]{0, 2, 0, 0, 0, 0, 0, 0, 0, 0}; slots[3] = slots[2].clone(); }
        if (level >= 3) slots[3] = new int[]{0, 3, 0, 0, 0, 0, 0, 0, 0, 0};
        if (level >= 4) slots[4] = slots[3].clone();
        if (level >= 5) slots[5] = new int[]{0, 4, 2, 0, 0, 0, 0, 0, 0, 0};
        if (level >= 6) slots[6] = slots[5].clone();
        if (level >= 7) slots[7] = slots[6].clone();
        if (level >= 8) slots[8] = new int[]{0, 4, 3, 0, 0, 0, 0, 0, 0, 0};
        if (level >= 9) slots[9] = slots[8].clone();
        if (level >= 10) slots[10] = slots[9].clone();
        if (level >= 11) slots[11] = new int[]{0, 4, 3, 2, 0, 0, 0, 0, 0, 0};
        if (level >= 12) slots[12] = slots[11].clone();
        if (level >= 13) slots[13] = slots[12].clone();
        if (level >= 14) slots[14] = new int[]{0, 4, 3, 3, 0, 0, 0, 0, 0, 0};
        if (level >= 15) slots[15] = slots[14].clone();
        if (level >= 16) slots[16] = slots[15].clone();
        if (level >= 17) slots[17] = new int[]{0, 4, 3, 3, 1, 0, 0, 0, 0, 0};
        if (level >= 18) slots[18] = slots[17].clone();
        if (level >= 19) slots[19] = slots[18].clone();
        if (level >= 20) slots[20] = slots[19].clone();
        return slots;
    }

    private int[][] getWarlockSlots(int level) {
        // Warlock: few slots, all at highest level available
        int[][] slots = new int[21][10];
        if (level >= 1) slots[1] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; // cantrips only
        if (level >= 2) slots[2] = new int[]{0, 0, 2, 0, 0, 0, 0, 0, 0, 0};
        if (level >= 3) slots[3] = slots[2].clone();
        if (level >= 4) slots[4] = new int[]{0, 0, 0, 0, 2, 0, 0, 0, 0, 0};
        if (level >= 5) slots[5] = slots[4].clone();
        if (level >= 6) slots[6] = slots[5].clone();
        if (level >= 7) slots[7] = new int[]{0, 0, 0, 0, 0, 0, 0, 3, 0, 0};
        if (level >= 8) slots[8] = slots[7].clone();
        if (level >= 9) slots[9] = slots[8].clone();
        if (level >= 10) slots[10] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 4};
        for (int i = 11; i <= 20; i++) slots[i] = slots[10].clone();
        return slots;
    }

    private int[][] getThirdCasterSlots(int level) {
        // Bard: similar to full caster but slightly fewer slots at higher levels
        return FULL_CASTER_SLOTS;
    }

    /**
     * Get proficiency bonus for a given level.
     */
    public int getProficiencyBonus(int level) {
        if (level < 1 || level > 20) return 2;
        return PROFICIENCY_BONUS[level];
    }
}
