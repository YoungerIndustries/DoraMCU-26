package com.yisy.modules.doramcu.babylon5.service;

import com.yisy.modules.doramcu.babylon5.model.B5ShipDesign;
import com.yisy.modules.doramcu.babylon5.model.B5ShipDesign.B5ShipWeapon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for creating and managing Babylon 5 starship designs.
 * <p>
 * This service provides:
 * <ul>
 *   <li>Predefined ship design presets for each B5 faction</li>
 *   <li>Custom ship design creation and validation</li>
 *   <li>Ship design statistics calculation</li>
 *   <li>Small craft management</li>
 * </ul>
 * </p>
 * <p>
 * Ship designs follow the tonnage-based allocation system from Traveller,
 * adapted for B5-themed components and faction-specific technology levels.
 * </p>
 */
@Service
public class B5ShipDesignService {

    private static final Logger log = LoggerFactory.getLogger(B5ShipDesignService.class);
    private static final int MIN_TONNAGE = 1000;

    private final Map<Integer, B5ShipDesign> designs = new ConcurrentHashMap<>();
    private final AtomicInteger designIdCounter = new AtomicInteger(1);

    /**
     * Initializes the service with predefined ship design presets.
     */
    public void initialize() {
        log.info("Initializing B5 ship design service with presets...");
        loadPresets();
        log.info("B5 ship design service initialized: {} presets loaded", designs.size());
    }

    // ==================== Preset Designs ====================

    private void loadPresets() {
        // Earth Alliance presets
        designs.put(createEarthForceCorvette().getId(), createEarthForceCorvette());
        designs.put(createEarthForceDestroyer().getId(), createEarthForceDestroyer());
        designs.put(createEarthForceBattlestar().getId(), createEarthForceBattlestar());
        designs.put(createEarthFreighter().getId(), createEarthFreighter());

        // Narn presets
        designs.put(createNarnCrusader().getId(), createNarnCrusader());
        designs.put(createNarnCruiser().getId(), createNarnCruiser());

        // Centauri presets
        designs.put(createCentauriNightstar().getId(), createCentauriNightstar());
        designs.put(createCentauriCruiser().getId(), createCentauriCruiser());

        // Minbari presets
        designs.put(createMinbariBattleCruiser().getId(), createMinbariBattleCruiser());
        designs.put(createMinbariCruiser().getId(), createMinbariCruiser());

        // Shadow presets
        designs.put(createShadowShip().getId(), createShadowShip());

        // Vorlon presets
        designs.put(createVorlonShip().getId(), createVorlonShip());
    }

    /**
     * Creates an Earth Force Corvette preset.
     * Small, fast, and heavily armed for its size.
     */
    private B5ShipDesign createEarthForceCorvette() {
        B5ShipDesign design = new B5ShipDesign(1, "Earth Force Corvette", 2000);
        design.setFaction("Earth Alliance");
        design.setShipClass("Corvette");
        design.setHullType(B5ShipDesign.HullType.STANDARD);
        design.setJumpDrive(B5ShipDesign.JumpDrive.J3);
        design.setFusionDrive(B5ShipDesign.FusionDrive.M3);
        design.setPowerPlant(B5ShipDesign.PowerPlant.MEDIUM);
        design.setLifeSupport(B5ShipDesign.LifeSupport.STANDARD);
        design.setComputer(B5ShipDesign.ShipComputer.CLASS2);
        design.setCargoTons(100);
        design.setFuelTons(200);
        design.addWeapon(new B5ShipWeapon("Laser Battery", "Laser", 2, 10, 2, 1));
        design.addWeapon(new B5ShipWeapon("Missile Pods", "Missile", 1, 20, 4, 1));
        return design;
    }

    /**
     * Creates an Earth Force Destroyer preset.
     * Medium-sized warship with balanced armament.
     */
    private B5ShipDesign createEarthForceDestroyer() {
        B5ShipDesign design = new B5ShipDesign(2, "Earth Force Destroyer", 10000);
        design.setFaction("Earth Alliance");
        design.setShipClass("Destroyer");
        design.setHullType(B5ShipDesign.HullType.REINFORCED);
        design.setJumpDrive(B5ShipDesign.JumpDrive.J4);
        design.setFusionDrive(B5ShipDesign.FusionDrive.M4);
        design.setPowerPlant(B5ShipDesign.PowerPlant.LARGE);
        design.setLifeSupport(B5ShipDesign.LifeSupport.STANDARD);
        design.setComputer(B5ShipDesign.ShipComputer.CLASS3);
        design.setCargoTons(500);
        design.setFuelTons(1000);
        design.addWeapon(new B5ShipWeapon("Laser Battery", "Laser", 4, 15, 3, 1));
        design.addWeapon(new B5ShipWeapon("Missile Pods", "Missile", 2, 25, 6, 1));
        design.addWeapon(new B5ShipWeapon("Particle Beam", "Particle Beam", 1, 30, 4, 2));
        return design;
    }

    /**
     * Creates an Earth Force Battlestar preset.
     * Large capital ship with devastating firepower.
     */
    private B5ShipDesign createEarthForceBattlestar() {
        B5ShipDesign design = new B5ShipDesign(3, "Earth Force Battlestar", 50000);
        design.setFaction("Earth Alliance");
        design.setShipClass("Battlestar");
        design.setHullType(B5ShipDesign.HullType.REINFORCED);
        design.setJumpDrive(B5ShipDesign.JumpDrive.J5);
        design.setFusionDrive(B5ShipDesign.FusionDrive.M5);
        design.setPowerPlant(B5ShipDesign.PowerPlant.LARGE);
        design.setLifeSupport(B5ShipDesign.LifeSupport.STANDARD);
        design.setComputer(B5ShipDesign.ShipComputer.CLASS4);
        design.setCargoTons(2000);
        design.setFuelTons(5000);
        design.addWeapon(new B5ShipWeapon("Laser Battery", "Laser", 8, 20, 4, 1));
        design.addWeapon(new B5ShipWeapon("Missile Pods", "Missile", 4, 30, 8, 1));
        design.addWeapon(new B5ShipWeapon("Particle Beam", "Particle Beam", 2, 40, 6, 2));
        design.addWeapon(new B5ShipWeapon("Torpedo Tubes", "Torpedo", 2, 50, 10, 2));
        return design;
    }

    /**
     * Creates an Earth Alliance Freighter preset.
     * Cargo-focused merchant vessel.
     */
    private B5ShipDesign createEarthFreighter() {
        B5ShipDesign design = new B5ShipDesign(4, "Earth Alliance Freighter", 20000);
        design.setFaction("Earth Alliance");
        design.setShipClass("Freighter");
        design.setHullType(B5ShipDesign.HullType.STANDARD);
        design.setJumpDrive(B5ShipDesign.JumpDrive.J3);
        design.setFusionDrive(B5ShipDesign.FusionDrive.M2);
        design.setPowerPlant(B5ShipDesign.PowerPlant.MEDIUM);
        design.setLifeSupport(B5ShipDesign.LifeSupport.STANDARD);
        design.setComputer(B5ShipDesign.ShipComputer.CLASS2);
        design.setCargoTons(8000);
        design.setPassengerCount(50);
        design.setPassengerClass(B5ShipDesign.PassengerClass.THIRD);
        design.setFuelTons(2000);
        design.addWeapon(new B5ShipWeapon("Point Defense Laser", "Laser", 2, 10, 1, 1));
        return design;
    }

    /**
     * Creates a Narn Crusader preset.
     * Fast and agile, designed for hit-and-run tactics.
     */
    private B5ShipDesign createNarnCrusader() {
        B5ShipDesign design = new B5ShipDesign(5, "Narn Crusader", 5000);
        design.setFaction("Narn");
        design.setShipClass("Fighter");
        design.setHullType(B5ShipDesign.HullType.STANDARD);
        design.setJumpDrive(B5ShipDesign.JumpDrive.J2);
        design.setFusionDrive(B5ShipDesign.FusionDrive.M5);
        design.setPowerPlant(B5ShipDesign.PowerPlant.MEDIUM);
        design.setLifeSupport(B5ShipDesign.LifeSupport.BASIC);
        design.setComputer(B5ShipDesign.ShipComputer.CLASS2);
        design.setCargoTons(100);
        design.setFuelTons(500);
        design.addWeapon(new B5ShipWeapon("Laser Battery", "Laser", 2, 10, 2, 1));
        design.addWeapon(new B5ShipWeapon("Missile Pods", "Missile", 1, 15, 3, 1));
        return design;
    }

    /**
     * Creates a Narn Cruiser preset.
     * Heavy warship with strong armor and firepower.
     */
    private B5ShipDesign createNarnCruiser() {
        B5ShipDesign design = new B5ShipDesign(6, "Narn Cruiser", 30000);
        design.setFaction("Narn");
        design.setShipClass("Cruiser");
        design.setHullType(B5ShipDesign.HullType.REINFORCED);
        design.setJumpDrive(B5ShipDesign.JumpDrive.J4);
        design.setFusionDrive(B5ShipDesign.FusionDrive.M4);
        design.setPowerPlant(B5ShipDesign.PowerPlant.LARGE);
        design.setLifeSupport(B5ShipDesign.LifeSupport.STANDARD);
        design.setComputer(B5ShipDesign.ShipComputer.CLASS3);
        design.setCargoTons(1000);
        design.setFuelTons(3000);
        design.addWeapon(new B5ShipWeapon("Laser Battery", "Laser", 6, 15, 3, 1));
        design.addWeapon(new B5ShipWeapon("Missile Pods", "Missile", 3, 25, 6, 1));
        design.addWeapon(new B5ShipWeapon("Plasma Cannon", "Plasma", 1, 40, 8, 2));
        return design;
    }

    /**
     * Creates a Centauri Nightstar preset.
     * Advanced fighter with superior weapons technology.
     */
    private B5ShipDesign createCentauriNightstar() {
        B5ShipDesign design = new B5ShipDesign(7, "Centauri Nightstar", 3000);
        design.setFaction("Centauri");
        design.setShipClass("Fighter");
        design.setHullType(B5ShipDesign.HullType.ADVANCED);
        design.setJumpDrive(B5ShipDesign.JumpDrive.J3);
        design.setFusionDrive(B5ShipDesign.FusionDrive.M6);
        design.setPowerPlant(B5ShipDesign.PowerPlant.MEDIUM);
        design.setLifeSupport(B5ShipDesign.LifeSupport.STANDARD);
        design.setComputer(B5ShipDesign.ShipComputer.CLASS3);
        design.setCargoTons(50);
        design.setFuelTons(300);
        design.addWeapon(new B5ShipWeapon("Laser Battery", "Laser", 2, 10, 3, 1));
        design.addWeapon(new B5ShipWeapon("Missile Pods", "Missile", 1, 15, 4, 1));
        return design;
    }

    /**
     * Creates a Centauri Cruiser preset.
     * Elegant warship with advanced weapons.
     */
    private B5ShipDesign createCentauriCruiser() {
        B5ShipDesign design = new B5ShipDesign(8, "Centauri Cruiser", 25000);
        design.setFaction("Centauri");
        design.setShipClass("Cruiser");
        design.setHullType(B5ShipDesign.HullType.ADVANCED);
        design.setJumpDrive(B5ShipDesign.JumpDrive.J4);
        design.setFusionDrive(B5ShipDesign.FusionDrive.M4);
        design.setPowerPlant(B5ShipDesign.PowerPlant.LARGE);
        design.setLifeSupport(B5ShipDesign.LifeSupport.LUXURY);
        design.setComputer(B5ShipDesign.ShipComputer.CLASS4);
        design.setCargoTons(1500);
        design.setFuelTons(2500);
        design.addWeapon(new B5ShipWeapon("Laser Battery", "Laser", 6, 15, 4, 1));
        design.addWeapon(new B5ShipWeapon("Missile Pods", "Missile", 3, 25, 6, 1));
        design.addWeapon(new B5ShipWeapon("Plasma Cannon", "Plasma", 2, 40, 6, 2));
        return design;
    }

    /**
     * Creates a Minbari Battle Cruiser preset.
     * The most powerful ship in the galaxy with superior shields.
     */
    private B5ShipDesign createMinbariBattleCruiser() {
        B5ShipDesign design = new B5ShipDesign(9, "Minbari Battle Cruiser", 60000);
        design.setFaction("Minbari");
        design.setShipClass("Battle Cruiser");
        design.setHullType(B5ShipDesign.HullType.REINFORCED);
        design.setJumpDrive(B5ShipDesign.JumpDrive.J5);
        design.setFusionDrive(B5ShipDesign.FusionDrive.M5);
        design.setPowerPlant(B5ShipDesign.PowerPlant.LARGE);
        design.setLifeSupport(B5ShipDesign.LifeSupport.STANDARD);
        design.setComputer(B5ShipDesign.ShipComputer.CLASS4);
        design.setCargoTons(2000);
        design.setFuelTons(6000);
        design.addWeapon(new B5ShipWeapon("Laser Battery", "Laser", 8, 20, 4, 1));
        design.addWeapon(new B5ShipWeapon("Missile Pods", "Missile", 4, 30, 8, 1));
        design.addWeapon(new B5ShipWeapon("Particle Beam", "Particle Beam", 2, 40, 6, 2));
        design.addWeapon(new B5ShipWeapon("Torpedo Tubes", "Torpedo", 2, 50, 10, 2));
        return design;
    }

    /**
     * Creates a Minbari Cruiser preset.
     * Versatile warship with advanced sensor technology.
     */
    private B5ShipDesign createMinbariCruiser() {
        B5ShipDesign design = new B5ShipDesign(10, "Minbari Cruiser", 20000);
        design.setFaction("Minbari");
        design.setShipClass("Cruiser");
        design.setHullType(B5ShipDesign.HullType.REINFORCED);
        design.setJumpDrive(B5ShipDesign.JumpDrive.J4);
        design.setFusionDrive(B5ShipDesign.FusionDrive.M4);
        design.setPowerPlant(B5ShipDesign.PowerPlant.LARGE);
        design.setLifeSupport(B5ShipDesign.LifeSupport.STANDARD);
        design.setComputer(B5ShipDesign.ShipComputer.CLASS3);
        design.setCargoTons(1000);
        design.setFuelTons(2000);
        design.addWeapon(new B5ShipWeapon("Laser Battery", "Laser", 4, 15, 3, 1));
        design.addWeapon(new B5ShipWeapon("Missile Pods", "Missile", 2, 25, 6, 1));
        design.addWeapon(new B5ShipWeapon("Particle Beam", "Particle Beam", 1, 30, 4, 2));
        return design;
    }

    /**
     * Creates a Shadow ship preset.
     * Massive warship with devastating firepower.
     */
    private B5ShipDesign createShadowShip() {
        B5ShipDesign design = new B5ShipDesign(11, "Shadow Ship", 100000);
        design.setFaction("Shadow");
        design.setShipClass("Dreadnought");
        design.setHullType(B5ShipDesign.HullType.SHADOW_GRADE);
        design.setJumpDrive(B5ShipDesign.JumpDrive.J6);
        design.setFusionDrive(B5ShipDesign.FusionDrive.M6);
        design.setPowerPlant(B5ShipDesign.PowerPlant.SHADOW);
        design.setLifeSupport(B5ShipDesign.LifeSupport.STANDARD);
        design.setComputer(B5ShipDesign.ShipComputer.VORLON);
        design.setCargoTons(5000);
        design.setFuelTons(10000);
        design.addWeapon(new B5ShipWeapon("Plasma Cannon", "Plasma", 4, 50, 10, 3));
        design.addWeapon(new B5ShipWeapon("Torpedo Tubes", "Torpedo", 4, 60, 12, 3));
        design.addWeapon(new B5ShipWeapon("Particle Beam", "Particle Beam", 4, 50, 8, 2));
        return design;
    }

    /**
     * Creates a Vorlon ship preset.
     * Mysterious ship with advanced technology.
     */
    private B5ShipDesign createVorlonShip() {
        B5ShipDesign design = new B5ShipDesign(12, "Vorlon Ship", 80000);
        design.setFaction("Vorlon");
        design.setShipClass("Dreadnought");
        design.setHullType(B5ShipDesign.HullType.ADVANCED);
        design.setJumpDrive(B5ShipDesign.JumpDrive.J6);
        design.setFusionDrive(B5ShipDesign.FusionDrive.M6);
        design.setPowerPlant(B5ShipDesign.PowerPlant.VORLON);
        design.setLifeSupport(B5ShipDesign.LifeSupport.VORLON);
        design.setComputer(B5ShipDesign.ShipComputer.VORLON);
        design.setCargoTons(3000);
        design.setFuelTons(8000);
        design.addWeapon(new B5ShipWeapon("Energy Weapon", "Plasma", 4, 50, 10, 3));
        design.addWeapon(new B5ShipWeapon("Torpedo Tubes", "Torpedo", 4, 60, 12, 3));
        return design;
    }

    // ==================== Custom Design Creation ====================

    /**
     * Creates a new custom ship design.
     *
     * @param name the ship's name
     * @param totalTonnage the total tonnage (minimum 1000)
     * @return a new ship design with default settings
     */
    public B5ShipDesign createDesign(String name, int totalTonnage) {
        if (totalTonnage < MIN_TONNAGE) {
            throw new IllegalArgumentException("Minimum ship tonnage is " + MIN_TONNAGE + " tons");
        }
        int id = designIdCounter.getAndIncrement();
        B5ShipDesign design = new B5ShipDesign(id, name, totalTonnage);
        designs.put(id, design);
        log.info("Created new ship design: {}", design);
        return design;
    }

    /**
     * Creates a quick military design with standard military configuration.
     *
     * @param name the ship's name
     * @param tonnage the total tonnage
     * @param faction the owning faction
     * @return a configured military ship design
     */
    public B5ShipDesign createMilitaryDesign(String name, int tonnage, String faction) {
        B5ShipDesign design = createDesign(name, tonnage);
        design.setFaction(faction);
        design.setShipClass("Warship");
        design.setHullType(B5ShipDesign.HullType.REINFORCED);
        design.setJumpDrive(B5ShipDesign.JumpDrive.J4);
        design.setFusionDrive(B5ShipDesign.FusionDrive.M4);
        design.setPowerPlant(B5ShipDesign.PowerPlant.LARGE);
        design.setLifeSupport(B5ShipDesign.LifeSupport.STANDARD);
        design.setComputer(B5ShipDesign.ShipComputer.CLASS3);
        design.setFuelTons(tonnage / 10);

        // Allocate weapons based on tonnage
        int weaponTons = tonnage / 5;
        if (weaponTons >= 100) {
            design.addWeapon(new B5ShipWeapon("Laser Battery", "Laser", 4, 15, 3, 1));
        }
        if (weaponTons >= 200) {
            design.addWeapon(new B5ShipWeapon("Missile Pods", "Missile", 2, 25, 6, 1));
        }
        if (weaponTons >= 400) {
            design.addWeapon(new B5ShipWeapon("Particle Beam", "Particle Beam", 2, 40, 6, 2));
        }
        if (weaponTons >= 800) {
            design.addWeapon(new B5ShipWeapon("Torpedo Tubes", "Torpedo", 2, 50, 10, 2));
        }

        // Remaining tons to cargo
        design.setCargoTons(design.getRemainingTons());
        return design;
    }

    /**
     * Creates a quick merchant design with cargo-focused configuration.
     *
     * @param name the ship's name
     * @param tonnage the total tonnage
     * @param faction the owning faction
     * @return a configured merchant ship design
     */
    public B5ShipDesign createMerchantDesign(String name, int tonnage, String faction) {
        B5ShipDesign design = createDesign(name, tonnage);
        design.setFaction(faction);
        design.setShipClass("Freighter");
        design.setHullType(B5ShipDesign.HullType.STANDARD);
        design.setJumpDrive(B5ShipDesign.JumpDrive.J3);
        design.setFusionDrive(B5ShipDesign.FusionDrive.M2);
        design.setPowerPlant(B5ShipDesign.PowerPlant.MEDIUM);
        design.setLifeSupport(B5ShipDesign.LifeSupport.STANDARD);
        design.setComputer(B5ShipDesign.ShipComputer.CLASS2);
        design.setFuelTons(tonnage / 10);
        design.setPassengerCount(tonnage / 400);
        design.setPassengerClass(B5ShipDesign.PassengerClass.THIRD);

        // Minimal point defense
        design.addWeapon(new B5ShipWeapon("Point Defense Laser", "Laser", 2, 10, 1, 1));

        // Remaining tons to cargo
        design.setCargoTons(design.getRemainingTons());
        return design;
    }

    // ==================== Design Management ====================

    /**
     * Gets a ship design by ID.
     *
     * @param id the design ID
     * @return the ship design, or null if not found
     */
    public B5ShipDesign getDesign(int id) {
        return designs.get(id);
    }

    /**
     * Gets all ship designs.
     *
     * @return a list of all ship designs
     */
    public List<B5ShipDesign> getAllDesigns() {
        return new ArrayList<>(designs.values());
    }

    /**
     * Gets all ship designs for a specific faction.
     *
     * @param faction the faction name
     * @return a list of ship designs for that faction
     */
    public List<B5ShipDesign> getDesignsByFaction(String faction) {
        List<B5ShipDesign> result = new ArrayList<>();
        for (B5ShipDesign design : designs.values()) {
            if (faction.equals(design.getFaction())) {
                result.add(design);
            }
        }
        return result;
    }

    /**
     * Validates a ship design.
     *
     * @param design the design to validate
     * @return true if the design is valid
     */
    public boolean validateDesign(B5ShipDesign design) {
        try {
            design.validate();
            return true;
        } catch (IllegalStateException e) {
            log.warn("Invalid ship design: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Calculates design statistics for a ship.
     *
     * @param design the ship design
     * @return a map of statistics
     */
    public Map<String, Object> calculateDesignStats(B5ShipDesign design) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTonnage", design.getTotalTonnage());
        stats.put("fixedSystemTons", design.getFixedSystemTons());
        stats.put("weaponTons", design.getWeaponTons());
        stats.put("passengerTons", design.getPassengerTons());
        stats.put("cargoTons", design.getCargoTons());
        stats.put("fuelTons", design.getFuelTons());
        stats.put("unallocatedTons", design.getRemainingTons());
        stats.put("hullPoints", design.getHullPoints());
        stats.put("estimatedCrew", design.getEstimatedCrew());
        stats.put("constructionCost", design.getConstructionCost());
        stats.put("annualMaintenanceCost", design.getAnnualMaintenanceCost());
        stats.put("jumpRating", design.getMaxJumpParsecs());
        stats.put("maneuverRating", design.getManeuverRating());
        stats.put("totalDamageDice", design.getTotalDamageDice());
        stats.put("averageDamage", design.getTotalAverageDamage());
        stats.put("gunneryBonus", design.getGunneryBonus());
        stats.put("passengerCount", design.getPassengerCount());
        stats.put("isValid", design.isValid());
        stats.put("weaponCount", design.getWeapons().size());
        return stats;
    }

    // ==================== Small Craft ====================

    /**
     * Gets all small craft types.
     *
     * @return an array of all small craft types
     */
    public com.yisy.modules.doramcu.babylon5.model.B5SmallCraft[] getAllSmallCraft() {
        return com.yisy.modules.doramcu.babylon5.model.B5SmallCraft.values();
    }

    /**
     * Gets small craft by faction.
     *
     * @param faction the faction name
     * @return an array of small craft for that faction
     */
    public com.yisy.modules.doramcu.babylon5.model.B5SmallCraft[] getSmallCraftByFaction(String faction) {
        List<com.yisy.modules.doramcu.babylon5.model.B5SmallCraft> result = new ArrayList<>();
        for (com.yisy.modules.doramcu.babylon5.model.B5SmallCraft craft :
             com.yisy.modules.doramcu.babylon5.model.B5SmallCraft.values()) {
            if (faction.equals(craft.getFaction())) {
                result.add(craft);
            }
        }
        return result.toArray(new com.yisy.modules.doramcu.babylon5.model.B5SmallCraft[0]);
    }

    /**
     * Gets small craft by role.
     *
     * @param role the role (e.g., "Fighter", "Cargo", "Shuttle")
     * @return an array of small craft with that role
     */
    public com.yisy.modules.doramcu.babylon5.model.B5SmallCraft[] getSmallCraftByRole(String role) {
        List<com.yisy.modules.doramcu.babylon5.model.B5SmallCraft> result = new ArrayList<>();
        for (com.yisy.modules.doramcu.babylon5.model.B5SmallCraft craft :
             com.yisy.modules.doramcu.babylon5.model.B5SmallCraft.values()) {
            if (role.equals(craft.getRole())) {
                result.add(craft);
            }
        }
        return result.toArray(new com.yisy.modules.doramcu.babylon5.model.B5SmallCraft[0]);
    }

    /**
     * Gets combat-capable small craft (armament >= 1).
     *
     * @return an array of combat-capable small craft
     */
    public com.yisy.modules.doramcu.babylon5.model.B5SmallCraft[] getCombatSmallCraft() {
        List<com.yisy.modules.doramcu.babylon5.model.B5SmallCraft> result = new ArrayList<>();
        for (com.yisy.modules.doramcu.babylon5.model.B5SmallCraft craft :
             com.yisy.modules.doramcu.babylon5.model.B5SmallCraft.values()) {
            if (craft.getArmament() >= 1) {
                result.add(craft);
            }
        }
        return result.toArray(new com.yisy.modules.doramcu.babylon5.model.B5SmallCraft[0]);
    }
}
