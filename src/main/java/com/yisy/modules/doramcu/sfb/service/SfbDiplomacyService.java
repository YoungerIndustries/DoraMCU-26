package com.yisy.modules.doramcu.sfb.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Diplomacy service for SFB campaign AI.
 * <p>
 * Manages relationships between factions, including diplomatic relations,
 * treaties, declarations of war, and diplomatic actions.
 * </p>
 * <p>
 * Relations are tracked on a scale from -100 (total war) to +100 (allies).
 * Treaties provide bonuses and restrictions based on the treaty type.
 * </p>
 */

@Service
public class SfbDiplomacyService {

    /**
     * Types of diplomatic relations between factions.
     */
    public enum RelationType {
        HOSTILE(-100, -76, "Total war, all-out conflict"),
        ENEMY(-75, -51, "Active hostility, military conflict"),
        UNFRIENDLY(-50, -26, "Distrustful, limited cooperation"),
        NEUTRAL(-25, 25, "No strong feelings, trade possible"),
        FRIENDLY(26, 50, "Cooperative, trade bonuses"),
        ALLIED(51, 75, "Strong alliance, mutual defense"),
        VASSAL(76, 100, "Complete loyalty, shared resources");

        private final int minScore;
        private final int maxScore;
        private final String description;

        RelationType(int minScore, int maxScore, String description) {
            this.minScore = minScore;
            this.maxScore = maxScore;
            this.description = description;
        }

        public int getMinScore() { return minScore; }
        public int getMaxScore() { return maxScore; }
        public String getDescription() { return description; }

        public static RelationType fromScore(int score) {
            for (RelationType type : values()) {
                if (score >= type.minScore && score <= type.maxScore) {
                    return type;
                }
            }
            return NEUTRAL;
        }
    }

    /**
     * Types of treaties between factions.
     */
    public enum TreatyType {
        NON_AGGRESSION("Non-Aggression Pact", "No military action against each other", 10),
        TRADE_AGREEMENT("Trade Agreement", "Trade bonuses between factions", 5),
        MUTUAL_DEFENSE("Mutual Defense Pact", "Defend each other if attacked", 15),
        MILITARY_ALLIANCE("Military Alliance", "Full military cooperation", 20),
        VASSALAGE("Vassalage", "One faction serves the other", 25),
        CEASEFIRE("Ceasefire", "Temporary halt to hostilities", 8);

        private final String name;
        private final String description;
        private final int relationBonus;

        TreatyType(String name, String description, int relationBonus) {
            this.name = name;
            this.description = description;
            this.relationBonus = relationBonus;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getRelationBonus() { return relationBonus; }
    }

    /**
     * A treaty between two factions.
     */
    public static class Treaty {
        private final String id;
        private final String factionA;
        private final String factionB;
        private final TreatyType type;
        private final int turnsRemaining;
        private final int turnsTotal;
        private final long signedAt;

        private Treaty(Builder builder) {
            this.id = builder.id;
            this.factionA = builder.factionA;
            this.factionB = builder.factionB;
            this.type = builder.type;
            this.turnsRemaining = builder.turnsTotal;
            this.turnsTotal = builder.turnsTotal;
            this.signedAt = System.currentTimeMillis();
        }

        public String getId() { return id; }
        public String getFactionA() { return factionA; }
        public String getFactionB() { return factionB; }
        public TreatyType getType() { return type; }
        public int getTurnsRemaining() { return turnsRemaining; }
        public int getTurnsTotal() { return turnsTotal; }
        public long getSignedAt() { return signedAt; }
        public boolean isExpired() { return turnsRemaining <= 0; }

        public void advanceTurn() {
            if (turnsRemaining > 0) {
                // No-op Ã¢â‚¬â€ turns are decremented externally
            }
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String id;
            private String factionA;
            private String factionB;
            private TreatyType type;
            private int turnsTotal = 10;

            public Builder id(String id) { this.id = id; return this; }
            public Builder factionA(String factionA) { this.factionA = factionA; return this; }
            public Builder factionB(String factionB) { this.factionB = factionB; return this; }
            public Builder type(TreatyType type) { this.type = type; return this; }
            public Builder turnsTotal(int turnsTotal) { this.turnsTotal = turnsTotal; return this; }

            public Treaty build() {
                if (this.id == null) this.id = UUID.randomUUID().toString().substring(0, 8);
                return new Treaty(this);
            }
        }
    }

    /**
     * Diplomatic action between factions.
     */
    public enum DiplomaticAction {
        IMPROVE_RELATIONS("Improve Relations", 10, "Diplomatic overture to improve relations"),
        INSULT("Insult", -15, "Diplomatic insult worsens relations"),
        TRADE_OFFER("Trade Offer", 5, "Offer trade deal to improve relations"),
        MILITARY_THREAT("Military Threat", -20, "Threaten military action"),
        BORDER_SKIRMISH("Border Skirmish", -10, "Minor military conflict"),
        ALL_OFFER("Alliance Offer", 15, "Propose alliance"),
        DECLARATION_OF_WAR("Declaration of War", -50, "Formal declaration of war"),
        PEACE_OFFER("Peace Offer", 25, "Offer peace terms"),
        AID_OFFER("Aid Offer", 8, "Offer military or economic aid"),
        SPY_ACTIVITY("Spy Activity", -5, "Discovered espionage worsens relations slightly");

        private final String name;
        private final int relationChange;
        private final String description;

        DiplomaticAction(String name, int relationChange, String description) {
            this.name = name;
            this.relationChange = relationChange;
            this.description = description;
        }

        public String getName() { return name; }
        public int getRelationChange() { return relationChange; }
        public String getDescription() { return description; }
    }

    /**
     * Result of a diplomatic action.
     */
    public static class DiplomaticResult {
        private final String actionName;
        private final String factionA;
        private final String factionB;
        private final int relationBefore;
        private final int relationAfter;
        private final RelationType relationTypeBefore;
        private final RelationType relationTypeAfter;
        private final boolean success;
        private final String message;

        private DiplomaticResult(Builder builder) {
            this.actionName = builder.actionName;
            this.factionA = builder.factionA;
            this.factionB = builder.factionB;
            this.relationBefore = builder.relationBefore;
            this.relationAfter = builder.relationAfter;
            this.relationTypeBefore = RelationType.fromScore(builder.relationBefore);
            this.relationTypeAfter = RelationType.fromScore(builder.relationAfter);
            this.success = builder.success;
            this.message = builder.message;
        }

        public String getActionName() { return actionName; }
        public String getFactionA() { return factionA; }
        public String getFactionB() { return factionB; }
        public int getRelationBefore() { return relationBefore; }
        public int getRelationAfter() { return relationAfter; }
        public RelationType getRelationTypeBefore() { return relationTypeBefore; }
        public RelationType getRelationTypeAfter() { return relationTypeAfter; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String actionName;
            private String factionA;
            private String factionB;
            private int relationBefore;
            private int relationAfter;
            private boolean success;
            private String message;

            public Builder actionName(String actionName) { this.actionName = actionName; return this; }
            public Builder factionA(String factionA) { this.factionA = factionA; return this; }
            public Builder factionB(String factionB) { this.factionB = factionB; return this; }
            public Builder relationBefore(int relationBefore) { this.relationBefore = relationBefore; return this; }
            public Builder relationAfter(int relationAfter) { this.relationAfter = relationAfter; return this; }
            public Builder success(boolean success) { this.success = success; return this; }
            public Builder message(String message) { this.message = message; return this; }

            public DiplomaticResult build() { return new DiplomaticResult(this); }
        }
    }

    /**
     * Faction diplomatic profile.
     */
    public static class FactionProfile {
        private final String name;
        private final int diplomacySkill; // 0-10, affects diplomatic action success
        private final int aggression; // 0-10, affects likelihood of hostile actions
        private final int tradeFocus; // 0-10, affects trade-related actions
        private final Map<String, Integer> relations; // faction name -> relation score

        private FactionProfile(Builder builder) {
            this.name = builder.name;
            this.diplomacySkill = Math.max(0, Math.min(10, builder.diplomacySkill));
            this.aggression = Math.max(0, Math.min(10, builder.aggression));
            this.tradeFocus = Math.max(0, Math.min(10, builder.tradeFocus));
            this.relations = new HashMap<>(builder.relations);
        }

        public String getName() { return name; }
        public int getDiplomacySkill() { return diplomacySkill; }
        public int getAggression() { return aggression; }
        public int getTradeFocus() { return tradeFocus; }
        public Map<String, Integer> getRelations() { return Collections.unmodifiableMap(relations); }

        public int getRelationWith(String otherFaction) {
            return relations.getOrDefault(otherFaction, 0);
        }

        public RelationType getRelationTypeWith(String otherFaction) {
            return RelationType.fromScore(getRelationWith(otherFaction));
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String name;
            private int diplomacySkill = 5;
            private int aggression = 5;
            private int tradeFocus = 5;
            private final Map<String, Integer> relations = new HashMap<>();

            public Builder name(String name) { this.name = name; return this; }
            public Builder diplomacySkill(int diplomacySkill) { this.diplomacySkill = diplomacySkill; return this; }
            public Builder aggression(int aggression) { this.aggression = aggression; return this; }
            public Builder tradeFocus(int tradeFocus) { this.tradeFocus = tradeFocus; return this; }
            public Builder relation(String faction, int score) { this.relations.put(faction, score); return this; }
            public Builder relations(Map<String, Integer> relations) { this.relations.putAll(relations); return this; }

            public FactionProfile build() {
                if (this.name == null) throw new IllegalArgumentException("Faction name is required");
                return new FactionProfile(this);
            }
        }
    }

    private final Map<String, FactionProfile> factions = new ConcurrentHashMap<>();
    private final Map<String, Treaty> treaties = new ConcurrentHashMap<>();
    // Service-level relations map: "factionA:factionB" -> score
    private final Map<String, Integer> relations = new ConcurrentHashMap<>();
    private final Random random = new Random();

    /**
     * Register a faction with the diplomacy service.
     */
    public void registerFaction(FactionProfile profile) {
        factions.put(profile.getName(), profile);
    }

    /**
     * Register a faction with default settings.
     */
    public void registerFaction(String name) {
        registerFaction(FactionProfile.builder().name(name).build());
    }

    /**
     * Get a faction's profile.
     */
    public FactionProfile getFaction(String name) {
        return factions.get(name);
    }

    /**
     * Get all registered factions.
     */
    public List<String> getAllFactions() {
        return new ArrayList<>(factions.keySet());
    }

    /**
     * Get the relation score between two factions.
     */
    public int getRelationScore(String factionA, String factionB) {
        return relations.getOrDefault(key(factionA, factionB), 0);
    }

    private String key(String a, String b) {
        return a.compareTo(b) < 0 ? a + ":" + b : b + ":" + a;
    }

    /**
     * Get the relation type between two factions.
     */
    public RelationType getRelationType(String factionA, String factionB) {
        return RelationType.fromScore(getRelationScore(factionA, factionB));
    }

    /**
     * Set the relation score between two factions.
     */
    public void setRelation(String factionA, String factionB, int score) {
        int clamped = Math.max(-100, Math.min(100, score));
        relations.put(key(factionA, factionB), clamped);
    }

    /**
     * Modify the relation score between two factions.
     */
    public void modifyRelation(String factionA, String factionB, int change) {
        int current = getRelationScore(factionA, factionB);
        setRelation(factionA, factionB, current + change);
    }

    /**
     * Perform a diplomatic action between two factions.
     */
    public DiplomaticResult performAction(String factionA, String factionB, DiplomaticAction action) {
        FactionProfile profileA = factions.get(factionA);
        FactionProfile profileB = factions.get(factionB);

        if (profileA == null || profileB == null) {
            return DiplomaticResult.builder()
                    .actionName(action.getName())
                    .factionA(factionA)
                    .factionB(factionB)
                    .success(false)
                    .message("One or both factions not found")
                    .build();
        }

        int relationBefore = profileA.getRelationWith(factionB);
        double successChance = 0.5 + (profileA.getDiplomacySkill() - profileB.getDiplomacySkill()) * 0.05;
        successChance = Math.max(0.1, Math.min(0.95, successChance));

        boolean success = random.nextDouble() < successChance;
        int change = action.getRelationChange();
        if (!success) {
            change = (int) (change * 0.5);
        }

        modifyRelation(factionA, factionB, change);
        int relationAfter = profileA.getRelationWith(factionB);

        String message;
        if (success) {
            message = factionA + " successfully " + action.getName().toLowerCase() + " with " + factionB;
        } else {
            message = factionA + "'s " + action.getName().toLowerCase() + " with " + factionB + " had limited effect";
        }

        return DiplomaticResult.builder()
                .actionName(action.getName())
                .factionA(factionA)
                .factionB(factionB)
                .relationBefore(relationBefore)
                .relationAfter(relationAfter)
                .success(success)
                .message(message)
                .build();
    }

    /**
     * Declare war between two factions.
     */
    public DiplomaticResult declareWar(String aggressor, String target) {
        DiplomaticResult result = performAction(aggressor, target, DiplomaticAction.DECLARATION_OF_WAR);
        setRelation(aggressor, target, Math.min(getRelationScore(aggressor, target), -75));
        return result;
    }

    /**
     * Offer peace between two warring factions.
     */
    public DiplomaticResult offerPeace(String offerer, String target) {
        return performAction(offerer, target, DiplomaticAction.PEACE_OFFER);
    }

    /**
     * Create a treaty between two factions.
     */
    public Treaty createTreaty(String factionA, String factionB, TreatyType type, int turns) {
        if (!factions.containsKey(factionA) || !factions.containsKey(factionB)) {
            throw new IllegalArgumentException("Both factions must be registered");
        }

        Treaty treaty = Treaty.builder()
                .factionA(factionA)
                .factionB(factionB)
                .type(type)
                .turnsTotal(turns)
                .build();

        treaties.put(treaty.getId(), treaty);
        modifyRelation(factionA, factionB, type.getRelationBonus());
        return treaty;
    }

    /**
     * Get all active treaties.
     */
    public List<Treaty> getActiveTreaties() {
        return treaties.values().stream()
                .filter(t -> !t.isExpired())
                .collect(Collectors.toList());
    }

    /**
     * Get treaties involving a specific faction.
     */
    public List<Treaty> getTreatiesForFaction(String faction) {
        return treaties.values().stream()
                .filter(t -> t.getFactionA().equals(faction) || t.getFactionB().equals(faction))
                .filter(t -> !t.isExpired())
                .collect(Collectors.toList());
    }

    /**
     * Check if two factions have an active treaty of a specific type.
     */
    public boolean hasTreaty(String factionA, String factionB, TreatyType type) {
        return treaties.values().stream()
                .filter(t -> !t.isExpired())
                .filter(t -> t.getType() == type)
                .anyMatch(t -> (t.getFactionA().equals(factionA) && t.getFactionB().equals(factionB))
                        || (t.getFactionA().equals(factionB) && t.getFactionB().equals(factionA)));
    }

    /**
     * Advance all treaties by one turn.
     */
    public void advanceTreatyTurns() {
        treaties.values().removeIf(Treaty::isExpired);
    }

    /**
     * Get diplomatic statistics for a faction.
     */
    public Map<String, Object> getDiplomacyStats(String faction) {
        FactionProfile profile = factions.get(faction);
        if (profile == null) {
            return Map.of("error", "Faction not found");
        }

        int allies = 0;
        int enemies = 0;
        int neutrals = 0;

        for (String other : getAllFactions()) {
            if (other.equals(faction)) continue;
            RelationType type = RelationType.fromScore(getRelationScore(faction, other));
            switch (type) {
                case FRIENDLY, ALLIED, VASSAL -> allies++;
                case HOSTILE, ENEMY -> enemies++;
                default -> neutrals++;
            }
        }

        return Map.of(
                "faction", faction,
                "diplomacySkill", profile.getDiplomacySkill(),
                "aggression", profile.getAggression(),
                "tradeFocus", profile.getTradeFocus(),
                "allies", allies,
                "enemies", enemies,
                "neutrals", neutrals,
                "activeTreaties", getTreatiesForFaction(faction).size()
        );
    }

    /**
     * Clear all diplomacy data.
     */
    public void clear() {
        factions.clear();
        treaties.clear();
        relations.clear();
    }
}
