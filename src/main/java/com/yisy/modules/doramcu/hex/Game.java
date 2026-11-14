package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.hex.spi.AiCommanderExperienceSpi;
import com.yisy.modules.doramcu.hex.spi.AiEntityRegistration;
import com.yisy.modules.doramcu.hex.spi.StationAiSupportSpi;
import com.yisy.modules.doramcu.unified.GameEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.manager.CharacterManager;
import com.yisy.modules.doramcu.hex.manager.CombatResolver;
import com.yisy.modules.doramcu.hex.manager.FacilityConstructionManager;
import com.yisy.modules.doramcu.hex.manager.FleetManager;
import com.yisy.modules.doramcu.hex.manager.GalaxyManager;
import com.yisy.modules.doramcu.hex.manager.GameContext;
import com.yisy.modules.doramcu.hex.manager.MercenaryIncomeManager;
import com.yisy.modules.doramcu.hex.manager.PlanetManager;
import com.yisy.modules.doramcu.hex.manager.ResourceManager;
import com.yisy.modules.doramcu.hex.manager.TradeManager;
import com.yisy.modules.doramcu.hex.manager.TurnManager;
import com.yisy.modules.doramcu.hex.manager.TaxManager;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.ShipOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Central controller for a turn-based 4X empire game.
 * <p>
 * The Game class acts as a thin coordinator that delegates to specialized manager classes:
 * <ul>
 *   <li>{@link CharacterManager} - Traveller character creation and career progression</li>
 *   <li>{@link TurnManager} - turn progression and end-of-turn processing</li>
 *   <li>{@link ResourceManager} - ship affordability and build queuing</li>
 *   <li>{@link FleetManager} - fleet creation and ship factories</li>
 *   <li>{@link CombatResolver} - battle creation and resolution</li>
 *   <li>{@link GalaxyManager} - galaxy and solar system management</li>
 *   <li>{@link PlanetManager} - planet creation and home system setup</li>
 * </ul>
 * </p>
 * <p>
 * Game implements {@link GameContext} to provide shared access to core game state
 * (players, planets, fleets, etc.) for all manager classes.
 * </p>
 */
public class Game implements GameContext, GameEngine {

    private final String name;
    private final List<Player> players = new ArrayList<>();
    private final Map<Integer, Planet> planets = new HashMap<>();
    private final Map<Integer, Fleet> fleets = new HashMap<>();
    private final Map<Integer, Ship> ships = new HashMap<>();
    private final Map<Integer, Galaxy> galaxies = new HashMap<>();
    private final Map<Integer, SolarSystem> solarSystems = new HashMap<>();
    private final Map<Integer, Battle> battles = new HashMap<>();
    private final List<SpaceStation> spaceStations = new ArrayList<>();
    private final Map<Integer, Empire> empires = new HashMap<>();

    /** Default turn duration in seconds (30 seconds = one in-game day). */
    public static final long DEFAULT_TURN_DURATION = 30;

    /** Current game time in seconds since game start. */
    private long currentTime;

    /** Duration of one turn in seconds. */
    private long turnDuration;

    private int currentPlayerIndex;
    private boolean gameEnded;
    private int nextPlanetId = 1;
    private int nextFleetId = 1;
    private int nextShipId = 1;
    private int nextBattleId = 1;
    private int nextSolarSystemId = 1;
    private int nextEmpireId = 1;
    private final Random random;
    private final long randomSeed;

    /** Empire integrity calculator with cached results. */
    private final EmpireIntegrityCalculator integrityCalculator = new EmpireIntegrityCalculator();

    /** Game state serializer for persistence. */
    private final GameStateSerializer gameStateSerializer = new GameStateSerializer();

    // Manager instances
    private final CharacterManager characterManager;
    private final TurnManager turnManager;
    private final ResourceManager resourceManager;
    private final FleetManager fleetManager;
    private final CombatResolver combatResolver;
    private final GalaxyManager galaxyManager;
    private final PlanetManager planetManager;
    private final FacilityConstructionManager facilityConstructionManager;
    private final TradeManager tradeManager;
    private final MercenaryIncomeManager mercenaryIncomeManager;
    private final TaxManager taxManager;

    /** Active survey missions across all empires. */
    private final List<SurveyMission> activeSurveyMissions = new ArrayList<>();
    private int nextSurveyMissionId = 1;

    /** Optional service for registering entities as AI characters (set via Spring). */
    private AiEntityRegistration aiEntityRegistrationService;

    /** When true, trade prices use the B07 Actual Value table (2D6 roll) for random variation. */
    private boolean actualValueEnabled;

    /** Optional service for recording AI commander experience (set via Spring). */
    private AiCommanderExperienceSpi aiCommanderExperienceService;

    /** Optional service for station AI support during battles (set via Spring). */
    private StationAiSupportSpi stationAiSupportService;

    /**
     * Creates a new game with the given name.
     *
     * @param name the name of this game
     */
    public Game(String name) {
        this(name, 0);
    }

    /**
     * Creates a new game with the given name and random seed.
     * A non-zero seed enables reproducible game outcomes for debugging and replay.
     *
     * @param name the name of this game
     * @param randomSeed the seed for the random number generator (0 for non-deterministic)
     */
    public Game(String name, long randomSeed) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Game name must not be null or blank");
        }
        this.name = name;
        this.randomSeed = randomSeed;
        this.random = randomSeed != 0 ? new Random(randomSeed) : new Random();
        this.currentTime = 0;
        this.turnDuration = DEFAULT_TURN_DURATION;
        this.currentPlayerIndex = 0;
        this.gameEnded = false;

        // Initialize managers
        this.characterManager = new CharacterManager(this);
        this.turnManager = new TurnManager(this);
        this.resourceManager = new ResourceManager(this);
        this.fleetManager = new FleetManager(this);
        this.combatResolver = new CombatResolver(this);
        this.galaxyManager = new GalaxyManager(this);
        this.planetManager = new PlanetManager(this);
        this.facilityConstructionManager = new FacilityConstructionManager(this);
        this.tradeManager = new TradeManager(this);
        this.tradeManager.setActualValueEnabled(actualValueEnabled);
        this.mercenaryIncomeManager = new MercenaryIncomeManager(this);
        this.taxManager = new TaxManager(this);
    }

    // ============================================================
    // GameContext interface implementation
    // ============================================================

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public int getCurrentTurn() {
        return (int) (currentTime / turnDuration);
    }

    /**
     * Returns the current game time in seconds since game start.
     *
     * @return the current time in seconds
     */
    public long getCurrentTime() {
        return currentTime;
    }

    /**
     * Returns the turn duration in seconds.
     *
     * @return the turn duration in seconds
     */
    public long getTurnDuration() {
        return turnDuration;
    }

    @Override
    public GameType getGameType() {
        return GameType.DORAMCU_4X;
    }

    /**
     * Sets the turn duration in seconds.
     *
     * @param turnDuration the turn duration in seconds
     */
    public void setTurnDuration(long turnDuration) {
        if (turnDuration <= 0) {
            throw new IllegalArgumentException("Turn duration must be positive: " + turnDuration);
        }
        this.turnDuration = turnDuration;
    }

    @Override
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    @Override
    public void setCurrentPlayerIndex(int index) {
        this.currentPlayerIndex = index;
    }

    @Override
    public Player getCurrentPlayer() {
        if (players.isEmpty()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }

    @Override
    public boolean isGameEnded() {
        return gameEnded;
    }

    @Override
    public void setGameEnded(boolean ended) {
        this.gameEnded = ended;
    }

    @Override
    public void incrementTurn() {
        currentTime += turnDuration;
    }

    /**
     * Advances the game time by the specified number of seconds.
     * This may trigger multiple turns if the elapsed time exceeds the turn duration.
     *
     * @param seconds the number of seconds to advance
     * @return the number of turns that elapsed
     */
    public int advanceTime(long seconds) {
        if (seconds <= 0) {
            throw new IllegalArgumentException("Time advance must be positive: " + seconds);
        }
        long previousTime = currentTime;
        currentTime += seconds;
        int turnsElapsed = (int) ((currentTime / turnDuration) - (previousTime / turnDuration));
        return turnsElapsed;
    }

    /**
     * Advances the game by one turn (turnDuration seconds).
     * This is a convenience method equivalent to advanceTime(turnDuration).
     *
     * @return 1 (one turn elapsed)
     */
    public int advanceOneTurn() {
        return advanceTime(turnDuration);
    }

    @Override
    public Random getRandom() {
        return random;
    }

    @Override
    public int getActivePlayerCount() {
        return (int) players.stream().filter(p -> !p.isEliminated()).count();
    }

    @Override
    public Player addPlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }
        players.add(player);
        return player;
    }

    @Override
    public Player getPlayer(int playerId) {
        return players.stream()
                .filter(p -> p.getId() == playerId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Map<Integer, Planet> getPlanets() {
        return planets;
    }

    @Override
    public Planet getPlanet(int planetId) {
        return planets.get(planetId);
    }

    @Override
    public Map<Integer, Fleet> getFleets() {
        return fleets;
    }

    @Override
    public Fleet getFleet(int fleetId) {
        return fleets.get(fleetId);
    }

    @Override
    public Fleet addFleet(Fleet fleet, Player owner) {
        if (fleet == null) {
            throw new IllegalArgumentException("Fleet must not be null");
        }
        if (owner == null) {
            throw new IllegalArgumentException("Owner must not be null");
        }
        int fleetId = fleet.getId();
        fleets.put(fleetId, fleet);
        owner.addFleet(fleetId);
        return fleet;
    }

    /**
     * Removes a fleet from the game.
     *
     * @param fleetId the fleet ID to remove
     * @return the removed fleet, or null if not found
     */
    public Fleet removeFleet(int fleetId) {
        return fleets.remove(fleetId);
    }

    @Override
    public Map<Integer, Ship> getShips() {
        return ships;
    }

    @Override
    public Ship getShip(int shipId) {
        return ships.get(shipId);
    }

    @Override
    public Ship addShip(Ship ship) {
        if (ship == null) {
            throw new IllegalArgumentException("Ship must not be null");
        }
        ships.put(ship.getId(), ship);
        return ship;
    }

    @Override
    public int getNextShipIdAndIncrement() {
        return nextShipId++;
    }

    /** Returns the current next ship ID without incrementing. */
    public int getCurrentNextShipId() {
        return nextShipId;
    }

    @Override
    public Map<Integer, Galaxy> getGalaxies() {
        return galaxies;
    }

    @Override
    public Galaxy getGalaxy(int galaxyId) {
        return galaxies.get(galaxyId);
    }

    /**
     * Registers an empire in the game.
     *
     * @param empire the empire to register
     */
    public void registerEmpire(Empire empire) {
        if (empire == null) return;
        empires.put(empire.getId(), empire);
    }

    /**
     * Returns the empire with the given ID.
     *
     * @param empireId the empire ID
     * @return the empire, or null if not found
     */
    public Empire getEmpire(int empireId) {
        return empires.get(empireId);
    }

    /**
     * Returns all empires in the game.
     *
     * @return unmodifiable map of empire ID to empire
     */
    public Map<Integer, Empire> getEmpires() {
        return Collections.unmodifiableMap(empires);
    }

    @Override
    public Galaxy addGalaxy(Galaxy galaxy) {
        if (galaxy == null) {
            throw new IllegalArgumentException("Galaxy must not be null");
        }
        galaxies.put(galaxy.getId(), galaxy);
        return galaxy;
    }

    @Override
    public Map<Integer, SolarSystem> getSolarSystems() {
        return solarSystems;
    }

    @Override
    public Map<Integer, Battle> getBattles() {
        return battles;
    }

    @Override
    public List<SpaceStation> getSpaceStations() {
        return spaceStations;
    }

    @Override
    public Battle getBattle(int battleId) {
        return battles.get(battleId);
    }

    @Override
    public Battle addBattle(Battle battle) {
        battles.put(battle.getId(), battle);
        return battle;
    }

    @Override
    public int getNextPlanetIdAndIncrement() {
        return nextPlanetId++;
    }

    /** Returns the current next planet ID without incrementing. */
    public int getCurrentNextPlanetId() {
        return nextPlanetId;
    }

    @Override
    public int getNextFleetIdAndIncrement() {
        return nextFleetId++;
    }

    /** Returns the current next fleet ID without incrementing. */
    public int getCurrentNextFleetId() {
        return nextFleetId;
    }

    @Override
    public int getNextBattleIdAndIncrement() {
        return nextBattleId++;
    }

    /** Returns the current next battle ID without incrementing. */
    public int getCurrentNextBattleId() {
        return nextBattleId;
    }

    @Override
    public int getNextSolarSystemIdAndIncrement() {
        return nextSolarSystemId++;
    }

    /**
     * Returns the next empire ID and increments the counter.
     *
     * @return the next empire ID
     */
    public int getNextEmpireIdAndIncrement() {
        return nextEmpireId++;
    }

    /** Returns the current next solar system ID without incrementing. */
    public int getCurrentNextSolarSystemId() {
        return nextSolarSystemId;
    }

    // ============================================================
    // Public API - delegates to managers
    // ============================================================

    /** Returns the list of players (unmodifiable). */
    public List<Player> getPlayerList() {
        return Collections.unmodifiableList(players);
    }

    /** Returns the number of players. */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * Creates and adds a new player with an auto-generated ID.
     *
     * @param name the player name
     * @return the new player
     */
    public Player createPlayer(String name) {
        int id = players.size();
        Player player = new Player(id, name);
        addPlayer(player);
        return player;
    }

    /**
     * Creates and adds a new player with a specific ID.
     * Use this when the player ID must match an external identity (e.g., session player ID).
     *
     * @param id the player ID
     * @param name the player name
     * @return the new player
     */
    public Player createPlayer(int id, String name) {
        Player player = new Player(id, name);
        addPlayer(player);
        return player;
    }

    /**
     * Result of setting up a new player, containing the player, empire, and starting fleets.
     */
    public static class PlayerSetupResult {
        private final Player player;
        private final Empire empire;
        private final List<Fleet> startingFleets;

        public PlayerSetupResult(Player player, Empire empire, List<Fleet> startingFleets) {
            this.player = player;
            this.empire = empire;
            this.startingFleets = startingFleets;
        }

        public Player getPlayer() {
            return player;
        }

        public Empire getEmpire() {
            return empire;
        }

        public List<Fleet> getStartingFleets() {
            return startingFleets;
        }
    }

    /**
     * Sets up a new player with a complete starting configuration:
     * <ul>
     *   <li>Creates the player and adds them to the game</li>
     *   <li>Assigns a unique empire ID and creates an Empire instance</li>
     *   <li>Sets up a home system with starting facilities</li>
     *   <li>Creates starting fleets (military and mining)</li>
     *   <li>Synchronizes fleets to the Empire navy</li>
     *   <li>Synchronizes planets to the Empire</li>
     *   <li>Sets the Empire capital planet</li>
     * </ul>
     *
     * @param playerId the player ID (e.g., session player ID)
     * @param playerName the player name
     * @return the setup result containing the player, empire, and starting fleets
     */
    public PlayerSetupResult setupPlayer(int playerId, String playerName) {
        return setupPlayer(playerId, playerName, PlanetSize.MEDIUM, MineralQuality.NORMAL,
                StartingFacility.COLONY_DOME, StartingFacility.SHIPYARD, StartingFacility.MINING_FACILITY);
    }

    /**
     * Sets up a new player with a complete starting configuration and custom planet properties.
     *
     * @param playerId the player ID
     * @param playerName the player name
     * @param size the home planet size
     * @param mineralQuality the home planet mineral quality
     * @param facilities the starting facilities to build
     * @return the setup result containing the player, empire, and starting fleets
     */
    public PlayerSetupResult setupPlayer(int playerId, String playerName, PlanetSize size,
                                         MineralQuality mineralQuality, StartingFacility... facilities) {
        // 1. Create the player
        Player player = createPlayer(playerId, playerName);

        // 2. Assign a unique empire ID
        int newEmpireId = getNextEmpireIdAndIncrement();
        player.setEmpireId(newEmpireId);

        // 3. Create and register an Empire for this player
        Empire empire = new Empire(newEmpireId, playerName + " Empire", player.getId());
        registerEmpire(empire);

        // 4. Set up home system with starting facilities
        Planet homePlanet = setupHomeSystem(player, size, mineralQuality, facilities);

        // 5. Synchronize the home planet to the Empire
        if (homePlanet != null) {
            empire.claimPlanet(homePlanet.getId());
            empire.setCapitalPlanet(homePlanet.getId());

            // Sync the home planet's starting facilities to the Empire facilities component
            for (StartingFacility facility : homePlanet.getStartingFacilities()) {
                PlanetFacility pf = empire.getFacilities().buildFacility(homePlanet.getId(), facility);
                registerFacilityAsAi(pf, empire.getName());
            }
        }

        // 6. Create starting fleets and sync to Empire navy
        List<Fleet> startingFleets = createStartingFleets(player);
        for (Fleet fleet : startingFleets) {
            empire.getNavy().addFleet(fleet);
        }

        return new PlayerSetupResult(player, empire, startingFleets);
    }

    /**
     * Ends the game.
     */
    public void endGame() {
        this.gameEnded = true;
    }

    /**
     * Returns the map of all planets by ID (unmodifiable).
     */
    public Map<Integer, Planet> getPlanetsMap() {
        return Collections.unmodifiableMap(planets);
    }

    /**
     * Returns the map of all fleets by ID (unmodifiable).
     */
    public Map<Integer, Fleet> getFleetsMap() {
        return Collections.unmodifiableMap(fleets);
    }

    /**
     * Returns the map of all ships by ID (unmodifiable).
     */
    public Map<Integer, Ship> getShipsMap() {
        return Collections.unmodifiableMap(ships);
    }

    /**
     * Returns the map of all galaxies by ID (unmodifiable).
     */
    public Map<Integer, Galaxy> getGalaxiesMap() {
        return Collections.unmodifiableMap(galaxies);
    }

    /**
     * Returns the map of all battles by ID (unmodifiable).
     */
    public Map<Integer, Battle> getBattlesMap() {
        return Collections.unmodifiableMap(battles);
    }

    // ---- Player management (delegated) ----

    /**
     * Returns all planets owned by a specific player.
     */
    public List<Planet> getPlanetsByPlayer(Player player) {
        return player.getOwnedPlanetIds().stream()
                .map(planets::get)
                .filter(p -> p != null)
                .collect(Collectors.toList());
    }

    /**
     * Returns all fleets owned by a specific player.
     */
    public List<Fleet> getFleetsByPlayer(Player player) {
        return player.getOwnedFleetIds().stream()
                .map(fleets::get)
                .filter(f -> f != null)
                .collect(Collectors.toList());
    }

    /**
     * Returns the total military power of a player (sum of all fleet damage).
     */
    public int getTotalMilitaryPower(Player player) {
        return getFleetsByPlayer(player).stream()
                .mapToInt(Fleet::getTotalDamage)
                .sum();
    }

    // getTotalProduction removed – use ResourcePipelineManager for mining/processing

    // ============================================================
    // Empire integrity (delegated to EmpireIntegrityCalculator)
    // ============================================================

    /**
     * Marks the cached empire integrity as dirty so it will be recomputed
     * on the next call to {@link #getEmpireIntegrity()}.
     * Call this method whenever ships are damaged or destroyed.
     */
    public void markIntegrityDirty() {
        integrityCalculator.markDirty();
    }

    /**
     * Returns the cached empire integrity as a percentage (0-100).
     * Computes the value on demand and caches it until marked dirty.
     *
     * @return integrity percentage (100 = no damage, 0 = all destroyed)
     */
    public double getEmpireIntegrity() {
        return integrityCalculator.calculate(this);
    }

    /**
     * Returns the winner of the game, or null if the game is not ended.
     */
    public Player getWinner() {
        if (!gameEnded) {
            return null;
        }
        return players.stream()
                .filter(p -> !p.isEliminated())
                .findFirst()
                .orElse(null);
    }

    // ---- Turn management (delegated) ----

    /**
     * Advances to the next player's turn.
     */
    public void nextPlayer() {
        turnManager.nextPlayer();
    }

    /**
     * Advances the game by one full turn, processing all players.
     */
    public void nextTurn() {
        turnManager.nextTurn();
    }

    // ---- Ship building (delegated) ----

    /**
     * Checks if a player can afford to build a ship using pipeline modules.
     */
    public boolean canAffordShip(Player player, Ship ship, ResourcePipelineManager pipeline) {
        return resourceManager.canAffordShipWithModules(player, ship, pipeline);
    }

    /**
     * Checks if a player can build a ship given available strategic resources.
     */
    public boolean canBuildShipWithResources(Player player, Ship ship) {
        return resourceManager.canBuildShipWithResources(player, ship);
    }

    /**
     * Queues a ship for construction using pipeline modules.
     */
    public ShipOrder queueShipBuild(Player player, Ship ship, int targetFleetId,
                                     ResourcePipelineManager pipeline) {
        return resourceManager.queueShipBuild(player, ship, targetFleetId, pipeline);
    }

    // ---- Resource Pipeline Access ----

    /**
     * Gets or creates the resource pipeline manager for the given empire.
     *
     * @param empireId the empire ID
     * @return the pipeline manager for that empire
     */
    public ResourcePipelineManager getPipelineManager(int empireId) {
        return turnManager.getPipelineManager(empireId);
    }

    /**
     * Returns all pipeline managers.
     */
    public Map<Integer, ResourcePipelineManager> getPipelineManagers() {
        return turnManager.getPipelineManagers();
    }

    /**
     * Returns the EconomicState for the given empire.
     *
     * @param empireId the empire ID
     * @return the economic state, or null if not yet initialized
     */
    public com.yisy.modules.doramcu.hex.manager.EconomicState getEconomicState(int empireId) {
        return turnManager.getEconomicState(empireId);
    }

    // ---- Facility Construction (delegated) ----

    /**
     * Gets the facility construction manager.
     */
    @Override
    public FacilityConstructionManager getFacilityConstructionManager() {
        return facilityConstructionManager;
    }

    /**
     * Checks if a player can build a space station with the given module requirements.
     */
    public boolean canBuildStation(int empireId, SpaceStationType stationType) {
        ResourcePipelineManager pipeline = getPipelineManager(empireId);
        return facilityConstructionManager.canBuildStation(pipeline, stationType);
    }

    /**
     * Builds a space station by consuming modules from the pipeline.
     */
    public SpaceStation buildStation(Player owner, SpaceStationType stationType,
                                      SolarSystemEntity homeSystem) {
        ResourcePipelineManager pipeline = getPipelineManager(owner.getEmpireId());
        SpaceStation station = facilityConstructionManager.buildStation(owner, stationType, homeSystem, pipeline);
        if (station != null) {
            registerStationAsAi(station, owner.getName());
        }
        return station;
    }

    /**
     * Checks if a player can build a planetary facility.
     */
    public boolean canBuildPlanetFacility(int empireId, StartingFacility facility) {
        ResourcePipelineManager pipeline = getPipelineManager(empireId);
        return facilityConstructionManager.canBuildPlanetFacility(pipeline, facility);
    }

    /**
     * Adds a facility to a planet by consuming modules from the pipeline,
     * creating a PlanetFacility entity with an AI core and registering it as an AI character.
     */
    public boolean buildPlanetFacility(Planet planet, StartingFacility facility, int empireId) {
        ResourcePipelineManager pipeline = getPipelineManager(empireId);
        boolean consumed = facilityConstructionManager.buildPlanetFacility(planet, facility, pipeline);
        if (consumed) {
            Empire empire = getEmpire(empireId);
            if (empire != null) {
                PlanetFacility pf = empire.getFacilities().buildFacility(planet.getId(), facility);
                registerFacilityAsAi(pf, empire.getName());
            }
        }
        return consumed;
    }

    /**
     * Sets the AI entity registration service (injected by Spring when available).
     * Propagates the service to sub-managers that also need it.
     */
    public void setAiEntityRegistrationService(AiEntityRegistration aiEntityRegistrationService) {
        this.aiEntityRegistrationService = aiEntityRegistrationService;
        if (turnManager != null) {
            turnManager.setAiEntityRegistrationService(aiEntityRegistrationService);
        }
        if (fleetManager != null) {
            fleetManager.setAiEntityRegistrationService(aiEntityRegistrationService);
        }
        // Propagate to all empires' armies
        for (Empire empire : empires.values()) {
            empire.setAiEntityRegistrationService(aiEntityRegistrationService);
        }
    }

    /**
     * Sets the AI commander experience service (injected by Spring when available).
     * Propagates the service to sub-managers that also need it.
     */
    public void setAiCommanderExperienceService(AiCommanderExperienceSpi aiCommanderExperienceService) {
        this.aiCommanderExperienceService = aiCommanderExperienceService;
        if (turnManager != null) {
            turnManager.setAiCommanderExperienceService(aiCommanderExperienceService);
        }
        // Propagate GameContext for AI Core tier sync
        if (aiCommanderExperienceService != null) {
            aiCommanderExperienceService.setGameContext(this);
        }
    }

    /**
     * Sets the station AI support service (injected by Spring when available).
     * Propagates the service to sub-managers that also need it.
     */
    public void setStationAiSupportService(StationAiSupportSpi stationAiSupportService) {
        this.stationAiSupportService = stationAiSupportService;
        if (combatResolver != null) {
            combatResolver.setStationAiSupportService(stationAiSupportService);
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

    // ---- Fleet management (delegated) ----

    /**
     * Creates and adds a new fleet with an auto-generated ID.
     */
    public Fleet createFleet(String name, Player owner) {
        return fleetManager.createFleet(name, owner);
    }

    /**
     * Creates starting fleets for a player.
     */
    public List<Fleet> createStartingFleets(Player player) {
        return fleetManager.createStartingFleets(player);
    }

    /**
     * Creates starting fleets for a player, using the given Galaxy for solar system lookup.
     */
    public List<Fleet> createStartingFleets(Galaxy galaxy, Player player) {
        return fleetManager.createStartingFleets(galaxy, player);
    }

    /**
     * Creates a basic military frigate with beam weapons and shields.
     */
    public Ship createMilitaryFrigate(String name) {
        return fleetManager.createMilitaryFrigate(name);
    }

    /**
     * Creates a basic military destroyer with beam weapons and shields.
     */
    public Ship createMilitaryDestroyer(String name) {
        return fleetManager.createMilitaryDestroyer(name);
    }

    /**
     * Creates a basic mining frigate for resource exploration.
     */
    public Ship createMiningFrigate(String name) {
        return fleetManager.createMiningFrigate(name);
    }

    // ---- Planet management (delegated) ----

    /**
     * Adds a planet to the game and assigns it to a player.
     */
    public Planet addPlanet(Planet planet, Player owner) {
        return planetManager.addPlanet(planet, owner);
    }

    /**
     * Creates and adds a new planet with an auto-generated ID.
     */
    public Planet createPlanet(String name, PlanetSize size, MineralQuality mineralQuality, Player owner) {
        return planetManager.createPlanet(name, size, mineralQuality, owner);
    }

    /**
     * Transfers ownership of a planet from one player to another.
     */
    public boolean transferPlanet(int planetId, Player newOwner) {
        return planetManager.transferPlanet(planetId, newOwner);
    }

    /**
     * Sets up the home system for a player with default starting facilities.
     */
    public Planet setupHomeSystem(Player player) {
        return planetManager.setupHomeSystem(player);
    }

    /**
     * Sets up the home system for a player, registering with the given Galaxy.
     */
    public Planet setupHomeSystem(Galaxy galaxy, Player player) {
        return planetManager.setupHomeSystem(galaxy, player);
    }

    /**
     * Sets up the home system for a player with custom properties, registering with the given Galaxy.
     */
    public Planet setupHomeSystem(Galaxy galaxy, Player player, PlanetSize size, MineralQuality mineralQuality,
                                  StartingFacility... facilities) {
        return planetManager.setupHomeSystem(galaxy, player, size, mineralQuality, facilities);
    }

    /**
     * Sets up the home system for a player with custom planet properties and starting facilities.
     */
    public Planet setupHomeSystem(Player player, PlanetSize size, MineralQuality mineralQuality,
                                  StartingFacility... facilities) {
        return planetManager.setupHomeSystem(player, size, mineralQuality, facilities);
    }

    // ---- Combat (delegated) ----

    /**
     * Engages two fleets in combat using the Battle system.
     * Creates a battle at the attacker's position and fights it to completion.
     */
    public Battle.BattleResult engageFleets(Fleet attacker, Fleet defender) {
        Battle battle = combatResolver.createBattle(attacker, defender, attacker.getPosition());
        return battle.fightToCompletion();
    }

    /**
     * Creates a new structured battle between two fleets at the given location.
     */
    public Battle createBattle(Fleet attacker, Fleet defender, Hex3D location) {
        return combatResolver.createBattle(attacker, defender, location);
    }

    /**
     * Creates a new structured battle with a defender planet for bonus damage reduction.
     */
    public Battle createBattle(Fleet attacker, Fleet defender, Hex3D location, Planet defenderPlanet) {
        return combatResolver.createBattle(attacker, defender, location, defenderPlanet);
    }

    /**
     * Fights a battle to completion and returns the result.
     */
    public Battle.BattleResult fightBattle(int battleId) {
        return combatResolver.fightBattle(battleId);
    }

    /**
     * Fights a battle one round at a time.
     */
    public Battle.RoundResult fightBattleRound(int battleId) {
        return combatResolver.fightBattleRound(battleId);
    }

    /**
     * Starts a battle that has been created but not yet started.
     */
    public void startBattle(int battleId) {
        combatResolver.startBattle(battleId);
    }

    /**
     * Retreats from an active battle, ending it prematurely.
     */
    public void retreatBattle(int battleId) {
        combatResolver.retreatBattle(battleId);
    }

    /**
     * Adds reinforcement ships to the attacker side of an active battle.
     */
    public int reinforceAttacker(int battleId, Fleet reinforcements) {
        return combatResolver.reinforceAttacker(battleId, reinforcements);
    }

    /**
     * Adds reinforcement ships to the defender side of an active battle.
     */
    public int reinforceDefender(int battleId, Fleet reinforcements) {
        return combatResolver.reinforceDefender(battleId, reinforcements);
    }

    /**
     * Returns all active (in-progress) battles.
     */
    public List<Battle> getActiveBattles() {
        return combatResolver.getActiveBattles();
    }

    /**
     * Returns all completed battles.
     */
    public List<Battle> getCompletedBattles() {
        return combatResolver.getCompletedBattles();
    }

    // ---- Galaxy management (delegated) ----

    /**
     * Returns the number of galaxies in the game.
     */
    public int getGalaxyCount() {
        return galaxyManager.getGalaxyCount();
    }

    /**
     * Creates and adds a new galaxy with the given ID and default settings.
     */
    public Galaxy createGalaxy(int id) {
        return galaxyManager.createGalaxy(id);
    }

    /**
     * Creates and adds a new galaxy with the given ID and name.
     */
    public Galaxy createGalaxy(int id, String name) {
        return galaxyManager.createGalaxy(id, name);
    }

    /**
     * Removes a galaxy from the game.
     */
    public Galaxy removeGalaxy(int galaxyId) {
        return galaxyManager.removeGalaxy(galaxyId);
    }

    /**
     * Returns a solar system by ID, or null if not found.
     */
    public SolarSystem getSolarSystem(int systemId) {
        return galaxyManager.getSolarSystem(systemId);
    }

    /**
     * Returns all solar systems across all galaxies.
     */
    public List<SolarSystem> getAllSolarSystems() {
        return galaxyManager.getAllSolarSystems();
    }

    /**
     * Returns the total number of solar systems across all galaxies.
     */
    public int getTotalSolarSystemCount() {
        return galaxyManager.getTotalSolarSystemCount();
    }

    /**
     * Returns the total number of celestial bodies across all galaxies.
     */
    public int getTotalBodyCount() {
        return galaxyManager.getTotalBodyCount();
    }

    /**
     * Returns the total number of stars across all galaxies.
     */
    public int getTotalStarCount() {
        return galaxyManager.getTotalStarCount();
    }

    /**
     * Returns the total number of planets across all galaxies.
     */
    public int getTotalPlanetCount() {
        return galaxyManager.getTotalPlanetCount();
    }

    /**
     * Finds a solar system by ID across all galaxies.
     */
    public SolarSystem findSolarSystem(int systemId) {
        return galaxyManager.findSolarSystem(systemId);
    }

    /**
     * Returns all hyperlanes across all galaxies.
     */
    public List<Hyperlane> getAllHyperlanes() {
        return galaxyManager.getAllHyperlanes();
    }

    // ---- Manager accessors (for external code that needs direct access) ----

    /** Returns the CharacterManager instance. */
    public CharacterManager getCharacterManager() {
        return characterManager;
    }

    /** Returns the TurnManager instance. */
    public TurnManager getTurnManager() {
        return turnManager;
    }

    /** Returns the ResourceManager instance. */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    /** Returns the FleetManager instance. */
    public FleetManager getFleetManager() {
        return fleetManager;
    }

    /** Returns the CombatResolver instance. */
    public CombatResolver getCombatResolver() {
        return combatResolver;
    }

    /** Returns the GalaxyManager instance. */
    public GalaxyManager getGalaxyManager() {
        return galaxyManager;
    }

    /** Returns the PlanetManager instance. */
    public PlanetManager getPlanetManager() {
        return planetManager;
    }

    /** Returns the TradeManager instance. */
    public TradeManager getTradeManager() {
        return tradeManager;
    }

    /** Returns the MercenaryIncomeManager instance. */
    public MercenaryIncomeManager getMercenaryIncomeManager() {
        return mercenaryIncomeManager;
    }

    /** Returns the TaxManager instance. */
    public TaxManager getTaxManager() {
        return taxManager;
    }

    // ============================================================
    // Survey Missions
    // ============================================================

    /**
     * Launches a survey mission to a celestial body.
     *
     * @param empireId the empire conducting the survey
     * @param bodyId the celestial body ID to survey
     * @param type the type of survey
     * @return the created survey mission, or null if the body is not found
     */
    public SurveyMission launchSurvey(int empireId, int bodyId, SurveyMission.SurveyType type) {
        CelestialBody body = findCelestialBody(bodyId);
        if (body == null) {
            return null;
        }
        SurveyMission mission = new SurveyMission(nextSurveyMissionId++, empireId, body, type, random);
        activeSurveyMissions.add(mission);
        return mission;
    }

    /**
     * Processes all active survey missions for one turn.
     * Completed missions are removed from the active list.
     *
     * @return the number of missions that completed this turn
     */
    public int processSurveyMissions() {
        int completed = 0;
        Iterator<SurveyMission> it = activeSurveyMissions.iterator();
        while (it.hasNext()) {
            SurveyMission mission = it.next();
            if (mission.processTurn()) {
                it.remove();
                completed++;
            }
        }
        return completed;
    }

    /**
     * Returns all active survey missions.
     *
     * @return list of active survey missions
     */
    public List<SurveyMission> getActiveSurveyMissions() {
        return new ArrayList<>(activeSurveyMissions);
    }

    /**
     * Returns active survey missions for a specific empire.
     *
     * @param empireId the empire ID
     * @return list of active survey missions for that empire
     */
    public List<SurveyMission> getActiveSurveyMissions(int empireId) {
        return activeSurveyMissions.stream()
                .filter(m -> m.getEmpireId() == empireId)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Finds a celestial body by ID across all solar systems.
     *
     * @param bodyId the body ID
     * @return the celestial body, or null if not found
     */
    private CelestialBody findCelestialBody(int bodyId) {
        // Check planets first
        Planet planet = planets.get(bodyId);
        if (planet != null) return planet;

        // Check solar systems for other bodies
        for (SolarSystem system : solarSystems.values()) {
            for (SolarSystemEntity entity : system.getAllSystems().values()) {
                for (SystemBody sb : entity.getAllBodies()) {
                    CelestialBody body = sb.getBodyAs(CelestialBody.class);
                    if (body != null && body.getId() == bodyId) {
                        return body;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Serializes the essential game state to a JSON-compatible Map.
     * <p>
     * Delegates to {@link GameStateSerializer} for persistence logic.
     * Galaxy/solar system data is NOT serialized here as it is stored separately
     * in the database by GalaxyService.
     * </p>
     *
     * @return a map representing the serializable game state
     */
    public Map<String, Object> serializeGameState() {
        return gameStateSerializer.serialize(this);
    }

    /**
     * Returns the random seed used for this game (0 = non-deterministic).
     */
    public long getRandomSeed() {
        return randomSeed;
    }

    /**
     * Returns whether B07 Actual Value rolls are enabled for trade price calculation.
     * When enabled, prices vary from 40% to 400% of base price based on a 2D6 roll.
     * @return true if Actual Value rolls are enabled
     */
    public boolean isActualValueEnabled() {
        return actualValueEnabled;
    }

    /**
     * Sets whether B07 Actual Value rolls are enabled for trade price calculation.
     * @param actualValueEnabled true to enable random price variation
     */
    public void setActualValueEnabled(boolean actualValueEnabled) {
        this.actualValueEnabled = actualValueEnabled;
        this.tradeManager.setActualValueEnabled(actualValueEnabled);
    }

    // ---- Traveller RPG Integration Methods ----

    /**
     * Creates a ship from a Traveller ship design.
     * @param player the player creating the ship
     * @param request the ship design request
     * @return the new ship ID
     */
    public int createShipFromDesign(Player player, com.yisy.modules.doramcu.dto.ShipDesignRequest request) {
        // Use TravellerShipFactory to convert design to ship
        int shipId = fleetManager.createShipFromDesign(player, request);
        return shipId;
    }

    /**
     * Buys a commodity at a world.
     * @param player the player buying
     * @param worldId the world ID
     * @param commodity the commodity name
     * @param quantity the quantity to buy
     * @return the total cost in credits
     */
    public int buyCommodity(Player player, int worldId, String commodity, int quantity) {
        return tradeManager.buyCommodity(player, worldId, commodity, quantity);
    }

    /**
     * Sells a commodity at a world.
     * @param player the player selling
     * @param worldId the world ID
     * @param commodity the commodity name
     * @param quantity the quantity to sell
     * @return the total revenue in credits
     */
    public int sellCommodity(Player player, int worldId, String commodity, int quantity) {
        return tradeManager.sellCommodity(player, worldId, commodity, quantity);
    }

    @Override
    public String toString() {
        return String.format("Game{name='%s', time=%ds (turn %d), players=%d, galaxies=%d, planets=%d, fleets=%d, battles=%d, seed=%d}",
                name, currentTime, getCurrentTurn(), players.size(), galaxies.size(), planets.size(), fleets.size(), battles.size(), randomSeed);
    }
}
