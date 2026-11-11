package com.yisy.modules.doramcu.babylon5.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Customs checks and contraband detection for B5 trade.
 * <p>
 * When a ship arrives at a station, customs may inspect the cargo for
 * contraband items. The inspection is based on the ship's cargo, the
 * station's strictness, and random chance.
 * </p>
 * <p>
 * Contraband items include illegal weapons, slaves, Shadow bio-weapons,
 * and other restricted goods. Being caught with contraband results in
 * fines, cargo seizure, or imprisonment.
 * </p>
 * <p>
 * The system follows the Traveller customs check rules: a roll of 2D6
 * determines if an inspection occurs, and the cargo is checked against
 * the station's contraband list.
 * </p>
 */
public class B5CustomsCheck {

    private final String stationName;
    private final CustomsStrictness strictness;
    private final Set<B5Commodity> contrabandList;
    private final Random random;
    private final List<CustomsRecord> inspectionHistory;
    private int tick;

    /**
     * Strictness levels for customs inspections.
     */
    public enum CustomsStrictness {
        /**
         * Very lax — rare inspections, low fines.
         */
        LAX("Lax", 10, 1000),

        /**
         * Relaxed — occasional inspections, moderate fines.
         */
        RELAXED("Relaxed", 20, 2000),

        /**
         * Normal — regular inspections, standard fines.
         */
        NORMAL("Normal", 35, 5000),

        /**
         * Strict — frequent inspections, high fines.
         */
        STRICT("Strict", 50, 10000),

        /**
         * Very strict — almost every ship inspected, very high fines.
         */
        VERY_STRICT("Very Strict", 70, 20000),

        /**
         * Military — all ships inspected, maximum penalties.
         */
        MILITARY("Military", 90, 50000);

        private final String name;
        private final int inspectionChance;
        private final int baseFine;

        CustomsStrictness(String name, int inspectionChance, int baseFine) {
            this.name = name;
            this.inspectionChance = inspectionChance;
            this.baseFine = baseFine;
        }

        public String getName() {
            return name;
        }

        public int getInspectionChance() {
            return inspectionChance;
        }

        public int getBaseFine() {
            return baseFine;
        }
    }

    /**
     * Result of a customs inspection.
     */
    public enum InspectionResult {
        NO_INSPECTION("No Inspection"),
        CLEARED("Cleared"),
        CONTRABAND_FOUND("Contraband Found"),
        SUSPICIOUS_ACTIVITY("Suspicious Activity"),
        DOCUMENTS_FAKE("Fake Documents");

        private final String description;

        InspectionResult(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Record of a customs inspection.
     */
    public static class CustomsRecord {
        private final String shipName;
        private final InspectionResult result;
        private final List<B5Commodity> contrabandFound;
        private final int fine;
        private final boolean cargoSeized;
        private final boolean imprisoned;
        private final String description;
        private final int tick;

        CustomsRecord(String shipName, InspectionResult result, List<B5Commodity> contrabandFound,
                      int fine, boolean cargoSeized, boolean imprisoned, String description, int tick) {
            this.shipName = shipName;
            this.result = result;
            this.contrabandFound = contrabandFound;
            this.fine = fine;
            this.cargoSeized = cargoSeized;
            this.imprisoned = imprisoned;
            this.description = description;
            this.tick = tick;
        }

        public String getShipName() {
            return shipName;
        }

        public InspectionResult getResult() {
            return result;
        }

        public List<B5Commodity> getContrabandFound() {
            return contrabandFound;
        }

        public int getFine() {
            return fine;
        }

        public boolean isCargoSeized() {
            return cargoSeized;
        }

        public boolean isImprisoned() {
            return imprisoned;
        }

        public String getDescription() {
            return description;
        }

        public int getTick() {
            return tick;
        }
    }

    /**
     * Cargo manifest for a ship.
     */
    public static class CargoManifest {
        private final Map<B5Commodity, Integer> cargo;
        private boolean hasFakeDocuments;

        public CargoManifest() {
            this.cargo = new HashMap<>();
            this.hasFakeDocuments = false;
        }

        public void addCargo(B5Commodity commodity, int units) {
            cargo.merge(commodity, units, Integer::sum);
        }

        public int getCargoUnits(B5Commodity commodity) {
            return cargo.getOrDefault(commodity, 0);
        }

        public Map<B5Commodity, Integer> getCargo() {
            return Collections.unmodifiableMap(cargo);
        }

        public boolean hasFakeDocuments() {
            return hasFakeDocuments;
        }

        public void setFakeDocuments(boolean fake) {
            this.hasFakeDocuments = fake;
        }

        public boolean isEmpty() {
            return cargo.isEmpty();
        }
    }

    public B5CustomsCheck(String stationName, CustomsStrictness strictness) {
        this.stationName = stationName;
        this.strictness = strictness;
        this.contrabandList = new HashSet<>();
        this.random = new Random();
        this.inspectionHistory = new ArrayList<>();
        this.tick = 0;
        initializeContrabandList();
    }

    /**
     * Initializes the contraband list based on strictness.
     * <p>
     * Lax stations only check for the most dangerous items.
     * Strict stations check for all illegal and extremely rare items.
     * </p>
     */
    private void initializeContrabandList() {
        // All stations consider these contraband
        contrabandList.add(B5Commodity.SLAVES);
        contrabandList.add(B5Commodity.SHADOW_BIO_WEAPON);

        if (strictness.getInspectionChance() >= 35) {
            // Normal and above: add illegal weapons
            contrabandList.add(B5Commodity.CONTRABAND_WEAPONS);
        }

        if (strictness.getInspectionChance() >= 50) {
            // Strict and above: add extremely rare items
            contrabandList.add(B5Commodity.VORLON_ARTIFACT);
            contrabandList.add(B5Commodity.SHADOW_CRYSTAL);
            contrabandList.add(B5Commodity.FIRST_ONE_TECH);
        }

        if (strictness.getInspectionChance() >= 70) {
            // Very strict: add psi materials
            contrabandList.add(B5Commodity.PSI_MATERIALS);
        }
    }

    // ---- Accessors ----

    /**
     * Returns the station name.
     *
     * @return the station name
     */
    public String getStationName() {
        return stationName;
    }

    /**
     * Returns the customs strictness level.
     *
     * @return the strictness level
     */
    public CustomsStrictness getStrictness() {
        return strictness;
    }

    /**
     * Returns the contraband list.
     *
     * @return set of contraband commodities
     */
    public Set<B5Commodity> getContrabandList() {
        return Collections.unmodifiableSet(contrabandList);
    }

    /**
     * Returns the inspection history.
     *
     * @return list of customs records
     */
    public List<CustomsRecord> getInspectionHistory() {
        return Collections.unmodifiableList(inspectionHistory);
    }

    /**
     * Returns the current tick.
     *
     * @return the tick number
     */
    public int getTick() {
        return tick;
    }

    // ---- Customs Checks ----

    /**
     * Performs a customs check on a ship.
     * <p>
     * The check determines if the ship is inspected, and if so,
     * whether contraband is found and what penalties apply.
     * </p>
     *
     * @param shipName the name of the ship
     * @param manifest the cargo manifest
     * @return the customs record
     */
    public CustomsRecord performCustomsCheck(String shipName, CargoManifest manifest) {
        tick++;

        // Check if inspection occurs
        int inspectionRoll = random.nextInt(100);
        if (inspectionRoll >= strictness.getInspectionChance()) {
            // No inspection
            CustomsRecord record = new CustomsRecord(
                    shipName,
                    InspectionResult.NO_INSPECTION,
                    Collections.emptyList(),
                    0,
                    false,
                    false,
                    "No customs inspection at " + stationName,
                    tick
            );
            inspectionHistory.add(record);
            return record;
        }

        // Check for fake documents
        if (manifest.hasFakeDocuments() && random.nextInt(100) < 50) {
            // Fake documents detected
            int fine = strictness.getBaseFine() * 2;
            CustomsRecord record = new CustomsRecord(
                    shipName,
                    InspectionResult.DOCUMENTS_FAKE,
                    Collections.emptyList(),
                    fine,
                    false,
                    true,
                    "Fake documents detected at " + stationName + "! Fine: " + fine + " credits, imprisoned.",
                    tick
            );
            inspectionHistory.add(record);
            return record;
        }

        // Check cargo for contraband
        List<B5Commodity> foundContraband = new ArrayList<>();
        for (Map.Entry<B5Commodity, Integer> entry : manifest.getCargo().entrySet()) {
            if (contrabandList.contains(entry.getKey())) {
                foundContraband.add(entry.getKey());
            }
        }

        if (!foundContraband.isEmpty()) {
            // Contraband found
            int fine = calculateFine(foundContraband, manifest);
            boolean cargoSeized = random.nextInt(100) < 50;
            boolean imprisoned = strictness.getInspectionChance() >= 70 && random.nextInt(100) < 30;

            String description = "Contraband found at " + stationName + ": " +
                    foundContraband.stream().map(B5Commodity::getName).reduce((a, b) -> a + ", " + b).orElse("") +
                    ". Fine: " + fine + " credits" +
                    (cargoSeized ? ", cargo seized" : "") +
                    (imprisoned ? ", imprisoned" : "") + ".";

            CustomsRecord record = new CustomsRecord(
                    shipName,
                    InspectionResult.CONTRABAND_FOUND,
                    foundContraband,
                    fine,
                    cargoSeized,
                    imprisoned,
                    description,
                    tick
            );
            inspectionHistory.add(record);
            return record;
        }

        // Check for suspicious activity
        if (random.nextInt(100) < 10) {
            // Suspicious activity
            int fine = strictness.getBaseFine() / 2;
            CustomsRecord record = new CustomsRecord(
                    shipName,
                    InspectionResult.SUSPICIOUS_ACTIVITY,
                    Collections.emptyList(),
                    fine,
                    false,
                    false,
                    "Suspicious activity detected at " + stationName + ". Fine: " + fine + " credits.",
                    tick
            );
            inspectionHistory.add(record);
            return record;
        }

        // Cleared
        CustomsRecord record = new CustomsRecord(
                shipName,
                InspectionResult.CLEARED,
                Collections.emptyList(),
                0,
                false,
                false,
                "Cleared customs at " + stationName,
                tick
        );
        inspectionHistory.add(record);
        return record;
    }

    /**
     * Calculates the fine for contraband.
     */
    private int calculateFine(List<B5Commodity> contraband, CargoManifest manifest) {
        int fine = strictness.getBaseFine();

        for (B5Commodity commodity : contraband) {
            int units = manifest.getCargoUnits(commodity);
            int commodityFine = commodity.getBasePrice() * units;

            // Illegal items have higher fines
            if (commodity.getRarity() == B5Commodity.Rarity.ILLEGAL) {
                commodityFine *= 5;
            } else if (commodity.getRarity() == B5Commodity.Rarity.EXTREMELY_RARE) {
                commodityFine *= 3;
            }

            fine += commodityFine;
        }

        return fine;
    }

    /**
     * Advances the simulation by one tick.
     */
    public void advanceTick() {
        tick++;
    }

    /**
     * Generates a summary of customs operations.
     *
     * @return a map with customs statistics
     */
    public Map<String, Object> getCustomsSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("stationName", stationName);
        summary.put("strictness", strictness.getName());
        summary.put("inspectionChance", strictness.getInspectionChance() + "%");
        summary.put("baseFine", strictness.getBaseFine());
        summary.put("contrabandItems", contrabandList.size());
        summary.put("totalInspections", inspectionHistory.size());

        long clearedCount = inspectionHistory.stream()
                .filter(r -> r.getResult() == InspectionResult.CLEARED)
                .count();
        summary.put("cleared", clearedCount);

        long contrabandCount = inspectionHistory.stream()
                .filter(r -> r.getResult() == InspectionResult.CONTRABAND_FOUND)
                .count();
        summary.put("contrabandFound", contrabandCount);

        int totalFines = inspectionHistory.stream().mapToInt(CustomsRecord::getFine).sum();
        summary.put("totalFines", totalFines);

        return summary;
    }
}
