package com.yisy.modules.doramcu.mechwarrior.engine;

import com.yisy.modules.doramcu.mechwarrior.model.MechComponent;
import com.yisy.modules.doramcu.mechwarrior.model.MechPilot;
import com.yisy.modules.doramcu.mechwarrior.model.MechChassis;
import com.yisy.modules.doramcu.mechwarrior.model.MechWeapon;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a BattleMech instance in the game with all its components and state.
 * <p>
 * Each mech has 8 components (head, center torso, left/right torso, left/right arm, left/right leg),
 * heat management system, stability tracking, and weapon systems.
 * </p>
 */
public class MechWarriorMech {

    private final MechPilot pilot;
    private final MechChassis chassis;
    private final Map<MechComponent.ComponentLocation, MechComponent> components;
    private final Map<String, MechWeapon> weapons;

    // Position and orientation
    private double posX, posY, posZ;
    private double torsoTwist;  // independent torso rotation angle
    private double facingAngle; // direction the legs are facing

    // Heat system
    private double heatLevel;
    private boolean heatOverride;
    private boolean shutdown;

    // Stability
    private double stabilityDamage;
    private boolean knockedDown;

    // Movement
    private boolean moving;
    private double currentSpeed;

    // State
    private boolean destroyed;
    private boolean ejected;
    private long lastFireTime;

    public MechWarriorMech(MechPilot pilot, MechChassis chassis) {
        this.pilot = pilot;
        this.chassis = chassis;
        this.components = new EnumMap<>(MechComponent.ComponentLocation.class);
        this.weapons = new ConcurrentHashMap<>();
        this.heatLevel = 0;
        this.heatOverride = false;
        this.shutdown = false;
        this.stabilityDamage = 0;
        this.knockedDown = false;
        this.destroyed = false;
        this.ejected = false;
        this.torsoTwist = 0;
        this.facingAngle = 0;
        this.currentSpeed = 0;

        // Initialize components based on chassis tonnage
        initializeComponents(chassis);
    }

    /**
     * Initialize the 8 components with armor and structure based on chassis.
     */
    private void initializeComponents(MechChassis chassis) {
        int tonnage = chassis.getTonnage();

        // Head: 5% of total armor/structure
        components.put(MechComponent.ComponentLocation.HEAD,
                new MechComponent(MechComponent.ComponentLocation.HEAD,
                        tonnage / 20, tonnage / 10));

        // Center Torso: 30% of total armor/structure
        components.put(MechComponent.ComponentLocation.CENTER_TORSO,
                new MechComponent(MechComponent.ComponentLocation.CENTER_TORSO,
                        tonnage / 3, tonnage / 3));

        // Left Torso: 20% of total armor/structure
        components.put(MechComponent.ComponentLocation.LEFT_TORSO,
                new MechComponent(MechComponent.ComponentLocation.LEFT_TORSO,
                        tonnage / 5, tonnage / 5));

        // Right Torso: 20% of total armor/structure
        components.put(MechComponent.ComponentLocation.RIGHT_TORSO,
                new MechComponent(MechComponent.ComponentLocation.RIGHT_TORSO,
                        tonnage / 5, tonnage / 5));

        // Left Arm: 10% of total armor/structure
        components.put(MechComponent.ComponentLocation.LEFT_ARM,
                new MechComponent(MechComponent.ComponentLocation.LEFT_ARM,
                        tonnage / 10, tonnage / 10));

        // Right Arm: 10% of total armor/structure
        components.put(MechComponent.ComponentLocation.RIGHT_ARM,
                new MechComponent(MechComponent.ComponentLocation.RIGHT_ARM,
                        tonnage / 10, tonnage / 10));

        // Left Leg: 7.5% of total armor/structure
        components.put(MechComponent.ComponentLocation.LEFT_LEG,
                new MechComponent(MechComponent.ComponentLocation.LEFT_LEG,
                        tonnage / 13, tonnage / 13));

        // Right Leg: 7.5% of total armor/structure
        components.put(MechComponent.ComponentLocation.RIGHT_LEG,
                new MechComponent(MechComponent.ComponentLocation.RIGHT_LEG,
                        tonnage / 13, tonnage / 13));
    }

    /**
     * Mount a weapon to a specific component location.
     */
    public boolean mountWeapon(MechWeapon weapon, MechComponent.ComponentLocation location) {
        MechComponent component = components.get(location);
        if (component == null || !component.canMountWeapon(weapon)) {
            return false;
        }
        component.mountWeapon(weapon);
        weapons.put(weapon.getId(), weapon);
        return true;
    }

    /**
     * Get a weapon by ID.
     */
    public MechWeapon getWeapon(String weaponId) {
        return weapons.get(weaponId);
    }

    /**
     * Get all weapons on this mech.
     */
    public Collection<MechWeapon> getAllWeapons() {
        return weapons.values();
    }

    /**
     * Get a component by location.
     */
    public MechComponent getComponent(MechComponent.ComponentLocation location) {
        return components.get(location);
    }

    /**
     * Get all components.
     */
    public Map<MechComponent.ComponentLocation, MechComponent> getAllComponents() {
        return components;
    }

    /**
     * Check if the mech is operational.
     */
    public boolean isOperational() {
        if (destroyed || ejected) return false;

        // Check if both legs are destroyed
        boolean leftLegDestroyed = components.get(MechComponent.ComponentLocation.LEFT_LEG).isDestroyed();
        boolean rightLegDestroyed = components.get(MechComponent.ComponentLocation.RIGHT_LEG).isDestroyed();
        if (leftLegDestroyed && rightLegDestroyed) return false;

        // Check if center torso is destroyed
        boolean centerTorsoDestroyed = components.get(MechComponent.ComponentLocation.CENTER_TORSO).isDestroyed();
        if (centerTorsoDestroyed) return false;

        // Check if head is destroyed (pilot killed)
        boolean headDestroyed = components.get(MechComponent.ComponentLocation.HEAD).isDestroyed();
        if (headDestroyed) return false;

        return true;
    }

    /**
     * Calculate distance to another mech.
     */
    public double getDistanceTo(MechWarriorMech other) {
        double dx = this.posX - other.posX;
        double dy = this.posY - other.posY;
        double dz = this.posZ - other.posZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Add stability damage.
     */
    public void addStabilityDamage(double damage) {
        this.stabilityDamage += damage;
    }

    // Getters and Setters

    public MechPilot getPilot() {
        return pilot;
    }

    public MechChassis getChassis() {
        return chassis;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public void setPosZ(double posZ) {
        this.posZ = posZ;
    }

    public double getTorsoTwist() {
        return torsoTwist;
    }

    public void setTorsoTwist(double torsoTwist) {
        this.torsoTwist = torsoTwist;
    }

    public double getFacingAngle() {
        return facingAngle;
    }

    public void setFacingAngle(double facingAngle) {
        this.facingAngle = facingAngle;
    }

    public double getHeatLevel() {
        return heatLevel;
    }

    public void setHeatLevel(double heatLevel) {
        this.heatLevel = heatLevel;
    }

    public boolean isHeatOverride() {
        return heatOverride;
    }

    public void setHeatOverride(boolean heatOverride) {
        this.heatOverride = heatOverride;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    public double getStabilityDamage() {
        return stabilityDamage;
    }

    public void setStabilityDamage(double stabilityDamage) {
        this.stabilityDamage = stabilityDamage;
    }

    public boolean isKnockedDown() {
        return knockedDown;
    }

    public void setKnockedDown(boolean knockedDown) {
        this.knockedDown = knockedDown;
    }

    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public boolean isEjected() {
        return ejected;
    }

    public void setEjected(boolean ejected) {
        this.ejected = ejected;
    }

    public long getLastFireTime() {
        return lastFireTime;
    }

    public void setLastFireTime(long lastFireTime) {
        this.lastFireTime = lastFireTime;
    }
}
