package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterFeat;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eFeat;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterFeatRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eFeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing D&D 5e Feats (2024 PHB).
 * <p>
 * Feats are optional benefits that characters can select instead of
 * taking an ability score improvement at levels 4, 6, 8, 12, 14, 16, and 19.
 * </p>
 * <p>
 * The 2024 PHB revises several feats:
 * - Lucky: grants advantage instead of rerolls
 * - Sharpshooter/Great Weapon Master: -5/+10 removed
 * - Sentinel/Polearm Master combo addressed
 * - Magic Initiate: learned spells can use other spell slots
 * - Musician: awards Heroic Inspiration after any rest
 * </p>
 */
@Service
public class Dnd5eFeatService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eFeatService.class);

    private static final int EXPECTED_FEAT_COUNT_MIN = 20;

    private final Dnd5eFeatRepository featRepository;
    private final Dnd5eCharacterFeatRepository characterFeatRepository;
    private final Dnd5eCharacterRepository characterRepository;
    private boolean initialized = false;

    public Dnd5eFeatService(Dnd5eFeatRepository featRepository,
                            Dnd5eCharacterFeatRepository characterFeatRepository,
                            Dnd5eCharacterRepository characterRepository) {
        this.featRepository = featRepository;
        this.characterFeatRepository = characterFeatRepository;
        this.characterRepository = characterRepository;
    }

    // ==================== Initialization ====================

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeFeatDatabase() {
        if (initialized) {
            return;
        }
        long existingCount = featRepository.count();
        if (existingCount >= EXPECTED_FEAT_COUNT_MIN) {
            log.info("D&D 5e feat database already populated with {} feats", existingCount);
            initialized = true;
            return;
        }

        log.info("Initializing D&D 5e feat database...");
        populateFeats();
        initialized = true;
        log.info("D&D 5e feat database initialized: {} feats", featRepository.count());
    }

    // ==================== Feat CRUD ====================

    /**
     * Get a feat by name.
     */
    @Transactional(readOnly = true)
    public Dnd5eFeat getFeatByName(String name) {
        return featRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Feat not found: " + name));
    }

    /**
     * Get a feat by ID.
     */
    @Transactional(readOnly = true)
    public Dnd5eFeat getFeat(Long id) {
        return featRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feat not found: " + id));
    }

    /**
     * List all available feats.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eFeat> listFeats() {
        return featRepository.findAll();
    }

    /**
     * List feats by category.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eFeat> listFeatsByCategory(String category) {
        return featRepository.findByCategory(category);
    }

    /**
     * List feats by ability requirement.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eFeat> listFeatsByAbility(String abilityRequirement) {
        return featRepository.findByAbilityRequirement(abilityRequirement);
    }

    /**
     * Search feats by keyword in name or description.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eFeat> searchFeats(String keyword) {
        return featRepository.searchByNameOrDescription(keyword);
    }

    /**
     * List feats that a character can take based on their ability scores.
     * A character meets the requirement if their score >= the feat's required score.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eFeat> listAvailableFeats(Long characterId) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        // Find the minimum ability score for requirement checking
        int minScore = Math.min(
                Math.min(character.getStrScore(), character.getDexScore()),
                Math.min(character.getConScore(),
                        Math.min(character.getIntScore(),
                                Math.min(character.getWisScore(), character.getChaScore()))));

        List<Dnd5eFeat> allFeats = featRepository.findByAbilityScoreLessThanEqual(minScore);

        // Filter out feats the character already has (unless repeatable)
        List<Dnd5eCharacterFeat> characterFeats = characterFeatRepository.findByCharacterIdAndActiveTrue(characterId);
        List<String> ownedFeatNames = characterFeats.stream()
                .map(Dnd5eCharacterFeat::getFeatName)
                .toList();

        return allFeats.stream()
                .filter(feat -> {
                    // Allow repeatable feats
                    if (feat.isRepeatable()) return true;
                    // Exclude already-owned feats
                    return !ownedFeatNames.contains(feat.getName());
                })
                .toList();
    }

    /**
     * Create a new feat definition.
     */
    @Transactional
    public Dnd5eFeat createFeat(String name, String description, String category,
                                 String abilityRequirement, int abilityScoreRequired,
                                 String prerequisite, boolean repeatable,
                                 int abilityIncrease, String abilityIncreased) {
        // Check for duplicate
        if (featRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Feat already exists: " + name);
        }

        Dnd5eFeat feat = new Dnd5eFeat();
        feat.setName(name);
        feat.setDescription(description);
        feat.setCategory(category);
        feat.setAbilityRequirement(abilityRequirement);
        feat.setAbilityScoreRequired(abilityScoreRequired);
        feat.setPrerequisite(prerequisite != null ? prerequisite : "None");
        feat.setRepeatable(repeatable);
        feat.setAbilityIncrease(Math.max(0, Math.min(2, abilityIncrease)));
        feat.setAbilityIncreased(abilityIncreased);

        Dnd5eFeat saved = featRepository.save(feat);
        log.info("Created feat: {}", name);
        return saved;
    }

    /**
     * Delete a feat definition.
     */
    @Transactional
    public void deleteFeat(String name) {
        Dnd5eFeat feat = getFeatByName(name);
        featRepository.delete(feat);
        log.info("Deleted feat: {}", name);
    }

    // ==================== Character Feat Management ====================

    /**
     * Assign a feat to a character.
     * Checks prerequisites and ability requirements.
     */
    @Transactional
    public Dnd5eCharacterFeat assignFeatToCharacter(Long characterId, String featName) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        Dnd5eFeat feat = getFeatByName(featName);

        // Check if character already has this feat (and it's not repeatable)
        if (!feat.isRepeatable()) {
            if (characterFeatRepository.existsByCharacterIdAndFeatName(characterId, featName)) {
                throw new IllegalArgumentException("Character already has this feat: " + featName);
            }
        }

        // Check ability score requirement
        if (feat.getAbilityScoreRequired() > 0) {
            int characterScore = getAbilityScore(character, feat.getAbilityRequirement());
            if (characterScore < feat.getAbilityScoreRequired()) {
                throw new IllegalArgumentException(
                        "Character does not meet ability requirement: " + feat.getAbilityRequirement() + " " + feat.getAbilityScoreRequired());
            }
        }

        // Check prerequisite
        if (feat.getPrerequisite() != null && !feat.getPrerequisite().equals("None")) {
            boolean hasPrerequisite = characterFeatRepository
                    .findByCharacterIdAndFeatName(characterId, feat.getPrerequisite())
                    .isEmpty() == false;
            if (!hasPrerequisite) {
                throw new IllegalArgumentException(
                        "Character does not meet prerequisite: " + feat.getPrerequisite());
            }
        }

        // Apply ability score increase if any
        if (feat.getAbilityIncrease() > 0 && feat.getAbilityIncreased() != null) {
            applyAbilityIncrease(character, feat.getAbilityIncreased(), feat.getAbilityIncrease());
            character = characterRepository.save(character);
        }

        // Create the character-feat association
        Dnd5eCharacterFeat characterFeat = new Dnd5eCharacterFeat();
        characterFeat.setCharacterId(characterId);
        characterFeat.setCharacterName(character.getName());
        characterFeat.setFeatName(featName);
        characterFeat.setLevelAcquired(character.getLevel());
        characterFeat.setActive(true);

        Dnd5eCharacterFeat saved = characterFeatRepository.save(characterFeat);
        log.info("Assigned feat '{}' to '{}' at level {}", featName, character.getName(), character.getLevel());
        return saved;
    }

    /**
     * Remove a feat from a character.
     */
    @Transactional
    public void removeFeatFromCharacter(Long characterId, String featName) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        List<Dnd5eCharacterFeat> feats = characterFeatRepository
                .findByCharacterIdAndFeatName(characterId, featName);

        if (feats.isEmpty()) {
            throw new IllegalArgumentException("Character does not have this feat: " + featName);
        }

        characterFeatRepository.deleteByCharacterIdAndFeatName(characterId, featName);
        log.info("Removed feat '{}' from '{}'", featName, character.getName());
    }

    /**
     * Toggle a feat's active state for a character.
     */
    @Transactional
    public Dnd5eCharacterFeat toggleFeatActive(Long characterId, String featName, boolean active) {
        List<Dnd5eCharacterFeat> feats = characterFeatRepository
                .findByCharacterIdAndFeatName(characterId, featName);

        if (feats.isEmpty()) {
            throw new IllegalArgumentException("Character does not have this feat: " + featName);
        }

        Dnd5eCharacterFeat characterFeat = feats.get(0);
        characterFeat.setActive(active);
        return characterFeatRepository.save(characterFeat);
    }

    /**
     * Get all feats assigned to a character.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eCharacterFeat> listCharacterFeats(Long characterId) {
        return characterFeatRepository.findByCharacterIdOrderByCreatedAtDesc(characterId);
    }

    /**
     * Get all active feats for a character.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eCharacterFeat> listActiveCharacterFeats(Long characterId) {
        return characterFeatRepository.findByCharacterIdAndActiveTrue(characterId);
    }

    /**
     * Check if a character has a specific feat.
     */
    @Transactional(readOnly = true)
    public boolean hasFeat(Long characterId, String featName) {
        return characterFeatRepository.existsByCharacterIdAndFeatName(characterId, featName);
    }

    // ==================== Helpers ====================

    private int getAbilityScore(Dnd5eCharacter character, String abilityRequirement) {
        return switch (abilityRequirement != null ? abilityRequirement.toUpperCase() : "") {
            case "STR" -> character.getStrScore();
            case "DEX" -> character.getDexScore();
            case "CON" -> character.getConScore();
            case "INT" -> character.getIntScore();
            case "WIS" -> character.getWisScore();
            case "CHA" -> character.getChaScore();
            default -> 0;
        };
    }

    private void applyAbilityIncrease(Dnd5eCharacter character, String ability, int increase) {
        String upperAbility = ability.toUpperCase();
        int currentScore;
        int newScore;

        switch (upperAbility) {
            case "STR":
                currentScore = character.getStrScore();
                newScore = Math.min(20, currentScore + increase);
                character.setStrScore(newScore);
                break;
            case "DEX":
                currentScore = character.getDexScore();
                newScore = Math.min(20, currentScore + increase);
                character.setDexScore(newScore);
                break;
            case "CON":
                currentScore = character.getConScore();
                newScore = Math.min(20, currentScore + increase);
                character.setConScore(newScore);
                break;
            case "INT":
                currentScore = character.getIntScore();
                newScore = Math.min(20, currentScore + increase);
                character.setIntScore(newScore);
                break;
            case "WIS":
                currentScore = character.getWisScore();
                newScore = Math.min(20, currentScore + increase);
                character.setWisScore(newScore);
                break;
            case "CHA":
                currentScore = character.getChaScore();
                newScore = Math.min(20, currentScore + increase);
                character.setChaScore(newScore);
                break;
            case "ANY":
                // Default to STR if "Any" is specified
                currentScore = character.getStrScore();
                newScore = Math.min(20, currentScore + increase);
                character.setStrScore(newScore);
                break;
            default:
                log.warn("Unknown ability: {}", ability);
                break;
        }
    }

    // ==================== Feat Database Population ====================

    @Transactional
    public void populateFeats() {
        List<Dnd5eFeat> feats = new ArrayList<>();

        // --- GENERAL FEATS ---

        feats.add(newFeat("Tough", "General",
                "Beginning at 1st level, your hit point maximum increases by 2, and by 2 every time you gain a level.",
                null, 0, "None", false, 0, null));

        feats.add(newFeat("Observant", "General",
                "You can spot the smallest details. Your Wisdom score increases by 1, to a maximum of 20. " +
                "You gain a +5 bonus to your passive Wisdom (Perception) score.",
                null, 0, "None", false, 1, "WIS"));

        feats.add(newFeat("Lucky", "General",
                "2024 Revision: You have 3 charges of luck. When you make an ability check, attack roll, or saving throw, " +
                "you can spend 1 charge to gain advantage on the roll. When an attack roll is made against you, " +
                "you can spend 1 charge to impose disadvantage on that roll. You regain your expended charges when you finish a long rest.",
                null, 0, "None", false, 0, null));

        feats.add(newFeat("Skill Expert", "General",
                "Your proficiency in a skill can take you far. Your choice of skill or tool proficiencies increases " +
                "your proficiency bonus with those proficiencies by 1. In addition, you gain proficiency with " +
                "three skills of your choice from any class.",
                null, 0, "None", false, 0, null));

        feats.add(newFeat("Feather Weight", "General",
                "You are adept at handling and carrying items. Your Dexterity score increases by 1, to a maximum of 20. " +
                "You ignore the encumbrance rules. You can reduce the bulk of non-magical items by half.",
                "DEX", 13, "None", false, 1, "DEX"));

        // --- COMBAT FEATS ---

        feats.add(newFeat("Great Weapon Master", "Combat",
                "2024 Revision: You've learned to put the weight of a weapon to your advantage. " +
                "When you score a critical hit with a heavy weapon, you can roll one additional weapon damage die. " +
                "When you score a critical hit with a two-handed weapon, you can roll two additional weapon damage dice. " +
                "The -5/+10 penalty/bonus from the 2014 version has been removed.",
                "STR", 13, "None", false, 0, null));

        feats.add(newFeat("Sharpshooter", "Combat",
                "2024 Revision: You have mastered the use of ranged weapons. You ignore half cover and three-quarters cover. " +
                "Ranged weapon attacks you make ignore distance penalties. " +
                "The -5/+10 penalty/bonus from the 2014 version has been removed.",
                "DEX", 13, "None", false, 0, null));

        feats.add(newFeat("Sentinel", "Combat",
                "2024 Revision: You can prevent your enemies from targeting your allies. " +
                "When a creature within 5 feet of you makes an attack against a target other than you, " +
                "you can use your reaction to make a melee weapon attack against that creature. " +
                "When you hit a creature with an opportunity attack, its speed becomes 0 for the rest of the turn.",
                "STR", 13, "None", false, 0, null));

        feats.add(newFeat("Polearm Master", "Combat",
                "2024 Revision: You can use polearms to keep enemies at bay. " +
                "When you make a melee attack with a polearm, you can use a bonus action to make a " +
                "5-foot reach attack with the opposite end. " +
                "Creatures provoke opportunity attacks from you when they enter your reach.",
                "STR", 13, "None", false, 0, null));

        feats.add(newFeat("Duelist", "Combat",
                "You are deadly with a one-handed weapon. Your Dexterity score increases by 1, to a maximum of 20. " +
                "When you are wielding a melee weapon in one hand and no other weapons, you gain a +2 bonus to damage rolls.",
                "DEX", 13, "None", false, 1, "DEX"));

        feats.add(newFeat("Moderately Armored", "Combat",
                "You can wear and use medium armor effectively. You gain proficiency with light and medium armor. " +
                "Your Dexterity score increases by 1, to a maximum of 20.",
                "DEX", 13, "None", false, 1, "DEX"));

        feats.add(newFeat("Heavily Armored", "Combat",
                "You can wear and use heavy armor effectively. You gain proficiency with light, medium, and heavy armor. " +
                "Your Strength score increases by 1, to a maximum of 20.",
                "STR", 13, "None", false, 1, "STR"));

        // --- MAGIC FEATS ---

        feats.add(newFeat("Magic Initiate", "Magic",
                "2024 Revision: You study the basics of magic. You learn three cantrips of your choice from any class's spell list. " +
                "You also learn one 1st-level spell from any class's spell list. You can cast that spell once without a spell slot, " +
                "regaining the ability after a long rest. Learned spells can use other spell slots of the appropriate level.",
                null, 0, "None", true, 0, null));

        feats.add(newFeat("Ritual Caster", "Magic",
                "You know how to cast spells as rituals. You have a ritual book containing two 1st-level spells of your choice. " +
                "You can cast a spell as a ritual if it has the ritual tag. You can add spells to your ritual book.",
                null, 0, "None", false, 0, null));

        feats.add(newFeat("War Caster", "Magic",
                "You have 200 years or more of practice concentrating on your magic. Your Constitution score increases by 1, " +
                "to a maximum of 20. You have advantage on Constitution saving throws to maintain concentration. " +
                "You can use your reaction to cast a spell when triggered by an enemy's attack.",
                "INT", 13, "None", false, 1, "INT"));

        // --- SKILL FEATS ---

        feats.add(newFeat("Actor", "Skill",
                "You have mastered the art of performance. Your Charisma score increases by 1, to a maximum of 20. " +
                "You have advantage on Charisma (Deception) and Charisma (Performance) checks to impersonate others.",
                "CHA", 13, "None", false, 1, "CHA"));

        feats.add(newFeat("Crusader", "Skill",
                "You are a skilled warrior. Your Strength score increases by 1, to a maximum of 20. " +
                "You gain proficiency with one type of armor of your choice. When you score a critical hit, " +
                "you can roll one additional weapon damage die.",
                "STR", 13, "None", false, 1, "STR"));

        feats.add(newFeat("Musician", "Skill",
                "2024 PHB: You have mastered a musical instrument. Your Charisma score increases by 1, to a maximum of 20. " +
                "You gain proficiency with one musical instrument of your choice. " +
                "You gain one Heroic Inspiration after any rest (short or long).",
                "CHA", 13, "None", false, 1, "CHA"));

        feats.add(newFeat("Athlete", "Skill",
                "Your Strength score increases by 1, to a maximum of 20. You can climb at full speed. " +
                "When you make a running long jump, the distance you can jump is equal to your Strength score in feet.",
                "STR", 13, "None", false, 1, "STR"));

        feats.add(newFeat("Navigator", "Skill",
                "Your Wisdom score increases by 1, to a maximum of 20. You have advantage on Wisdom (Survival) checks " +
                "to navigate. You can determine your direction and distance to major geographical features even in " +
                "unfamiliar terrain or magical environments.",
                "WIS", 13, "None", false, 1, "WIS"));

        // --- DEFENSE FEATS ---

        feats.add(newFeat("Resilient", "Defense",
                "You gain resistance to one damage type of your choice. Your choice of Constitution, Dexterity, " +
                "or Wisdom increases by 1, to a maximum of 20. You gain proficiency in saving throws using the chosen ability.",
                null, 0, "None", true, 1, "CON"));

        feats.add(newFeat("Durable", "Defense",
                "Your Constitution score increases by 1, to a maximum of 20. When you take damage that would reduce you to 0 hit points, " +
                "you instead drop to 1 hit point. The damage is reduced to half. You can use this feature once per long rest.",
                "CON", 13, "None", false, 1, "CON"));

        featRepository.saveAll(feats);
        log.info("Populated {} D&D 5e feats", feats.size());
    }

    private Dnd5eFeat newFeat(String name, String category, String description,
                               String abilityRequirement, int abilityScoreRequired,
                               String prerequisite, boolean repeatable,
                               int abilityIncrease, String abilityIncreased) {
        Dnd5eFeat feat = new Dnd5eFeat();
        feat.setName(name);
        feat.setDescription(description);
        feat.setCategory(category);
        feat.setAbilityRequirement(abilityRequirement);
        feat.setAbilityScoreRequired(abilityScoreRequired);
        feat.setPrerequisite(prerequisite != null ? prerequisite : "None");
        feat.setRepeatable(repeatable);
        feat.setAbilityIncrease(abilityIncrease);
        feat.setAbilityIncreased(abilityIncreased);
        return feat;
    }
}
