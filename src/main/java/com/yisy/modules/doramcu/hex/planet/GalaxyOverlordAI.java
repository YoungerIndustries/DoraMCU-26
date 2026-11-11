package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.ai.AiDecision;
import com.yisy.modules.doramcu.ai.AiDecisionContext;
import com.yisy.modules.doramcu.ai.EmpireAiCore;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.Player;

import java.util.*;

/**
 * AI layer that controls 4X empire-level decisions for the integrated campaign.
 * <p>
 * GalaxyOverlordAI wraps the existing {@link EmpireAiCore} and adds empire-to-empire
 * diplomacy, trade policy management, expansion into new systems, war declarations,
 * and contract issuance to player corporations. It uses the existing {@link AiDecision}
 * framework with new action types: {@code EXPAND_COLONY}, {@code DIPLOMATIC_STANCE},
 * {@code OPTIMIZE_TRADE}.
 * </p>
 * <p>
 * Each AI empire in the campaign has its own GalaxyOverlordAI instance that:
 * <ul>
 *   <li>Decides when to expand into new solar systems</li>
 *   <li>Declares war or forms alliances with other empires</li>
 *   <li>Sets trade policies (tariffs, embargoes, free trade zones)</li>
 *   <li>Issues contracts to the player's corporation</li>
 *   <li>Reacts to player actions (reputation changes)</li>
 * </ul>
 * </p>
 * <p>
 * The AI personality (aggressive, diplomatic, economic, scientific) shapes all decisions.
 * An aggressive empire is more likely to declare war and set high tariffs.
 * A diplomatic empire prefers alliances and free trade.
 * An economic empire focuses on trade optimization and contract issuance.
 * </p>
 */
public class GalaxyOverlordAI {

    private final int empireId;
    private final String empireName;
    private final EmpireAiCore empireAiCore;
    private final AiPersonality personality;

    // Empire relationships: empireId -> relation score (-100 to 100)
    private final Map<Integer, Integer> relations;

    // Current allies
    private final Set<Integer> allies;

    // Current enemies (at war)
    private final Set<Integer> enemies;

    // Trade policy for this empire
    private EmpireTradePolicy tradePolicy;

    // Available contracts issued by this empire
    private final List<PlanetContract> availableContracts;

    // Random for decision making
    private final Random random;

    /**
     * Personality types that shape empire behavior.
     */
    public enum AiPersonality {
        /** Aggressive empire: expands quickly, declares war, high tariffs */
        AGGRESSIVE(0.9, 0.3, 0.4, 0.2, "Expansionist Warlord"),

        /** Diplomatic empire: forms alliances, free trade, low tariffs */
        DIPLOMATIC(0.3, 0.9, 0.6, 0.5, "Peaceful Trader"),

        /** Economic empire: focuses on trade, moderate tariffs, many contracts */
        ECONOMIC(0.5, 0.6, 0.9, 0.4, "Merchant Prince"),

        /** Scientific empire: slow expansion, research-focused, neutral trade */
        SCIENTIFIC(0.4, 0.5, 0.5, 0.9, "Arcane Scholar"),

        /** Balanced empire: moderate in all aspects */
        BALANCED(0.6, 0.6, 0.6, 0.6, "Steady Leader");

        private final double aggression;
        private final double diplomacy;
        private final double economicFocus;
        private final double scientificFocus;
        private final String label;

        AiPersonality(double aggression, double diplomacy, double economicFocus,
                      double scientificFocus, String label) {
            this.aggression = aggression;
            this.diplomacy = diplomacy;
            this.economicFocus = economicFocus;
            this.scientificFocus = scientificFocus;
            this.label = label;
        }

        public double getAggression() {
            return aggression;
        }

        public double getDiplomacy() {
            return diplomacy;
        }

        public double getEconomicFocus() {
            return economicFocus;
        }

        public double getScientificFocus() {
            return scientificFocus;
        }

        public String getLabel() {
            return label;
        }

        /**
         * Returns a random personality type.
         */
        public static AiPersonality random(Random rand) {
            AiPersonality[] values = values();
            return values[rand.nextInt(values.length)];
        }
    }

    /**
     * Creates a new GalaxyOverlordAI for an empire.
     *
     * @param empireId unique identifier for this empire
     * @param empireName display name of the empire
     * @param player the player entity controlling this empire (AI player)
     * @param researchLevel AI research level (0-10)
     * @param personality the personality type shaping decisions
     */
    public GalaxyOverlordAI(int empireId, String empireName, Player player,
                            int researchLevel, AiPersonality personality) {
        this.empireId = empireId;
        this.empireName = empireName;
        this.empireAiCore = new EmpireAiCore(empireId, player, researchLevel);
        this.personality = personality;
        this.relations = new HashMap<>();
        this.allies = new HashSet<>();
        this.enemies = new HashSet<>();
        this.tradePolicy = new EmpireTradePolicy();
        this.availableContracts = new ArrayList<>();
        this.random = new Random();
    }

    /**
     * Creates a new GalaxyOverlordAI with a random personality.
     *
     * @param empireId unique identifier for this empire
     * @param empireName display name of the empire
     * @param player the player entity controlling this empire (AI player)
     * @param researchLevel AI research level (0-10)
     */
    public GalaxyOverlordAI(int empireId, String empireName, Player player,
                            int researchLevel) {
        this(empireId, empireName, player, researchLevel, AiPersonality.random(new Random()));
    }

    /** @return the empire ID */
    public int getEmpireId() {
        return empireId;
    }

    /** @return the empire display name */
    public String getEmpireName() {
        return empireName;
    }

    /** @return the underlying EmpireAiCore */
    public EmpireAiCore getEmpireAiCore() {
        return empireAiCore;
    }

    /** @return the AI personality type */
    public AiPersonality getPersonality() {
        return personality;
    }

    /** @return unmodifiable map of relations with other empires */
    public Map<Integer, Integer> getRelations() {
        return Collections.unmodifiableMap(relations);
    }

    /** @return unmodifiable set of ally empire IDs */
    public Set<Integer> getAllies() {
        return Collections.unmodifiableSet(allies);
    }

    /** @return unmodifiable set of enemy empire IDs */
    public Set<Integer> getEnemies() {
        return Collections.unmodifiableSet(enemies);
    }

    /** @return the current trade policy */
    public EmpireTradePolicy getTradePolicy() {
        return tradePolicy;
    }

    /** Sets the trade policy */
    public void setTradePolicy(EmpireTradePolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("Trade policy must not be null");
        }
        this.tradePolicy = policy;
    }

    /** @return list of available contracts issued by this empire */
    public List<PlanetContract> getAvailableContracts() {
        return Collections.unmodifiableList(availableContracts);
    }

    /**
     * Gets the relation score with another empire.
     *
     * @param otherEmpireId the other empire's ID
     * @return relation score (-100 to 100), 0 if no relation set
     */
    public int getRelationWith(int otherEmpireId) {
        return relations.getOrDefault(otherEmpireId, 0);
    }

    /**
     * Sets the relation score with another empire.
     *
     * @param otherEmpireId the other empire's ID
     * @param score relation score (-100 to 100)
     */
    public void setRelationWith(int otherEmpireId, int score) {
        relations.put(otherEmpireId, Math.max(-100, Math.min(100, score)));
        updateAlliesAndEnemies();
    }

    /**
     * Modifies the relation score with another empire.
     *
     * @param otherEmpireId the other empire's ID
     * @param delta change in relation score
     */
    public void modifyRelationWith(int otherEmpireId, int delta) {
        int current = getRelationWith(otherEmpireId);
        setRelationWith(otherEmpireId, current + delta);
    }

    /**
     * Checks if another empire is an ally.
     *
     * @param otherEmpireId the other empire's ID
     * @return true if allied
     */
    public boolean isAllyWith(int otherEmpireId) {
        return allies.contains(otherEmpireId);
    }

    /**
     * Checks if another empire is an enemy (at war).
     *
     * @param otherEmpireId the other empire's ID
     * @return true if at war
     */
    public boolean isEnemyWith(int otherEmpireId) {
        return enemies.contains(otherEmpireId);
    }

    /**
     * Updates allies and enemies sets based on relation scores.
     * Allies have relation >= 50, enemies have relation <= -50.
     */
    private void updateAlliesAndEnemies() {
        allies.clear();
        enemies.clear();
        for (Map.Entry<Integer, Integer> entry : relations.entrySet()) {
            if (entry.getValue() >= 50) {
                allies.add(entry.getKey());
            } else if (entry.getValue() <= -50) {
                enemies.add(entry.getKey());
            }
        }
        // Update trade policy war zone status
        tradePolicy.setWarZone(!enemies.isEmpty());
    }

    /**
     * Processes a new turn for this empire, making decisions about expansion,
     * diplomacy, war, and contract issuance.
     *
     * @param context the current decision context
     * @param turnNumber the current turn number
     * @return list of decisions made this turn
     */
    public List<AiDecision> processTurn(AiDecisionContext context, int turnNumber) {
        List<AiDecision> decisions = new ArrayList<>();

        // 1. Decide on expansion
        AiDecision expansionDecision = decideExpansion(context);
        if (expansionDecision != null) {
            decisions.add(expansionDecision);
        }

        // 2. Decide on diplomacy
        AiDecision diplomacyDecision = decideDiplomacy(context);
        if (diplomacyDecision != null) {
            decisions.add(diplomacyDecision);
        }

        // 3. Decide on war
        AiDecision warDecision = decideWar(context);
        if (warDecision != null) {
            decisions.add(warDecision);
        }

        // 4. Update trade policy based on current state
        updateTradePolicy();

        // 5. Issue new contracts periodically
        if (turnNumber % 5 == 0) {
            issueNewContracts();
        }

        return decisions;
    }

    /**
     * Decides whether to expand into new systems.
     *
     * @param context the current decision context
     * @return expansion decision or null
     */
    public AiDecision decideExpansion(AiDecisionContext context) {
        // Aggressive and scientific personalities expand more
        double expansionDrive = personality.getAggression() * 0.6 + personality.getScientificFocus() * 0.4;
        if (random.nextDouble() < expansionDrive * 0.3) {
            return AiDecision.expandColony(
                    "Personality-driven expansion: " + personality.getLabel(),
                    expansionDrive);
        }
        return null;
    }

    /**
     * Decides whether to declare war on another empire.
     *
     * @param context the current decision context
     * @return war decision or null
     */
    public AiDecision decideWar(AiDecisionContext context) {
        // Only aggressive personalities declare war frequently
        if (random.nextDouble() < personality.getAggression() * 0.1) {
            // Find the most hated empire
            Integer targetEmpire = null;
            int minRelation = 100;
            for (Map.Entry<Integer, Integer> entry : relations.entrySet()) {
                if (!enemies.contains(entry.getKey()) && entry.getValue() < minRelation) {
                    minRelation = entry.getValue();
                    targetEmpire = entry.getKey();
                }
            }
            if (targetEmpire != null && minRelation < -30) {
                return AiDecision.declareWar(
                        targetEmpire,
                        "Relation too low: " + minRelation,
                        personality.getAggression());
            }
        }
        return null;
    }

    /**
     * Decides on diplomatic actions: form alliances, improve relations, etc.
     *
     * @param context the current decision context
     * @return diplomacy decision or null
     */
    public AiDecision decideDiplomacy(AiDecisionContext context) {
        // Diplomatic personalities seek alliances
        if (random.nextDouble() < personality.getDiplomacy() * 0.2) {
            // Find the most friendly non-allied empire
            Integer targetEmpire = null;
            int maxRelation = -100;
            for (Map.Entry<Integer, Integer> entry : relations.entrySet()) {
                if (!allies.contains(entry.getKey()) && entry.getValue() > maxRelation) {
                    maxRelation = entry.getValue();
                    targetEmpire = entry.getKey();
                }
            }
            if (targetEmpire != null && maxRelation > 30) {
                return AiDecision.formAlliance(
                        targetEmpire,
                        "Relation high enough for alliance: " + maxRelation,
                        personality.getDiplomacy());
            }
        }
        return null;
    }

    /**
     * Updates the trade policy based on current empire state.
     */
    public void updateTradePolicy() {
        // Aggressive empires set higher tariffs
        int baseTariff = (int) (personality.getAggression() * 30);

        // At war? Increase tariffs
        if (!enemies.isEmpty()) {
            baseTariff = Math.min(50, baseTariff + 15);
        }

        // Diplomatic empires reduce tariffs
        if (personality.getDiplomacy() > 0.7) {
            baseTariff = Math.max(0, baseTariff - 10);
        }

        tradePolicy.setTariffPercentage(baseTariff);

        // Economic empires issue more contracts
        if (personality.getEconomicFocus() > 0.7) {
            tradePolicy.setFreeTradePlanets(new HashSet<>());
        }
    }

    /**
     * Issues new contracts based on empire needs.
     */
    public void issueNewContracts() {
        // Clear expired contracts
        availableContracts.removeIf(c -> c.isExpired() || c.isCompleted());

        // Economic empires issue more contracts
        int maxContracts = (int) (personality.getEconomicFocus() * 5) + 2;
        if (availableContracts.size() < maxContracts) {
            // Issue a random contract type
            PlanetContractType[] types = PlanetContractType.values();
            PlanetContractType type = types[random.nextInt(types.length)];

            PlanetContract contract = new PlanetContract(
                    "contract_" + empireId + "_" + System.nanoTime(),
                    type,
                    empireId,
                    1000 + random.nextInt(5000),
                    50 + random.nextInt(100),
                    5 + random.nextInt(10)
            );
            availableContracts.add(contract);
        }
    }

    /**
     * Issues contracts to the player's corporation based on reputation.
     *
     * @param playerCorp the player's corporation
     * @param reputationTracker the reputation tracker
     * @return list of contracts available to the player
     */
    public List<PlanetContract> issueContracts(TransportCorporation playerCorp,
                                                EmpireReputationTracker reputationTracker) {
        int reputation = reputationTracker.getReputation(empireId);
        List<PlanetContract> filtered = new ArrayList<>();

        for (PlanetContract contract : availableContracts) {
            // Check if player's reputation is high enough
            if (reputation >= contract.getMinReputation()) {
                // Check if player's rank is high enough
                if (playerCorp.getRank().ordinal() >= contract.getMinRank().ordinal()) {
                    filtered.add(contract);
                }
            }
        }
        return filtered;
    }

    /**
     * Reacts to the player completing a contract for this empire.
     *
     * @param reputationTracker the reputation tracker to update
     * @param reputationDelta base reputation change
     */
    public void onContractCompleted(EmpireReputationTracker reputationTracker,
                                     int reputationDelta) {
        // Economic empires reward more
        double rewardMultiplier = 1.0 + personality.getEconomicFocus() * 0.5;
        int adjustedDelta = (int) (reputationDelta * rewardMultiplier);
        reputationTracker.modifyReputation(empireId, adjustedDelta);
    }

    /**
     * Reacts to the player completing a contract for an enemy empire.
     *
     * @param reputationTracker the reputation tracker to update
     */
    public void onEnemyContractCompleted(EmpireReputationTracker reputationTracker) {
        // Aggressive empires punish more
        int penalty = (int) (personality.getAggression() * 10) + 5;
        reputationTracker.modifyReputation(empireId, -penalty);
    }
}
