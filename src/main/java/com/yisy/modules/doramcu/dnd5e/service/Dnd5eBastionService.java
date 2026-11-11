package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eBastion;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eBastionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing D&D 5e Bastions (party bases) from the DMG.
 * <p>
 * Provides CRUD operations for bastions, level-up mechanics with perk unlocking,
 * resource management, facility upgrades, and bastion defense.
 * </p>
 * <p>
 * Key mechanics:
 * - Level: Bastion level (1-20) determines available perks
 * - Theme: Fortress, Tower, Ship, Camp, Temple, etc.
 * - Perks: Unlocked at levels 1, 5, 10, 15, and 20
 * - Facilities: Crafting, Training, Library, Healing
 * - Resources: Gold invested determines upgrade options
 * - Defense: HP and AC for siege combat
 * </p>
 */
@Service
public class Dnd5eBastionService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eBastionService.class);

    private final Dnd5eBastionRepository bastionRepository;

    public Dnd5eBastionService(Dnd5eBastionRepository bastionRepository) {
        this.bastionRepository = bastionRepository;

        // Initialize database with example bastions if empty
        initializeBastionDatabase();
    }

    // ==================== Bastion CRUD ====================

    @Transactional
    public Dnd5eBastion createBastion(Dnd5eBastion bastion) {
        log.info("Creating bastion: {}", bastion.getName());
        return bastionRepository.save(bastion);
    }

    public Optional<Dnd5eBastion> getBastion(Long id) {
        return bastionRepository.findById(id);
    }

    public Optional<Dnd5eBastion> getBastionByName(String name) {
        return bastionRepository.findByName(name);
    }

    public List<Dnd5eBastion> listBastions() {
        return bastionRepository.findAll();
    }

    public List<Dnd5eBastion> listActiveBastions() {
        return bastionRepository.findByActiveTrue();
    }

    public List<Dnd5eBastion> listBastionsByTheme(String theme) {
        return bastionRepository.findByTheme(theme);
    }

    public List<Dnd5eBastion> listBastionsByLevelRange(int minLevel, int maxLevel) {
        return bastionRepository.findByLevelRange(minLevel, maxLevel);
    }

    public List<Dnd5eBastion> listBastionsWithCrafting() {
        return bastionRepository.findByHasCrafting();
    }

    public List<Dnd5eBastion> listBastionsWithTraining() {
        return bastionRepository.findByHasTraining();
    }

    public List<Dnd5eBastion> listBastionsWithLibrary() {
        return bastionRepository.findByHasLibrary();
    }

    public List<Dnd5eBastion> listBastionsWithHealing() {
        return bastionRepository.findByHasHealing();
    }

    public List<Dnd5eBastion> searchBastions(String keyword) {
        return bastionRepository.searchByKeyword(keyword);
    }

    @Transactional
    public Dnd5eBastion updateBastion(Long id, Dnd5eBastion updates) {
        return bastionRepository.findById(id)
                .map(bastion -> {
                    if (updates.getName() != null) {
                        bastion.setName(updates.getName());
                    }
                    if (updates.getTheme() != null) {
                        bastion.setTheme(updates.getTheme());
                    }
                    if (updates.getDescription() != null) {
                        bastion.setDescription(updates.getDescription());
                    }
                    if (updates.getLocation() != null) {
                        bastion.setLocation(updates.getLocation());
                    }
                    bastion.setLevel(updates.getLevel());
                    bastion.setHitPoints(updates.getHitPoints());
                    bastion.setMaxHitPoints(updates.getMaxHitPoints());
                    bastion.setArmorClass(updates.getArmorClass());
                    bastion.setGoldInvested(updates.getGoldInvested());
                    bastion.setPartySize(updates.getPartySize());
                    bastion.setRoomCount(updates.getRoomCount());
                    bastion.setHasCrafting(updates.isHasCrafting());
                    bastion.setHasTraining(updates.isHasTraining());
                    bastion.setHasLibrary(updates.isHasLibrary());
                    bastion.setHasHealing(updates.isHasHealing());
                    bastion.setActive(updates.isActive());
                    return bastionRepository.save(bastion);
                })
                .orElseThrow(() -> new RuntimeException("Bastion not found: " + id));
    }

    @Transactional
    public boolean deleteBastion(Long id) {
        if (bastionRepository.existsById(id)) {
            bastionRepository.deleteById(id);
            log.info("Deleted bastion: {}", id);
            return true;
        }
        log.warn("Cannot delete bastion - not found: {}", id);
        return false;
    }

    // ==================== Level-Up Mechanics ====================

    /**
     * Levels up a bastion, unlocking new perks and increasing stats.
     * Perks unlock at levels 1, 5, 10, 15, and 20.
     *
     * @param bastionId the bastion ID
     * @return list of newly unlocked perks
     */
    @Transactional
    public List<String> levelUpBastion(Long bastionId) {
        Dnd5eBastion bastion = bastionRepository.findById(bastionId)
                .orElseThrow(() -> new RuntimeException("Bastion not found: " + bastionId));

        int oldLevel = bastion.getLevel();
        int newLevel = Math.min(20, oldLevel + 1);
        if (newLevel == oldLevel) {
            log.info("Bastion {} is already at max level", bastion.getName());
            return List.of();
        }
        bastion.setLevel(newLevel);

        // Increase stats based on level
        bastion.setMaxHitPoints(bastion.getMaxHitPoints() + 20);
        bastion.setHitPoints(bastion.getMaxHitPoints());
        bastion.setArmorClass(bastion.getArmorClass() + (newLevel % 5 == 0 ? 1 : 0));
        bastion.setRoomCount(bastion.getRoomCount() + (newLevel % 5 == 0 ? 1 : 0));

        bastionRepository.save(bastion);

        // Determine unlocked perks
        List<String> perks = new ArrayList<>();
        switch (newLevel) {
            case 1:
                perks.add("Basic shelter and safe resting");
                perks.add("Party can long rest without interruption");
                break;
            case 5:
                perks.add("Reinforced walls (+1 AC)");
                perks.add("Additional room for storage");
                perks.add("Guard posts for early warning");
                break;
            case 10:
                perks.add("Magical wards against scrying");
                perks.add("Teleportation circle access");
                perks.add("Expanded storage capacity");
                break;
            case 15:
                perks.add("Planar protection barriers");
                perks.add("Automated defenses (construct sentries)");
                perks.add("Healing sanctuary bonus (+1d4 HP per long rest)");
                break;
            case 20:
                perks.add("Legendary status — known across the realm");
                perks.add("Extraplanar allies may visit");
                perks.add("Immunity to most siege weapons");
                break;
            default:
                // Minor improvements for intermediate levels
                if (newLevel % 2 == 0) {
                    perks.add("Improved defenses and fortifications");
                } else {
                    perks.add("Enhanced facilities and amenities");
                }
                break;
        }

        log.info("Bastion {} leveled up from {} to {}. Unlocked perks: {}",
                bastion.getName(), oldLevel, newLevel, perks);
        return perks;
    }

    // ==================== Facility Management ====================

    /**
     * Adds a facility to the bastion.
     * Cost: 500 gp per facility.
     */
    @Transactional
    public BastionFacilityResult addFacility(Long bastionId, String facilityType) {
        return bastionRepository.findById(bastionId)
                .map(bastion -> {
                    int cost = 500;
                    if (bastion.getGoldInvested() < cost) {
                        return new BastionFacilityResult(false, bastion.getGoldInvested(), cost,
                                "Insufficient gold. Need " + cost + " gp, have " + bastion.getGoldInvested() + " gp.");
                    }

                    boolean added = false;
                    switch (facilityType.toLowerCase()) {
                        case "crafting":
                            if (!bastion.isHasCrafting()) {
                                bastion.setHasCrafting(true);
                                added = true;
                            }
                            break;
                        case "training":
                            if (!bastion.isHasTraining()) {
                                bastion.setHasTraining(true);
                                added = true;
                            }
                            break;
                        case "library":
                            if (!bastion.isHasLibrary()) {
                                bastion.setHasLibrary(true);
                                added = true;
                            }
                            break;
                        case "healing":
                            if (!bastion.isHasHealing()) {
                                bastion.setHasHealing(true);
                                added = true;
                            }
                            break;
                        default:
                            return new BastionFacilityResult(false, bastion.getGoldInvested(), cost,
                                    "Unknown facility type: " + facilityType);
                    }

                    if (added) {
                        bastion.setGoldInvested(bastion.getGoldInvested() - cost);
                        bastionRepository.save(bastion);
                        log.info("Added {} facility to bastion {}", facilityType, bastion.getName());
                        return new BastionFacilityResult(true, bastion.getGoldInvested(), cost,
                                facilityType + " facility added to " + bastion.getName());
                    } else {
                        return new BastionFacilityResult(false, bastion.getGoldInvested(), cost,
                                bastion.getName() + " already has " + facilityType + " facility");
                    }
                })
                .orElseThrow(() -> new RuntimeException("Bastion not found: " + bastionId));
    }

    // ==================== Resource Management ====================

    /**
     * Invests gold into the bastion for upgrades.
     */
    @Transactional
    public void investGold(Long bastionId, int amount) {
        bastionRepository.findById(bastionId).ifPresent(bastion -> {
            bastion.setGoldInvested(bastion.getGoldInvested() + amount);
            bastionRepository.save(bastion);
            log.info("Invested {} gp into bastion {}. Total: {} gp",
                    amount, bastion.getName(), bastion.getGoldInvested());
        });
    }

    /**
     * Withdraws gold from the bastion.
     */
    @Transactional
    public boolean withdrawGold(Long bastionId, int amount) {
        return bastionRepository.findById(bastionId)
                .map(bastion -> {
                    if (bastion.getGoldInvested() >= amount) {
                        bastion.setGoldInvested(bastion.getGoldInvested() - amount);
                        bastionRepository.save(bastion);
                        log.info("Withdrew {} gp from bastion {}. Remaining: {} gp",
                                amount, bastion.getName(), bastion.getGoldInvested());
                        return true;
                    }
                    log.warn("Insufficient gold in bastion {} to withdraw {} gp", bastion.getName(), amount);
                    return false;
                })
                .orElseGet(() -> {
                    log.error("Bastion not found: {}", bastionId);
                    return false;
                });
    }

    // ==================== Bastion Defense ====================

    /**
     * Deals siege damage to the bastion.
     */
    @Transactional
    public BastionDamageResult dealSiegeDamage(Long bastionId, int damage) {
        return bastionRepository.findById(bastionId)
                .map(bastion -> {
                    int remainingHp = Math.max(0, bastion.getHitPoints() - damage);
                    bastion.setHitPoints(remainingHp);
                    bastionRepository.save(bastion);

                    boolean isDestroyed = remainingHp <= 0;
                    String message;
                    if (isDestroyed) {
                        message = bastion.getName() + " has been destroyed!";
                        bastion.setActive(false);
                        bastionRepository.save(bastion);
                    } else {
                        message = bastion.getName() + " has " + remainingHp + " HP remaining";
                    }

                    log.info("Siege damage to {}: {} damage. {} {}",
                            bastion.getName(), damage, remainingHp, message);
                    return new BastionDamageResult(remainingHp, isDestroyed, message);
                })
                .orElseThrow(() -> new RuntimeException("Bastion not found: " + bastionId));
    }

    /**
     * Repairs the bastion, restoring HP.
     */
    @Transactional
    public void repairBastion(Long bastionId, int amount) {
        bastionRepository.findById(bastionId).ifPresent(bastion -> {
            int newHp = Math.min(bastion.getMaxHitPoints(), bastion.getHitPoints() + amount);
            bastion.setHitPoints(newHp);
            if (newHp > 0 && !bastion.isActive()) {
                bastion.setActive(true);
            }
            bastionRepository.save(bastion);
            log.info("Repaired bastion {} by {} HP. Current: {} / {}",
                    bastion.getName(), amount, bastion.getHitPoints(), bastion.getMaxHitPoints());
        });
    }

    /**
     * Gets the available perks for a bastion at its current level.
     */
    public List<String> getAvailablePerks(Long bastionId) {
        return bastionRepository.findById(bastionId)
                .map(bastion -> {
                    List<String> perks = new ArrayList<>();
                    int level = bastion.getLevel();

                    if (level >= 1) {
                        perks.add("Basic shelter and safe resting");
                        perks.add("Party can long rest without interruption");
                    }
                    if (level >= 5) {
                        perks.add("Reinforced walls (+1 AC)");
                        perks.add("Additional room for storage");
                        perks.add("Guard posts for early warning");
                    }
                    if (level >= 10) {
                        perks.add("Magical wards against scrying");
                        perks.add("Teleportation circle access");
                        perks.add("Expanded storage capacity");
                    }
                    if (level >= 15) {
                        perks.add("Planar protection barriers");
                        perks.add("Automated defenses (construct sentries)");
                        perks.add("Healing sanctuary bonus (+1d4 HP per long rest)");
                    }
                    if (level >= 20) {
                        perks.add("Legendary status — known across the realm");
                        perks.add("Extraplanar allies may visit");
                        perks.add("Immunity to most siege weapons");
                    }

                    // Facility perks
                    if (bastion.isHasCrafting()) {
                        perks.add("Crafting facility — reduced crafting time by 25%");
                    }
                    if (bastion.isHasTraining()) {
                        perks.add("Training facility — advantage on skill checks related to training");
                    }
                    if (bastion.isHasLibrary()) {
                        perks.add("Library — advantage on Arcana, History, and Religion checks");
                    }
                    if (bastion.isHasHealing()) {
                        perks.add("Healing sanctuary — +1d4 HP per long rest");
                    }

                    return perks;
                })
                .orElseThrow(() -> new RuntimeException("Bastion not found: " + bastionId));
    }

    // ==================== Bastion Themes Reference ====================

    public List<String> getBastionThemes() {
        return List.of("Fortress", "Tower", "Ship", "Camp", "Temple", "Manor",
                "Underground Hideout", "Treehouse", "Floating Platform", "Cave System");
    }

    // ==================== Database Initialization ====================

    @Transactional
    void initializeBastionDatabase() {
        if (bastionRepository.count() > 0) {
            log.info("Bastion database already populated, skipping initialization");
            return;
        }

        log.info("Initializing bastion database with example bastions");
        populateExampleBastions();
        log.info("Bastion database initialization complete");
    }

    void populateExampleBastions() {
        // Fortress — The Iron Keep
        Dnd5eBastion ironKeep = new Dnd5eBastion();
        ironKeep.setName("The Iron Keep");
        ironKeep.setTheme("Fortress");
        ironKeep.setLevel(1);
        ironKeep.setDescription("A massive stone fortress perched on a cliff overlooking the valley. " +
                "Its walls are reinforced with iron bands and guarded by loyal mercenaries.");
        ironKeep.setLocation("Cliffside overlooking the Western Valley");
        ironKeep.setMaxHitPoints(200);
        ironKeep.setHitPoints(200);
        ironKeep.setArmorClass(15);
        ironKeep.setGoldInvested(1000);
        ironKeep.setPartySize(4);
        ironKeep.setRoomCount(5);
        ironKeep.setHasCrafting(true);
        ironKeep.setHasTraining(false);
        ironKeep.setHasLibrary(false);
        ironKeep.setHasHealing(false);
        ironKeep.setActive(true);
        bastionRepository.save(ironKeep);

        // Tower — Arcane Spire
        Dnd5eBastion arcaneSpire = new Dnd5eBastion();
        arcaneSpire.setName("Arcane Spire");
        arcaneSpire.setTheme("Tower");
        arcaneSpire.setLevel(1);
        arcaneSpire.setDescription("A towering spire of white marble, wreathed in perpetual arcane energy. " +
                "The library within contains tomes of forgotten magic.");
        arcaneSpire.setLocation("The Enchanted Forest, near the Feywild border");
        arcaneSpire.setMaxHitPoints(150);
        arcaneSpire.setHitPoints(150);
        arcaneSpire.setArmorClass(13);
        arcaneSpire.setGoldInvested(800);
        arcaneSpire.setPartySize(3);
        arcaneSpire.setRoomCount(4);
        arcaneSpire.setHasCrafting(false);
        arcaneSpire.setHasTraining(false);
        arcaneSpire.setHasLibrary(true);
        arcaneSpire.setHasHealing(false);
        arcaneSpire.setActive(true);
        bastionRepository.save(arcaneSpire);

        // Ship — The Sea Wanderer
        Dnd5eBastion seaWanderer = new Dnd5eBastion();
        seaWanderer.setName("The Sea Wanderer");
        seaWanderer.setTheme("Ship");
        seaWanderer.setLevel(1);
        seaWanderer.setDescription("A sturdy galleon that serves as a mobile base. " +
                "Its hold is enchanted to provide comfortable quarters and storage.");
        seaWanderer.setLocation("The Sword Coast, currently anchored at Waterdeep");
        seaWanderer.setMaxHitPoints(120);
        seaWanderer.setHitPoints(120);
        seaWanderer.setArmorClass(12);
        seaWanderer.setGoldInvested(600);
        seaWanderer.setPartySize(4);
        seaWanderer.setRoomCount(3);
        seaWanderer.setHasCrafting(false);
        seaWanderer.setHasTraining(false);
        seaWanderer.setHasLibrary(false);
        seaWanderer.setHasHealing(false);
        seaWanderer.setActive(true);
        bastionRepository.save(seaWanderer);

        // Camp — Wanderer's Rest
        Dnd5eBastion wanderersRest = new Dnd5eBastion();
        wanderersRest.setName("Wanderer's Rest");
        wanderersRest.setTheme("Camp");
        wanderersRest.setLevel(1);
        wanderersRest.setDescription("A mobile encampment with enchanted tents that provide comfort " +
                "and protection from the elements. Easy to relocate as needed.");
        wanderersRest.setLocation("Mobile — currently in the High Forest");
        wanderersRest.setMaxHitPoints(50);
        wanderersRest.setHitPoints(50);
        wanderersRest.setArmorClass(10);
        wanderersRest.setGoldInvested(200);
        wanderersRest.setPartySize(4);
        wanderersRest.setRoomCount(3);
        wanderersRest.setHasCrafting(false);
        wanderersRest.setHasTraining(false);
        wanderersRest.setHasLibrary(false);
        wanderersRest.setHasHealing(false);
        wanderersRest.setActive(true);
        bastionRepository.save(wanderersRest);

        // Temple — Sanctuary of Light
        Dnd5eBastion sanctuaryOfLight = new Dnd5eBastion();
        sanctuaryOfLight.setName("Sanctuary of Light");
        sanctuaryOfLight.setTheme("Temple");
        sanctuaryOfLight.setLevel(1);
        sanctuaryOfLight.setDescription("A radiant temple dedicated to the gods of good. " +
                "Its healing sanctuary provides powerful restoration to those who rest within.");
        sanctuaryOfLight.setLocation("The heart of the Holy City of Illefarn");
        sanctuaryOfLight.setMaxHitPoints(180);
        sanctuaryOfLight.setHitPoints(180);
        sanctuaryOfLight.setArmorClass(14);
        sanctuaryOfLight.setGoldInvested(1200);
        sanctuaryOfLight.setPartySize(5);
        sanctuaryOfLight.setRoomCount(6);
        sanctuaryOfLight.setHasCrafting(false);
        sanctuaryOfLight.setHasTraining(false);
        sanctuaryOfLight.setHasLibrary(true);
        sanctuaryOfLight.setHasHealing(true);
        sanctuaryOfLight.setActive(true);
        bastionRepository.save(sanctuaryOfLight);

        // Underground Hideout — The Deep Warrens
        Dnd5eBastion deepWarrens = new Dnd5eBastion();
        deepWarrens.setName("The Deep Warrens");
        deepWarrens.setTheme("Underground Hideout");
        deepWarrens.setLevel(1);
        deepWarrens.setDescription("A network of tunnels and chambers carved deep beneath the city. " +
                "Perfect for covert operations and hiding from pursuers.");
        deepWarrens.setLocation("Beneath the Undercity of Baldur's Gate");
        deepWarrens.setMaxHitPoints(100);
        deepWarrens.setHitPoints(100);
        deepWarrens.setArmorClass(11);
        deepWarrens.setGoldInvested(400);
        deepWarrens.setPartySize(3);
        deepWarrens.setRoomCount(4);
        deepWarrens.setHasCrafting(true);
        deepWarrens.setHasTraining(false);
        deepWarrens.setHasLibrary(false);
        deepWarrens.setHasHealing(false);
        deepWarrens.setActive(true);
        bastionRepository.save(deepWarrens);
    }

    /**
     * DTO for bastion facility results.
     */
    public static class BastionFacilityResult {
        public final boolean success;
        public final int remainingGold;
        public final int cost;
        public final String message;

        public BastionFacilityResult(boolean success, int remainingGold, int cost, String message) {
            this.success = success;
            this.remainingGold = remainingGold;
            this.cost = cost;
            this.message = message;
        }
    }

    /**
     * DTO for bastion damage results.
     */
    public static class BastionDamageResult {
        public final int remainingHp;
        public final boolean isDestroyed;
        public final String message;

        public BastionDamageResult(int remainingHp, boolean isDestroyed, String message) {
            this.remainingHp = remainingHp;
            this.isDestroyed = isDestroyed;
            this.message = message;
        }
    }
}
