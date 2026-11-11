package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5ePsion;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5ePsionicPower;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5ePsionRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5ePsionicPowerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for managing D&D 5e Psion class mechanics.
 * <p>
 * The Psion is a new class in D&D 2024 that uses psionic power points instead of
 * spell slots. Psions have four subclasses: Psi Warper, Metamorph, Psykinetic, and Telepath.
 * </p>
 * <p>
 * Key mechanics:
 * - Power Points: Used to fuel psionic powers instead of spell slots
 * - Psionic Surge: A class feature that enhances psionic effects
 * - Subclass abilities: Each subclass grants unique psionic powers
 * - Psionic training: Psions can learn new powers through study
 * </p>
 * <p>
 * Power point progression (based on psion level):
 * - Level 1: 2 power points
 * - Level 2: 3 power points
 * - Level 3: 4 power points
 * - Level 4: 5 power points
 * - Level 5: 6 power points
 * - Level 6: 7 power points
 * - Level 7: 8 power points
 * - Level 8: 9 power points
 * - Level 9: 10 power points
 * - Level 10: 11 power points
 * - Level 11: 12 power points
 * - Level 12: 13 power points
 * - Level 13: 14 power points
 * - Level 14: 15 power points
 * - Level 15: 16 power points
 * - Level 16: 17 power points
 * - Level 17: 18 power points
 * - Level 18: 19 power points
 * - Level 19: 20 power points
 * - Level 20: 21 power points
 * </p>
 */
@Service
public class Dnd5ePsionService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5ePsionService.class);
    private static final Random random = new Random();

    private final Dnd5ePsionRepository psionRepository;
    private final Dnd5ePsionicPowerRepository powerRepository;
    private final Dnd5eCharacterRepository characterRepository;

    public Dnd5ePsionService(Dnd5ePsionRepository psionRepository,
                             Dnd5ePsionicPowerRepository powerRepository,
                             Dnd5eCharacterRepository characterRepository) {
        this.psionRepository = psionRepository;
        this.powerRepository = powerRepository;
        this.characterRepository = characterRepository;
    }

    // ==================== Psion Management ====================

    /**
     * Get or create a psion record for a character.
     */
    @Transactional
    public Dnd5ePsion getOrCreatePsion(Long characterId, String subclass) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        // Validate subclass
        if (!isValidSubclass(subclass)) {
            throw new IllegalArgumentException("Invalid psion subclass: " + subclass +
                    ". Valid subclasses: Psi Warper, Metamorph, Psykinetic, Telepath");
        }

        return psionRepository.findByCharacterId(characterId)
                .orElseGet(() -> {
                    Dnd5ePsion psion = new Dnd5ePsion();
                    psion.setCharacterId(characterId);
                    psion.setCharacterName(character.getName());
                    psion.setSubclass(subclass);
                    psion.setPsionLevel(1);
                    psion.setPrimaryAbility("INT");
                    psion.setKnownPowers(4);
                    updatePowerPoints(psion);
                    updateSurgePoints(psion);
                    Dnd5ePsion saved = psionRepository.save(psion);
                    log.info("Created psion record for '{}' with subclass {}", character.getName(), subclass);
                    return saved;
                });
    }

    /**
     * Get a psion by character ID.
     */
    @Transactional(readOnly = true)
    public Dnd5ePsion getPsion(Long characterId) {
        return psionRepository.findByCharacterId(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Psion not found for character: " + characterId));
    }

    /**
     * List all psions.
     */
    @Transactional(readOnly = true)
    public List<Dnd5ePsion> listAllPsions() {
        return psionRepository.findAll();
    }

    /**
     * List psions by subclass.
     */
    @Transactional(readOnly = true)
    public List<Dnd5ePsion> listPsionsBySubclass(String subclass) {
        return psionRepository.findBySubclass(subclass);
    }

    /**
     * Update a psion's level and recalculate power points and surge points.
     */
    @Transactional
    public Dnd5ePsion updatePsionLevel(Long characterId, int newLevel) {
        Dnd5ePsion psion = getPsion(characterId);
        if (newLevel < 1 || newLevel > 20) {
            throw new IllegalArgumentException("Psion level must be between 1 and 20");
        }
        psion.setPsionLevel(newLevel);
        updatePowerPoints(psion);
        updateSurgePoints(psion);
        // Update known powers based on level
        psion.setKnownPowers(getKnownPowersForLevel(newLevel));
        Dnd5ePsion saved = psionRepository.save(psion);
        log.info("Updated psion '{}' to level {}", psion.getCharacterName(), newLevel);
        return saved;
    }

    /**
     * Set the psion's primary ability (INT or WIS).
     */
    @Transactional
    public Dnd5ePsion setPrimaryAbility(Long characterId, String ability) {
        Dnd5ePsion psion = getPsion(characterId);
        if (!"INT".equalsIgnoreCase(ability) && !"WIS".equalsIgnoreCase(ability)) {
            throw new IllegalArgumentException("Primary ability must be INT or WIS");
        }
        psion.setPrimaryAbility(ability.toUpperCase());
        Dnd5ePsion saved = psionRepository.save(psion);
        log.info("Set primary ability for '{}' to {}", psion.getCharacterName(), ability.toUpperCase());
        return saved;
    }

    // ==================== Power Point Management ====================

    /**
     * Spend power points to use a psionic power.
     */
    @Transactional
    public PsionicPowerResult spendPowerPoints(Long characterId, String powerName, int cost) {
        Dnd5ePsion psion = getPsion(characterId);

        if (psion.getPowerPoints() < cost) {
            return new PsionicPowerResult(
                    false, psion.getPowerPoints(), cost,
                    "Not enough power points. Have " + psion.getPowerPoints() + ", need " + cost
            );
        }

        psion.setPowerPoints(psion.getPowerPoints() - cost);
        psionRepository.save(psion);
        log.info("Character '{}' spent {} power points for '{}'", psion.getCharacterName(), cost, powerName);

        return new PsionicPowerResult(
                true, psion.getPowerPoints(), cost,
                "Successfully spent " + cost + " power points for '" + powerName + "'"
        );
    }

    /**
     * Restore all power points (e.g., after a long rest).
     */
    @Transactional
    public Dnd5ePsion restorePowerPoints(Long characterId) {
        Dnd5ePsion psion = getPsion(characterId);
        psion.setPowerPoints(psion.getMaxPowerPoints());
        psionRepository.save(psion);
        log.info("Restored all power points for '{}'", psion.getCharacterName());
        return psion;
    }

    /**
     * Add power points (e.g., from a feature or item).
     */
    @Transactional
    public Dnd5ePsion addPowerPoints(Long characterId, int amount) {
        Dnd5ePsion psion = getPsion(characterId);
        psion.setPowerPoints(psion.getPowerPoints() + amount);
        psionRepository.save(psion);
        log.info("Added {} power points to '{}'", amount, psion.getCharacterName());
        return psion;
    }

    // ==================== Psionic Surge ====================

    /**
     * Activate psionic surge, enhancing the next psionic power.
     */
    @Transactional
    public Dnd5ePsion activateSurge(Long characterId) {
        Dnd5ePsion psion = getPsion(characterId);

        if (psion.getSurgePoints() < 1) {
            throw new IllegalStateException("No surge points available for '" + psion.getCharacterName() + "'");
        }

        psion.setSurgePoints(psion.getSurgePoints() - 1);
        psion.setSurgeActive(true);
        psionRepository.save(psion);
        log.info("Activated psionic surge for '{}'", psion.getCharacterName());
        return psion;
    }

    /**
     * Deactivate psionic surge (e.g., after using a power or at end of turn).
     */
    @Transactional
    public Dnd5ePsion deactivateSurge(Long characterId) {
        Dnd5ePsion psion = getPsion(characterId);
        psion.setSurgeActive(false);
        psionRepository.save(psion);
        log.info("Deactivated psionic surge for '{}'", psion.getCharacterName());
        return psion;
    }

    /**
     * Restore all surge points (e.g., after a long rest).
     */
    @Transactional
    public Dnd5ePsion restoreSurgePoints(Long characterId) {
        Dnd5ePsion psion = getPsion(characterId);
        psion.setSurgePoints(psion.getMaxSurgePoints());
        psion.setSurgeActive(false);
        psionRepository.save(psion);
        log.info("Restored all surge points for '{}'", psion.getCharacterName());
        return psion;
    }

    // ==================== Psionic Powers ====================

    /**
     * List all psionic powers.
     */
    @Transactional(readOnly = true)
    public List<Dnd5ePsionicPower> listAllPowers() {
        return powerRepository.findAll();
    }

    /**
     * Get a psionic power by name.
     */
    @Transactional(readOnly = true)
    public Dnd5ePsionicPower getPowerByName(String name) {
        return powerRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Psionic power not found: " + name));
    }

    /**
     * List psionic powers by level.
     */
    @Transactional(readOnly = true)
    public List<Dnd5ePsionicPower> listPowersByLevel(int level) {
        return powerRepository.findByPowerLevel(level);
    }

    /**
     * List psionic powers by subclass.
     */
    @Transactional(readOnly = true)
    public List<Dnd5ePsionicPower> listPowersBySubclass(String subclass) {
        return powerRepository.findBySubclass(subclass);
    }

    /**
     * List psionic cantrips (level 0).
     */
    @Transactional(readOnly = true)
    public List<Dnd5ePsionicPower> listCantrips() {
        return powerRepository.findByPowerLevelZero();
    }

    /**
     * List psionic powers by school.
     */
    @Transactional(readOnly = true)
    public List<Dnd5ePsionicPower> listPowersBySchool(String school) {
        return powerRepository.findBySchool(school);
    }

    /**
     * Search psionic powers by name.
     */
    @Transactional(readOnly = true)
    public List<Dnd5ePsionicPower> searchPowers(String searchTerm) {
        return powerRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    /**
     * Create a new psionic power definition.
     */
    @Transactional
    public Dnd5ePsionicPower createPower(String name, int level, int cost, String castingTime,
                                          String duration, String range, boolean concentration,
                                          boolean ritual, String subclass, String school, String description) {
        if (powerRepository.existsByName(name)) {
            throw new IllegalStateException("Psionic power already exists: " + name);
        }

        Dnd5ePsionicPower power = new Dnd5ePsionicPower();
        power.setName(name);
        power.setPowerLevel(level);
        power.setPowerPointCost(cost);
        power.setCastingTime(castingTime);
        power.setDuration(duration);
        power.setRange(range);
        power.setConcentration(concentration);
        power.setRitual(ritual);
        power.setSubclass(subclass);
        power.setSchool(school);
        power.setDescription(description);

        Dnd5ePsionicPower saved = powerRepository.save(power);
        log.info("Created psionic power: {}", name);
        return saved;
    }

    /**
     * Delete a psionic power by name.
     */
    @Transactional
    public void deletePower(String name) {
        Dnd5ePsionicPower power = getPowerByName(name);
        powerRepository.delete(power);
        log.info("Deleted psionic power: {}", name);
    }

    /**
     * Use a psionic power, spending power points and optionally activating surge.
     */
    @Transactional
    public PsionicPowerResult usePower(Long characterId, String powerName, boolean useSurge) {
        Dnd5ePsion psion = getPsion(characterId);
        Dnd5ePsionicPower power = getPowerByName(powerName);

        // Check if power is available to this subclass
        if (!"All".equals(power.getSubclass()) && !power.getSubclass().equals(psion.getSubclass())) {
            return new PsionicPowerResult(
                    false, psion.getPowerPoints(), power.getPowerPointCost(),
                    "Power '" + powerName + "' is not available to " + psion.getSubclass() + " subclass"
            );
        }

        // Calculate cost (cantrips are free)
        int cost = power.getPowerLevel() == 0 ? 0 : power.getPowerPointCost();

        if (psion.getPowerPoints() < cost) {
            return new PsionicPowerResult(
                    false, psion.getPowerPoints(), cost,
                    "Not enough power points. Have " + psion.getPowerPoints() + ", need " + cost
            );
        }

        // Spend power points
        psion.setPowerPoints(psion.getPowerPoints() - cost);

        // Handle surge
        if (useSurge && psion.isSurgeActive()) {
            psion.setSurgeActive(false);
            log.info("Psionic surge consumed by '{}' for '{}'", psion.getCharacterName(), powerName);
        }

        psionRepository.save(psion);

        // Roll for power effect
        int effectRoll = random.nextInt(20) + 1;
        String result;
        if (effectRoll == 20) {
            result = "Critical psionic resonance! The power achieves maximum effect.";
        } else if (effectRoll == 1) {
            result = "Psionic backlash! The power misfires with unexpected consequences.";
        } else {
            result = "The psionic power manifests successfully.";
        }

        return new PsionicPowerResult(
                true, psion.getPowerPoints(), cost,
                result, powerName, effectRoll, useSurge && psion.isSurgeActive()
        );
    }

    // ==================== Long Rest ====================

    /**
     * Restore all psionic resources after a long rest.
     */
    @Transactional
    public Dnd5ePsion longRest(Long characterId) {
        Dnd5ePsion psion = getPsion(characterId);
        psion.setPowerPoints(psion.getMaxPowerPoints());
        psion.setSurgePoints(psion.getMaxSurgePoints());
        psion.setSurgeActive(false);
        psionRepository.save(psion);
        log.info("Long rest completed for psion '{}'", psion.getCharacterName());
        return psion;
    }

    // ==================== Helper Methods ====================

    private boolean isValidSubclass(String subclass) {
        return "Psi Warper".equalsIgnoreCase(subclass) ||
                "Metamorph".equalsIgnoreCase(subclass) ||
                "Psykinetic".equalsIgnoreCase(subclass) ||
                "Telepath".equalsIgnoreCase(subclass);
    }

    private void updatePowerPoints(Dnd5ePsion psion) {
        int maxPoints = calculateMaxPowerPoints(psion.getPsionLevel());
        psion.setMaxPowerPoints(maxPoints);
        psion.setPowerPoints(maxPoints);
    }

    private void updateSurgePoints(Dnd5ePsion psion) {
        int maxSurge = calculateMaxSurgePoints(psion.getPsionLevel());
        psion.setMaxSurgePoints(maxSurge);
        psion.setSurgePoints(maxSurge);
    }

    private int calculateMaxPowerPoints(int level) {
        // Power points = level + 1
        return level + 1;
    }

    private int calculateMaxSurgePoints(int level) {
        // Surge points: 1 at level 1, increases at levels 4, 8, 12, 16, 20
        if (level >= 20) return 5;
        if (level >= 16) return 4;
        if (level >= 12) return 3;
        if (level >= 8) return 2;
        if (level >= 4) return 1;
        return 0;
    }

    private int getKnownPowersForLevel(int level) {
        // Known powers increase with level
        if (level >= 20) return 15;
        if (level >= 19) return 14;
        if (level >= 17) return 13;
        if (level >= 15) return 12;
        if (level >= 13) return 11;
        if (level >= 11) return 10;
        if (level >= 9) return 9;
        if (level >= 7) return 8;
        if (level >= 5) return 7;
        if (level >= 3) return 6;
        return 4;
    }

    // ==================== Database Initialization ====================

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializePsionDatabase() {
        if (powerRepository.count() > 0) {
            log.info("Psionic powers database already populated, skipping initialization");
            return;
        }

        log.info("Initializing psionic powers database...");
        populatePsionicPowers();
        log.info("Psionic powers database initialized successfully");
    }

    private void populatePsionicPowers() {
        // --- Cantrips (Level 0) ---
        createPower("Psionic Blast", 0, 0, "1 action", "Instantaneous", "60 feet",
                false, false, "All", "Evocation",
                "You release a burst of psionic energy in a line. Each target must make a DEX save or take force damage.");

        createPower("Mind Sliver", 0, 0, "1 action", "Instantaneous", "60 feet",
                false, false, "All", "Enchantment",
                "You fling a shard of psychic energy at a creature. It takes psychic damage and has disadvantage on its next INT, WIS, or CHA check.");

        createPower("Message", 0, 0, "1 action", "1 round", "120 feet",
                false, false, "Telepath", "Divination",
                "You send a word to a creature within range as a telepathic message.");

        createPower("Prestidigitation", 0, 0, "1 action", "1 hour", "Touch",
                false, false, "All", "Transmutation",
                "You manifest minor psionic effects for sensory or cosmetic purposes.");

        // --- Level 1 Powers ---
        createPower("Dissonant Whispers", 1, 2, "1 action", "Instantaneous", "60 feet",
                false, false, "All", "Enchantment",
                "A whispering voice fills a creature's mind. The target must make a WIS save or take psychic damage and have disadvantage on attack rolls.");

        createPower("Heroism", 1, 2, "1 action", "Concentration, up to 1 minute", "Touch",
                true, false, "All", "Enchantment",
                "A willing creature you touch is imbued with psionic bravery. The target has advantage on WIS saves and can reroll failed ability checks.");

        createPower("Identify", 1, 2, "1 action", "Instantaneous", "Touch",
                false, true, "All", "Divination",
                "You use psionic senses to identify a magic item or other object.");

        createPower("Shield", 1, 2, "1 reaction", "1 round", "Self",
                false, false, "All", "Abjuration",
                "An invisible barrier of psionic energy protects you. You have advantage on your next DEX save.");

        // --- Level 2 Powers ---
        createPower("Crown of Madness", 2, 3, "1 action", "Concentration, up to 1 minute", "60 feet",
                true, false, "All", "Enchantment",
                "You plant a psionic suggestion in a creature's mind. You control the creature as an action until the spell ends.");

        createPower("Detect Thoughts", 2, 3, "1 action", "Concentration, up to 1 minute", "Self",
                true, false, "Telepath", "Divination",
                "You can detect thinking creatures and read surface thoughts of creatures within 30 feet.");

        createPower("Mirror Image", 2, 3, "1 action", "1 minute", "Self",
                false, false, "Metamorph", "Illusion",
                "Psionic energy creates phantasmal duplicates of you. Attackers have disadvantage until the images are destroyed.");

        createPower("Suggestion", 2, 3, "1 action", "Concentration, up to 8 hours", "30 feet",
                true, false, "All", "Enchantment",
                "You suggest a course of activity to a creature. The target must make a WIS save or follow your suggestion.");

        // --- Level 3 Powers ---
        createPower("Clairvoyance", 3, 4, "10 minutes", "Concentration, up to 10 minutes", "1 mile",
                true, false, "Telepath", "Divination",
                "You create a sensor to perceive your surroundings at a distant location through psionic connection.");

        createPower("Dispel Magic", 3, 4, "1 action", "Instantaneous", "120 feet",
                false, false, "All", "Abjuration",
                "You use psionic energy to dispel magical effects. Make an ability check for each spell of 4th level or higher.");

        createPower("Fear", 3, 4, "1 action", "Concentration, up to 1 minute", "30 feet",
                true, false, "All", "Illusion",
                "You project psionic terror. Each target must make a WIS save or be frightened and drop what it holds.");

        createPower("Psychic Scream", 3, 4, "1 action", "Instantaneous", "60 feet",
                false, false, "Psi Warper", "Enchantment",
                "You unleash a psionic scream. The target takes psychic damage and is stunned until the end of its next turn.");

        // --- Level 4 Powers ---
        createPower("Dominate Person", 4, 5, "1 action", "Concentration, up to 1 minute", "60 feet",
                true, false, "All", "Enchantment",
                "You attempt to control a humanoid. The target must make a WIS save or you control its actions on your turns.");

        createPower("Greater Invisibility", 4, 5, "1 action", "Concentration, up to 1 minute", "Touch",
                true, false, "Metamorph", "Illusion",
                "A creature you touch becomes invisible until the end of the spell. Attacks don't break invisibility.");

        createPower("Hallucinatory Terrain", 4, 5, "10 minutes", "Concentration, up to 24 hours", "150 feet",
                true, false, "Psi Warper", "Illusion",
                "You use psionic energy to make terrain look, sound, and smell different. Creatures have disadvantage on WIS checks.");

        createPower("Modify Memory", 4, 5, "1 action", "Concentration, up to 1 minute", "30 feet",
                true, false, "Telepath", "Enchantment",
                "You attempt to modify a creature's memories. The target must make a WIS save or you can suppress or alter memories.");

        // --- Level 5 Powers ---
        createPower("Contact Other Plane", 5, 6, "1 action", "Special", "Special",
                false, true, "Telepath", "Necromancy",
                "You establish a psionic link to an entity on another plane. The entity may answer your questions.");

        createPower("Dominate Monster", 5, 6, "1 action", "Concentration, up to 1 minute", "60 feet",
                true, false, "All", "Enchantment",
                "You attempt to control a creature. The target must make a WIS save or you control its actions on your turns.");

        createPower("Mass Suggestion", 5, 6, "1 action", "Concentration, up to 8 hours", "60 feet",
                true, false, "All", "Enchantment",
                "You suggest a course of activity to multiple creatures. Each target must make a WIS save.");

        createPower("Telekinesis", 5, 6, "1 action", "Concentration, up to 10 minutes", "60 feet",
                true, false, "Psykinetic", "Transmutation",
                "You move or manipulate objects or creatures with your mind. You can push, pull, or lift objects weighing up to 1,000 pounds.");

        // --- Subclass-specific powers ---
        createPower("Psychic Enervation", 2, 3, "1 action", "Instantaneous", "60 feet",
                false, false, "Psi Warper", "Necromancy",
                "You drain the life force of a creature. It takes necrotic damage and its maximum HP is reduced until it finishes a long rest.");

        createPower("Psychic Reanimation", 3, 4, "1 action", "Instantaneous", "Touch",
                false, false, "Psi Warper", "Necromancy",
                "You reanimate a dead creature as a mindless servant. The servant obeys your mental commands.");

        createPower("Psychic Transformation", 1, 2, "1 action", "Concentration, up to 1 hour", "Self",
                true, false, "Metamorph", "Transmutation",
                "You alter your physical form. You gain resistance to one damage type and can grow limbs or appendages.");

        createPower("Psychic Resilience", 2, 3, "1 reaction", "1 minute", "Self",
                false, false, "Metamorph", "Abjuration",
                "You harden your body with psionic energy. You gain temporary hit points and advantage on CON saves.");

        createPower("Psychic Crush", 3, 4, "1 action", "Concentration, up to 1 minute", "60 feet",
                true, false, "Psykinetic", "Evocation",
                "You crush a creature with psionic force. The target takes force damage and is grappled until the spell ends.");

        createPower("Psychic Bulwark", 4, 5, "1 reaction", "Concentration, up to 1 minute", "60 feet",
                true, false, "Psykinetic", "Abjuration",
                "You create a barrier of psionic force. Creatures have disadvantage on attack rolls against you.");

        createPower("Psychic Link", 1, 2, "1 action", "Concentration, up to 1 hour", "60 feet",
                true, false, "Telepath", "Divination",
                "You form a psionic link with a creature. You can communicate telepathically and share sensory information.");

        createPower("Psychic Probe", 2, 3, "1 action", "Concentration, up to 1 minute", "30 feet",
                true, false, "Telepath", "Divination",
                "You probe a creature's mind. You learn its surface thoughts, intentions, and emotional state.");
    }

    // ==================== Result DTOs ====================

    /**
     * Result of spending power points or using a psionic power.
     */
    public static class PsionicPowerResult {
        private final boolean success;
        private final int remainingPoints;
        private final int cost;
        private final String message;
        private final String powerName;
        private final int effectRoll;
        private final boolean surgeActive;

        public PsionicPowerResult(boolean success, int remainingPoints, int cost, String message) {
            this(success, remainingPoints, cost, message, null, 0, false);
        }

        public PsionicPowerResult(boolean success, int remainingPoints, int cost, String message,
                                   String powerName, int effectRoll, boolean surgeActive) {
            this.success = success;
            this.remainingPoints = remainingPoints;
            this.cost = cost;
            this.message = message;
            this.powerName = powerName;
            this.effectRoll = effectRoll;
            this.surgeActive = surgeActive;
        }

        public boolean isSuccess() { return success; }
        public int getRemainingPoints() { return remainingPoints; }
        public int getCost() { return cost; }
        public String getMessage() { return message; }
        public String getPowerName() { return powerName; }
        public int getEffectRoll() { return effectRoll; }
        public boolean isSurgeActive() { return surgeActive; }
    }
}
