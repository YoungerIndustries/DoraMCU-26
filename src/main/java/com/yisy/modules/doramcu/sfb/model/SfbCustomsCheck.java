package com.yisy.modules.doramcu.sfb.model;

import java.util.*;

/**
 * Customs checks for SFB.
 * <p>
 * Handles cargo inspections, contraband detection, fake document detection,
 * fines, cargo seizure, and imprisonment.
 * </p>
 */
public class SfbCustomsCheck {

    private static final Random RANDOM = new Random();

    private final String stationName;
    private final CustomsStrictness strictness;

    public SfbCustomsCheck(String stationName, CustomsStrictness strictness) {
        this.stationName = stationName;
        this.strictness = strictness;
    }

    /**
     * Performs a customs check on a cargo manifest.
     *
     * @param shipName name of the ship
     * @param manifest the cargo manifest
     * @return the customs record
     */
    public CustomsRecord performCustomsCheck(String shipName, CargoManifest manifest) {
        CustomsRecord record = new CustomsRecord(shipName, stationName);

        // Check for contraband
        List<SfbCommodity> contraband = new ArrayList<>();
        for (Map.Entry<SfbCommodity, Integer> entry : manifest.getCargo().entrySet()) {
            if (isContraband(entry.getKey())) {
                contraband.add(entry.getKey());
            }
        }

        record.setContrabandFound(!contraband.isEmpty());
        record.setContrabandItems(contraband);

        // Detection chance based on strictness
        int detectionChance = strictness.getDetectionChance();
        boolean detected = RANDOM.nextInt(100) < detectionChance;

        // Fake documents reduce detection chance
        if (manifest.hasFakeDocuments()) {
            detected = detected && RANDOM.nextInt(100) < detectionChance / 2;
        }

        record.setDetected(detected);

        if (detected && !contraband.isEmpty()) {
            // Determine penalty
            Penalty penalty = determinePenalty(contraband.size());
            record.setPenalty(penalty);

            switch (penalty) {
                case FINE:
                    record.setFineAmount(calculateFine(contraband));
                    break;
                case CARGO_SEIZURE:
                    record.setCargoSeized(true);
                    break;
                case IMPRISONMENT:
                    record.setImprisoned(true);
                    record.setImprisonmentDays(strictness.getImprisonmentDays());
                    break;
            }
        }

        record.setPassed(!detected || contraband.isEmpty());
        return record;
    }

    /**
     * Checks if a commodity is contraband at this station.
     */
    private boolean isContraband(SfbCommodity commodity) {
        if (commodity.getRarity() == SfbCommodity.Rarity.ILLEGAL) {
            return true;
        }
        if (strictness == CustomsStrictness.MILITARY &&
            commodity.getRarity() == SfbCommodity.Rarity.EXTREMELY_RARE) {
            return true;
        }
        return false;
    }

    /**
     * Determines the penalty based on contraband amount.
     */
    private Penalty determinePenalty(int contrabandCount) {
        if (contrabandCount >= 3) return Penalty.IMPRISONMENT;
        if (contrabandCount >= 2) return Penalty.CARGO_SEIZURE;
        return Penalty.FINE;
    }

    /**
     * Calculates the fine amount.
     */
    private int calculateFine(List<SfbCommodity> contraband) {
        int total = 0;
        for (SfbCommodity c : contraband) {
            total += c.getBasePrice() * 2;
        }
        return total * strictness.getFineMultiplier();
    }

    public String getStationName() {
        return stationName;
    }

    public CustomsStrictness getStrictness() {
        return strictness;
    }

    /**
     * Strictness levels for customs checks.
     */
    public enum CustomsStrictness {
        LAX("Lax", 10, 1, 1),
        NORMAL("Normal", 30, 2, 3),
        STRICT("Strict", 50, 3, 5),
        VERY_STRICT("Very Strict", 70, 4, 7),
        MILITARY("Military", 90, 5, 14),
        FEDERATION("Federation", 40, 2, 5);

        private final String name;
        private final int detectionChance;
        private final int fineMultiplier;
        private final int imprisonmentDays;

        CustomsStrictness(String name, int detectionChance, int fineMultiplier, int imprisonmentDays) {
            this.name = name;
            this.detectionChance = detectionChance;
            this.fineMultiplier = fineMultiplier;
            this.imprisonmentDays = imprisonmentDays;
        }

        public String getName() { return name; }
        public int getDetectionChance() { return detectionChance; }
        public int getFineMultiplier() { return fineMultiplier; }
        public int getImprisonmentDays() { return imprisonmentDays; }
    }

    /**
     * Types of penalties.
     */
    public enum Penalty {
        FINE("Fine"),
        CARGO_SEIZURE("Cargo Seizure"),
        IMPRISONMENT("Imprisonment");

        private final String name;

        Penalty(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * A cargo manifest for customs checks.
     */
    public static class CargoManifest {
        private final Map<SfbCommodity, Integer> cargo = new HashMap<>();
        private boolean hasFakeDocuments;

        public void addCargo(SfbCommodity commodity, int quantity) {
            cargo.merge(commodity, quantity, Integer::sum);
        }

        public Map<SfbCommodity, Integer> getCargo() {
            return cargo;
        }

        public boolean hasFakeDocuments() {
            return hasFakeDocuments;
        }

        public void setFakeDocuments(boolean hasFakeDocuments) {
            this.hasFakeDocuments = hasFakeDocuments;
        }
    }

    /**
     * A customs check record.
     */
    public static class CustomsRecord {
        private final String shipName;
        private final String stationName;
        private boolean contrabandFound;
        private List<SfbCommodity> contrabandItems = new ArrayList<>();
        private boolean detected;
        private Penalty penalty;
        private int fineAmount;
        private boolean cargoSeized;
        private boolean imprisoned;
        private int imprisonmentDays;
        private boolean passed;

        public CustomsRecord(String shipName, String stationName) {
            this.shipName = shipName;
            this.stationName = stationName;
        }

        public String getShipName() { return shipName; }
        public String getStationName() { return stationName; }
        public boolean isContrabandFound() { return contrabandFound; }
        public void setContrabandFound(boolean contrabandFound) { this.contrabandFound = contrabandFound; }
        public List<SfbCommodity> getContrabandItems() { return contrabandItems; }
        public void setContrabandItems(List<SfbCommodity> contrabandItems) { this.contrabandItems = contrabandItems; }
        public boolean isDetected() { return detected; }
        public void setDetected(boolean detected) { this.detected = detected; }
        public Penalty getPenalty() { return penalty; }
        public void setPenalty(Penalty penalty) { this.penalty = penalty; }
        public int getFineAmount() { return fineAmount; }
        public void setFineAmount(int fineAmount) { this.fineAmount = fineAmount; }
        public boolean isCargoSeized() { return cargoSeized; }
        public void setCargoSeized(boolean cargoSeized) { this.cargoSeized = cargoSeized; }
        public boolean isImprisoned() { return imprisoned; }
        public void setImprisoned(boolean imprisoned) { this.imprisoned = imprisoned; }
        public int getImprisonmentDays() { return imprisonmentDays; }
        public void setImprisonmentDays(int imprisonmentDays) { this.imprisonmentDays = imprisonmentDays; }
        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
    }
}
