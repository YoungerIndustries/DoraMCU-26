package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.model.*;
import com.yisy.modules.doramcu.util.DiceUtil;

import java.util.*;

/**
 * Handles special systems in SFB combat.
 * <p>
 * Manages cloaking, tractor beams, transporters, EW (ECM/ECCM),
 * sensors, and scanners.
 * </p>
 */
public class SpecialSystemsService {

    /**
     * Cloaking system state.
     */
    public enum CloakState {
        UNCLOAKED, CLOAKING, CLOAKED, DECLOAKING
    }

    /**
     * Tractor beam state.
     */
    public enum TractorState {
        OFF, ACTIVATING, ACTIVE, DEACTIVATING
    }

    /**
     * EW (Electronic Warfare) result.
     */
    public static class EWResult {
        private final boolean ecmSuccess;
        private final boolean eccmSuccess;
        private final int accuracyPenalty;
        private final String message;

        public EWResult(boolean ecmSuccess, boolean eccmSuccess, int accuracyPenalty, String message) {
            this.ecmSuccess = ecmSuccess;
            this.eccmSuccess = eccmSuccess;
            this.accuracyPenalty = accuracyPenalty;
            this.message = message;
        }

        public boolean isEcmSuccess() { return ecmSuccess; }
        public boolean isEccmSuccess() { return eccmSuccess; }
        public int getAccuracyPenalty() { return accuracyPenalty; }
        public String getMessage() { return message; }
    }

    /**
     * Sensor detection result.
     */
    public static class SensorResult {
        private final boolean detected;
        private final boolean identified;
        private final int range;
        private final String message;

        public SensorResult(boolean detected, boolean identified, int range, String message) {
            this.detected = detected;
            this.identified = identified;
            this.range = range;
            this.message = message;
        }

        public boolean isDetected() { return detected; }
        public boolean isIdentified() { return identified; }
        public int getRange() { return range; }
        public String getMessage() { return message;
        }
    }

    // Sensor and scanner ratings per ship
    private final Map<String, Integer> sensorRatings = new HashMap<>();
    private final Map<String, Integer> scannerRatings = new HashMap<>();
    private final Map<String, Integer> ecmRatings = new HashMap<>();
    private final Map<String, Integer> eccmRatings = new HashMap<>();
    private final Map<String, Integer> cloakRatings = new HashMap<>();
    private final Map<String, Integer> tractorRatings = new HashMap<>();

    /**
     * Set the sensor rating for a ship (1-9 scale).
     */
    public void setSensorRating(SfbShip ship, int rating) {
        sensorRatings.put(ship.getId(), Math.max(1, Math.min(9, rating)));
    }

    /**
     * Set the scanner rating for a ship (1-9 scale).
     */
    public void setScannerRating(SfbShip ship, int rating) {
        scannerRatings.put(ship.getId(), Math.max(1, Math.min(9, rating)));
    }

    /**
     * Set the ECM rating for a ship.
     */
    public void setEcmRating(SfbShip ship, int rating) {
        ecmRatings.put(ship.getId(), Math.max(0, rating));
    }

    /**
     * Set the ECCM rating for a ship.
     */
    public void setEccmRating(SfbShip ship, int rating) {
        eccmRatings.put(ship.getId(), Math.max(0, rating));
    }

    /**
     * Set the cloaking rating for a ship.
     */
    public void setCloakRating(SfbShip ship, int rating) {
        cloakRatings.put(ship.getId(), Math.max(0, rating));
    }

    /**
     * Set the tractor beam rating for a ship.
     */
    public void setTractorRating(SfbShip ship, int rating) {
        tractorRatings.put(ship.getId(), Math.max(0, rating));
    }

    /**
     * Attempt to cloak a ship.
     * @return true if the ship is now cloaked
     */
    public boolean cloakShip(SfbShip ship) {
        int rating = cloakRatings.getOrDefault(ship.getId(), 0);
        if (rating <= 0) return false;

        // Cloaking requires a successful roll
        int roll = DiceUtil.rollD6();
        if (roll <= rating) {
            ship.setCloaked(true);
            return true;
        }
        return false;
    }

    /**
     * Attempt to decloak a ship.
     */
    public void decloakShip(SfbShip ship) {
        ship.setCloaked(false);
    }

    /**
     * Attempt to detect a cloaked ship using sensors.
     * @param detector the ship doing the detecting
     * @param target the potentially cloaked ship
     * @return sensor result
     */
    public SensorResult detectCloakedShip(SfbShip detector, SfbShip target) {
        if (!target.isCloaked()) {
            return new SensorResult(true, true, 
                    detector.getPosition().distanceTo(target.getPosition()),
                    target.getName() + " is not cloaked");
        }

        int sensorRating = sensorRatings.getOrDefault(detector.getId(), 0);
        int cloakRating = cloakRatings.getOrDefault(target.getId(), 0);
        int scannerRating = scannerRatings.getOrDefault(detector.getId(), 0);

        if (sensorRating <= 0) {
            return new SensorResult(false, false, 0, "No sensors available");
        }

        // Detection roll: sensor + scanner vs cloak rating
        int roll = DiceUtil.rollD6();
        int detectionValue = sensorRating + scannerRating / 2;
        
        boolean detected = roll <= detectionValue && detectionValue > cloakRating;
        boolean identified = detected && scannerRating >= cloakRating;

        int range = detector.getPosition().distanceTo(target.getPosition());

        return new SensorResult(detected, identified, range,
                detected ? (identified ? "Cloaked ship detected and identified" : "Cloaked ship detected but not identified")
                         : "No cloaked ship detected");
    }

    /**
     * Resolve EW (Electronic Warfare) between two ships.
     * @param ecmShip the ship using ECM
     * @param targetShip the ship being targeted
     * @return EW result
     */
    public EWResult resolveEW(SfbShip ecmShip, SfbShip targetShip) {
        int ecmRating = ecmRatings.getOrDefault(ecmShip.getId(), 0);
        int eccmRating = eccmRatings.getOrDefault(targetShip.getId(), 0);

        if (ecmRating <= 0) {
            return new EWResult(false, false, 0, "No ECM available");
        }

        // ECM vs ECCM resolution
        int ecmRoll = DiceUtil.rollD6();
        int eccmRoll = DiceUtil.rollD6();

        boolean ecmSuccess = ecmRoll + ecmRating > eccmRoll + eccmRating;
        boolean eccmSuccess = !ecmSuccess;

        int accuracyPenalty = ecmSuccess ? Math.max(1, ecmRating - eccmRating) : 0;

        return new EWResult(ecmSuccess, eccmSuccess, accuracyPenalty,
                ecmSuccess ? "ECM successful, -" + accuracyPenalty + " to accuracy" : "ECCM successful, no penalty");
    }

    /**
     * Attempt to use tractor beam on a target.
     * @param tractorShip the ship using the tractor beam
     * @param target the target ship
     * @return true if the tractor beam is active
     */
    public boolean activateTractorBeam(SfbShip tractorShip, SfbShip target) {
        int rating = tractorRatings.getOrDefault(tractorShip.getId(), 0);
        if (rating <= 0) return false;

        // Tractor beam requires line of sight and range
        int distance = tractorShip.getPosition().distanceTo(target.getPosition());
        if (distance > 12) return false; // Max tractor range

        int roll = DiceUtil.rollD6();
        return roll <= rating;
    }

    /**
     * Attempt to break free from a tractor beam.
     * @param trappedShip the ship trying to break free
     * @return true if the ship breaks free
     */
    public boolean breakTractorBeam(SfbShip trappedShip) {
        // Use impulse power to break free
        SfbSSD ssd = trappedShip.getSsd();
        if (ssd == null) return false;

        int impulsePower = ssd.getAvailableImpulsePower();
        int roll = DiceUtil.rollD6();
        return roll <= impulsePower;
    }

    /**
     * Calculate sensor range for a ship.
     */
    public int getSensorRange(SfbShip ship) {
        int rating = sensorRatings.getOrDefault(ship.getId(), 0);
        // Base range: rating * 4 hexes
        return rating * 4;
    }

    /**
     * Calculate scanner range for a ship.
     */
    public int getScannerRange(SfbShip ship) {
        int rating = scannerRatings.getOrDefault(ship.getId(), 0);
        // Scanner range: rating * 2 hexes
        return rating * 2;
    }

    /**
     * Check if a ship can be detected by sensors.
     */
    public boolean isDetectable(SfbShip detector, SfbShip target) {
        if (target.isDestroyed()) return false;
        
        int sensorRange = getSensorRange(detector);
        int distance = detector.getPosition().distanceTo(target.getPosition());
        
        if (target.isCloaked()) {
            SensorResult result = detectCloakedShip(detector, target);
            return result.isDetected();
        }
        
        return distance <= sensorRange;
    }

    /**
     * Get the EW accuracy penalty for a ship due to enemy ECM.
     */
    public int getEwAccuracyPenalty(SfbShip attacker, SfbShip defender) {
        int ecmRating = ecmRatings.getOrDefault(defender.getId(), 0);
        int eccmRating = eccmRatings.getOrDefault(attacker.getId(), 0);

        if (ecmRating <= 0) return 0;

        int ecmRoll = DiceUtil.rollD6();
        int eccmRoll = DiceUtil.rollD6();

        if (ecmRoll + ecmRating > eccmRoll + eccmRating) {
            return Math.max(1, ecmRating - eccmRating);
        }
        return 0;
    }
}
