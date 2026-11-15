package com.yisy.modules.doramcu.hex.manager;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.hex.Battle;
import com.yisy.modules.doramcu.hex.ConstructionModuleType;
import com.yisy.modules.doramcu.hex.ConstructionOrder;
import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.ModuleAssembler;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.ProcessingFacility;
import com.yisy.modules.doramcu.hex.ProcessingRecipe;
import com.yisy.modules.doramcu.hex.ResourcePipelineManager;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.ShipComponent;
import com.yisy.modules.doramcu.hex.SpaceStation;
import com.yisy.modules.doramcu.hex.SolarSystemEntity;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.ShipOrder;
import com.yisy.modules.doramcu.hex.AiCoreComponent;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.PlanetFacility;
import com.yisy.modules.doramcu.hex.StartingFacility;
import com.yisy.modules.doramcu.hex.manager.EconomicState;
import com.yisy.modules.doramcu.hex.spi.AiCommanderExperienceSpi;
import com.yisy.modules.doramcu.hex.spi.AiEntityRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages turn progression and end-of-turn processing.
 * <p>
 * Responsible for advancing turns, processing resource production,
 * ship construction, fleet repairs, and population growth.
 * Includes Dora AI integration for AI-assisted turn summaries.
 * </p>
 */
public class TurnManager {

    private final GameContext context;

    // Resource pipeline managers per empire (lazy initialized)
    private final Map<Integer, ResourcePipelineManager> pipelineManagers = new HashMap<>();

    // Economic state per empire (lazy initialized)
    private final Map<Integer, EconomicState> economicStates = new HashMap<>();

    /** Optional service for registering entities as AI characters (set via Game). */
    private AiEntityRegistration aiEntityRegistrationService;

    /** Optional service for recording AI commander experience (set via Game). */
    private AiCommanderExperienceSpi aiCommanderExperienceService;

    /**
     * Creates a new TurnManager.
     *
     * @param context the shared game context
     */
    public TurnManager(GameContext context) {
        this.context = context;
    }

    /**
     * Sets the AI entity registration service (injected by Game when available).
     */
    public void setAiEntityRegistrationService(AiEntityRegistration aiEntityRegistrationService) {
        this.aiEntityRegistrationService = aiEntityRegistrationService;
    }

    /**
     * Sets the AI commander experience service (injected by Game when available).
     */
    public void setAiCommanderExperienceService(AiCommanderExperienceSpi aiCommanderExperienceService) {
        this.aiCommanderExperienceService = aiCommanderExperienceService;
    }

    /**
     * Generates a summary of the current turn state.
     *
     * @return the turn summary string
     */
    public String getTurnSummary() {
        int turn = context.getCurrentTurn();
        int activePlayers = context.getActivePlayerCount();
        Player currentPlayer = context.getCurrentPlayer();
        String playerName = currentPlayer != null ? currentPlayer.getName() : "Unknown";
        return String.format(
            "Turn %d, %d active players, current player: %s",
            turn, activePlayers, playerName
        );
    }

    /**
     * Advances to the next player's turn.
     * Skips eliminated players.
     */
    public void nextPlayer() {
        List<Player> players = context.getPlayers();
        if (players.isEmpty()) {
            return;
        }
        int currentIndex = context.getCurrentPlayerIndex();
        int nextIndex = (currentIndex + 1) % players.size();
        // Skip eliminated players
        int safety = 0;
        while (players.get(nextIndex).isEliminated() && safety < players.size()) {
            nextIndex = (nextIndex + 1) % players.size();
            safety++;
        }
        context.setCurrentPlayerIndex(nextIndex);
    }

    /**
     * Advances the game by one full turn, processing all players.
     * <p>
     * Each turn processes:
     * <ul>
     *   <li>Resource production from owned planets</li>
     *   <li>Ship construction progress</li>
     *   <li>Fleet shield recharge and hull repair</li>
     *   <li>Ship fuel and maintenance consumption</li>
     *   <li>Active battle resolution</li>
     *   <li>Space station upkeep and resource production</li>
     *   <li>Trade market fluctuations</li>
     *   <li>Mercenary income</li>
     *   <li>Economic state finalization</li>
     * </ul>
     * </p>
     */
    public void nextTurn() {
        context.incrementTurn();
        for (Player player : context.getPlayers()) {
            if (player.isEliminated()) {
                continue;
            }
            // Process ship construction
            processShipConstruction(player);

            // Process fleet repairs
            processFleetRepairs(player);
        }

        // Process resource pipelines for all empires
        processResourcePipelines();

        // Process ship fuel and maintenance consumption
        processShipMaintenance();

        // Resolve active battles — advance each battle by one round per turn
        resolveActiveBattles();

        // Process space station upkeep and resource production
        processStationUpkeep();

        // Process trade market fluctuations
        processTradeMarkets();

        // Process mercenary income for all empires
        processMercenaryIncome();

        // Process survey missions
        processSurveyMissions();

        // Process tax collection for all empires
        processTaxCollection();

        // Finalize economic state for all empires
        finalizeEconomicTurn();

        // Check for game end conditions
        checkGameEndConditions();
    }

    // ============================================================
    // Trade Market Processing
    // ============================================================

    /**
     * Processes trade market fluctuations at end-of-turn.
     * Market conditions change, affecting commodity prices.
     * Delegates to TradeManager which handles: market fluctuation, economic events,
     * trade route network building, auto-trade, credit exchange, and player trade orders.
     */
    private void processTradeMarkets() {
        if (context instanceof com.yisy.modules.doramcu.hex.Game game) {
            com.yisy.modules.doramcu.hex.manager.TradeManager tradeManager = game.getTradeManager();
            if (tradeManager != null) {
                // Process player trade orders
                tradeManager.processTradeOrders();
                // Process auto-trade and market fluctuations
                tradeManager.processEndOfTurn(pipelineManagers);
            }
        }
    }

    // ============================================================
    // Mercenary Income Processing
    // ============================================================

    /**
     * Processes mercenary ticket income for all empires at end-of-turn.
     * Delegates to MercenaryIncomeManager which handles: contract processing,
     * mission resolution, ticket income calculation, and credit distribution.
     */
    private void processMercenaryIncome() {
        if (context instanceof com.yisy.modules.doramcu.hex.Game game) {
            com.yisy.modules.doramcu.hex.manager.MercenaryIncomeManager mercManager = game.getMercenaryIncomeManager();
            if (mercManager != null) {
                mercManager.processEndOfTurn();
            }
        }
    }

    /**
     * Processes survey missions for all empires.
     * Advances each mission by one turn and removes completed missions.
     */
    private void processSurveyMissions() {
        if (context instanceof com.yisy.modules.doramcu.hex.Game game) {
            game.processSurveyMissions();
        }
    }

    /**
     * Processes tax collection for all empires at end-of-turn.
     * Delegates to TaxManager which handles: calculating tax based on planet
     * characteristics, applying tax policy, and feeding income into EconomicState.
     */
    private void processTaxCollection() {
        if (context instanceof com.yisy.modules.doramcu.hex.Game game) {
            com.yisy.modules.doramcu.hex.manager.TaxManager taxManager = game.getTaxManager();
            if (taxManager != null) {
                taxManager.processEndOfTurn();
            }
        }
    }

    /**
     * Finalizes the economic state for all empires at end-of-turn.
     * Records turn data and resets per-turn counters.
     */
    private void finalizeEconomicTurn() {
        int turn = context.getCurrentTurn();
        for (EconomicState state : economicStates.values()) {
            state.finalizeTurn(turn);
        }
    }

    /**
     * Resolves all active battles by advancing each one round.
     * When a battle completes, the results are applied to the actual game state:
     * damage is applied to fleets, destroyed ships are removed, destroyed fleets
     * are cleaned up, and planets may be captured.
     */
    private void resolveActiveBattles() {
        List<Battle> activeBattles = new ArrayList<>();
        for (Battle battle : context.getBattles().values()) {
            if (battle.getState() == Battle.BattleState.IN_PROGRESS) {
                activeBattles.add(battle);
            }
        }

        for (Battle battle : activeBattles) {
            // Skip if already completed (race condition check)
            if (battle.getState() != Battle.BattleState.IN_PROGRESS) continue;

            // Advance battle one round
            Battle.RoundResult round = battle.fightRound();
            if (round == null) continue; // stalemate

            // If battle completed, apply results
            if (battle.getState() == Battle.BattleState.COMPLETED) {
                applyBattleResultsToGame(battle);
            }
        }
    }

    /**
     * Applies the results of a completed battle to the actual game state.
     */
    private void applyBattleResultsToGame(Battle battle) {
        Fleet attacker = battle.getAttacker();
        Fleet defender = battle.getDefender();
        Battle.BattleOutcome outcome = battle.getOutcome();

        // Apply damage from battle simulation to actual fleets
        applyFleetDamageFromBattle(attacker, battle);
        applyFleetDamageFromBattle(defender, battle);

        // Deactivate AI characters for destroyed ships before removal
        deactivateAiForDestroyedShips(attacker);
        deactivateAiForDestroyedShips(defender);

        // Record battle experience for surviving ships' AI characters
        recordBattleExperienceForFleet(attacker, outcome, true);
        recordBattleExperienceForFleet(defender, outcome, false);

        // Remove destroyed ships
        attacker.removeDestroyedShips();
        defender.removeDestroyedShips();

        // Handle fleet destruction
        if (attacker.isDestroyed()) {
            removeDestroyedFleet(attacker, attacker.getOwner());
        }
        if (defender.isDestroyed()) {
            removeDestroyedFleet(defender, defender.getOwner());
        }

        // Handle planet capture on attacker victory
        if (outcome == Battle.BattleOutcome.ATTACKER_VICTORY) {
            Player attackerPlayer = attacker.getOwner();
            Player defenderPlayer = defender.getOwner();
            if (attackerPlayer != null && defenderPlayer != null) {
                capturePlanetsAfterBattle(attackerPlayer, defenderPlayer, battle);
            }
        }

        // Check player elimination
        checkPlayerElimination(attacker.getOwner());
        checkPlayerElimination(defender.getOwner());
    }

    /**
     * Applies damage from battle simulation to the actual fleet.
     */
    private void applyFleetDamageFromBattle(Fleet actualFleet, Battle battle) {
        boolean isAttacker = actualFleet.getId() == battle.getAttacker().getId();
        int totalDamageDealt = isAttacker ? battle.getTotalDefenderDamage() : battle.getTotalAttackerDamage();
        if (totalDamageDealt > 0) {
            actualFleet.distributeDamage(totalDamageDealt);
        }
    }

    /**
     * Removes a destroyed fleet from the game context.
     */
    private void removeDestroyedFleet(Fleet fleet, Player owner) {
        if (owner != null) {
            owner.getOwnedFleetIds().remove(fleet.getId());
        }
        context.getFleets().remove(fleet.getId());
    }

    /**
     * Captures planets when the attacker wins a battle.
     * Since planets don't have positions, captures non-capital planets from the defender.
     */
    private void capturePlanetsAfterBattle(Player attackerPlayer, Player defenderPlayer, Battle battle) {
        if (defenderPlayer.getOwnedPlanetIds().isEmpty()) return;

        Fleet defender = battle.getDefender();
        List<Integer> planetsToCapture = new ArrayList<>();

        // If defender fleet has a home system, capture non-capital planets
        if (defender.getHomeSystem() != null) {
            for (Integer planetId : defenderPlayer.getOwnedPlanetIds()) {
                if (defenderPlayer.getHomePlanetId() != null && defenderPlayer.getHomePlanetId().equals(planetId)) {
                    continue;
                }
                planetsToCapture.add(planetId);
            }
        }

        // If no home system, capture one random non-capital planet
        if (planetsToCapture.isEmpty()) {
            for (Integer planetId : defenderPlayer.getOwnedPlanetIds()) {
                if (defenderPlayer.getHomePlanetId() != null && defenderPlayer.getHomePlanetId().equals(planetId)) {
                    continue;
                }
                planetsToCapture.add(planetId);
                break;
            }
        }

        // Transfer captured planets
        for (Integer planetId : planetsToCapture) {
            defenderPlayer.removePlanet(planetId);
            attackerPlayer.addPlanet(planetId);
        }
    }

    /**
     * Checks if a player should be eliminated.
     */
    private void checkPlayerElimination(Player player) {
        if (player == null || player.isEliminated()) return;
        if (player.getOwnedFleetIds().isEmpty() && player.getOwnedPlanetIds().isEmpty()) {
            player.eliminate();
        }
    }

// Flat resource production removed – use ResourcePipelineManager for mining/processing

    /**
     * Processes ship construction queue for a player.
     *
     * @param player the player
     */
    private void processShipConstruction(Player player) {
        // Separate completed and pending orders
        List<ShipOrder> pendingQueue = new ArrayList<>();
        List<ShipOrder> completedOrders = new ArrayList<>();
        for (ShipOrder order : player.getShipQueue()) {
            if (order.advanceTurn()) {
                completedOrders.add(order);
            } else {
                pendingQueue.add(order);
            }
        }

        // Remove completed orders from player's ship queue (keep only pending)
        while (player.getShipQueue().size() > pendingQueue.size()) {
            player.completeNextShipOrder();
        }

        // Add completed ships to fleets
        for (ShipOrder order : completedOrders) {
            Fleet fleet = context.getFleet(order.getTargetFleetId());
            if (fleet != null) {
                Ship completedShip = new Ship(context.getNextShipIdAndIncrement(), order.getShipTemplate().getShipType());
                completedShip.setName(order.getShipTemplate().getName());
                for (ShipComponent comp : order.getShipTemplate().getComponents()) {
                    ShipComponent newComp;
                    switch (comp.getType()) {
                        case WEAPON:
                            newComp = new ShipComponent(comp.getWeaponType(), comp.getQuantity());
                            break;
                        case SHIELD:
                            newComp = ShipComponent.shield(comp.getQuantity());
                            break;
                        case HULL_REINFORCEMENT:
                            newComp = ShipComponent.hullReinforcement(comp.getQuantity());
                            break;
                        default:
                            continue;
                    }
                    completedShip.addComponent(newComp);
                }
                if (order.getShipTemplate().getHomePlanet() != null) {
                    completedShip.setHomePlanet(order.getShipTemplate().getHomePlanet());
                }
                // Install AI Core for network connection
                completedShip.setAiCore(new AiCoreComponent());
                context.addShip(completedShip);
                fleet.addShip(completedShip);
                // Register as AI character
                registerShipAsAi(completedShip, player.getName());
            }
        }
    }

    /**
     * Processes fleet shield recharge and hull repair.
     *
     * @param player the player
     */
    private void processFleetRepairs(Player player) {
        for (Integer fleetId : player.getOwnedFleetIds()) {
            Fleet fleet = context.getFleet(fleetId);
            if (fleet != null) {
                fleet.endTurnRepair();
            }
        }
    }

// Population growth removed – use ResourcePipelineManager for mining/processing

    /**
     * Checks if the game should end (only one player remaining).
     */
    private void checkGameEndConditions() {
        if (context.getActivePlayerCount() <= 1) {
            context.setGameEnded(true);
        }
    }

    // ---- Resource Pipeline Integration ----

    /**
     * Gets or creates the resource pipeline manager for the given empire.
     * <p>
     * When first created, the pipeline is initialized with mining operations
     * for all planets owned by the empire, and default processing facilities
     * and assemblers are added. Also creates the EconomicState for this empire.
     * </p>
     *
     * @param empireId the empire ID
     * @return the pipeline manager for that empire
     */
    public ResourcePipelineManager getPipelineManager(int empireId) {
        return pipelineManagers.computeIfAbsent(empireId, id -> {
            ResourcePipelineManager pipeline = new ResourcePipelineManager(id);
            initializePipeline(pipeline, id);
            // Create and register EconomicState
            EconomicState economicState = new EconomicState(id, pipeline);
            economicStates.put(id, economicState);
            registerEconomicStateWithServices(economicState);
            return pipeline;
        });
    }

    /**
     * Gets or creates the EconomicState for the given empire.
     *
     * @param empireId the empire ID
     * @return the economic state for that empire
     */
    public EconomicState getEconomicState(int empireId) {
        return economicStates.computeIfAbsent(empireId, id -> {
            // Ensure pipeline exists first
            ResourcePipelineManager pipeline = getPipelineManager(id);
            EconomicState state = new EconomicState(id, pipeline);
            registerEconomicStateWithServices(state);
            return state;
        });
    }

    /**
     * Registers an EconomicState with all services that need it.
     *
     * @param economicState the economic state to register
     */
    private void registerEconomicStateWithServices(EconomicState economicState) {
        if (context instanceof com.yisy.modules.doramcu.hex.Game game) {
            TradeManager tradeManager = game.getTradeManager();
            if (tradeManager != null) {
                CreditExchangeService ces = tradeManager.getCreditExchangeService();
                if (ces != null) {
                    ces.registerEconomicState(economicState);
                }
                // Wire MercenaryIncomeManager to CreditExchangeService
                MercenaryIncomeManager mercManager = game.getMercenaryIncomeManager();
                if (mercManager != null && ces != null) {
                    mercManager.setCreditExchangeService(ces);
                }
                // Wire TaxManager to CreditExchangeService
                TaxManager taxManager = game.getTaxManager();
                if (taxManager != null && ces != null) {
                    taxManager.setCreditExchangeService(ces);
                }
            }
        }
    }

    /**
     * Initializes a newly created pipeline with mining operations,
     * processing facilities, and module assemblers.
     * Mining bonuses from planetary facilities and nearby stations are applied.
     *
     * @param pipeline the pipeline to initialize
     * @param empireId the empire ID
     */
    private void initializePipeline(ResourcePipelineManager pipeline, int empireId) {
        // Find the player for this empire
        Player owner = null;
        for (Player player : context.getPlayers()) {
            if (player.getEmpireId() == empireId) {
                owner = player;
                break;
            }
        }
        if (owner == null) {
            return;
        }

        // Add mining operations for all owned planets with facility bonuses
        Map<Integer, Planet> planets = context.getPlanets();
        for (Integer planetId : owner.getOwnedPlanetIds()) {
            Planet planet = planets.get(planetId);
            if (planet != null) {
                // Combine planet facility bonus with base bonus
                int miningBonus = planet.getMiningBonus();
                pipeline.addMiningOperation(planet, miningBonus);
            }
        }

        // Add default processing facilities (one per resource type)
        for (ProcessingRecipe recipe : ProcessingRecipe.STANDARD_RECIPES.values()) {
            ProcessingFacility facility = new ProcessingFacility(recipe);
            pipeline.addProcessingFacility(facility);
        }

        // Add default module assemblers (one per module type)
        for (ConstructionModuleType moduleType : ConstructionModuleType.values()) {
            ModuleAssembler assembler = new ModuleAssembler(moduleType);
            pipeline.addAssembler(assembler);
        }
    }

    /**
     * Processes all resource pipelines for one turn.
     * After pipeline processing, completed construction orders are fulfilled.
     */
    private void processResourcePipelines() {
        for (Player player : context.getPlayers()) {
            if (player.isEliminated()) {
                continue;
            }
            ResourcePipelineManager pipeline = pipelineManagers.get(player.getEmpireId());
            if (pipeline == null) {
                continue;
            }

            // Process pipeline turn (mining, transport, processing, assembly)
            // Pass technology tree so alternative recipes can be unlocked
            EconomicState economicState = economicStates.get(player.getEmpireId());
            com.yisy.modules.doramcu.hex.manager.TechnologyTree technologyTree =
                    economicState != null ? economicState.getTechnologyTree() : null;
            pipeline.processTurn(technologyTree);

            // Process completed construction orders for this player
            processCompletedConstructionForPlayer(player, pipeline);
        }
    }

    /**
     * Registers a space station as an AI character in the AI character repository.
     * No-op if the AI entity registration service is not available.
     */
    private void registerStationAsAi(SpaceStation station, String empireName) {
        if (aiEntityRegistrationService != null && station != null) {
            try {
                aiEntityRegistrationService.registerStation(station, empireName);
            } catch (Exception e) {
                // Silently ignore if registration fails
            }
        }
    }

    /**
     * Registers a ship as an AI character in the AI character repository.
     * No-op if the AI entity registration service is not available.
     */
    private void registerShipAsAi(Ship ship, String empireName) {
        if (aiEntityRegistrationService != null && ship != null) {
            try {
                aiEntityRegistrationService.registerShip(ship, empireName);
            } catch (Exception e) {
                // Silently ignore if registration fails
            }
        }
    }

    /**
     * Registers a facility as an AI character in the AI character repository.
     * No-op if the AI entity registration service is not available.
     */
    private void registerFacilityAsAi(PlanetFacility facility, String empireName) {
        if (aiEntityRegistrationService != null && facility != null) {
            try {
                aiEntityRegistrationService.registerFacility(facility, empireName);
            } catch (Exception e) {
                // Silently ignore if registration fails
            }
        }
    }

    /**
     * Deactivates AI characters for destroyed ships in a fleet.
     * No-op if the AI entity registration service is not available.
     */
    private void deactivateAiForDestroyedShips(Fleet fleet) {
        if (aiEntityRegistrationService == null || fleet == null) {
            return;
        }
        for (Ship ship : fleet.getDestroyedShips()) {
            try {
                aiEntityRegistrationService.deactivateShipAiCharacter(ship.getId());
            } catch (Exception e) {
                // Silently ignore if deactivation fails
            }
        }
    }

    /**
     * Deactivates AI characters for destroyed stations.
     * No-op if the AI entity registration service is not available.
     */
    private void deactivateAiForDestroyedStations() {
        if (aiEntityRegistrationService == null) {
            return;
        }
        for (SpaceStation station : context.getSpaceStations()) {
            if (station.isDestroyed()) {
                try {
                    aiEntityRegistrationService.deactivateStationAiCharacter(station.getId());
                } catch (Exception e) {
                    // Silently ignore if deactivation fails
                }
            }
        }
    }

    /**
     * Records battle experience for surviving ships' AI characters in a fleet.
     * No-op if the AI commander experience service is not available.
     *
     * @param fleet the fleet to process
     * @param battleOutcome the overall battle outcome
     * @param isAttacker true if this fleet was the attacker in the battle
     */
    private void recordBattleExperienceForFleet(Fleet fleet, Battle.BattleOutcome battleOutcome, boolean isAttacker) {
        if (aiCommanderExperienceService == null || fleet == null) {
            return;
        }
        // Determine the outcome for this fleet's side
        com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome outcome;
        if (battleOutcome == Battle.BattleOutcome.ATTACKER_VICTORY) {
            outcome = isAttacker
                ? com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.VICTORY
                : com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.DEFEAT;
        } else if (battleOutcome == Battle.BattleOutcome.DEFENDER_VICTORY) {
            outcome = isAttacker
                ? com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.DEFEAT
                : com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.VICTORY;
        } else {
            outcome = com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.STALEMATE;
        }
        // Record experience for each surviving ship's AI character
        for (Ship ship : fleet.getShips()) {
            if (ship.isDestroyed()) {
                continue;
            }
            try {
                Optional<com.yisy.modules.doramcu.eve.model.AiCharacter> aiCharOpt =
                    aiEntityRegistrationService.findShipAiCharacter(ship.getId());
                aiCharOpt.ifPresent(aiChar -> {
                    try {
                        aiCommanderExperienceService.recordBattleExperience(
                            aiChar.getId(),
                            "TRAVELLER",
                            "Naval Battle",
                            outcome,
                            "Naval battle participation",
                            null
                        );
                        // Advance training level if XP threshold is reached
                        aiCommanderExperienceService.advanceTrainingLevel(aiChar.getId());
                    } catch (Exception e) {
                        // Silently ignore if experience recording fails
                    }
                });
            } catch (Exception e) {
                // Silently ignore if AI character lookup fails
            }
        }
    }

    /**
     * Processes completed construction orders for a single player,
     * creating stations and facilities from completed orders.
     */
    private void processCompletedConstructionForPlayer(Player player, ResourcePipelineManager pipeline) {
        FacilityConstructionManager facilityManager = context.getFacilityConstructionManager();
        if (facilityManager == null) {
            return;
        }

        List<ConstructionOrder> completedOrders = pipeline.processConstructionQueue();
        for (ConstructionOrder order : completedOrders) {
            if (order.getConstructionType() == ConstructionOrder.ConstructionType.SPACE_STATION) {
                SolarSystemEntity homeSystem = order.getTargetSystem();
                if (homeSystem != null) {
                    SpaceStation station = facilityManager.completeStation(order, player, homeSystem);
                    if (station != null) {
                        // Add station to the solar system at a default position
                        homeSystem.addSpaceStation(station, new Hex3D(0, 0, 0));
                        registerStationAsAi(station, player.getName());
                    }
                }
            } else if (order.getConstructionType() == ConstructionOrder.ConstructionType.PLANET_FACILITY) {
                // Find the target planet
                Planet planet = null;
                if (order.getTargetPlanetId() != null) {
                    planet = context.getPlanet(order.getTargetPlanetId());
                }
                if (planet != null) {
                    facilityManager.completePlanetFacility(order, planet);
                    // Create PlanetFacility entity with AI core and register as AI character
                    StartingFacility facility = order.getPlanetFacility();
                    if (facility != null) {
                        Empire empire = context.getEmpire(player.getEmpireId());
                        if (empire != null) {
                            PlanetFacility pf = empire.getFacilities().buildFacility(planet.getId(), facility);
                            registerFacilityAsAi(pf, player.getName());
                        }
                    }
                }
            }
        }
    }

    /**
     * Processes ship fuel and maintenance consumption for all players.
     * Each player's fleets consume GAS fuel and construction modules from their pipeline.
     */
    private void processShipMaintenance() {
        for (Player player : context.getPlayers()) {
            if (player.isEliminated()) {
                continue;
            }
            ResourcePipelineManager pipeline = pipelineManagers.get(player.getEmpireId());
            if (pipeline == null) {
                continue;
            }
            // Collect this player's fleets
            List<Fleet> fleets = new ArrayList<>();
            for (Integer fleetId : player.getOwnedFleetIds()) {
                Fleet fleet = context.getFleet(fleetId);
                if (fleet != null) {
                    fleets.add(fleet);
                }
            }
            if (!fleets.isEmpty()) {
                pipeline.processShipMaintenance(fleets);
            }
        }
    }

    /**
     * Processes space station upkeep and resource production for all players.
     * Each station consumes construction modules for maintenance and produces
     * raw/refined resources based on its type. Station income and expenses
     * are tracked via EconomicState.
     */
    private void processStationUpkeep() {
        for (Player player : context.getPlayers()) {
            if (player.isEliminated()) {
                continue;
            }
            ResourcePipelineManager pipeline = pipelineManagers.get(player.getEmpireId());
            if (pipeline == null) {
                continue;
            }

            // Get EconomicState for this empire
            EconomicState economicState = economicStates.get(player.getEmpireId());

            // Collect all stations owned by this player
            List<SpaceStation> stations = new ArrayList<>();
            for (SpaceStation station : context.getSpaceStations()) {
                if (station.getOwner() != null && station.getOwner().getEmpireId() == player.getEmpireId()) {
                    stations.add(station);
                }
            }

            // Process each station's economy and AI Core
            for (SpaceStation station : stations) {
                if (!station.isDestroyed()) {
                    station.getEconomy().processTurn(pipeline, economicState);
                    // Process station AI Core if installed
                    if (station.hasAiCore()) {
                        com.yisy.modules.doramcu.ai.StationAiCore aiCore =
                                (com.yisy.modules.doramcu.ai.StationAiCore) station.getAiCore();
                        aiCore.processTurn(null);
                    }
                }
            }
        }

        // Deactivate AI characters for any destroyed stations
        deactivateAiForDestroyedStations();
    }

    /**
     * Returns all pipeline managers.
     */
    public Map<Integer, ResourcePipelineManager> getPipelineManagers() {
        return Map.copyOf(pipelineManagers);
    }

    /**
     * Returns all economic states.
     *
     * @return map of empire ID to economic state
     */
    public Map<Integer, EconomicState> getEconomicStates() {
        return Map.copyOf(economicStates);
    }
}
