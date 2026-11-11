package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Default implementation of {@link EmpireBuilder} that creates generic empires
 * with randomized stats.
 * <p>
 * This builder is used when a {@link GalaxyTemplate} does not specify a themed
 * empire builder. It creates empires with random planet sizes, mineral qualities,
 * and fleet compositions based on the template's empire count.
 * </p>
 * <p>
 * The builder uses a configurable random seed for reproducibility.
 * </p>
 */
@Service
public class DefaultEmpireBuilder implements EmpireBuilder {

    private static final Logger log = LoggerFactory.getLogger(DefaultEmpireBuilder.class);

    /** Default random seed for reproducible empire building. */
    public static final long DEFAULT_EMPIRE_SEED = 42L;

    /** Generic empire names used when no themed names are provided. */
    private static final String[] DEFAULT_EMPIRE_NAMES = {
            "Alpha Empire", "Beta Empire", "Gamma Empire", "Delta Empire",
            "Epsilon Empire", "Zeta Empire", "Eta Empire", "Theta Empire",
            "Iota Empire", "Kappa Empire", "Lambda Empire", "Mu Empire",
            "Nu Empire", "Xi Empire", "Omicron Empire", "Pi Empire"
    };

    private final Random random;

    public DefaultEmpireBuilder() {
        this(DEFAULT_EMPIRE_SEED);
    }

    /**
     * Constructor with custom seed for testing.
     */
    public DefaultEmpireBuilder(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Builds the specified number of empires and registers them in the game.
     * <p>
     * The empire count is determined by the galaxy's generation config.
     * Each empire gets a generic name, random planet properties, and
     * a starting fleet.
     * </p>
     *
     * @param game the game to register empires in
     * @param galaxy the galaxy containing solar systems for home planets
     * @return the list of created empires
     */
    @Override
    public List<Empire> buildEmpires(Game game, Galaxy galaxy) {
        int empireCount = galaxy.getEmpireCount();
        if (empireCount <= 0) {
            log.warn("Empire count is 0 or negative. Skipping empire building.");
            return new ArrayList<>();
        }

        List<Empire> empires = new ArrayList<>();
        for (int i = 0; i < empireCount; i++) {
            try {
                String name = DEFAULT_EMPIRE_NAMES[i % DEFAULT_EMPIRE_NAMES.length];
                Player player = game.createPlayer(name);
                EmpireConfig config = generateRandomConfig();
                Empire empire = createEmpire(name, game, player, galaxy, config);
                if (empire != null) {
                    empires.add(empire);
                    game.registerEmpire(empire);
                    log.info("Created default empire '{}'", empire.getName());
                }
            } catch (Exception e) {
                log.error("Failed to create default empire {}: {}", i, e.getMessage(), e);
            }
        }

        log.info("Built {} default empires", empires.size());
        return empires;
    }

    /**
     * Creates a single empire with the given configuration.
     * <p>
     * Sets up a home system with the configured planet properties,
     * assigns starting fleets based on the config flags, and registers
     * the empire in the game.
     * </p>
     *
     * @param name the empire name
     * @param game the game context for empire registration
     * @param player the player controlling the empire
     * @param galaxy the galaxy for home system registration
     * @param config configuration for the empire
     * @return the created empire
     */
    @Override
    public Empire createEmpire(String name, Game game, Player player, Galaxy galaxy, EmpireConfig config) {
        if (name == null || name.isBlank()) {
            log.warn("Skipping empire creation: name is null or blank");
            return null;
        }
        if (player == null) {
            log.warn("Skipping empire creation: player is null");
            return null;
        }
        if (game == null) {
            log.warn("Skipping empire creation: game is null");
            return null;
        }

        // Create empire
        int empireId = game.getNextEmpireIdAndIncrement();
        Empire empire = new Empire(empireId, name, player.getId());

        // Set up home system
        PlanetSize planetSize = config != null ? config.getPlanetSize() : PlanetSize.MEDIUM;
        MineralQuality mineralQuality = config != null ? config.getMineralQuality() : MineralQuality.NORMAL;
        StartingFacility[] facilities = config != null ? config.getStartingFacilities() :
                new StartingFacility[]{StartingFacility.SHIPYARD};

        Planet capitalPlanet = game.setupHomeSystem(galaxy, player, planetSize, mineralQuality, facilities);

        // Set capital planet
        if (capitalPlanet != null) {
            empire.claimPlanet(capitalPlanet.getId());
            empire.setCapitalPlanet(capitalPlanet.getId());

            int industrialLevel = config != null ? config.getIndustrialLevel() : 1;
            if (industrialLevel > 0) {
                empire.setPlanetIndustrialLevel(capitalPlanet.getId(), industrialLevel);
            }
        }

        // Assign starting fleets
        assignStartingFleets(empire, player, config, game);

        return empire;
    }

    /**
     * Generates a random empire configuration for variety.
     */
    private EmpireConfig generateRandomConfig() {
        EmpireConfig config = new EmpireConfig();

        // Random planet size
        PlanetSize[] sizes = PlanetSize.values();
        config.setPlanetSize(sizes[random.nextInt(sizes.length)]);

        // Random mineral quality
        MineralQuality[] qualities = MineralQuality.values();
        config.setMineralQuality(qualities[random.nextInt(qualities.length)]);

        // Random industrial level (1-5)
        config.setIndustrialLevel(random.nextInt(5) + 1);

        // Random facilities
        List<StartingFacility> facilities = new ArrayList<>();
        facilities.add(StartingFacility.SHIPYARD);
        if (random.nextBoolean()) {
            facilities.add(StartingFacility.MINING_FACILITY);
        }
        config.setStartingFacilities(facilities.toArray(new StartingFacility[0]));

        // Random fleet composition
        config.setHasMilitaryFleet(random.nextBoolean());
        config.setHasMiningFleet(random.nextBoolean());
        config.setHasReserveFleet(random.nextInt(3) == 0); // ~33% chance

        return config;
    }

    /**
     * Assigns starting fleets based on the config flags.
     */
    private void assignStartingFleets(Empire empire, Player player, EmpireConfig config, Game game) {
        if (config == null) {
            // Default: mining fleet only
            Fleet fleet = game.createFleet(empire.getName() + " Exploration Fleet", player);
            Ship miningFrigate = game.createMiningFrigate(empire.getName() + " Miner");
            fleet.addShip(miningFrigate);
            game.addFleet(fleet, player);
            return;
        }

        if (config.isHasMilitaryFleet()) {
            Fleet fleet = game.createFleet(empire.getName() + " Defense Fleet", player);
            Ship frigate = game.createMilitaryFrigate(empire.getName() + " Frigate");
            fleet.addShip(frigate);
            Ship destroyer = game.createMilitaryDestroyer(empire.getName() + " Destroyer");
            fleet.addShip(destroyer);
            game.addFleet(fleet, player);
        }

        if (config.isHasMiningFleet()) {
            Fleet fleet = game.createFleet(empire.getName() + " Mining Fleet", player);
            Ship miningFrigate = game.createMiningFrigate(empire.getName() + " Miner");
            fleet.addShip(miningFrigate);
            game.addFleet(fleet, player);
        }

        if (config.isHasReserveFleet()) {
            Fleet fleet = game.createFleet(empire.getName() + " Reserve Fleet", player);
            Ship frigate = game.createMilitaryFrigate(empire.getName() + " Reserve Frigate");
            fleet.addShip(frigate);
            game.addFleet(fleet, player);
        }
    }

}
