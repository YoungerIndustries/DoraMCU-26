package com.yisy.modules.doramcu.babylon5.service;

import com.yisy.modules.doramcu.babylon5.model.B5Location;
import com.yisy.modules.doramcu.babylon5.model.B5Race;
import com.yisy.modules.doramcu.hex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Builds empires from Babylon 5 race data and maps B5 locations to galaxy systems.
 * <p>
 * This service reads B5 race and location data from {@link Babylon5DatabaseService}
 * and creates {@link Empire} instances with appropriate stats derived from the race
 * characteristics (combatStyle, tradeBonus, socialDM, navigationDM, armorBonus).
 * </p>
 * <p>
 * Each B5 race becomes an empire with:
 * <ul>
 *   <li>Empire name = race name (e.g., "Narn", "Centauri")</li>
 *   <li>Capital planet = race homeworld (e.g., "Narn 3", "Centauri Prime")</li>
 *   <li>Industrial level = derived from race tradeBonus</li>
 *   <li>Shipyard chance = derived from race combatStyle and armorBonus</li>
 * </ul>
 * </p>
 */
@Service
public class Babylon5EmpireBuilder {

    private static final Logger log = LoggerFactory.getLogger(Babylon5EmpireBuilder.class);

    /** Default random seed for reproducible empire building. */
    public static final long DEFAULT_EMPIRE_SEED = 42L;

    private final Babylon5DatabaseService databaseService;
    private final Random random;

    public Babylon5EmpireBuilder(Babylon5DatabaseService databaseService) {
        this(databaseService, DEFAULT_EMPIRE_SEED);
    }

    /**
     * Constructor with custom seed for testing.
     */
    public Babylon5EmpireBuilder(Babylon5DatabaseService databaseService, long seed) {
        this.databaseService = databaseService;
        this.random = new Random(seed);
    }

    /**
     * Builds empires for all B5 races and registers them in the game.
     * <p>
     * Creates one empire per B5 race, sets up home systems, and assigns
     * starting fleets based on race combat style.
     * </p>
     *
     * @param game the game to register empires in
     * @param galaxy the galaxy containing solar systems for home planets
     * @return the list of created empires
     */
    public List<Empire> buildEmpires(Game game, Galaxy galaxy) {
        List<B5Race> races = databaseService.getAllRaces();
        if (races.isEmpty()) {
            log.warn("No B5 races loaded from database. Skipping empire building.");
            return new ArrayList<>();
        }

        List<Empire> empires = new ArrayList<>();
        for (B5Race race : races) {
            try {
                Empire empire = createEmpireFromRace(race, game, galaxy);
                if (empire != null) {
                    empires.add(empire);
                    game.registerEmpire(empire);
                    log.info("Created empire '{}' from B5 race '{}'", empire.getName(), race.getName());
                }
            } catch (Exception e) {
                log.error("Failed to create empire for B5 race '{}': {}", race.getName(), e.getMessage(), e);
            }
        }

        log.info("Built {} empires from {} B5 races", empires.size(), races.size());
        return empires;
    }

    /**
     * Creates an empire from a B5 race.
     * <p>
     * Maps race data to empire properties:
     * <ul>
     *   <li>Empire name = race name</li>
     *   <li>Capital planet = race homeworld</li>
     *   <li>Industrial level = derived from tradeBonus</li>
     *   <li>Shipyard chance = derived from combatStyle and armorBonus</li>
     * </ul>
     * </p>
     *
     * @param race the B5 race to create an empire from
     * @param game the game context
     * @param galaxy the galaxy for home system registration
     * @return the created empire, or null if the race data is incomplete
     */
    public Empire createEmpireFromRace(B5Race race, Game game, Galaxy galaxy) {
        if (race == null || race.getName() == null) {
            log.warn("Skipping empire creation: race is null or has no name");
            return null;
        }

        // Create player for this empire
        String raceName = race.getName();
        Player player = game.createPlayer(raceName);

        // Create empire
        int empireId = game.getNextEmpireIdAndIncrement();
        Empire empire = new Empire(empireId, raceName, player.getId());

        // Set up home system with the race's homeworld as the capital planet
        String homeworld = race.getHomeworld();
        if (homeworld == null || homeworld.isBlank()) {
            homeworld = raceName + " Prime";
        }

        // Determine planet properties based on race stats
        PlanetSize planetSize = determinePlanetSize(race);
        MineralQuality mineralQuality = determineMineralQuality(race);
        StartingFacility[] facilities = determineStartingFacilities(race);

        // Set up home system
        Planet capitalPlanet = game.setupHomeSystem(galaxy, player, planetSize, mineralQuality, facilities);

        // Set capital planet
        if (capitalPlanet != null) {
            // Claim the planet for the empire before setting it as capital
            empire.claimPlanet(capitalPlanet.getId());
            empire.setCapitalPlanet(capitalPlanet.getId());

            // Set industrial level based on trade bonus
            int industrialLevel = calculateIndustrialLevel(race);
            if (industrialLevel > 0) {
                empire.setPlanetIndustrialLevel(capitalPlanet.getId(), industrialLevel);
            }
        }

        // Assign starting fleets based on combat style
        assignStartingFleets(empire, player, race, game);

        return empire;
    }

    /**
     * Assigns starting fleets to an empire based on the race's combat style.
     * <p>
     * Aggressive combat styles get more military ships, while diplomatic styles
     * get more mining/exploration ships.
     * </p>
     *
     * @param empire the empire to assign fleets to
     * @param player the player controlling the empire
     * @param race the B5 race determining fleet composition
     * @param game the game context
     */
    public void assignStartingFleets(Empire empire, Player player, B5Race race, Game game) {
        String combatStyle = race.getCombatStyle();
        int armorBonus = race.getArmorBonus();

        // Determine fleet composition based on combat style
        if (isAggressiveCombatStyle(combatStyle)) {
            // Aggressive races get military fleets
            Fleet fleet = game.createFleet(race.getName() + " Defense Fleet", player);
            Ship frigate = game.createMilitaryFrigate(race.getName() + " Frigate");
            fleet.addShip(frigate);
            Ship destroyer = game.createMilitaryDestroyer(race.getName() + " Destroyer");
            fleet.addShip(destroyer);
            game.addFleet(fleet, player);
            log.debug("Assigned military fleet to aggressive race '{}'", race.getName());
        } else {
            // Non-aggressive races get mining/exploration fleets
            Fleet fleet = game.createFleet(race.getName() + " Exploration Fleet", player);
            Ship miningFrigate = game.createMiningFrigate(race.getName() + " Miner");
            fleet.addShip(miningFrigate);
            game.addFleet(fleet, player);
            log.debug("Assigned mining fleet to non-aggressive race '{}'", race.getName());
        }

        // High armor bonus races get an additional fleet
        if (armorBonus >= 2) {
            Fleet secondFleet = game.createFleet(race.getName() + " Reserve Fleet", player);
            Ship frigate = game.createMilitaryFrigate(race.getName() + " Reserve Frigate");
            secondFleet.addShip(frigate);
            game.addFleet(secondFleet, player);
            log.debug("Assigned reserve fleet to high-armor race '{}'", race.getName());
        }
    }

    /**
     * Finds a solar system in the galaxy that matches a B5 location.
     * <p>
     * Since solar systems do not have names, this method returns the first available
     * solar system in the galaxy. The B5 location's system field is used for logging
     * purposes only.
     * </p>
     *
     * @param location the B5 location to find a system for
     * @param galaxy the galaxy to search
     * @return the first available solar system, or null if no systems are available
     */
    public SolarSystem findHomeSystem(B5Location location, Galaxy galaxy) {
        if (location == null || galaxy == null) {
            return null;
        }

        // Return the first available solar system in the galaxy
        Map<Integer, SolarSystem> allSystems = galaxy.getAllSystems();
        if (!allSystems.isEmpty()) {
            int firstSystemId = allSystems.keySet().iterator().next();
            SolarSystem system = galaxy.getSolarSystem(firstSystemId);
            if (system != null) {
                log.debug("Mapped B5 location '{}' to solar system {}", location.getName(), system.getId());
                return system;
            }
        }

        return null;
    }

    /**
     * Determines the planet size based on the race's characteristics.
     * Races with high ST (Strength) get larger planets.
     */
    private PlanetSize determinePlanetSize(B5Race race) {
        if (race.getCharacteristics() != null) {
            int st = race.getCharacteristics().getST();
            if (st >= 8) return PlanetSize.LARGE;
            if (st >= 6) return PlanetSize.MEDIUM;
        }
        return PlanetSize.SMALL;
    }

    /**
     * Determines the mineral quality based on the race's trade bonus.
     * Higher trade bonus indicates better resource access.
     */
    private MineralQuality determineMineralQuality(B5Race race) {
        int tradeBonus = race.getTradeBonus();
        if (tradeBonus >= 3) return MineralQuality.RICH;
        if (tradeBonus >= 1) return MineralQuality.NORMAL;
        return MineralQuality.POOR;
    }

    /**
     * Determines starting facilities based on the race's combat style and stats.
     */
    private StartingFacility[] determineStartingFacilities(B5Race race) {
        List<StartingFacility> facilities = new ArrayList<>();

        // All races get a basic shipyard
        facilities.add(StartingFacility.SHIPYARD);

        // Aggressive races get military facilities
        if (isAggressiveCombatStyle(race.getCombatStyle())) {
            facilities.add(StartingFacility.MINING_FACILITY);
        }

        return facilities.toArray(new StartingFacility[0]);
    }

    /**
     * Calculates the industrial level based on the race's trade bonus.
     * Higher trade bonus = higher industrial level.
     */
    private int calculateIndustrialLevel(B5Race race) {
        int tradeBonus = race.getTradeBonus();
        // Map trade bonus to industrial level (1-5)
        return Math.max(1, Math.min(5, tradeBonus + 1));
    }

    /**
     * Checks if a combat style is considered aggressive.
     */
    private boolean isAggressiveCombatStyle(String combatStyle) {
        if (combatStyle == null) return false;
        String lower = combatStyle.toLowerCase();
        return lower.contains("aggressive") ||
               lower.contains("military") ||
               lower.contains("warrior") ||
               lower.contains("offensive");
    }
}
