package com.yisy.modules.doramcu.earth2026.survival.encounter;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.model.E26Monster;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import com.yisy.modules.doramcu.earth2026.repository.E26MonsterRepository;
import com.yisy.modules.doramcu.earth2026.service.BiosphereService;
import com.yisy.modules.doramcu.earth2026.survival.SurvivalState;
import com.yisy.modules.doramcu.earth2026.survival.SurvivalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for generating and resolving survival encounters.
 * <p>
 * Uses the existing E26Monster system and BiosphereService for weather/day-night
 * effects on encounter rates and outcomes.
 * </p>
 */
@Service
public class EncounterService {

    private static final Logger log = LoggerFactory.getLogger(EncounterService.class);
    private static final Random random = new Random();

    // Base encounter chance per tick (5%)
    private static final double BASE_ENCOUNTER_CHANCE = 0.05;

    // Night multiplier for encounter chance
    private static final double NIGHT_MULTIPLIER = 1.5;

    private final SurvivalEncounterRepository encounterRepository;
    private final Earth2026CharacterRepository characterRepository;
    private final E26MonsterRepository monsterRepository;
    private final BiosphereService biosphereService;
    private final SurvivalService survivalService;

    public EncounterService(SurvivalEncounterRepository encounterRepository,
                             Earth2026CharacterRepository characterRepository,
                             E26MonsterRepository monsterRepository,
                             BiosphereService biosphereService,
                             SurvivalService survivalService) {
        this.encounterRepository = encounterRepository;
        this.characterRepository = characterRepository;
        this.monsterRepository = monsterRepository;
        this.biosphereService = biosphereService;
        this.survivalService = survivalService;
    }

    // ==================== Encounter Generation ====================

    /**
     * Check if an encounter should occur for a character.
     *
     * @param characterId the character ID
     * @return true if an encounter should occur
     */
    @Transactional(readOnly = true)
    public boolean shouldEncounterOccur(Long characterId) {
        Earth2026Character character = characterRepository.findById(characterId).orElse(null);
        if (character == null) return false;

        SurvivalState state = survivalService.getState(characterId);
        if (state == null || !state.isActive()) return false;

        // Get environmental conditions
        boolean isNight = false;
        try {
            var context = biosphereService.createDefaultBiosphereContext(null);
            isNight = context.isNight();
        } catch (Exception e) {
            // Ignore
        }

        double chance = BASE_ENCOUNTER_CHANCE;
        if (isNight) {
            chance *= NIGHT_MULTIPLIER;
        }

        // Higher level characters attract more dangerous encounters
        double levelModifier = 1.0 + (character.getLevel() - 1) * 0.1;
        chance *= levelModifier;

        return random.nextDouble() < chance;
    }

    /**
     * Trigger a random encounter for a character.
     *
     * @param characterId the character ID
     * @return the encounter record
     */
    @Transactional
    public SurvivalEncounter triggerEncounter(Long characterId) {
        Earth2026Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        // Select a monster based on character level
        E26Monster monster = selectMonster(character);

        // Resolve the encounter
        String outcome = resolveEncounter(character, monster);

        // Generate loot
        Map<String, Integer> loot = new HashMap<>();
        if ("WON".equals(outcome)) {
            loot = LootTable.generateLoot(monster.getThreatLevel());
        }

        // Create encounter record
        SurvivalEncounter encounter = new SurvivalEncounter();
        encounter.setCharacterId(characterId);
        encounter.setMonsterId(monster.getId());
        encounter.setMonsterName(monster.getName());
        encounter.setOutcome(outcome);
        encounter.setLocation(character.getEarthLocation() != null ? character.getEarthLocation() : "Unknown");
        encounter.setEncounterTime(LocalDateTime.now());

        // Set night flag
        try {
            var context = biosphereService.createDefaultBiosphereContext(null);
            encounter.setNight(context.isNight());
        } catch (Exception e) {
            // Ignore
        }

        // Apply XP and damage
        if ("WON".equals(outcome)) {
            encounter.setXpGained(monster.getXp());
            character.setExperiencePoints(character.getExperiencePoints() + monster.getXp());
            characterRepository.save(character);
        } else if ("LOST".equals(outcome)) {
            encounter.setDamageTaken(monster.getHp() / 4);
            character.setHitPoints(Math.max(0, character.getHitPoints() - monster.getHp() / 4));
            characterRepository.save(character);
        }

        // Set loot JSON
        if (!loot.isEmpty()) {
            encounter.setLootJson(lootToJson(loot));
        }

        SurvivalEncounter saved = encounterRepository.save(encounter);
        log.info("Encounter: {} vs {} -> {} (XP: {}, Loot: {})",
                character.getName(), monster.getName(), outcome, encounter.getXpGained(), loot);
        return saved;
    }

    // ==================== Encounter Resolution ====================

    private String resolveEncounter(Earth2026Character character, E26Monster monster) {
        // Character combat score based on stats
        int combatScore = character.getStrScore() + character.getDexScore() + character.getConScore();
        combatScore += character.getLevel() * 3;

        // Monster combat score
        int monsterScore = monster.getStr() + monster.getDex() + monster.getCon();
        monsterScore += monster.getThreatLevel() * 3;

        // Roll for outcome
        int characterRoll = combatScore + random.nextInt(20);
        int monsterRoll = monsterScore + random.nextInt(20);

        if (characterRoll > monsterRoll) {
            return "WON";
        } else if (characterRoll < monsterRoll * 0.7) {
            // Character can flee if badly outmatched
            if (random.nextDouble() < 0.5) {
                return "FLED";
            }
            return "LOST";
        } else {
            // Close fight, slight advantage to character
            return random.nextDouble() < 0.6 ? "WON" : "LOST";
        }
    }

    // ==================== Monster Selection ====================

    private E26Monster selectMonster(Earth2026Character character) {
        // Select monsters within the character's threat range
        int minThreat = Math.max(1, character.getLevel() - 2);
        int maxThreat = Math.min(10, character.getLevel() + 2);

        List<E26Monster> allMonsters = monsterRepository.findAll();
        List<E26Monster> eligible = new ArrayList<>();

        for (E26Monster monster : allMonsters) {
            if (monster.getThreatLevel() >= minThreat && monster.getThreatLevel() <= maxThreat) {
                eligible.add(monster);
            }
        }

        if (eligible.isEmpty()) {
            // Fallback: use any monster
            if (allMonsters.isEmpty()) {
                throw new IllegalStateException("No monsters available for encounters");
            }
            return allMonsters.get(random.nextInt(allMonsters.size()));
        }

        return eligible.get(random.nextInt(eligible.size()));
    }

    // ==================== Helpers ====================

    private String lootToJson(Map<String, Integer> loot) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : loot.entrySet()) {
            if (!first) sb.append(", ");
            sb.append("\"").append(entry.getKey()).append("\": ").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Get recent encounters for a character.
     *
     * @param characterId the character ID
     * @return list of recent encounters
     */
    @Transactional(readOnly = true)
    public List<SurvivalEncounter> getRecentEncounters(Long characterId) {
        return encounterRepository.findByCharacterIdOrderByEncounterTimeDesc(characterId);
    }
}
