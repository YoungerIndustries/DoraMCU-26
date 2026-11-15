package com.yisy.modules.doramcu.mechwarrior.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a component of a BattleMech (head, torso, arms, legs).
 * <p>
 * Each component has armor and internal structure. When armor is breached,
 * damage is applied to internal structure. When structure reaches zero,
 * the component is destroyed.
 * </p>
 */
public class MechComponent {

    public enum ComponentLocation {
        HEAD,
        CENTER_TORSO,
        LEFT_TORSO,
        RIGHT_TORSO,
        LEFT_ARM,
        RIGHT_ARM,
        LEFT_LEG,
        RIGHT_LEG
    }

    private String id;
    private ComponentLocation location;
    private int armorCurrent;       // current armor points
    private int armorMax;           // maximum armor points
    private int structureCurrent;   // current structure points
    private int structureMax;       // maximum structure points
    private boolean destroyed;
    private List<MechWeapon> mountedWeapons;  // weapons mounted in this component
    private List<String> equipmentSlots;      // other equipment (heat sinks, ammo, etc.)

    public MechComponent() {
    }

    public MechComponent(ComponentLocation location, int armorPoints, int structurePoints) {
        this.id = UUID.randomUUID().toString();
        this.location = location;
        this.armorMax = armorPoints;
        this.armorCurrent = armorPoints;
        this.structureMax = structurePoints;
        this.structureCurrent = structurePoints;
        this.destroyed = false;
        this.mountedWeapons = new ArrayList<>();
        this.equipmentSlots = new ArrayList<>();
    }

    /**
     * Apply damage to this component.
     * Damage first reduces armor, then structure.
     *
     * @param damage amount of damage to apply
     * @return remaining damage that overflowed to adjacent components
     */
    public int applyDamage(int damage) {
        if (destroyed) {
            return damage;
        }

        int remaining = damage;

        // Apply to armor first
        if (armorCurrent > 0) {
            int armorDamage = Math.min(armorCurrent, remaining);
            armorCurrent -= armorDamage;
            remaining -= armorDamage;
        }

        // Apply remaining to structure
        if (remaining > 0 && structureCurrent > 0) {
            int structureDamage = Math.min(structureCurrent, remaining);
            structureCurrent -= structureDamage;
            remaining -= structureDamage;

            // Check if component is destroyed
            if (structureCurrent <= 0) {
                destroyed = true;
            }
        }

        return remaining;
    }

    /**
     * Check if this component can mount a weapon.
     */
    public boolean canMountWeapon(MechWeapon weapon) {
        if (destroyed) return false;
        // Simplified: check if there's space
        int usedSlots = mountedWeapons.stream().mapToInt(MechWeapon::getCriticalSlots).sum();
        int equipCount = equipmentSlots.size();
        return (usedSlots + equipCount + weapon.getCriticalSlots()) <= structureMax / 2;
    }

    /**
     * Mount a weapon to this component.
     */
    public boolean mountWeapon(MechWeapon weapon) {
        if (!canMountWeapon(weapon)) return false;
        mountedWeapons.add(weapon);
        return true;
    }

    /**
     * Remove a weapon from this component.
     */
    public boolean removeWeapon(String weaponId) {
        return mountedWeapons.removeIf(w -> w.getId().equals(weaponId));
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ComponentLocation getLocation() {
        return location;
    }

    public void setLocation(ComponentLocation location) {
        this.location = location;
    }

    public int getArmorCurrent() {
        return armorCurrent;
    }

    public void setArmorCurrent(int armorCurrent) {
        this.armorCurrent = armorCurrent;
    }

    public int getArmorMax() {
        return armorMax;
    }

    public void setArmorMax(int armorMax) {
        this.armorMax = armorMax;
    }

    public int getStructureCurrent() {
        return structureCurrent;
    }

    public void setStructureCurrent(int structureCurrent) {
        this.structureCurrent = structureCurrent;
    }

    public int getStructureMax() {
        return structureMax;
    }

    public void setStructureMax(int structureMax) {
        this.structureMax = structureMax;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public List<MechWeapon> getMountedWeapons() {
        return mountedWeapons;
    }

    public void setMountedWeapons(List<MechWeapon> mountedWeapons) {
        this.mountedWeapons = mountedWeapons;
    }

    public List<String> getEquipmentSlots() {
        return equipmentSlots;
    }

    public void setEquipmentSlots(List<String> equipmentSlots) {
        this.equipmentSlots = equipmentSlots;
    }

    /**
     * Get the integrity percentage (0-100).
     */
    public double getIntegrityPercentage() {
        if (destroyed) return 0.0;
        double armorPct = (double) armorCurrent / armorMax * 50.0;
        double structurePct = (double) structureCurrent / structureMax * 50.0;
        return armorPct + structurePct;
    }
}
