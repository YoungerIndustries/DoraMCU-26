package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacter;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

/**
 * Service for Lord of the Rings character CRUD and ability score generation.
 */
@Service
public class LotrCharacterService {

    private static final Logger log = LoggerFactory.getLogger(LotrCharacterService.class);
    private static final Random random = new Random();

    private final LotrCharacterRepository repository;

    public LotrCharacterService(LotrCharacterRepository repository) {
        this.repository = repository;
    }

    // --- CRUD ---

    public List<LotrCharacter> findAll() {
        return repository.findAll();
    }

    public LotrCharacter findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + id));
    }

    public List<LotrCharacter> findByRace(String race) {
        return repository.findByRace(race);
    }

    public List<LotrCharacter> findByCalling(String calling) {
        return repository.findByCalling(calling);
    }

    public List<LotrCharacter> findByFaction(String faction) {
        return repository.findByFaction(faction);
    }

    public List<LotrCharacter> findRingBearers() {
        return repository.findByOneRingBearerTrue();
    }

    @Transactional
    public LotrCharacter save(LotrCharacter character) {
        return repository.save(character);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    // --- Character Creation ---

    /**
     * Create a new LOTR character with randomly generated ability scores.
     */
    @Transactional
    public LotrCharacter createCharacter(String name, String race, String calling) {
        LotrCharacter character = new LotrCharacter();
        character.setName(name);
        character.setRace(race);
        character.setCalling(calling);
        character.setLevel(1);

        // Generate ability scores using 4d6 drop lowest
        int[] scores = generateAbilityScores();
        character.setStrScore(scores[0]);
        character.setDexScore(scores[1]);
        character.setConScore(scores[2]);
        character.setIntScore(scores[3]);
        character.setWisScore(scores[4]);
        character.setChaScore(scores[5]);

        // Set HP based on race and calling
        character.setMaxHitPoints(calculateMaxHp(race, calling, scores[2]));
        character.setHitPoints(character.getMaxHitPoints());

        // Set AC based on race
        character.setArmorClass(calculateBaseAc(race));

        // Set hit dice
        character.setHitDice(getHitDieForRace(race));

        // Set proficiency bonus
        character.setProficiencyBonus(2);

        // Set default hope/shadow
        character.setHope(5);
        character.setShadow(0);
        character.setCorruption(0);

        log.info("Created LOTR character: {} ({}, {})", name, race, calling);
        return repository.save(character);
    }

    /**
     * Generate 6 ability scores using 4d6 drop lowest.
     */
    public int[] generateAbilityScores() {
        int[] scores = new int[6];
        for (int i = 0; i < 6; i++) {
            scores[i] = roll4d6DropLowest();
        }
        return scores;
    }

    /**
     * Roll 4d6 and drop the lowest die.
     */
    public int roll4d6DropLowest() {
        int[] rolls = new int[4];
        int min = 6;
        int sum = 0;
        for (int i = 0; i < 4; i++) {
            rolls[i] = random.nextInt(6) + 1;
            if (rolls[i] < min) min = rolls[i];
            sum += rolls[i];
        }
        return sum - min;
    }

    /**
     * Calculate max HP for a character based on race, calling, and CON score.
     */
    public int calculateMaxHp(String race, String calling, int conScore) {
        int baseHp;
        switch (race.toLowerCase()) {
            case "ent": baseHp = 12; break;
            case "dwarf": baseHp = 10; break;
            case "elf": baseHp = 8; break;
            case "man": baseHp = 8; break;
            case "hobbit": baseHp = 6; break;
            case "maia": baseHp = 8; break;
            default: baseHp = 8; break;
        }
        int conMod = (conScore - 10) / 2;
        return baseHp + Math.max(0, conMod);
    }

    /**
     * Calculate base AC for a race.
     */
    public int calculateBaseAc(String race) {
        switch (race.toLowerCase()) {
            case "dwarf": return 14; // Natural armor
            case "ent": return 16; // Bark skin
            case "elf": return 12;
            case "hobbit": return 10;
            default: return 10;
        }
    }

    /**
     * Get hit die for a race.
     */
    public String getHitDieForRace(String race) {
        switch (race.toLowerCase()) {
            case "ent": return "d12";
            case "dwarf": return "d10";
            case "man": return "d8";
            case "elf": return "d8";
            case "maia": return "d8";
            case "hobbit": return "d6";
            default: return "d8";
        }
    }

    /**
     * Get proficiency bonus for a given level.
     */
    public int getProficiencyBonus(int level) {
        if (level <= 4) return 2;
        if (level <= 8) return 3;
        if (level <= 12) return 4;
        if (level <= 16) return 5;
        return 6;
    }
}
