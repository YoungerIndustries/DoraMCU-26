package com.yisy.modules.doramcu.hex.planet;

import java.util.*;

/**
 * Tracks player reputation with each AI empire independently (0-100 scale).
 * <p>
 * Reputation affects:
 * <ul>
 *   <li>Which contracts are available from each empire</li>
 *   <li>Trade tariffs and taxes on routes through empire space</li>
 *   <li>Access to exclusive planets and systems</li>
 *   <li>RPG quest availability</li>
 * </ul>
 * </p>
 * <p>
 * Reputation changes based on:
 * <ul>
 *   <li>Completing contracts for an empire (positive)</li>
 *   <li>Completing contracts for enemy empires (negative)</li>
 *   <li>Completing RPG quests (positive)</li>
 *   <li>Failing contracts (negative)</li>
 * </ul>
 * </p>
 */
public class EmpireReputationTracker {

    /** Reputation with each empire (empireId -> reputation 0-100) */
    private final Map<Integer, Integer> reputation;

    /** History of reputation changes for debugging/logging */
    private final List<ReputationChange> changeHistory;

    /** Maximum reputation value */
    public static final int MAX_REPUTATION = 100;

    /** Minimum reputation value */
    public static final int MIN_REPUTATION = 0;

    /** Reputation threshold for accessing exclusive planets */
    public static final int EXCLUSIVE_PLANET_THRESHOLD = 70;

    /** Reputation threshold for accessing exclusive contracts */
    public static final int EXCLUSIVE_CONTRACT_THRESHOLD = 50;

    /** Reputation threshold for free trade access */
    public static final int FREE_TRADE_THRESHOLD = 80;

    /**
     * Records a single reputation change event.
     */
    public static class ReputationChange {
        private final int empireId;
        private final int delta;
        private final int oldValue;
        private final int newValue;
        private final String reason;
        private final long timestamp;

        public ReputationChange(int empireId, int delta, int oldValue, int newValue, String reason) {
            this.empireId = empireId;
            this.delta = delta;
            this.oldValue = oldValue;
            this.newValue = newValue;            this.reason = reason;
            this.timestamp = System.currentTimeMillis();
        }

        public int getEmpireId() {
            return empireId;
        }

        public int getDelta() {
            return delta;
        }

        public int getOldValue() {
            return oldValue;
        }

        public int getNewValue() {
            return newValue;
        }

        public String getReason() {
            return reason;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "ReputationChange{" +
                    "empireId=" + empireId +
                    ", delta=" + delta +
                    ", oldValue=" + oldValue +
                    ", newValue=" + newValue +
                    ", reason='" + reason + '\'' +
                    '}';
        }
    }

    /**
     * Creates a new reputation tracker.
     */
    public EmpireReputationTracker() {
        this.reputation = new HashMap<>();
        this.changeHistory = new ArrayList<>();
    }

    /**
     * Gets the reputation with a specific empire.
     *
     * @param empireId the empire ID
     * @return reputation score (0-100), 0 if no reputation set
     */
    public int getReputation(int empireId) {
        return reputation.getOrDefault(empireId, 0);
    }

    /**
     * Sets the reputation with a specific empire.
     *
     * @param empireId the empire ID
     * @param value reputation score (0-100)
     */
    public void setReputation(int empireId, int value) {
        int oldValue = getReputation(empireId);
        int newValue = Math.max(MIN_REPUTATION, Math.min(MAX_REPUTATION, value));
        reputation.put(empireId, newValue);
        changeHistory.add(new ReputationChange(empireId, newValue - oldValue, oldValue, newValue,
                "Direct set"));
    }

    /**
     * Modifies the reputation with a specific empire.
     *
     * @param empireId the empire ID
     * @param delta change in reputation (positive or negative)
     */
    public void modifyReputation(int empireId, int delta) {
        modifyReputation(empireId, delta, "Unknown");
    }

    /**
     * Modifies the reputation with a specific empire, recording the reason.
     *
     * @param empireId the empire ID
     * @param delta change in reputation (positive or negative)
     * @param reason why the reputation changed
     */
    public void modifyReputation(int empireId, int delta, String reason) {
        int oldValue = getReputation(empireId);
        int newValue = Math.max(MIN_REPUTATION, Math.min(MAX_REPUTATION, oldValue + delta));
        reputation.put(empireId, newValue);
        changeHistory.add(new ReputationChange(empireId, newValue - oldValue, oldValue, newValue, reason));
    }

    /**
     * Checks if the player can access a planet owned by an empire.
     *
     * @param empireId the empire ID
     * @return true if reputation is high enough
     */
    public boolean canAccessPlanet(int empireId) {
        return getReputation(empireId) >= EXCLUSIVE_PLANET_THRESHOLD;
    }

    /**
     * Checks if the player can access exclusive contracts from an empire.
     *
     * @param empireId the empire ID
     * @return true if reputation is high enough
     */
    public boolean canAccessExclusiveContracts(int empireId) {
        return getReputation(empireId) >= EXCLUSIVE_CONTRACT_THRESHOLD;
    }

    /**
     * Checks if the player has free trade access with an empire.
     *
     * @param empireId the empire ID
     * @return true if reputation is high enough
     */
    public boolean hasFreeTradeAccess(int empireId) {
        return getReputation(empireId) >= FREE_TRADE_THRESHOLD;
    }

    /**
     * Gets the tariff discount based on reputation with an empire.
     *
     * @param empireId the empire ID
     * @return discount factor (0.0-1.0), higher reputation = higher discount
     */
    public double getTariffDiscount(int empireId) {
        int rep = getReputation(empireId);
        if (rep < 50) {
            return 0.0;
        }
        return (rep - 50) / 50.0; // 0.0 at 50 rep, 1.0 at 100 rep
    }

    /**
     * Gets the list of available contracts from an empire based on reputation.
     *
     * @param empireId the empire ID
     * @param allContracts all contracts issued by the empire
     * @return list of contracts accessible given current reputation
     */
    public List<PlanetContract> getAvailableContracts(int empireId, List<PlanetContract> allContracts) {
        int rep = getReputation(empireId);
        List<PlanetContract> available = new ArrayList<>();

        for (PlanetContract contract : allContracts) {
            if (rep >= contract.getMinReputation()) {
                available.add(contract);
            }
        }
        return available;
    }

    /**
     * Gets all empire IDs that have been tracked.
     *
     * @return set of empire IDs
     */
    public Set<Integer> getTrackedEmpireIds() {
        return Collections.unmodifiableSet(reputation.keySet());
    }

    /**
     * Gets the reputation with all empires.
     *
     * @return unmodifiable map of empire ID to reputation
     */
    public Map<Integer, Integer> getAllReputations() {
        return Collections.unmodifiableMap(reputation);
    }

    /**
     * Gets the change history.
     *
     * @return unmodifiable list of reputation changes
     */
    public List<ReputationChange> getChangeHistory() {
        return Collections.unmodifiableList(changeHistory);
    }

    /**
     * Gets the change history for a specific empire.
     *
     * @param empireId the empire ID
     * @return list of reputation changes for this empire
     */
    public List<ReputationChange> getChangeHistory(int empireId) {
        List<ReputationChange> filtered = new ArrayList<>();
        for (ReputationChange change : changeHistory) {
            if (change.getEmpireId() == empireId) {
                filtered.add(change);
            }
        }
        return filtered;
    }

    /**
     * Gets the reputation category for an empire.
     *
     * @param empireId the empire ID
     * @return reputation category
     */
    public ReputationCategory getCategory(int empireId) {
        int rep = getReputation(empireId);
        if (rep >= 90) {
            return ReputationCategory.ALLIED;
        } else if (rep >= 70) {
            return ReputationCategory.FRIENDLY;
        } else if (rep >= 50) {
            return ReputationCategory.NEUTRAL;
        } else if (rep >= 30) {
            return ReputationCategory.HOSTILE;
        } else if (rep >= 10) {
            return ReputationCategory.ENEMY;
        } else {
            return ReputationCategory.OUTLAW;
        }
    }

    /**
     * Reputation categories for quick reference.
     */
    public enum ReputationCategory {
        /** Reputation 90-100: Full alliance, best contracts, lowest tariffs */
        ALLIED(90, 100, "Allied", "Full trust, exclusive access, lowest tariffs"),

        /** Reputation 70-89: Friendly, good contracts, reduced tariffs */
        FRIENDLY(70, 89, "Friendly", "Good relations, most contracts available"),

        /** Reputation 50-69: Neutral, standard contracts, normal tariffs */
        NEUTRAL(50, 69, "Neutral", "Standard relations, normal trade"),

        /** Reputation 30-49: Hostile, limited contracts, higher tariffs */
        HOSTILE(30, 49, "Hostile", "Strained relations, limited access"),

        /** Reputation 10-29: Enemy, very few contracts, high tariffs */
        ENEMY(10, 29, "Enemy", "Very poor relations, restricted access"),

        /** Reputation 0-9: Outlaw, no contracts, maximum tariffs */
        OUTLAW(0, 9, "Outlaw", "No trust, almost no access");

        private final int minReputation;
        private final int maxReputation;
        private final String label;
        private final String description;

        ReputationCategory(int minReputation, int maxReputation, String label, String description) {
            this.minReputation = minReputation;
            this.maxReputation = maxReputation;
            this.label = label;
            this.description = description;
        }

        public int getMinReputation() {
            return minReputation;
        }

        public int getMaxReputation() {
            return maxReputation;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public String toString() {
        return "EmpireReputationTracker{" +
                "reputation=" + reputation +
                ", changeHistorySize=" + changeHistory.size() +
                '}';
    }
}
