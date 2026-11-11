package com.yisy.modules.doramcu.hex.planet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrates the Business Tycoon campaign mode.
 * <p>
 * This is the central manager that coordinates all aspects of the transportation
 * corporation campaign:
 * <ul>
 *   <li>Manages the player corporation and AI rival corporations</li>
 *   <li>Processes turns: cargo delivery, revenue, contract execution</li>
 *   <li>Tracks objectives and campaign progress</li>
 *   <li>Calculates market share and determines win/lose conditions</li>
 *   <li>Issues periodic contracts via PlanetContractManager</li>
 *   <li>Triggers rank progression for corporations</li>
 * </ul>
 * <p>
 * Follows the manager delegation pattern used by {@code Game.java}.
 * </p>
 */
public class TransportCampaignManager {

    /** Unique campaign ID */
    private final String campaignId;

    /** Campaign name */
    private final String campaignName;

    /** The player's corporation */
    private TransportCorporation playerCorporation;

    /** AI rival corporations */
    private final Map<String, TransportCorporation> aiCorporations;

    /** AI strategies for each rival */
    private final Map<String, AiCorporationStrategy> aiStrategies;

    /** Trade route manager */
    private final TradeRouteManager tradeRouteManager;

    /** Market share tracker */
    private final MarketShareTracker marketShareTracker;

    /** Planet surface builder for managing settlements, industries, stations, and vehicles */
    private final PlanetSurfaceBuilder surfaceBuilder;

    /** Contract manager for issuing and executing contracts */
    private final PlanetContractManager contractManager;

    /** Event manager for campaign events */
    private final PlanetEventManager eventManager;

    /** 4X AI empires that control the galaxy */
    private final Map<Integer, GalaxyOverlordAI> galaxyOverlords;

    /** Tracks player reputation with each AI empire */
    private final EmpireReputationTracker reputationTracker;

    /** RPG quest system for narrative events */
    private final RpgQuestSystem questSystem;

    /** List of campaign objectives */
    private final List<CampaignObjective> objectives;

    /** Completed objectives */
    private final List<CampaignObjective> completedObjectives;

    /** Current turn number */
    private int currentTurn;

    /** Maximum number of turns in the campaign */
    private int maxTurns;

    /** Whether the campaign has ended */
    private boolean campaignEnded;

    /** Campaign result (null until ended) */
    private CampaignResult campaignResult;

    /**
     * Campaign result when the campaign ends.
     */
    public enum CampaignResult {
        /** Player won by achieving market dominance */
        VICTORY,
        /** Player lost — ran out of credits or turns expired */
        DEFEAT,
        /** Player lost — bankruptcy */
        BANKRUPTCY
    }

    /**
     * Creates a new campaign manager using the builder pattern.
     *
     * @param builder the builder
     */
    private TransportCampaignManager(Builder builder) {
        this.campaignId = UUID.randomUUID().toString();
        this.campaignName = builder.campaignName;
        this.playerCorporation = builder.playerCorporation;
        this.aiCorporations = new ConcurrentHashMap<>();
        this.aiStrategies = new HashMap<>();
        this.tradeRouteManager = new TradeRouteManager();
        this.marketShareTracker = new MarketShareTracker(
                builder.winMarketSharePercentage, builder.winSustainTurns);
        this.surfaceBuilder = new DefaultPlanetSurfaceBuilder();
        this.contractManager = new PlanetContractManager(this.surfaceBuilder);
        this.eventManager = new PlanetEventManager();
        this.galaxyOverlords = new ConcurrentHashMap<>();
        this.reputationTracker = new EmpireReputationTracker();
        this.questSystem = new RpgQuestSystem();
        this.objectives = new ArrayList<>(builder.objectives);
        this.completedObjectives = new ArrayList<>();
        this.currentTurn = 0;
        this.maxTurns = builder.maxTurns;
        this.campaignEnded = false;
        this.campaignResult = null;

        // Wire up dependencies
        this.tradeRouteManager.setMarketShareTracker(this.marketShareTracker);
        this.tradeRouteManager.setEventManager(this.eventManager);

        // Add AI rivals
        for (Map.Entry<String, AiCorporationStrategy> entry : builder.aiRivals.entrySet()) {
            this.aiStrategies.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns the campaign ID.
     *
     * @return campaign ID
     */
    public String getCampaignId() {
        return campaignId;
    }

    /**
     * Returns the campaign name.
     *
     * @return campaign name
     */
    public String getCampaignName() {
        return campaignName;
    }

    /**
     * Returns the player's corporation.
     *
     * @return player corporation
     */
    public TransportCorporation getPlayerCorporation() {
        return playerCorporation;
    }

    /**
     * Returns all AI rival corporations.
     *
     * @return unmodifiable list of AI corporations
     */
    public List<TransportCorporation> getAiCorporations() {
        return Collections.unmodifiableList(new ArrayList<>(aiCorporations.values()));
    }

    /**
     * Returns all corporations (player + AI).
     *
     * @return list of all corporations
     */
    public List<TransportCorporation> getAllCorporations() {
        List<TransportCorporation> all = new ArrayList<>();
        all.add(playerCorporation);
        all.addAll(aiCorporations.values());
        return all;
    }

    /**
     * Returns a corporation by ID.
     *
     * @param corporationId the corporation ID
     * @return the corporation, or empty if not found
     */
    public Optional<TransportCorporation> getCorporation(String corporationId) {
        if (playerCorporation.getId().equals(corporationId)) {
            return Optional.of(playerCorporation);
        }
        return Optional.ofNullable(aiCorporations.get(corporationId));
    }

    /**
     * Returns the trade route manager.
     *
     * @return trade route manager
     */
    public TradeRouteManager getTradeRouteManager() {
        return tradeRouteManager;
    }

    /**
     * Returns the market share tracker.
     *
     * @return market share tracker
     */
    public MarketShareTracker getMarketShareTracker() {
        return marketShareTracker;
    }

    /**
     * Returns the contract manager.
     *
     * @return contract manager
     */
    public PlanetContractManager getContractManager() {
        return contractManager;
    }

    /**
     * Returns the planet surface builder.
     *
     * @return surface builder
     */
    public PlanetSurfaceBuilder getSurfaceBuilder() {
        return surfaceBuilder;
    }

    /**
     * Returns the event manager.
     *
     * @return event manager
     */
    public PlanetEventManager getEventManager() {
        return eventManager;
    }

    /**
     * Returns the 4X AI empires.
     *
     * @return map of empire ID to GalaxyOverlordAI
     */
    public Map<Integer, GalaxyOverlordAI> getGalaxyOverlords() {
        return Collections.unmodifiableMap(galaxyOverlords);
    }

    /**
     * Returns the reputation tracker.
     *
     * @return reputation tracker
     */
    public EmpireReputationTracker getReputationTracker() {
        return reputationTracker;
    }

    /**
     * Returns the quest system.
     *
     * @return quest system
     */
    public RpgQuestSystem getQuestSystem() {
        return questSystem;
    }

    /**
     * Returns the current campaign objectives.
     *
     * @return list of active objectives
     */
    public List<CampaignObjective> getObjectives() {
        return Collections.unmodifiableList(objectives);
    }

    /**
     * Returns the completed objectives.
     *
     * @return list of completed objectives
     */
    public List<CampaignObjective> getCompletedObjectives() {
        return Collections.unmodifiableList(completedObjectives);
    }

    /**
     * Returns the current turn number.
     *
     * @return current turn
     */
    public int getCurrentTurn() {
        return currentTurn;
    }

    /**
     * Returns the maximum number of turns.
     *
     * @return max turns
     */
    public int getMaxTurns() {
        return maxTurns;
    }

    /**
     * Returns whether the campaign has ended.
     *
     * @return true if ended
     */
    public boolean isCampaignEnded() {
        return campaignEnded;
    }

    /**
     * Returns the campaign result, or null if not ended.
     *
     * @return campaign result
     */
    public CampaignResult getCampaignResult() {
        return campaignResult;
    }

    /**
     * Processes one turn of the campaign.
     * This is the main game loop method that:
     * <ol>
     *   <li>Processes 4X galaxy events (AI empire expansion, war, diplomacy)</li>
     *   <li>Generates RPG quests from AI empires</li>
     *   <li>Processes trade routes (cargo delivery, revenue)</li>
     *   <li>Processes AI rival actions</li>
     *   <li>Processes contract countdowns</li>
     *   <li>Updates market share</li>
     *   <li>Checks objectives progress</li>
     *   <li>Processes RPG quest countdowns</li>
     *   <li>Checks rank progression</li>
     *   <li>Checks win/lose conditions</li>
     * </ol>
     *
     * @return turn summary with key metrics
     */
    public TurnSummary processTurn() {
        if (campaignEnded) {
            throw new IllegalStateException("Campaign has already ended");
        }

        currentTurn++;

        // Step 1: Process 4X galaxy events (AI empire expansion, war, diplomacy)
        processGalaxyOverlordTurns();

        // Step 2: Generate RPG quests from AI empires
        questSystem.generateQuests(galaxyOverlords, currentTurn);

        // Step 3: Process trade routes
        long totalCargoValue = tradeRouteManager.processTurn();

        // Step 4: Distribute revenue to corporations
        distributeRevenue();

        // Step 5: Process AI rival actions
        processAiTurns();

        // Step 6: Process contract countdowns (no-op without planet context)
        // contractManager.processTurns() requires a Planet parameter

        // Step 7: Update market share
        marketShareTracker.finalizeTurn();

        // Step 8: Check objectives
        checkObjectives();

        // Step 9: Process RPG quest countdowns
        questSystem.processTurn(playerCorporation, reputationTracker);

        // Step 10: Check rank progression
        checkRankProgression();

        // Step 11: Check win/lose conditions
        checkCampaignEnd();

        // Dispatch turn event
        eventManager.dispatch(new PlanetEvent(
                PlanetEventType.TURN_COMPLETED, System.nanoTime(),
                "turn-" + currentTurn,
                "Turn " + currentTurn + " completed. Cargo value: " + totalCargoValue
        ));

        return new TurnSummary(
                currentTurn,
                totalCargoValue,
                marketShareTracker.getMarketSharePercentage(playerCorporation.getId()),
                playerCorporation.getCredits()
        );
    }

    /**
     * Distributes revenue from trade routes to owning corporations.
     */
    private void distributeRevenue() {
        for (CampaignTradeRoute route : tradeRouteManager.getAllRoutes()) {
            if (route.isActive()) {
                getCorporation(route.getOwnerCorporationId()).ifPresent(corp -> {
                    int netProfit = route.getNetProfitPerTurn();
                    if (netProfit > 0) {
                        corp.addCredits(netProfit);
                    }
                });
            }
        }
    }

    /**
     * Processes one turn for all AI rival corporations.
     */
    private void processAiTurns() {
        // Get all available contracts across all planets (using planetId 0 as default)
        List<PlanetContract> availableContracts = contractManager.getAvailableContracts(0);

        for (Map.Entry<String, TransportCorporation> entry : aiCorporations.entrySet()) {
            String corpId = entry.getKey();
            TransportCorporation corp = entry.getValue();
            AiCorporationStrategy strategy = aiStrategies.get(corpId);

            if (strategy != null) {
                List<AiCorporationStrategy.AiAction> actions = strategy.processTurn(
                        corp, availableContracts, contractManager);

                // Dispatch AI action event
                for (AiCorporationStrategy.AiAction action : actions) {
                    eventManager.dispatch(new PlanetEvent(
                            PlanetEventType.CONTRACT_ACCEPTED, System.nanoTime(),
                            corpId,
                            "AI " + corp.getName() + ": " + action.description
                    ));
                }
            }
        }
    }

    /**
     * Processes turns for all 4X AI empires (Galaxy Overlords).
     * Each empire makes decisions about expansion, war, diplomacy, and trade policy.
     */
    private void processGalaxyOverlordTurns() {
        for (GalaxyOverlordAI overlord : galaxyOverlords.values()) {
            overlord.updateTradePolicy();
            if (currentTurn % 5 == 0) {
                overlord.issueNewContracts();
            }
        }
    }

    /**
     * Adds a 4X AI empire (Galaxy Overlord) to the campaign.
     *
     * @param empireId unique empire ID
     * @param empireName display name
     * @param personality the AI personality type
     * @return the created GalaxyOverlordAI
     */
    public GalaxyOverlordAI addGalaxyOverlord(int empireId, String empireName,
                                                GalaxyOverlordAI.AiPersonality personality) {
        GalaxyOverlordAI overlord = new GalaxyOverlordAI(
                empireId, empireName, null, 5, personality);
        galaxyOverlords.put(empireId, overlord);
        // Initialize reputation with this empire
        reputationTracker.setReputation(empireId, 20);
        return overlord;
    }

    /**
     * Adds a 4X AI empire with a random personality.
     *
     * @param empireId unique empire ID
     * @param empireName display name
     * @return the created GalaxyOverlordAI
     */
    public GalaxyOverlordAI addGalaxyOverlord(int empireId, String empireName) {
        GalaxyOverlordAI.AiPersonality[] personalities = GalaxyOverlordAI.AiPersonality.values();
        java.util.Random rand = new java.util.Random();
        GalaxyOverlordAI.AiPersonality personality = personalities[rand.nextInt(personalities.length)];
        return addGalaxyOverlord(empireId, empireName, personality);
    }

    /**
     * Checks and updates objective progress.
     */
    private void checkObjectives() {
        for (int i = objectives.size() - 1; i >= 0; i--) {
            CampaignObjective objective = objectives.get(i);
            if (objective.isCompleted()) {
                // Award rewards
                playerCorporation.addCredits(objective.getRewardCredits());
                playerCorporation.addXp(objective.getRewardXp());

                // Move to completed
                objectives.remove(i);
                completedObjectives.add(objective);

                eventManager.dispatch(new PlanetEvent(
                        PlanetEventType.OBJECTIVE_COMPLETED, System.nanoTime(),
                        objective.getId(),
                        "Objective completed: " + objective.getTitle()
                ));
            }
        }
    }

    /**
     * Checks for rank progression for all corporations.
     */
    private void checkRankProgression() {
        for (TransportCorporation corp : getAllCorporations()) {
            CorporationRank currentRank = corp.getRank();
            CorporationRank expectedRank = CorporationRank.fromCredits((int) corp.getCredits());
            if (expectedRank.ordinal() > currentRank.ordinal()) {
                corp.setRank(expectedRank);
                eventManager.dispatch(new PlanetEvent(
                        PlanetEventType.RANK_UP, System.nanoTime(),
                        corp.getId(),
                        corp.getName() + " promoted to " + expectedRank.getTitle()
                ));
            }
        }
    }

    /**
     * Checks win/lose conditions and ends the campaign if applicable.
     */
    private void checkCampaignEnd() {
        // Win condition: market share dominance
        if (marketShareTracker.hasWon(playerCorporation.getId())) {
            endCampaign(CampaignResult.VICTORY);
            return;
        }

        // Lose condition: bankruptcy
        if (playerCorporation.getCredits() <= 0) {
            endCampaign(CampaignResult.BANKRUPTCY);
            return;
        }

        // Lose condition: turns expired
        if (currentTurn >= maxTurns) {
            endCampaign(CampaignResult.DEFEAT);
            return;
        }
    }

    /**
     * Ends the campaign with the given result.
     *
     * @param result the campaign result
     */
    private void endCampaign(CampaignResult result) {
        this.campaignEnded = true;
        this.campaignResult = result;

        eventManager.dispatch(new PlanetEvent(
                PlanetEventType.CAMPAIGN_ENDED, System.nanoTime(),
                campaignId,
                "Campaign ended with result: " + result
        ));
    }

    /**
     * Adds an AI rival corporation to the campaign.
     *
     * @param name        the rival's corporation name
     * @param personality the AI personality
     * @param seed        random seed for the AI
     * @return the created AI corporation
     */
    public TransportCorporation addAiRival(String name, AiCorporationStrategy.AiPersonality personality, long seed) {
        TransportCorporation corp = new TransportCorporation(name, "ai-" + UUID.randomUUID().toString(), 1000);
        AiCorporationStrategy strategy = new AiCorporationStrategy(personality, seed);
        aiCorporations.put(corp.getId(), corp);
        aiStrategies.put(corp.getId(), strategy);
        return corp;
    }

    /**
     * Updates the progress of a campaign objective.
     *
     * @param objectiveId the objective ID
     * @param amount      the progress amount
     * @return true if the objective was completed
     */
    public boolean updateObjectiveProgress(String objectiveId, int amount) {
        for (CampaignObjective objective : objectives) {
            if (objective.getId().equals(objectiveId)) {
                return objective.addProgress(amount);
            }
        }
        return false;
    }

    /**
     * Returns the market share leaderboard.
     *
     * @return list of market share entries
     */
    public List<MarketShareTracker.CorporationMarketShare> getMarketShareLeaderboard() {
        return marketShareTracker.getMarketShareLeaderboard();
    }

    /**
     * Returns a summary of the current campaign state.
     *
     * @return campaign state summary
     */
    public CampaignStateSummary getCampaignStateSummary() {
        return new CampaignStateSummary(
                campaignName,
                currentTurn,
                maxTurns,
                campaignEnded,
                campaignResult,
                playerCorporation.getRank().getTitle(),
                playerCorporation.getCredits(),
                marketShareTracker.getMarketSharePercentage(playerCorporation.getId()),
                objectives.size(),
                completedObjectives.size(),
                aiCorporations.size(),
                tradeRouteManager.getRouteCount()
        );
    }

    /**
     * Summary of a single turn's results.
     */
    public static class TurnSummary {
        public final int turn;
        public final long totalCargoValue;
        public final double playerMarketShare;
        public final long playerCredits;

        TurnSummary(int turn, long totalCargoValue, double playerMarketShare, long playerCredits) {
            this.turn = turn;
            this.totalCargoValue = totalCargoValue;
            this.playerMarketShare = playerMarketShare;
            this.playerCredits = playerCredits;
        }

        @Override
        public String toString() {
            return String.format("Turn %d: Cargo=%d, MarketShare=%.1f%%, Credits=%d",
                    turn, totalCargoValue, playerMarketShare, playerCredits);
        }
    }

    /**
     * Summary of the current campaign state.
     */
    public static class CampaignStateSummary {
        public final String campaignName;
        public final int currentTurn;
        public final int maxTurns;
        public final boolean ended;
        public final CampaignResult result;
        public final String playerRank;
        public final long playerCredits;
        public final double playerMarketShare;
        public final int activeObjectives;
        public final int completedObjectives;
        public final int aiRivalCount;
        public final int totalRoutes;

        CampaignStateSummary(String campaignName, int currentTurn, int maxTurns,
                              boolean ended, CampaignResult result,
                              String playerRank, long playerCredits,
                              double playerMarketShare, int activeObjectives,
                              int completedObjectives, int aiRivalCount, int totalRoutes) {
            this.campaignName = campaignName;
            this.currentTurn = currentTurn;
            this.maxTurns = maxTurns;
            this.ended = ended;
            this.result = result;
            this.playerRank = playerRank;
            this.playerCredits = playerCredits;
            this.playerMarketShare = playerMarketShare;
            this.activeObjectives = activeObjectives;
            this.completedObjectives = completedObjectives;
            this.aiRivalCount = aiRivalCount;
            this.totalRoutes = totalRoutes;
        }
    }

    /**
     * Builder for creating a TransportCampaignManager.
     */
    public static class Builder {
        private String campaignName = "Business Tycoon";
        private TransportCorporation playerCorporation;
        private final Map<String, AiCorporationStrategy> aiRivals = new HashMap<>();
        private final List<CampaignObjective> objectives = new ArrayList<>();
        private int maxTurns = 100;
        private int winMarketSharePercentage = 60;
        private int winSustainTurns = 3;

        /**
         * Sets the campaign name.
         *
         * @param name the campaign name
         * @return this builder
         */
        public Builder campaignName(String name) {
            this.campaignName = Objects.requireNonNull(name);
            return this;
        }

        /**
         * Sets the player's corporation.
         *
         * @param corporation the player corporation
         * @return this builder
         */
        public Builder playerCorporation(TransportCorporation corporation) {
            this.playerCorporation = Objects.requireNonNull(corporation);
            return this;
        }

        /**
         * Adds an AI rival corporation.
         *
         * @param name        the rival's name
         * @param personality the AI personality
         * @param seed        random seed
         * @return this builder
         */
        public Builder addAiRival(String name, AiCorporationStrategy.AiPersonality personality, long seed) {
            TransportCorporation corp = new TransportCorporation(name, "ai-" + UUID.randomUUID().toString(), 1000);
            aiRivals.put(corp.getId(), new AiCorporationStrategy(personality, seed));
            return this;
        }

        /**
         * Adds a campaign objective.
         *
         * @param objective the objective
         * @return this builder
         */
        public Builder addObjective(CampaignObjective objective) {
            this.objectives.add(Objects.requireNonNull(objective));
            return this;
        }

        /**
         * Sets the maximum number of turns.
         *
         * @param turns the max turns
         * @return this builder
         */
        public Builder maxTurns(int turns) {
            this.maxTurns = turns;
            return this;
        }

        /**
         * Sets the win market share percentage.
         *
         * @param percentage the percentage (0-100)
         * @return this builder
         */
        public Builder winMarketSharePercentage(int percentage) {
            this.winMarketSharePercentage = percentage;
            return this;
        }

        /**
         * Sets the number of turns to sustain the win condition.
         *
         * @param turns the number of turns
         * @return this builder
         */
        public Builder winSustainTurns(int turns) {
            this.winSustainTurns = turns;
            return this;
        }

        /**
         * Builds the campaign manager.
         *
         * @return the campaign manager
         */
        public TransportCampaignManager build() {
            if (playerCorporation == null) {
                playerCorporation = new TransportCorporation("Player Transport", "player", 5000);
            }
            return new TransportCampaignManager(this);
        }
    }
}
