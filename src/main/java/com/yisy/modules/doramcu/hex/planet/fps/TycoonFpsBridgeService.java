package com.yisy.modules.doramcu.hex.planet.fps;

import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.ShipType;
import com.yisy.modules.doramcu.hex.StarBase;
import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModuleService;
import com.yisy.modules.doramcu.hex.planet.hullmodule.dto.HullModuleDto;
import com.yisy.modules.doramcu.hex.planet.military.MilitaryProductionService;
import com.yisy.modules.doramcu.hex.planet.military.UnifiedBattleResolver;
import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import com.yisy.modules.doramcu.thirdreich.service.CountryCampaignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Bridges tycoon state to the FPS battlefield.
 * <p>
 * The service:
 * <ul>
 *   <li>Exposes tycoon state to FPS renderer</li>
 *   <li>Converts Hex3D coordinates to 3D positions</li>
 *   <li>Provides world state snapshots</li>
 *   <li>Tracks vehicle position updates</li>
 * </ul>
 * </p>
 */
@Service
public class TycoonFpsBridgeService {

    private static final Logger log = LoggerFactory.getLogger(TycoonFpsBridgeService.class);

    private final HullModuleService hullModuleService;
    private final MilitaryProductionService militaryProductionService;
    private final UnifiedBattleResolver battleResolver;
    private final CountryCampaignService countryCampaignService;

    // In-memory storage for ships and starbases (populated by planet generation)
    private final Map<Long, List<Ship>> planetShips = new HashMap<>();
    private final Map<Long, List<StarBase>> planetStarbases = new HashMap<>();

    public TycoonFpsBridgeService(HullModuleService hullModuleService,
                                    MilitaryProductionService militaryProductionService,
                                    UnifiedBattleResolver battleResolver,
                                    CountryCampaignService countryCampaignService) {
        this.hullModuleService = hullModuleService;
        this.militaryProductionService = militaryProductionService;
        this.battleResolver = battleResolver;
        this.countryCampaignService = countryCampaignService;
    }

    /**
     * Register ships for a planet.
     */
    public void registerShips(Long planetId, List<Ship> ships) {
        planetShips.put(planetId, new ArrayList<>(ships));
    }

    /**
     * Register starbases for a planet.
     */
    public void registerStarbases(Long planetId, List<StarBase> starbases) {
        planetStarbases.put(planetId, new ArrayList<>(starbases));
    }

    /**
     * Gets a complete world state snapshot for the FPS renderer.
     *
     * @param builder the planet surface builder
     * @param planetId the planet ID
     * @param currentTurn the current turn number
     * @return the world state snapshot
     */
    public FpsWorldStateDto getWorldState(DefaultPlanetSurfaceBuilder builder,
                                           int planetId, int currentTurn) {
        Planet planet = new Planet(planetId);

        FpsWorldStateDto state = new FpsWorldStateDto(planetId, currentTurn);

        // Vehicles
        for (SurfaceVehicle vehicle : builder.getAllVehicles(planet)) {
            com.yisy.modules.doramcu.hex.Hex3D pos = vehicle.getCurrentPosition();
            state.addVehicle(new FpsVehicleStateDto(
                    vehicle.getId(),
                    vehicle.getType().getDisplayName(),
                    pos.getX(), pos.getY(), pos.getZ(),
                    vehicle.isActive(),
                    vehicle.getReliability(),
                    vehicle.getTotalCargoOnBoard(),
                    vehicle.getPassengerCount()
            ));
        }

        // Industries
        for (Industry industry : builder.getAllIndustries(planet)) {
            com.yisy.modules.doramcu.hex.Hex3D pos = industry.getLocation();
            state.addIndustry(new FpsIndustryStateDto(
                    industry.getId(),
                    industry.getType().name(),
                    industry.getType().getDisplayName(),
                    pos.getX(), pos.getY(), pos.getZ(),
                    industry.isActive(),
                    calculateWorkerCount(industry),
                    calculateProductionLevel(industry)
            ));
        }

        // Stations
        for (Station station : builder.getAllStations(planet)) {
            com.yisy.modules.doramcu.hex.Hex3D pos = station.getPosition();
            state.addStation(new FpsStationStateDto(
                    station.getId(),
                    station.getName(),
                    station.getStationType() != null ? station.getStationType().name() : "GENERAL",
                    pos.getX(), pos.getY(), pos.getZ(),
                    station.getTotalCargoWaiting(),
                    station.getPassengersWaiting()
            ));
        }

        // Hull Modules - unified building structures (industries, stations, residential, HQ hubs)
        // All buildings are now created as HullModule instances by DefaultPlanetSurfaceBuilder
        List<HullModuleDto> hullModules = hullModuleService.getModulesByPlanet((long) planetId);
        for (HullModuleDto hm : hullModules) {
            if (!hm.isActive()) continue;
            state.addHullModule(new FpsHullModuleStateDto(
                    hm.getId().toString(),
                    hm.getName(),
                    hm.getModuleType(),
                    hm.getCategory(),
                    hm.getHexX(), hm.getHexY(), hm.getHexZ(),
                    hm.getFloors(), hm.getBasementLevels(),
                    hm.getTotalCapacity(), hm.getOccupiedCapacity(),
                    hm.getOccupancyRate(),
                    hm.getProductionLevel(),
                    hm.getOwnerId(),
                    hm.isActive(),
                    hm.getDungeonId()
            ));
        }

        // Ships
        List<Ship> ships = planetShips.getOrDefault((long) planetId, List.of());
        for (Ship ship : ships) {
            // Position ships in orbit above the planet (offset z)
            int shipId = ship.getId();
            int shipX = (shipId & 0xFF);
            int shipY = ((shipId >> 8) & 0xFF);
            int shipZ = 100; // Orbit altitude
            ShipType shipType = ship.getShipType();
            state.addShip(new FpsShipStateDto(
                    String.valueOf(shipId),
                    ship.getName(),
                    shipType.name(),
                    shipX, shipY, shipZ,
                    ship.getCurrentHullPoints(), ship.getMaxHullPoints(),
                    ship.getCurrentShieldPoints(), ship.getMaxShieldPoints(),
                    ship.getTotalDamage(),
                    shipType.getMDriveSpeed(), shipType.getJDriveSpeed(),
                    shipType.getBaseEvasion(),
                    "SYSTEM", // Ship owner not tracked in Ship model
                    ship.getCurrentHullPoints() > 0
            ));
        }

        // Starbases
        List<StarBase> starbases = planetStarbases.getOrDefault((long) planetId, List.of());
        for (StarBase starbase : starbases) {
            int sbX = (starbase.getId() & 0xFF);
            int sbY = ((starbase.getId() >> 8) & 0xFF);
            int sbZ = 150; // Higher orbit
            state.addStarbase(new FpsStarBaseStateDto(
                    String.valueOf(starbase.getId()),
                    starbase.getName(),
                    starbase.getType().name(),
                    sbX, sbY, sbZ,
                    starbase.getModuleCount(), starbase.getDockSlots(), starbase.getShipsDocked(),
                    starbase.getDefenseRating(), starbase.getCurrentHitPoints(), starbase.getMaxHitPoints(),
                    String.valueOf(starbase.getOwnerId()),
                    starbase.getPlanetId(),
                    starbase.getCurrentHitPoints() > 0
            ));
        }

        // Cargo at stations
        for (Station station : builder.getAllStations(planet)) {
            com.yisy.modules.doramcu.hex.Hex3D pos = station.getPosition();
            if (station.getTotalCargoWaiting() > 0) {
                state.addCargo(new FpsCargoStateDto(
                        "CARGO", station.getTotalCargoWaiting(),
                        pos.getX(), pos.getY(), pos.getZ(),
                        station.getName(), "MIXED", "#8B4513"
                ));
            }
        }

        // Military units
        List<ThirdReichGroundUnit> allUnits = militaryProductionService.getAllUnits();
        for (ThirdReichGroundUnit unit : allUnits) {
            // Position military units at their base hex (use hash for distribution)
            int unitX = (unit.getId().hashCode() & 0xFF) % 50;
            int unitY = ((unit.getId().hashCode() >> 8) & 0xFF) % 50;
            state.addMilitaryUnit(new FpsMilitaryUnitStateDto(
                    unit.getId(), unit.getName(),
                    unit.getUnitType().name(), "ARMY",
                    unitX, unitY, 0,
                    unit.getStrength(), unit.getMaxStrength(),
                    unit.getMorale(), unit.getAttackValue(), unit.getDefenseValue(),
                    unit.getSourceGame() != null ? unit.getSourceGame() : "SYSTEM",
                    unit.isCombatEffective(), false
            ));
        }

        // Active battles
        List<UnifiedBattleResolver.BattleRecord> recentBattles = battleResolver.getRecentBattles(10);
        for (UnifiedBattleResolver.BattleRecord record : recentBattles) {
            state.addBattle(new FpsBattleStateDto(
                    record.getBattleId(), record.getBattleType().name(),
                    record.getAttackerFaction(), record.getDefenderFaction(),
                    0, 0, 0, // Position would come from battle request
                    "UNKNOWN", "COMPLETED",
                    record.getRounds(), record.getOutcome().name(),
                    record.getAttackerCasualties(), record.getDefenderCasualties(),
                    0, 0,
                    Collections.emptyList()
            ));
        }

        // AI Commanders from country campaigns
        List<CountryCampaign> campaigns = countryCampaignService.getAllCampaigns();
        int commanderIndex = 0;
        for (CountryCampaign campaign : campaigns) {
            AICommander commander = campaign.getCommander();
            if (commander == null) continue;

            // Position commanders near their country's territory (spread around planet)
            int cmdX = (commanderIndex * 7) % 50 - 25;
            int cmdY = (commanderIndex * 11) % 50 - 25;
            int cmdZ = 0;

            state.addAiCommander(new FpsAICommanderStateDto(
                    commander.getId(),
                    commander.getName(),
                    campaign.getIsoCode(),
                    campaign.getCountryName(),
                    commander.getLeadership(),
                    commander.getTacticalPreference() != null ? commander.getTacticalPreference().name() : "BALANCED",
                    10 + commander.getLeadership(), // Command radius scales with leadership
                    campaign.getArmyUnits().size(),
                    cmdX, cmdY, cmdZ,
                    !recentBattles.isEmpty(), // In combat if any battles active
                    commander.getTotalAttackBonus(),
                    commander.getTotalDefenseBonus()
            ));

            // Battlefield units for this campaign
            int unitIndex = 0;
            for (ThirdReichGroundUnit unit : campaign.getArmyUnits()) {
                if (!unit.isCombatEffective()) continue;
                // Spread units around commander position
                int unitX = cmdX + (unitIndex % 5) - 2;
                int unitY = cmdY + (unitIndex / 5) - 1;
                state.addBattlefieldUnit(new FpsBattlefieldUnitStateDto(
                        unit.getId(),
                        unit.getName(),
                        unit.getUnitType().name(),
                        campaign.getIsoCode(),
                        campaign.getCountryName(),
                        commander.getId(),
                        unit.getStrength(),
                        unit.getMaxStrength(),
                        unit.getMorale(),
                        unitX, unitY, cmdZ,
                        unitIndex % 5, unitIndex / 5, // Formation position
                        !recentBattles.isEmpty(),
                        false,
                        null
                ));
                unitIndex++;
            }
            commanderIndex++;
        }

        log.debug("Generated world state: {} vehicles, {} industries, {} stations, {} hull modules, {} ships, {} starbases, {} military units, {} battles, {} commanders, {} battlefield units",
                state.getVehicles().size(), state.getIndustries().size(),
                state.getStations().size(), state.getHullModules().size(),
                state.getShips().size(), state.getStarbases().size(),
                state.getMilitaryUnits().size(), state.getBattles().size(),
                state.getAiCommanders().size(), state.getBattlefieldUnits().size());

        return state;
    }

    /**
     * Converts Hex3D coordinates to 3D world coordinates.
     * <p>
     * Uses axial-to-cartesian conversion for isometric projection.
     * </p>
     *
     * @param hex the hex coordinate
     * @return the 3D position as a double array [x, y, z]
     */
    public double[] hexTo3D(com.yisy.modules.doramcu.hex.Hex3D hex) {
        double hexSize = 10.0;
        double x = hexSize * (Math.sqrt(3) * hex.getX() + Math.sqrt(3) / 2.0 * hex.getY());
        double z = hexSize * (3.0 / 2.0 * hex.getY());
        double y = hex.getZ() * 5.0; // Height based on z coordinate
        return new double[]{x, y, z};
    }

    /**
     * Calculates the number of workers at an industry.
     */
    private int calculateWorkerCount(Industry industry) {
        IndustryType type = industry.getType();
        switch (type.getLevel()) {
            case PRIMARY: return 5;
            case SECONDARY: return 10;
            case TERTIARY: return 15;
            default: return 5;
        }
    }

    /**
     * Calculates the production level of an industry (0-100).
     */
    private int calculateProductionLevel(Industry industry) {
        if (!industry.isActive()) return 0;
        // Simplified: active industries produce at 80-100%
        return 80 + (industry.getId().hashCode() % 21);
    }
}
