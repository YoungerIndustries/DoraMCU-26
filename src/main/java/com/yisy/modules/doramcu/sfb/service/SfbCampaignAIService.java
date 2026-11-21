package com.yisy.modules.doramcu.sfb.service;
import com.yisy.modules.doramcu.sfb.model.SfbShield;
import com.yisy.modules.doramcu.sfb.model.SfbSSD;
import com.yisy.modules.doramcu.sfb.model.SfbCrewStationType;
import com.yisy.modules.doramcu.sfb.model.SfbCharacterAssignment;
import com.yisy.modules.doramcu.sfb.model.SfbEnergyAllocation;
import com.yisy.modules.doramcu.sfb.service.SfbDiplomacyService.FactionProfile;
import com.yisy.modules.doramcu.sfb.service.SfbDiplomacyService.RelationType;
import com.yisy.modules.doramcu.sfb.service.SfbDiplomacyService.TreatyType;
import com.yisy.modules.doramcu.sfb.service.SfbDiplomacyService.DiplomaticAction;
import com.yisy.modules.doramcu.sfb.service.SfbEconomyService.FactionEconomy;
import com.yisy.modules.doramcu.sfb.service.SfbEconomyService.ResourceType;
import com.yisy.modules.doramcu.sfb.service.SfbEconomyService.ShipClassCategory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Strategic campaign AI for SFB.
 * <p>
 * Uses {@link SfbDiplomacyService} and {@link SfbEconomyService} to make
 * high-level campaign decisions for AI-controlled factions, including
 * fleet deployment strategy, diplomatic actions, shipbuilding priorities,
 * and trade decisions.
 * </p>
 * <p>
 * The AI evaluates the current campaign state and generates actionable
 * recommendations based on the faction's personality traits (aggression,
 * diplomacy skill, trade focus) and current resource/diplomatic situation.
 * </p>
 */

@Service
public class SfbCampaignAIService {

    /**
     * Types of strategic actions the AI can take.
     */
    public enum StrategicAction {
        DEPLOY_FLEET("Deploy Fleet", "Move fleet to strategic position"),
        BUILD_SHIP("Build Ship", "Construct new ship at shipyard"),
        REPAIR_FLEET("Repair Fleet", "Repair damaged ships in dockyard"),
        DIPLOMATIC_OVERTURE("Diplomatic Overture", "Improve relations with another faction"),
        DECLARE_WAR("Declare War", "Formally declare war on a faction"),
        OFFER_PEACE("Offer Peace", "Propose peace terms to enemy"),
        SIGN_TREATY("Sign Treaty", "Propose a treaty with another faction"),
        TRADE_DEAL("Trade Deal", "Execute trade with another faction"),
        SPY_OPERATION("Spy Operation", "Conduct espionage against another faction"),
        EXPAND_ECONOMY("Expand Economy", "Invest in economic infrastructure"),
        RECONNAISSANCE("Reconnaissance", "Scout enemy positions"),
        DEFENSIVE_POSTURE("Defensive Posture", "Strengthen defenses");

        private final String name;
        private final String description;

        StrategicAction(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    /**
     * AI decision with priority and reasoning.
     */
    public static class AIDecision {
        private final StrategicAction action;
        private final int priority; // 1-10, higher = more urgent
        private final String target; // target faction, sector, or resource
        private final String reasoning;
        private final Map<String, Object> details;

        private AIDecision(Builder builder) {
            this.action = builder.action;
            this.priority = Math.max(1, Math.min(10, builder.priority));
            this.target = builder.target;
            this.reasoning = builder.reasoning;
            this.details = Collections.unmodifiableMap(new HashMap<>(builder.details));
        }

        public StrategicAction getAction() { return action; }
        public int getPriority() { return priority; }
        public String getTarget() { return target; }
        public String getReasoning() { return reasoning; }
        public Map<String, Object> getDetails() { return details; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private StrategicAction action;
            private int priority = 5;
            private String target;
            private String reasoning;
            private final Map<String, Object> details = new HashMap<>();

            public Builder action(StrategicAction action) { this.action = action; return this; }
            public Builder priority(int priority) { this.priority = priority; return this; }
            public Builder target(String target) { this.target = target; return this; }
            public Builder reasoning(String reasoning) { this.reasoning = reasoning; return this; }
            public Builder detail(String key, Object value) { this.details.put(key, value); return this; }

            public AIDecision build() { return new AIDecision(this); }
        }
    }

    /**
     * Campaign state summary for AI evaluation.
     */
    public static class CampaignState {
        private final String factionName;
        private final int fleetCount;
        private final int totalShips;
        private final int enemies;
        private final int allies;
        private final int credits;
        private final int materials;
        private final int fuel;
        private final int manpower;
        private final List<String> threats;
        private final List<String> opportunities;

        private CampaignState(Builder builder) {
            this.factionName = builder.factionName;
            this.fleetCount = builder.fleetCount;
            this.totalShips = builder.totalShips;
            this.enemies = builder.enemies;
            this.allies = builder.allies;
            this.credits = builder.credits;
            this.materials = builder.materials;
            this.fuel = builder.fuel;
            this.manpower = builder.manpower;
            this.threats = new ArrayList<>(builder.threats);
            this.opportunities = new ArrayList<>(builder.opportunities);
        }

        public String getFactionName() { return factionName; }
        public int getFleetCount() { return fleetCount; }
        public int getTotalShips() { return totalShips; }
        public int getEnemies() { return enemies; }
        public int getAllies() { return allies; }
        public int getCredits() { return credits; }
        public int getMaterials() { return materials; }
        public int getFuel() { return fuel; }
        public int getManpower() { return manpower; }
        public List<String> getThreats() { return threats; }
        public List<String> getOpportunities() { return opportunities; }

        public static Builder builder(String factionName) { return new Builder(factionName); }

        public static class Builder {
            private final String factionName;
            private int fleetCount;
            private int totalShips;
            private int enemies;
            private int allies;
            private int credits;
            private int materials;
            private int fuel;
            private int manpower;
            private final List<String> threats = new ArrayList<>();
            private final List<String> opportunities = new ArrayList<>();

            public Builder(String factionName) { this.factionName = factionName; }

            public Builder fleetCount(int fleetCount) { this.fleetCount = fleetCount; return this; }
            public Builder totalShips(int totalShips) { this.totalShips = totalShips; return this; }
            public Builder enemies(int enemies) { this.enemies = enemies; return this; }
            public Builder allies(int allies) { this.allies = allies; return this; }
            public Builder credits(int credits) { this.credits = credits; return this; }
            public Builder materials(int materials) { this.materials = materials; return this; }
            public Builder fuel(int fuel) { this.fuel = fuel; return this; }
            public Builder manpower(int manpower) { this.manpower = manpower; return this; }
            public Builder threat(String threat) { this.threats.add(threat); return this; }
            public Builder threats(List<String> threats) { this.threats.addAll(threats); return this; }
            public Builder opportunity(String opportunity) { this.opportunities.add(opportunity); return this; }
            public Builder opportunities(List<String> opportunities) { this.opportunities.addAll(opportunities); return this; }

            public CampaignState build() { return new CampaignState(this); }
        }
    }

    private final SfbDiplomacyService diplomacy;
    private final SfbEconomyService economy;
    private final Random random = new Random();

    public SfbCampaignAIService(SfbDiplomacyService diplomacy, SfbEconomyService economy) {
        this.diplomacy = diplomacy;
        this.economy = economy;
    }

    /**
     * Evaluate the current campaign state for a faction.
     */
    public CampaignState evaluateCampaignState(String factionName) {
        FactionProfile profile = diplomacy.getFaction(factionName);
        FactionEconomy econ = economy.getEconomy(factionName);

        if (profile == null || econ == null) {
            return CampaignState.builder(factionName).build();
        }

        int enemies = 0;
        int allies = 0;
        List<String> threats = new ArrayList<>();
        List<String> opportunities = new ArrayList<>();

        for (String otherFaction : diplomacy.getAllFactions()) {
            if (otherFaction.equals(factionName)) continue;

            RelationType relation = diplomacy.getRelationType(factionName, otherFaction);
            switch (relation) {
                case HOSTILE, ENEMY -> {
                    enemies++;
                    threats.add(otherFaction + " is hostile (" + relation + ")");
                }
                case UNFRIENDLY -> {
                    threats.add(otherFaction + " is unfriendly");
                }
                case FRIENDLY, ALLIED, VASSAL -> {
                    allies++;
                    opportunities.add(otherFaction + " is allied (" + relation + ")");
                }
                default -> {
                    opportunities.add(otherFaction + " is neutral ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â diplomacy possible");
                }
            }
        }

        // Economic threats
        if (econ.getResource(ResourceType.CREDITS) < 100) {
            threats.add("Low credits: " + econ.getResource(ResourceType.CREDITS));
        }
        if (econ.getResource(ResourceType.MATERIALS) < 50) {
            threats.add("Low materials: " + econ.getResource(ResourceType.MATERIALS));
        }
        if (econ.getResource(ResourceType.FUEL) < 20) {
            threats.add("Low fuel: " + econ.getResource(ResourceType.FUEL));
        }

        // Economic opportunities
        if (econ.getResource(ResourceType.CREDITS) > 1000) {
            opportunities.add("Strong credit reserves: " + econ.getResource(ResourceType.CREDITS));
        }
        if (econ.getTurnsSinceLastTrade() > 5) {
            opportunities.add("No recent trade ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â trade deals available");
        }

        return CampaignState.builder(factionName)
                .enemies(enemies)
                .allies(allies)
                .credits(econ.getResource(ResourceType.CREDITS))
                .materials(econ.getResource(ResourceType.MATERIALS))
                .fuel(econ.getResource(ResourceType.FUEL))
                .manpower(econ.getResource(ResourceType.MANPOWER))
                .threats(threats)
                .opportunities(opportunities)
                .build();
    }

    /**
     * Generate strategic decisions for an AI faction.
     * Returns decisions sorted by priority (highest first).
     */
    public List<AIDecision> generateDecisions(String factionName) {
        List<AIDecision> decisions = new ArrayList<>();
        FactionProfile profile = diplomacy.getFaction(factionName);
        FactionEconomy econ = economy.getEconomy(factionName);

        if (profile == null || econ == null) {
            return decisions;
        }

        CampaignState state = evaluateCampaignState(factionName);

        // Decision 1: If under attack, prioritize defense
        if (state.getEnemies() > 0) {
            int priority = Math.min(10, 5 + state.getEnemies());
            decisions.add(AIDecision.builder()
                    .action(StrategicAction.DEFENSIVE_POSTURE)
                    .priority(priority)
                    .target(factionName)
                    .reasoning(state.getEnemies() + " enemy faction(s) detected ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â strengthen defenses")
                    .detail("enemyCount", state.getEnemies())
                    .build());

            // Consider declaring war on hostile neighbors
            for (String enemy : diplomacy.getAllFactions()) {
                if (enemy.equals(factionName)) continue;
                RelationType relation = diplomacy.getRelationType(factionName, enemy);
                if (relation == RelationType.HOSTILE && profile.getAggression() >= 7) {
                    decisions.add(AIDecision.builder()
                            .action(StrategicAction.DECLARE_WAR)
                            .priority(8)
                            .target(enemy)
                            .reasoning("High aggression (" + profile.getAggression() + ") ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â preemptive strike against " + enemy)
                            .build());
                }
            }
        }

        // Decision 2: Diplomatic overtures if diplomacy skill is high
        if (profile.getDiplomacySkill() >= 6) {
            for (String other : diplomacy.getAllFactions()) {
                if (other.equals(factionName)) continue;
                RelationType relation = diplomacy.getRelationType(factionName, other);
                if (relation == RelationType.NEUTRAL || relation == RelationType.UNFRIENDLY) {
                    decisions.add(AIDecision.builder()
                            .action(StrategicAction.DIPLOMATIC_OVERTURE)
                            .priority(6)
                            .target(other)
                            .reasoning("Diplomacy skill " + profile.getDiplomacySkill() + " ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â improve relations with " + other)
                            .build());
                }
            }
        }

        // Decision 3: Peace offers if aggression is low and at war
        if (profile.getAggression() <= 3) {
            for (String enemy : diplomacy.getAllFactions()) {
                if (enemy.equals(factionName)) continue;
                RelationType relation = diplomacy.getRelationType(factionName, enemy);
                if (relation == RelationType.HOSTILE || relation == RelationType.ENEMY) {
                    decisions.add(AIDecision.builder()
                            .action(StrategicAction.OFFER_PEACE)
                            .priority(7)
                            .target(enemy)
                            .reasoning("Low aggression (" + profile.getAggression() + ") ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â seek peace with " + enemy)
                            .build());
                }
            }
        }

        // Decision 4: Trade deals if trade focus is high
        if (profile.getTradeFocus() >= 6) {
            for (String other : diplomacy.getAllFactions()) {
                if (other.equals(factionName)) continue;
                RelationType relation = diplomacy.getRelationType(factionName, other);
                if (relation == RelationType.FRIENDLY || relation == RelationType.ALLIED) {
                    decisions.add(AIDecision.builder()
                            .action(StrategicAction.TRADE_DEAL)
                            .priority(5)
                            .target(other)
                            .reasoning("Trade focus " + profile.getTradeFocus() + " ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â trade with allied " + other)
                            .build());
                }
            }
        }

        // Decision 5: Shipbuilding if resources allow
        for (ShipClassCategory category : ShipClassCategory.values()) {
            if (category == ShipClassCategory.STATION) continue;
            int affordable = economy.shipsAffordable(factionName, category);
            if (affordable >= 2) {
                decisions.add(AIDecision.builder()
                        .action(StrategicAction.BUILD_SHIP)
                        .priority(4)
                        .target(category.name())
                        .reasoning("Can afford " + affordable + " " + category.name() + " ships")
                        .detail("category", category.name())
                        .detail("affordable", affordable)
                        .build());
                break; // Suggest the largest affordable ship type
            }
        }

        // Decision 6: Treaty proposals with allies
        if (state.getAllies() > 0) {
            for (String ally : diplomacy.getAllFactions()) {
                if (ally.equals(factionName)) continue;
                RelationType relation = diplomacy.getRelationType(factionName, ally);
                if (relation == RelationType.FRIENDLY && !diplomacy.hasTreaty(factionName, ally, TreatyType.MUTUAL_DEFENSE)) {
                    decisions.add(AIDecision.builder()
                            .action(StrategicAction.SIGN_TREATY)
                            .priority(5)
                            .target(ally)
                            .reasoning("Strengthen alliance with " + ally + " via mutual defense pact")
                            .detail("treatyType", TreatyType.MUTUAL_DEFENSE.name())
                            .build());
                }
            }
        }

        // Decision 7: Economy expansion if resources are strong
        if (econ.getResource(ResourceType.CREDITS) > 500 && econ.getResource(ResourceType.MATERIALS) > 200) {
            decisions.add(AIDecision.builder()
                    .action(StrategicAction.EXPAND_ECONOMY)
                    .priority(3)
                    .target(factionName)
                    .reasoning("Strong resources ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â invest in economic growth")
                    .build());
        }

        // Sort by priority (highest first)
        return decisions.stream()
                .sorted(Comparator.comparingInt(AIDecision::getPriority).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Execute the highest-priority decision for a faction.
     */
    public Optional<AIDecision> executeTopDecision(String factionName) {
        List<AIDecision> decisions = generateDecisions(factionName);
        if (decisions.isEmpty()) return Optional.empty();

        AIDecision decision = decisions.get(0);

        // Execute based on action type
        switch (decision.getAction()) {
            case DIPLOMATIC_OVERTURE -> {
                if (decision.getTarget() != null) {
                    diplomacy.performAction(factionName, decision.getTarget(), DiplomaticAction.IMPROVE_RELATIONS);
                }
            }
            case DECLARE_WAR -> {
                if (decision.getTarget() != null) {
                    diplomacy.declareWar(factionName, decision.getTarget());
                }
            }
            case OFFER_PEACE -> {
                if (decision.getTarget() != null) {
                    diplomacy.offerPeace(factionName, decision.getTarget());
                }
            }
            case BUILD_SHIP -> {
                if (decision.getTarget() != null) {
                    try {
                        ShipClassCategory category = ShipClassCategory.valueOf(decision.getTarget());
                        economy.buildShip(factionName, category);
                    } catch (IllegalArgumentException ignored) {
                        // Invalid ship category
                    }
                }
            }
            default -> {
                // Other actions are recommendations only
            }
        }

        return Optional.of(decision);
    }

    /**
     * Execute all decisions above a minimum priority threshold.
     */
    public List<AIDecision> executeDecisions(String factionName, int minPriority) {
        List<AIDecision> decisions = generateDecisions(factionName);
        List<AIDecision> executed = new ArrayList<>();

        for (AIDecision decision : decisions) {
            if (decision.getPriority() < minPriority) break;

            switch (decision.getAction()) {
                case DIPLOMATIC_OVERTURE -> {
                    if (decision.getTarget() != null) {
                        diplomacy.performAction(factionName, decision.getTarget(), DiplomaticAction.IMPROVE_RELATIONS);
                        executed.add(decision);
                    }
                }
                case DECLARE_WAR -> {
                    if (decision.getTarget() != null) {
                        diplomacy.declareWar(factionName, decision.getTarget());
                        executed.add(decision);
                    }
                }
                case OFFER_PEACE -> {
                    if (decision.getTarget() != null) {
                        diplomacy.offerPeace(factionName, decision.getTarget());
                        executed.add(decision);
                    }
                }
                case BUILD_SHIP -> {
                    if (decision.getTarget() != null) {
                        try {
                            ShipClassCategory category = ShipClassCategory.valueOf(decision.getTarget());
                            if (economy.buildShip(factionName, category)) {
                                executed.add(decision);
                            }
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
                default -> {
                    // Recommendations only
                }
            }
        }

        return executed;
    }

    /**
     * Get a summary of the AI's strategic assessment.
     */
    public Map<String, Object> getStrategicAssessment(String factionName) {
        CampaignState state = evaluateCampaignState(factionName);
        List<AIDecision> decisions = generateDecisions(factionName);

        return Map.of(
                "faction", factionName,
                "campaignState", Map.of(
                        "fleets", state.getFleetCount(),
                        "ships", state.getTotalShips(),
                        "enemies", state.getEnemies(),
                        "allies", state.getAllies(),
                        "credits", state.getCredits(),
                        "materials", state.getMaterials(),
                        "fuel", state.getFuel(),
                        "manpower", state.getManpower()
                ),
                "threats", state.getThreats(),
                "opportunities", state.getOpportunities(),
                "decisions", decisions.stream()
                        .map(d -> Map.of(
                                "action", d.getAction().getName(),
                                "priority", d.getPriority(),
                                "target", d.getTarget(),
                                "reasoning", d.getReasoning()
                        ))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Advance the campaign by one turn for all AI factions.
     */
    public void advanceCampaignTurn() {
        // Advance economy for all factions
        economy.advanceTurn();

        // Advance treaty turns
        diplomacy.advanceTreatyTurns();

        // Execute AI decisions for all factions
        for (String faction : diplomacy.getAllFactions()) {
            executeTopDecision(faction);
        }
    }

    /**
     * Get all strategic actions available.
     */
    public List<Map<String, String>> getAllActions() {
        List<Map<String, String>> actions = new ArrayList<>();
        for (StrategicAction action : StrategicAction.values()) {
            actions.add(Map.of(
                    "name", action.getName(),
                    "description", action.getDescription()
            ));
        }
        return actions;
    }
}
