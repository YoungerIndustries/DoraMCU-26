package com.yisy.modules.doramcu.hex.planet;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Planet;

import java.util.List;
import java.util.Map;

/**
 * Defines the operations an Empire (Galaxy Overlord) can perform on a planet surface.
 * <p>
 * This interface bridges the gap between the empire-level game logic and the
 * planet surface building system. It allows empires to:
 * <ul>
 *   <li>Generate biomes and terrain on a planet surface</li>
 *   <li>Found and manage settlements</li>
 *   <li>Build and manage industries</li>
 *   <li>Construct transportation infrastructure (roads, rails, stations)</li>
 *   <li>Deploy and manage surface vehicles</li>
 * </ul>
 * <p>
 * Implementations handle the actual placement of {@link TerrainData}, {@link Settlement},
 * {@link Industry}, {@link Station}, and {@link SurfaceVehicle} objects on the planet's
 * {@link Hex3D} crust layer.
 */
public interface PlanetSurfaceBuilder {

    // ---- Biome Generation ----

    /**
     * Generates biomes and terrain for the entire planet surface.
     * <p>
     * Populates each {@link Hex3D} cell on the planet's crust layer with
     * {@link TerrainData} containing biome information.
     *
     * @param planet the planet to generate terrain for
     * @param seed   the random seed for procedural generation
     * @return the number of hex cells processed
     */
    int generateBiomes(Planet planet, long seed);

    /**
     * Returns the terrain data for a specific hex cell.
     *
     * @param hex the hex cell
     * @return the terrain data, or null if not yet generated
     */
    TerrainData getTerrainData(Hex3D hex);

    // ---- Settlement Management ----

    /**
     * Found a new settlement on the planet surface.
     * <p>
     * Validates that the location is on land, has suitable biome, and is not
     * overlapping with existing settlements.
     *
     * @param planet      the planet
     * @param name        settlement name
     * @param centerHex   the center hex cell for the settlement
     * @param empireId    the empire ID that owns this settlement
     * @return the newly created settlement
     * @throws IllegalArgumentException if the location is invalid
     */
    Settlement foundSettlement(Planet planet, String name, Hex3D centerHex, String empireId);

    /**
     * Returns all settlements on a planet owned by a specific empire.
     *
     * @param planet   the planet
     * @param empireId the empire ID
     * @return list of settlements owned by the empire
     */
    List<Settlement> getSettlements(Planet planet, String empireId);

    /**
     * Returns all settlements on a planet.
     *
     * @param planet the planet
     * @return list of all settlements
     */
    List<Settlement> getAllSettlements(Planet planet);

    /**
     * Removes a settlement from the planet.
     *
     * @param settlementId the settlement ID to remove
     * @return true if the settlement was found and removed
     */
    boolean removeSettlement(String settlementId);

    // ---- Industry Management ----

    /**
     * Build an industry on the planet surface.
     * <p>
     * Validates that the industry type can be placed on the given hex cell
     * (terrain compatibility, resource availability, etc.).
     *
     * @param planet     the planet
     * @param industryType the type of industry to build
     * @param hex        the hex cell to build on
     * @param empireId   the empire ID that owns this industry
     * @return the newly created industry
     * @throws IllegalArgumentException if the location is invalid
     */
    Industry buildIndustry(Planet planet, IndustryType industryType, Hex3D hex, String empireId);

    /**
     * Returns all industries on a planet owned by a specific empire.
     *
     * @param planet   the planet
     * @param empireId the empire ID
     * @return list of industries owned by the empire
     */
    List<Industry> getIndustries(Planet planet, String empireId);

    /**
     * Returns all industries on a planet.
     *
     * @param planet the planet
     * @return list of all industries
     */
    List<Industry> getAllIndustries(Planet planet);

    /**
     * Removes an industry from the planet.
     *
     * @param industryId the industry ID to remove
     * @return true if the industry was found and removed
     */
    boolean removeIndustry(String industryId);

    // ---- Station Management ----

    /**
     * Build a station (cargo loading point) on the planet surface.
     *
     * @param planet     the planet
     * @param name       station name
     * @param hex        the hex cell to build on
     * @param empireId   the empire ID that owns this station
     * @return the newly created station
     */
    default Station buildStation(Planet planet, String name, Hex3D hex, String empireId) {
        return buildStation(planet, name, hex, empireId, Station.StationType.GENERAL);
    }

    /**
     * Build a station with a specific type on the planet surface.
     *
     * @param planet     the planet
     * @param name       station name
     * @param hex        the hex cell to build on
     * @param empireId   the empire ID that owns this station
     * @param stationType the type of station (bus stop, rail station, port, airport, etc.)
     * @return the newly created station
     */
    Station buildStation(Planet planet, String name, Hex3D hex, String empireId, Station.StationType stationType);

    /**
     * Returns all stations on a planet owned by a specific empire.
     *
     * @param planet   the planet
     * @param empireId the empire ID
     * @return list of stations owned by the empire
     */
    List<Station> getStations(Planet planet, String empireId);

    /**
     * Removes a station from the planet.
     *
     * @param stationId the station ID to remove
     * @return true if the station was found and removed
     */
    boolean removeStation(String stationId);

    // ---- Infrastructure Management ----

    /**
     * Build infrastructure (road, rail, etc.) on a hex cell.
     *
     * @param hex        the hex cell
     * @param infraType  the infrastructure type
     * @param empireId   the empire ID that owns this infrastructure
     * @return true if the infrastructure was built successfully
     */
    boolean buildInfrastructure(Hex3D hex, TerrainData.InfrastructureType infraType, String empireId);

    /**
     * Remove infrastructure from a hex cell.
     *
     * @param hex the hex cell
     * @return true if infrastructure was removed
     */
    boolean removeInfrastructure(Hex3D hex);

    // ---- Vehicle Management ----

    /**
     * Deploy a surface vehicle on the planet.
     *
     * @param planet     the planet
     * @param vehicleType the type of vehicle
     * @param startStation the starting station
     * @param empireId   the empire ID that owns this vehicle
     * @return the newly created vehicle
     */
    SurfaceVehicle deployVehicle(Planet planet, VehicleType vehicleType, Station startStation, String empireId);

    /**
     * Returns all vehicles on a planet owned by a specific empire.
     *
     * @param planet   the planet
     * @param empireId the empire ID
     * @return list of vehicles owned by the empire
     */
    List<SurfaceVehicle> getVehicles(Planet planet, String empireId);

    /**
     * Removes a vehicle from the planet.
     *
     * @param vehicleId the vehicle ID to remove
     * @return true if the vehicle was found and removed
     */
    boolean removeVehicle(String vehicleId);

    // ---- Turn Processing ----

    /**
     * Process one turn for all surface elements on a planet.
     * <p>
     * This processes:
     * <ul>
     *   <li>Settlement growth (population increase)</li>
     *   <li>Industry production (cargo output)</li>
     *   <li>Vehicle movement and cargo delivery</li>
     *   <li>Contract expiration and auto-fail for expired AVAILABLE/ACTIVE contracts</li>
     *   <li>Quest expiration and auto-fail for expired AVAILABLE/IN_PROGRESS quests</li>
     *   <li>Trade route revenue generation and cargo delivery</li>
     *   <li>Corporation credit accumulation from completed contracts and trade routes</li>
     * </ul>
     *
     * @param planet the planet
     * @return a summary map with turn processing statistics (contracts failed, quests failed, revenue earned, etc.)
     */
    Map<String, Long> processTurn(Planet planet);

    // ---- Event Management ----

    /**
     * Returns the event manager for this surface builder.
     *
     * @return the planet event manager
     */
    PlanetEventManager getEventManager();

    // ---- 3D City Building ----

    /**
     * Builds a complete city on a planet surface for 3D adventuring.
     * <p>
     * The Galaxy Overlord can request an entire city for use in 3D adventuring.
     * The city includes buildings, roads, settlements, and points of interest
     * suitable for player exploration.
     * </p>
     *
     * @param planet      the planet where the city is built
     * @param cityName    the name of the city
     * @param centerHex   the center hex cell for the city
     * @param empireId    the empire ID that owns this city
     * @param districtCount the number of districts to generate
     * @return the newly created settlement representing the city center
     */
    Settlement buildCity(Planet planet, String cityName, Hex3D centerHex,
                          String empireId, int districtCount);

    /**
     * Automatically populates a planet with settlements, industries, stations,
     * and vehicles after biome generation.
     * <p>
     * This method places settlements on suitable terrain, then builds industries
     * near settlements, constructs stations at settlement centers, and deploys
     * vehicles to connect the transport network. It uses the given seed for
     * deterministic placement so the same planet always gets the same layout.
     * </p>
     *
     * @param planet   the planet to populate
     * @param seed     random seed for deterministic placement
     * @param empireId the empire ID that owns all placed objects
     * @return a summary map with counts of each object type placed
     */
    Map<String, Integer> autoPopulatePlanet(Planet planet, long seed, String empireId);

    // ---- Persistence helpers ----

    /**
     * Returns all terrain data for this surface builder.
     */
    Map<String, TerrainData> getAllTerrainData();

    /**
     * Returns all stations on a planet.
     */
    List<Station> getAllStations(Planet planet);

    /**
     * Returns all vehicles on a planet.
     */
    List<SurfaceVehicle> getAllVehicles(Planet planet);

    /**
     * Returns all transport corporations on a planet.
     */
    List<TransportCorporation> getAllCorporations(Planet planet);

    /**
     * Returns all delivery contracts on a planet.
     */
    List<DeliveryContract> getAllContracts(Planet planet);

    /**
     * Add a delivery contract to a planet.
     */
    void addContract(Planet planet, DeliveryContract contract);

    /**
     * Returns all quests on a planet.
     */
    List<PlanetQuest> getAllQuests(Planet planet);

    /**
     * Add a quest to a planet.
     */
    void addQuest(Planet planet, PlanetQuest quest);

    /**
     * Returns all trade routes on a planet.
     */
    List<CampaignTradeRoute> getAllTradeRoutes(Planet planet);

    /**
     * Add a trade route to a planet.
     */
    void addTradeRoute(Planet planet, CampaignTradeRoute route);
}
