package com.yisy.modules.doramcu.sfb.model;

import java.util.*;

/**
 * Represents the Ship System Display (SSD) for an SFB ship.
 * <p>
 * The SSD tracks all system boxes on the ship, including warp engines,
 * impulse engines, shields, weapons, control spaces, DamCon, and hull.
 * Boxes can be marked out due to damage, affecting the ship's capabilities.
 * </p>
 */
public class SfbSSD {

    public enum SystemCategory {
        WARP_ENGINES, IMPULSE_ENGINES, AUXILIARY_ENGINES,
        SHIELDS, WEAPONS, CONTROL_SPACES,
        DAMCON, HULL, BATTERIES, CAPACITORS,
        CLOAKING, TRACTOR_BEAM, TRANSPORTER,
        SENSORS, SCANNERS, ECM, ECCM,
        PLASMA_TORPEDOES, DRONES,
        OTHER
    }

    /**
     * Represents a single box on the SSD.
     */
    public static class SsdBox {
        private final String id;
        private final SystemCategory category;
        private boolean operational;

        public SsdBox(String id, SystemCategory category) {
            this.id = id;
            this.category = category;
            this.operational = true;
        }

        public String getId() { return id; }
        public SystemCategory getCategory() { return category; }
        public boolean isOperational() { return operational; }

        public void markOut() { this.operational = false; }
        public void repair() { this.operational = true; }

        @Override
        public String toString() {
            return "SsdBox{id=" + id + ", category=" + category + ", operational=" + operational + "}";
        }
    }

    private final Map<String, SsdBox> systems;
    private int damConRating;
    private int hullBoxes;
    private int excessDamage;

    public SfbSSD() {
        this.systems = new LinkedHashMap<>();
        this.damConRating = 0;
        this.hullBoxes = 0;
        this.excessDamage = 0;
    }

    public Map<String, SsdBox> getSystems() { return Collections.unmodifiableMap(systems); }
    public int getDamConRating() { return damConRating; }
    public int getHullBoxes() { return hullBoxes; }
    public int getExcessDamage() { return excessDamage; }

    public void setExcessDamage(int excessDamage) { 
        this.excessDamage = Math.max(0, excessDamage); 
    }

    public void setDamConRating(int damConRating) { this.damConRating = damConRating; }
    public void setHullBoxes(int hullBoxes) { this.hullBoxes = hullBoxes; }

    /**
     * Add a system box to the SSD.
     */
    public void addSystem(String id, SystemCategory category) {
        systems.put(id, new SsdBox(id, category));
    }

    /**
     * Mark a system box as non-operational.
     */
    public void markOutBox(String id) {
        SsdBox box = systems.get(id);
        if (box != null) {
            box.markOut();
        }
    }

    /**
     * Repair a system box.
     */
    public void repairBox(String id) {
        SsdBox box = systems.get(id);
        if (box != null) {
            box.repair();
        }
    }

    /**
     * Get the number of operational warp engine boxes.
     */
    public int getAvailableWarpPower() {
        int count = 0;
        for (SsdBox box : systems.values()) {
            if (box.getCategory() == SystemCategory.WARP_ENGINES && box.isOperational()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get the number of operational impulse engine boxes.
     */
    public int getAvailableImpulsePower() {
        int count = 0;
        for (SsdBox box : systems.values()) {
            if (box.getCategory() == SystemCategory.IMPULSE_ENGINES && box.isOperational()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get the number of operational auxiliary engine boxes.
     */
    public int getAvailableAuxiliaryPower() {
        int count = 0;
        for (SsdBox box : systems.values()) {
            if (box.getCategory() == SystemCategory.AUXILIARY_ENGINES && box.isOperational()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get the number of operational shield boxes in a given direction.
     */
    public int getShieldBoxes(int direction) {
        int count = 0;
        for (SsdBox box : systems.values()) {
            if (box.getCategory() == SystemCategory.SHIELDS && box.isOperational()) {
                // Shield boxes are named like "SHIELD_0", "SHIELD_1", etc.
                String id = box.getId();
                if (id.startsWith("SHIELD_") && Integer.parseInt(id.substring(7)) == direction) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Check if the ship is crippled (all control spaces marked out).
     */
    public boolean isCrippled() {
        for (SsdBox box : systems.values()) {
            if (box.getCategory() == SystemCategory.CONTROL_SPACES && box.isOperational()) {
                return false;
            }
        }
        return !systems.isEmpty(); // Crippled if there are any control spaces and all are out
    }

    /**
     * Get all operational weapon systems.
     */
    public List<SsdBox> getOperationalWeapons() {
        List<SsdBox> weapons = new ArrayList<>();
        for (SsdBox box : systems.values()) {
            if (box.getCategory() == SystemCategory.WEAPONS && box.isOperational()) {
                weapons.add(box);
            }
        }
        return weapons;
    }

    /**
     * Check if a specific system category is operational.
     */
    public boolean isSystemOperational(SystemCategory category) {
        for (SsdBox box : systems.values()) {
            if (box.getCategory() == category && box.isOperational()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add excess damage. When excess damage reaches hull boxes, the ship is destroyed.
     */
    public void addExcessDamage(int damage) {
        this.excessDamage += damage;
    }

    /**
     * Check if the ship is destroyed (excess damage >= hull boxes).
     */
    public boolean isDestroyed() {
        return excessDamage >= hullBoxes && hullBoxes > 0;
    }

    /**
     * Get total number of boxes on the SSD.
     */
    public int getTotalBoxes() {
        return systems.size() + hullBoxes;
    }

    /**
     * Get remaining operational boxes.
     */
    public int getRemainingBoxes() {
        int count = 0;
        for (SsdBox box : systems.values()) {
            if (box.isOperational()) {
                count++;
            }
        }
        // Hull boxes remaining
        int hullRemaining = Math.max(0, hullBoxes - excessDamage);
        return count + hullRemaining;
    }

    /**
     * Mark out a warp engine box. Returns true if the last warp box was marked out.
     */
    public boolean markOutWarpBox() {
        return markOutCategory(SystemCategory.WARP_ENGINES);
    }

    /**
     * Mark out an impulse engine box. Returns true if the last impulse box was marked out.
     */
    public boolean markOutImpulseBox() {
        return markOutCategory(SystemCategory.IMPULSE_ENGINES);
    }

    /**
     * Mark out a shield box. Returns true if the last shield box was marked out.
     */
    public boolean markOutShieldBox() {
        return markOutCategory(SystemCategory.SHIELDS);
    }

    /**
     * Mark out a weapon box. Returns true if the last weapon box was marked out.
     */
    public boolean markOutWeaponBox() {
        return markOutCategory(SystemCategory.WEAPONS);
    }

    /**
     * Mark out a control space box. Returns true if the last control box was marked out.
     */
    public boolean markOutControlBox() {
        return markOutCategory(SystemCategory.CONTROL_SPACES);
    }

    /**
     * Mark out a DamCon box. Returns true if DamCon is now disabled.
     */
    public boolean markOutDamConBox() {
        if (damConRating > 0) {
            damConRating--;
            return damConRating <= 0;
        }
        return false;
    }

    /**
     * Mark out a special system box. Returns true if the last special system box was marked out.
     */
    public boolean markOutSpecialSystemBox() {
        return markOutCategory(SystemCategory.CLOAKING) ||
               markOutCategory(SystemCategory.TRACTOR_BEAM) ||
               markOutCategory(SystemCategory.TRANSPORTER) ||
               markOutCategory(SystemCategory.ECM) ||
               markOutCategory(SystemCategory.ECCM);
    }

    /**
     * Repair a warp engine box.
     */
    public void repairWarpBox() {
        repairCategory(SystemCategory.WARP_ENGINES);
    }

    /**
     * Repair an impulse engine box.
     */
    public void repairImpulseBox() {
        repairCategory(SystemCategory.IMPULSE_ENGINES);
    }

    /**
     * Repair a shield box.
     */
    public void repairShieldBox() {
        repairCategory(SystemCategory.SHIELDS);
    }

    /**
     * Repair a weapon box.
     */
    public void repairWeaponBox() {
        repairCategory(SystemCategory.WEAPONS);
    }

    /**
     * Helper to mark out one box of a given category.
     * Returns true if this was the last operational box of that category.
     */
    private boolean markOutCategory(SystemCategory category) {
        SsdBox firstOperational = null;
        for (SsdBox box : systems.values()) {
            if (box.getCategory() == category && box.isOperational()) {
                firstOperational = box;
                break;
            }
        }
        if (firstOperational != null) {
            firstOperational.markOut();
            // Check if any remain
            for (SsdBox box : systems.values()) {
                if (box.getCategory() == category && box.isOperational()) {
                    return false;
                }
            }
            return true; // Last one marked out
        }
        return false;
    }

    /**
     * Helper to repair one non-operational box of a given category.
     */
    private void repairCategory(SystemCategory category) {
        for (SsdBox box : systems.values()) {
            if (box.getCategory() == category && !box.isOperational()) {
                box.repair();
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "SfbSSD{systems=" + systems.size() + ", damCon=" + damConRating +
                ", hull=" + hullBoxes + ", excessDamage=" + excessDamage + "}";
    }
}
