package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.model.*;
import com.yisy.modules.doramcu.util.DiceUtil;

import java.util.*;

/**
 * Processes damage allocation in SFB combat.
 * <p>
 * Handles the 5-step damage process: shield penetration, Damage Allocation Chart (DAC) rolls,
 * system damage, excess damage, and crippled/self-destruction checks.
 * </p>
 */
public class DamageAllocationService {

    /**
     * Damage categories for DAC rolls.
     * Based on the SFB Damage Allocation Chart.
     */
    public enum DamageCategory {
        WARP_ENGINES("Warp engines"),
        IMPULSE_ENGINES("Impulse engines"),
        SHIELDS("Shields"),
        WEAPONS("Weapons"),
        CONTROL_SPACES("Control spaces"),
        DAMCON("DamCon"),
        HULL("Hull"),
        SPECIAL_SYSTEMS("Special systems"),
        CREW("Crew"),
        SELF_DESTRUCT("Self-destruct");

        private final String description;

        DamageCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Result of a damage allocation roll.
     */
    public static class DamageResult {
        private final DamageCategory category;
        private final int damageAmount;
        private final boolean systemDisabled;
        private final boolean excessDamage;
        private final String message;

        public DamageResult(DamageCategory category, int damageAmount, boolean systemDisabled,
                            boolean excessDamage, String message) {
            this.category = category;
            this.damageAmount = damageAmount;
            this.systemDisabled = systemDisabled;
            this.excessDamage = excessDamage;
            this.message = message;
        }

        public DamageCategory getCategory() { return category; }
        public int getDamageAmount() { return damageAmount; }
        public boolean isSystemDisabled() { return systemDisabled; }
        public boolean isExcessDamage() { return excessDamage; }
        public String getMessage() { return message; }
    }

    /**
     * DAC (Damage Allocation Chart) results mapped by roll value (2-12 on 2d6).
     * Each result maps to a damage category.
     */
    private static final Map<Integer, DamageCategory> DAC_CHART = new HashMap<>();

    static {
        // Standard DAC chart (simplified)
        DAC_CHART.put(2, DamageCategory.WARP_ENGINES);
        DAC_CHART.put(3, DamageCategory.WARP_ENGINES);
        DAC_CHART.put(4, DamageCategory.IMPULSE_ENGINES);
        DAC_CHART.put(5, DamageCategory.SHIELDS);
        DAC_CHART.put(6, DamageCategory.WEAPONS);
        DAC_CHART.put(7, DamageCategory.WEAPONS);
        DAC_CHART.put(8, DamageCategory.CONTROL_SPACES);
        DAC_CHART.put(9, DamageCategory.DAMCON);
        DAC_CHART.put(10, DamageCategory.HULL);
        DAC_CHART.put(11, DamageCategory.SPECIAL_SYSTEMS);
        DAC_CHART.put(12, DamageCategory.CREW);
    }

    /**
     * Process damage that penetrates shields to internal systems.
     * @param damage the amount of damage penetrating shields
     * @param ship the damaged ship
     * @return list of damage results
     */
    public List<DamageResult> processInternalDamage(int damage, SfbShip ship) {
        List<DamageResult> results = new ArrayList<>();

        if (damage <= 0 || ship.isDestroyed()) {
            return results;
        }

        SfbSSD ssd = ship.getSsd();
        if (ssd == null) {
            return results;
        }

        // Roll on DAC for each point of damage (or use combined roll for efficiency)
        int roll = DiceUtil.roll2D6();
        DamageCategory category = DAC_CHART.getOrDefault(roll, DamageCategory.HULL);

        // Apply damage based on category
        boolean systemDisabled = false;
        boolean excess = false;

        switch (category) {
            case WARP_ENGINES:
                systemDisabled = ssd.markOutWarpBox();
                break;
            case IMPULSE_ENGINES:
                systemDisabled = ssd.markOutImpulseBox();
                break;
            case SHIELDS:
                systemDisabled = ssd.markOutShieldBox();
                break;
            case WEAPONS:
                systemDisabled = ssd.markOutWeaponBox();
                break;
            case CONTROL_SPACES:
                systemDisabled = ssd.markOutControlBox();
                break;
            case DAMCON:
                systemDisabled = ssd.markOutDamConBox();
                break;
            case HULL:
                ssd.addExcessDamage(damage);
                excess = ssd.isDestroyed();
                break;
            case SPECIAL_SYSTEMS:
                systemDisabled = ssd.markOutSpecialSystemBox();
                break;
            case CREW:
                // Crew damage - could integrate with Captain's Log character injuries
                break;
            case SELF_DESTRUCT:
                // Immediate self-destruct check
                if (DiceUtil.rollD6() <= 1) {
                    ship.setDestroyed(true);
                    results.add(new DamageResult(category, damage, false, false,
                            "Self-destruct triggered! " + ship.getName() + " is destroyed"));
                    return results;
                }
                break;
        }

        results.add(new DamageResult(category, damage, systemDisabled, excess,
                "DAC roll " + roll + " hits " + category.getDescription() +
                (systemDisabled ? " (system disabled)" : "") +
                (excess ? " (excess damage)" : "")));

        // Check for crippled condition
        if (ssd.isCrippled() && !ship.isCrippled()) {
            ship.setCrippled(true);
            results.add(new DamageResult(null, 0, false, false,
                    ship.getName() + " is crippled!"));
        }

        // Check for destroyed
        if (ssd.isDestroyed() && !ship.isDestroyed()) {
            ship.setDestroyed(true);
            results.add(new DamageResult(null, 0, false, false,
                    ship.getName() + " is destroyed!"));
        }

        return results;
    }

    /**
     * Process a complete damage hit from weapons fire.
     * This is the full 5-step damage process.
     *
     * Step 1: Determine shield hit direction
     * Step 2: Apply damage to shields
     * Step 3: Calculate penetrating damage
     * Step 4: Roll on DAC for penetrating damage
     * Step 5: Check for crippled/destroyed
     */
    public List<DamageResult> processDamageHit(SfbShip attacker, SfbShip target,
                                                int damage, int direction) {
        List<DamageResult> results = new ArrayList<>();

        if (target.isDestroyed()) {
            results.add(new DamageResult(null, 0, false, false,
                    target.getName() + " is already destroyed"));
            return results;
        }

        // Step 1-2: Apply damage to shields
        SfbShield shield = target.getShields()[direction % 6];
        int shieldAbsorbed = 0;
        int penetrating = damage;

        if (shield != null && !shield.isDown()) {
            penetrating = shield.absorbDamage(damage);
            shieldAbsorbed = damage - penetrating;
        }

        results.add(new DamageResult(DamageCategory.SHIELDS, shieldAbsorbed, false, false,
                shieldAbsorbed + " damage absorbed by " + direction + " shield, " +
                penetrating + " penetrating"));

        // Step 3-5: Process penetrating damage
        if (penetrating > 0) {
            List<DamageResult> internalResults = processInternalDamage(penetrating, target);
            results.addAll(internalResults);
        }

        return results;
    }

    /**
     * Repair damage using DamCon (Damage Control).
     * @param ship the ship being repaired
     * @param systemType the type of system to repair
     * @return true if repair was successful
     */
    public boolean repairSystem(SfbShip ship, String systemType) {
        SfbSSD ssd = ship.getSsd();
        if (ssd == null || ssd.getDamConRating() <= 0) {
            return false;
        }

        // Roll for DamCon repair
        int roll = DiceUtil.rollD6();
        int damConRating = ssd.getDamConRating();

        if (roll <= damConRating) {
            // Repair successful
            switch (systemType.toLowerCase()) {
                case "warp":
                    ssd.repairWarpBox();
                    break;
                case "impulse":
                    ssd.repairImpulseBox();
                    break;
                case "shield":
                    ssd.repairShieldBox();
                    break;
                case "weapon":
                    ssd.repairWeaponBox();
                    break;
                default:
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Check if a ship is crippled based on SSD state.
     */
    public boolean isCrippled(SfbShip ship) {
        SfbSSD ssd = ship.getSsd();
        return ssd != null && ssd.isCrippled();
    }

    /**
     * Check if a ship is destroyed based on SSD state.
     */
    public boolean isDestroyed(SfbShip ship) {
        SfbSSD ssd = ship.getSsd();
        return ssd != null && ssd.isDestroyed();
    }

    /**
     * Get the remaining hull integrity of a ship (0-100%).
     */
    public double getHullIntegrity(SfbShip ship) {
        SfbSSD ssd = ship.getSsd();
        if (ssd == null) return 0.0;

        int totalBoxes = ssd.getTotalBoxes();
        int remainingBoxes = ssd.getRemainingBoxes();

        if (totalBoxes == 0) return 0.0;
        return (double) remainingBoxes / totalBoxes * 100.0;
    }

    /**
     * Get the remaining shield integrity for a specific direction.
     */
    public double getShieldIntegrity(SfbShip ship, int direction) {
        SfbShield shield = ship.getShields()[direction % 6];
        if (shield == null) return 0.0;

        int total = shield.getTotalBoxes();
        if (total == 0) return 0.0;

        return (double) shield.getTotalProtection() / total * 100.0;
    }
}
