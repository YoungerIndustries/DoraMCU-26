package com.yisy.modules.doramcu.star_trek_4x.expansion;

import com.yisy.modules.doramcu.star_trek_4x.model.Colony;
import com.yisy.modules.doramcu.star_trek_4x.model.GameSession;
import com.yisy.modules.doramcu.star_trek_4x.model.StarSystem;
import com.yisy.modules.doramcu.star_trek_4x.model.StarTrekFaction;
import com.yisy.modules.doramcu.star_trek_4x.repository.ColonyRepository;
import com.yisy.modules.doramcu.star_trek_4x.repository.GameSessionRepository;
import com.yisy.modules.doramcu.star_trek_4x.repository.StarSystemRepository;
import com.yisy.modules.doramcu.star_trek_4x.repository.StarTrekFactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Service for the eXpand pillar: colony establishment, starbases, and territorial control.
 * <p>
 * Handles establishing colonies on discovered systems, building starbases,
 * and claiming sectors for territorial control.
 * </p>
 */
@Service("starTrek4xExpansionService")
public class ExpansionService {

    private static final Logger log = LoggerFactory.getLogger(ExpansionService.class);
    private static final Random random = new Random();

    private final ColonyRepository colonyRepository;
    private final StarSystemRepository starSystemRepository;
    private final StarTrekFactionRepository factionRepository;
    private final GameSessionRepository gameSessionRepository;
    private final ColonyManager colonyManager;
    private final StarbaseBuilder starbaseBuilder;
    private final TerritoryService territoryService;

    public ExpansionService(ColonyRepository colonyRepository,
                             StarSystemRepository starSystemRepository,
                             StarTrekFactionRepository factionRepository,
                             GameSessionRepository gameSessionRepository,
                             ColonyManager colonyManager,
                             StarbaseBuilder starbaseBuilder,
                             TerritoryService territoryService) {
        this.colonyRepository = colonyRepository;
        this.starSystemRepository = starSystemRepository;
        this.factionRepository = factionRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.colonyManager = colonyManager;
        this.starbaseBuilder = starbaseBuilder;
        this.territoryService = territoryService;
    }

    /**
     * Establish a colony on a discovered system.
     *
     * @param gameSessionId the game session ID
     * @param factionId     the faction establishing the colony
     * @param systemId      the system ID
     * @param colonyName    the colony name
     * @return the established colony
     */
    public Colony establishColony(Long gameSessionId, Long factionId, Long systemId, String colonyName) {
        log.info("Establishing colony '{}' on system {} by faction {}", colonyName, systemId, factionId);

        GameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + gameSessionId));

        StarSystem system = starSystemRepository.findById(systemId)
                .orElseThrow(() -> new IllegalArgumentException("System not found: " + systemId));

        if (!system.isDiscovered()) {
            throw new IllegalStateException("System not discovered: " + systemId);
        }

        // Check if colony already exists on this system
        if (colonyRepository.findByStarSystemId(systemId).stream()
                .anyMatch(c -> c.getGameSession().getId().equals(gameSessionId))) {
            throw new IllegalStateException("Colony already exists on system: " + systemId);
        }

        // Create colony
        Colony colony = new Colony();
        colony.setName(colonyName);
        colony.setStarSystemId(systemId);
        colony.setOwnerFactionId(factionId);
        colony.setGameSession(session);

        // Initialize resources based on system properties
        initializeColonyResources(colony, system);

        colony = colonyRepository.save(colony);

        // Update system owner
        system.setOwnerFactionId(factionId);
        starSystemRepository.save(system);

        // Update faction colony count
        factionRepository.findById(factionId).ifPresent(faction -> {
            faction.setColonyCount(faction.getColonyCount() + 1);
            factionRepository.save(faction);
        });

        log.info("Colony '{}' established successfully", colonyName);
        return colony;
    }

    /**
     * Initialize colony resources based on system properties.
     */
    private void initializeColonyResources(Colony colony, StarSystem system) {
        // Base resources
        colony.setPopulation(1000);
        colony.setMineralsPerTurn(10);
        colony.setEnergyPerTurn(5);
        colony.setFoodPerTurn(15);
        colony.setResearchPerTurn(2);

        // Modify based on UWP properties
        if (system.getUwpPopulation() != null) {
            int popCode = Character.getNumericValue(system.getUwpPopulation().charAt(0));
            colony.setPopulation(colony.getPopulation() * (popCode + 1));
        }

        // Warp deposits increase minerals
        if (system.isHasWarpDeposits()) {
            colony.setMineralsPerTurn(colony.getMineralsPerTurn() * 3);
        }

        // Trade value increases energy
        colony.setEnergyPerTurn(colony.getEnergyPerTurn() + system.getTradeValue() / 2);
    }

    /**
     * Build a starbase in a system.
     *
     * @param gameSessionId the game session ID
     * @param factionId     the faction building the starbase
     * @param systemId      the system ID
     * @param starbaseName  the starbase name
     * @return starbase details
     */
    public Map<String, Object> buildStarbase(Long gameSessionId, Long factionId, Long systemId, String starbaseName) {
        log.info("Building starbase '{}' in system {} by faction {}", starbaseName, systemId, factionId);

        StarSystem system = starSystemRepository.findById(systemId)
                .orElseThrow(() -> new IllegalArgumentException("System not found: " + systemId));

        return starbaseBuilder.buildStarbase(starbaseName, system, factionId);
    }

    /**
     * Claim a sector for a faction.
     *
     * @param gameSessionId the game session ID
     * @param factionId     the faction claiming the sector
     * @param sectorId      the sector ID
     * @return claim result
     */
    public Map<String, Object> claimSector(Long gameSessionId, Long factionId, int sectorId) {
        log.info("Claiming sector {} for faction {}", sectorId, factionId);

        return territoryService.claimSector(gameSessionId, factionId, sectorId);
    }

    /**
     * Get all colonies for a faction.
     *
     * @param gameSessionId the game session ID
     * @param factionId     the faction ID
     * @return list of colonies
     */
    public List<Colony> getColonies(Long gameSessionId, Long factionId) {
        return colonyRepository.findByOwnerFactionId(factionId)
                .stream()
                .filter(c -> c.getGameSession().getId().equals(gameSessionId))
                .toList();
    }

    /**
     * Get all colonies in a sector.
     *
     * @param gameSessionId the game session ID
     * @param sectorId      the sector ID
     * @return list of colonies
     */
    public List<Colony> getColoniesInSector(Long gameSessionId, int sectorId) {
        List<StarSystem> systems = starSystemRepository.findBySectorId(sectorId)
                .stream()
                .filter(s -> s.getGameSession().getId().equals(gameSessionId))
                .toList();

        return systems.stream()
                .flatMap(system -> colonyRepository.findByStarSystemId(system.getId()).stream())
                .toList();
    }

    /**
     * Process colony growth for a turn.
     *
     * @param gameSessionId the game session ID
     */
    public void processColonyGrowth(Long gameSessionId) {
        log.info("Processing colony growth for game session {}", gameSessionId);

        List<Colony> colonies = colonyRepository.findAll().stream()
                .filter(c -> c.getGameSession().getId().equals(gameSessionId))
                .filter(Colony::isOperational)
                .toList();

        for (Colony colony : colonies) {
            colonyManager.processGrowth(colony);
        }

        colonyRepository.saveAll(colonies);
    }
}
