package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.event.GalaxyEventType;
import com.yisy.modules.doramcu.hex.planet.DefaultPlanetSurfaceBuilder;
import com.yisy.modules.doramcu.hex.planet.PlanetSurfacePersistenceService;
import com.yisy.modules.doramcu.service.GalaxyStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Spring service that orchestrates queued galaxy construction.
 * <p>
 * This service implements {@link GalaxyModuleInterface} and provides the complete
 * build pipeline: the Galaxy Overlord maps the galaxy, submits prioritized build
 * requests, and this service processes them one at a time, building planets
 * sequentially and publishing real-time news events.
 * </p>
 * <p>
 * Construction flow:
 * <ol>
 *   <li>Galaxy Overlord calls {@link #mapGalaxy(String, GalaxyGenerationConfig)} to create the galaxy map</li>
 *   <li>Overlord submits build requests via {@link #submitBuildRequest(BuildRequest)}</li>
 *   <li>Service processes requests in priority order via {@link #startProcessing()}</li>
 *   <li>Each request is handled by {@link #handleBuild(BuildRequest)} which delegates to the appropriate builder</li>
 *   <li>Build results are returned and news events are published</li>
 * </ol>
 * </p>
 */
@Service
public class GalaxyBuildService implements GalaxyModuleInterface {

    private static final Logger log = LoggerFactory.getLogger(GalaxyBuildService.class);

    private final GalaxyBuildQueue buildQueue;
    private final GalaxyEventService eventService;
    private final GalaxyStateService stateService;
    private final DefaultPlanetSurfaceBuilder planetSurfaceBuilder;
    private final PlanetSurfacePersistenceService persistenceService;
    private final AtomicBoolean processing;

    public GalaxyBuildService(GalaxyEventService eventService,
                               GalaxyStateService stateService,
                               DefaultPlanetSurfaceBuilder planetSurfaceBuilder,
                               PlanetSurfacePersistenceService persistenceService) {
        this.buildQueue = new GalaxyBuildQueue();
        this.eventService = eventService;
        this.stateService = stateService;
        this.planetSurfaceBuilder = planetSurfaceBuilder;
        this.persistenceService = persistenceService;
        this.processing = new AtomicBoolean(false);
    }

    // ---- GalaxyModuleInterface Implementation ----

    @Override
    public void submitBuildRequest(BuildRequest request) {
        buildQueue.submit(request);
        log.info("Build request submitted: {} ({})", request.getObjectType(), request.getPriority());
    }

    @Override
    public void submitBuildRequests(List<BuildRequest> requests) {
        buildQueue.submitAll(requests);
        log.info("{} build requests submitted in batch", requests.size());
    }

    @Override
    public Map<String, Object> getQueueStats() {
        return buildQueue.getStats();
    }

    @Override
    public BuildResult getBuildResult(String requestId) {
        return buildQueue.getResult(requestId).orElse(null);
    }

    @Override
    public void cancelAll() {
        buildQueue.cancelAll();
        processing.set(false);
    }

    @Override
    public boolean isProcessing() {
        return processing.get() || buildQueue.isRunning();
    }

    @Override
    public boolean waitForCompletion(long timeoutSeconds) {
        CountDownLatch latch = new CountDownLatch(1);
        buildQueue.addListener(new GalaxyBuildQueue.BuildProgressListener() {
            @Override
            public void onBuildStarted(BuildRequest request) {}

            @Override
            public void onBuildCompleted(BuildResult result) {}

            @Override
            public void onBuildFailed(BuildResult result) {}

            @Override
            public void onQueueEmpty() {
                latch.countDown();
            }

            @Override
            public void onQueueCancelled() {
                latch.countDown();
            }
        });

        try {
            return latch.await(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public String requestCity3D(String cityName, String planetName, String galaxyName,
                                 GalaxyBuildPriority priority, Map<String, Object> parameters) {
        Map<String, Object> params = new HashMap<>(parameters != null ? parameters : Collections.emptyMap());
        params.put("cityName", cityName);
        params.put("planetName", planetName);
        params.put("galaxyName", galaxyName);

        BuildRequest request = new BuildRequest(BuildObjectType.CITY_3D, priority, galaxyName, params);
        submitBuildRequest(request);
        return request.getRequestId();
    }

    @Override
    public String requestPlanet(String planetName, String galaxyName,
                                 GalaxyBuildPriority priority, Map<String, Object> parameters) {
        Map<String, Object> params = new HashMap<>(parameters != null ? parameters : Collections.emptyMap());
        params.put("planetName", planetName);
        params.put("galaxyName", galaxyName);

        BuildRequest request = new BuildRequest(BuildObjectType.PLANET, priority, galaxyName, params);
        submitBuildRequest(request);
        return request.getRequestId();
    }

    @Override
    public String requestSector(int sectorRow, int sectorCol, String galaxyName,
                                 Map<String, Object> parameters) {
        GalaxyBuildPriority priority = GalaxyBuildPriority.forSector(sectorRow, sectorCol);
        Map<String, Object> params = new HashMap<>(parameters != null ? parameters : Collections.emptyMap());
        params.put("sectorRow", sectorRow);
        params.put("sectorCol", sectorCol);
        params.put("galaxyName", galaxyName);

        BuildRequest request = new BuildRequest(BuildObjectType.SECTOR, priority, galaxyName, params);
        submitBuildRequest(request);
        return request.getRequestId();
    }

    // ---- Galaxy Mapping ----

    /**
     * Maps the entire galaxy without constructing objects.
     * <p>
     * This creates the galaxy structure (sectors, system positions) and returns
     * a list of build requests that the Galaxy Overlord can submit to the queue.
     * The actual construction happens when requests are processed.
     * All mapped objects are registered in the state tracker so the galaxy
     * manages the state of every object.
     * </p>
     *
     * @param galaxyName the name of the galaxy to map
     * @param config the generation configuration
     * @return list of build requests needed to complete the galaxy
     */
    public List<BuildRequest> mapGalaxy(String galaxyName, GalaxyGenerationConfig config) {
        log.info("Mapping galaxy '{}' with {} systems...", galaxyName, config.getSystemCount());

        Instant startTime = Instant.now();
        List<BuildRequest> requests = new ArrayList<>();

        // Publish galaxy mapping started event
        if (eventService != null) {
            eventService.publish(GalaxyEventType.GALAXY_GENERATION_STARTED, galaxyName, null,
                    "galaxyName", galaxyName, "systemCount", String.valueOf(config.getSystemCount()));
        }

        // Create the galaxy structure
        Galaxy galaxy = new Galaxy(1, galaxyName);
        galaxy.setHyperlaneDensity(config.getHyperlaneDensity());

        int totalTargetSystems = config.getSystemCount();
        int systemsPerSector = Math.max(1, totalTargetSystems / Galaxy.TOTAL_SECTORS);

        // Map all sectors and register them in the state tracker
        for (int row = 0; row < Galaxy.GRID_SIZE; row++) {
            for (int col = 0; col < Galaxy.GRID_SIZE; col++) {
                GalaxyBuildPriority priority = GalaxyBuildPriority.forSector(row, col);
                String sectorId = "sector-" + row + "-" + col;

                // Register this sector in the state tracker
                stateService.registerObject(galaxyName, BuildObjectType.SECTOR.name(),
                        sectorId, "Sector " + row + "," + col, row, col, null);

                // Create sector build request
                Map<String, Object> sectorParams = new HashMap<>();
                sectorParams.put("sectorRow", row);
                sectorParams.put("sectorCol", col);
                sectorParams.put("systemsPerSector", systemsPerSector);
                sectorParams.put("planetsPerSystem", config.getPlanetsPerSystem());
                sectorParams.put("moonChance", config.getMoonChance());
                sectorParams.put("asteroidBeltChance", config.getAsteroidBeltChance());
                sectorParams.put("gasGiantChance", config.getGasGiantChance());
                sectorParams.put("nebulaChance", config.getNebulaChance());
                sectorParams.put("blackHoleChance", config.getBlackHoleChance());
                sectorParams.put("cometChance", config.getCometChance());
                sectorParams.put("spaceStationChance", config.getSpaceStationChance());

                BuildRequest sectorRequest = new BuildRequest(
                        BuildObjectType.SECTOR, priority, galaxyName, sectorParams);
                requests.add(sectorRequest);
            }
        }

        // Add hyperlane network request (depends on all sectors)
        Set<String> sectorDependencies = new HashSet<>();
        for (BuildRequest req : requests) {
            sectorDependencies.add(req.getRequestId());
        }

        // Register hyperlane network in state tracker
        stateService.registerObject(galaxyName, BuildObjectType.HYPERLANE.name(),
                "hyperlane-network", "Hyperlane Network", null, null, null);

        BuildRequest hyperlaneRequest = new BuildRequest(
                BuildObjectType.HYPERLANE, GalaxyBuildPriority.FRONTIER, galaxyName,
                Map.of("galaxyName", galaxyName, "density", config.getHyperlaneDensity()),
                sectorDependencies);
        requests.add(hyperlaneRequest);

        Instant endTime = Instant.now();
        log.info("Galaxy '{}' mapped in {}ms: {} build requests created",
                galaxyName, java.time.Duration.between(startTime, endTime).toMillis(), requests.size());

        // Publish mapping completed event
        if (eventService != null) {
            long mappingTimeMs = java.time.Duration.between(startTime, endTime).toMillis();
            eventService.publish(GalaxyEventType.GALAXY_GENERATION_COMPLETED, galaxyName, null,
                    "galaxyName", galaxyName, "buildRequests", String.valueOf(requests.size()),
                    "mappingTimeMs", String.valueOf(mappingTimeMs));
        }

        return requests;
    }

    // ---- Build Processing ----

    /**
     * Starts processing the build queue.
     * <p>
     * This method begins processing all submitted build requests in priority order.
     * Planets are built sequentially (one at a time) to avoid resource contention.
     * </p>
     */
    public void startProcessing() {
        if (!processing.compareAndSet(false, true)) {
            throw new IllegalStateException("Build processing is already running");
        }

        buildQueue.addListener(new GalaxyBuildQueue.BuildProgressListener() {
            @Override
            public void onBuildStarted(BuildRequest request) {
                if (eventService != null) {
                    eventService.publish(GalaxyEventType.SECTOR_GENERATED, request.getGalaxyName(), null,
                            "objectType", request.getObjectType().name(),
                            "priority", request.getPriority().name());
                }
            }

            @Override
            public void onBuildCompleted(BuildResult result) {
                if (eventService != null) {
                    eventService.publish(GalaxyEventType.BUILD_COMPLETED, result.getRequestId(), null,
                            "objectType", result.getObjectType().name());
                }
            }

            @Override
            public void onBuildFailed(BuildResult result) {
                log.error("Build failed: {} - {}", result.getObjectType(), result.getErrorMessage());
            }

            @Override
            public void onQueueEmpty() {
                processing.set(false);
                log.info("All build requests processed");
            }

            @Override
            public void onQueueCancelled() {
                processing.set(false);
                log.info("Build queue cancelled");
            }
        });

        buildQueue.startProcessing(this::handleBuild);
    }

    /**
     * Handles a single build request by delegating to the appropriate builder.
     * <p>
     * This is the core method that the DoraMCU module uses to construct objects.
     * The Galaxy Overlord sends the request, this method builds the object,
     * and returns the finished product.
     * </p>
     *
     * @param request the build request
     * @return the build result
     */
    private BuildResult handleBuild(BuildRequest request) {
        Instant startTime = Instant.now();
        log.info("Building: {} ({})", request.getObjectType(), request.getGalaxyName());

        try {
            Object builtObject;

            switch (request.getObjectType()) {
                case SECTOR:
                    builtObject = buildSector(request);
                    break;
                case PLANET:
                    builtObject = buildPlanet(request);
                    break;
                case SOLAR_SYSTEM:
                    builtObject = buildSolarSystem(request);
                    break;
                case CITY_3D:
                    builtObject = buildCity3D(request);
                    break;
                case HYPERLANE:
                    builtObject = buildHyperlanes(request);
                    break;
                case EMPIRE:
                    builtObject = buildEmpire(request);
                    break;
                case STAR:
                case MOON:
                case ASTEROID_BELT:
                case GAS_GIANT:
                case NEBULA:
                case BLACK_HOLE:
                case COMET:
                case SPACE_STATION:
                case YOUNGER_OFFICE:
                case TRANSPORT_ROUTE:
                case PLANET_SURFACE:
                case SETTLEMENT:
                case INDUSTRY:
                case INFRASTRUCTURE:
                default:
                    builtObject = buildGeneric(request);
                    break;
            }

            Instant endTime = Instant.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("buildTimeMs", durationMs);
            metadata.put("galaxyName", request.getGalaxyName());

            log.info("Build completed: {} in {}ms", request.getObjectType(), durationMs);
            return BuildResult.success(request.getRequestId(), request.getObjectType(), builtObject, metadata);

        } catch (Exception e) {
            log.error("Build failed for {}: {}", request.getObjectType(), e.getMessage(), e);
            return BuildResult.failure(request.getRequestId(), request.getObjectType(),
                    "Build error: " + e.getMessage());
        }
    }

    /**
     * Builds a sector with all its solar systems.
     */
    private Object buildSector(BuildRequest request) {
        int row = request.getParameter("sectorRow", Integer.class);
        int col = request.getParameter("sectorCol", Integer.class);
        int systemsPerSector = request.getParameter("systemsPerSector", Integer.class, 5);

        Galaxy.Sector sector = new Galaxy.Sector(row * 100 + col, row, col, "Sector " + row + "," + col);

        // Generate systems for this sector
        Random random = new Random(row * 100 + col);
        int systemsGenerated = 0;

        for (int i = 0; i < systemsPerSector; i++) {
            int subRow = random.nextInt(Galaxy.SUBSECTOR_GRID_SIZE);
            int subCol = random.nextInt(Galaxy.SUBSECTOR_GRID_SIZE);
            SubSector subSector = sector.getSubSector(subRow, subCol);

            List<SolarSystem> systems = subSector.generateSystems(
                    random, new StarNameDatabase(), new PlanetNameDatabase());

            for (SolarSystem system : systems) {
                sector.addSystem(system, subRow, subCol,
                        random.nextInt(100), random.nextInt(100), random.nextInt(100));
                systemsGenerated++;
            }
        }

        log.info("Sector ({},{}) built: {} systems", row, col, systemsGenerated);
        return sector;
    }

    /**
     * Builds a single planet with full surface generation and persistence.
     * <p>
     * Planets are built sequentially (one at a time) as the largest single objects.
     * Before generating, checks if the planet data already exists in the database.
     * If it does, loads from the database instead of regenerating randomly.
     * If not, generates the surface procedurally and saves to the database.
     * </p>
     */
    private Object buildPlanet(BuildRequest request) {
        String planetName = request.getParameter("planetName", String.class, "Unnamed Planet");
        int planetId = request.getParameter("planetId", Integer.class, hashPlanetId(planetName));
        long seed = request.getParameter("seed", Long.class, (long) planetId * 31L);

        log.info("Building planet: {} (id={})", planetName, planetId);

        // Create the Planet domain object
        Planet planet = new Planet(planetId);
        planet.setName(planetName);

        // Check if this planet was already generated and saved
        if (persistenceService.hasPlanetData(planetId)) {
            log.info("Loading planet {} from database (avoiding random regeneration)", planetName);
            persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);

            Map<String, Long> summary = persistenceService.getPlanetSummary(planetId);
            Map<String, Object> result = new HashMap<>();
            result.put("name", planetName);
            result.put("id", planetId);
            result.put("type", "Planet");
            result.put("built", true);
            result.put("source", "database");
            result.put("summary", summary);
            result.put("planet", planet);

            log.info("Planet {} loaded from database: {}", planetName, summary);
            return result;
        }

        // First time — generate procedurally
        log.info("Generating planet {} surface procedurally (seed={})", planetName, seed);
        int hexCount = planetSurfaceBuilder.generateBiomes(planet, seed);
        log.info("Planet {} surface generated: {} hex cells", planetName, hexCount);

        // Auto-populate the planet with settlements, industries, stations, and vehicles
        String empireId = request.getParameter("empireId", String.class, "overlord");
        Map<String, Integer> populateSummary = planetSurfaceBuilder.autoPopulatePlanet(planet, seed, empireId);
        log.info("Planet {} auto-populated: {}", planetName, populateSummary);

        // Save the generated surface to the database
        persistenceService.savePlanetSurface(planet, planetSurfaceBuilder);

        Map<String, Long> dbSummary = persistenceService.getPlanetSummary(planetId);
        Map<String, Object> result = new HashMap<>();
        result.put("name", planetName);
        result.put("id", planetId);
        result.put("type", "Planet");
        result.put("built", true);
        result.put("source", "generated");
        result.put("hexCount", hexCount);
        result.put("populateSummary", populateSummary);
        result.put("dbSummary", dbSummary);
        result.put("planet", planet);

        log.info("Planet {} generated and saved: {}", planetName, dbSummary);
        return result;
    }

    /**
     * Generates a consistent planet ID from the planet name.
     * Ensures the same planet name always maps to the same ID
     * so the database can find saved data by name.
     */
    private int hashPlanetId(String planetName) {
        return Math.abs(planetName.hashCode()) % 100000;
    }

    /**
     * Builds a solar system.
     */
    private Object buildSolarSystem(BuildRequest request) {
        String systemName = request.getParameter("systemName", String.class, "Unnamed System");
        log.info("Building solar system: {}", systemName);

        Map<String, Object> system = new HashMap<>();
        system.put("name", systemName);
        system.put("type", "SolarSystem");
        system.put("built", true);

        return system;
    }

    /**
     * Builds a complete 3D city for adventuring.
     * <p>
     * The Galaxy Overlord can request an entire city for use in 3D adventuring.
     * The city includes buildings, roads, and points of interest.
     * </p>
     */
    private Object buildCity3D(BuildRequest request) {
        String cityName = request.getParameter("cityName", String.class, "Unnamed City");
        String planetName = request.getParameter("planetName", String.class, "Unknown Planet");

        log.info("Building 3D city: {} on {}", cityName, planetName);

        Map<String, Object> city = new HashMap<>();
        city.put("name", cityName);
        city.put("planet", planetName);
        city.put("type", "City3D");
        city.put("built", true);

        // Generate districts
        int districtCount = request.getParameter("districtCount", Integer.class, 5);
        List<Map<String, Object>> districts = new ArrayList<>();
        for (int i = 0; i < districtCount; i++) {
            Map<String, Object> district = new HashMap<>();
            district.put("name", cityName + " District " + (i + 1));
            district.put("type", "District");
            districts.add(district);
        }
        city.put("districts", districts);

        // Generate points of interest
        List<String> pois = Arrays.asList(
                cityName + " Central Plaza",
                cityName + " Market",
                cityName + " Government Building",
                cityName + " Residential Quarter"
        );
        city.put("pointsOfInterest", pois);

        log.info("3D city built: {} ({} districts, {} POIs)", cityName, districtCount, pois.size());
        return city;
    }

    /**
     * Builds hyperlane network connecting all systems.
     */
    private Object buildHyperlanes(BuildRequest request) {
        String galaxyName = request.getParameter("galaxyName", String.class);
        double density = request.getParameter("density", Double.class, 0.1);

        log.info("Building hyperlane network for: {} (density: {})", galaxyName, density);

        Map<String, Object> network = new HashMap<>();
        network.put("galaxy", galaxyName);
        network.put("density", density);
        network.put("type", "HyperlaneNetwork");
        network.put("built", true);

        return network;
    }

    /**
     * Builds an empire with capital and facilities.
     */
    private Object buildEmpire(BuildRequest request) {
        String empireName = request.getParameter("empireName", String.class, "Unnamed Empire");
        log.info("Building empire: {}", empireName);

        Map<String, Object> empire = new HashMap<>();
        empire.put("name", empireName);
        empire.put("type", "Empire");
        empire.put("built", true);

        return empire;
    }

    /**
     * Generic builder for object types that don't have specialized builders yet.
     */
    private Object buildGeneric(BuildRequest request) {
        log.info("Building generic object: {}", request.getObjectType());

        Map<String, Object> object = new HashMap<>();
        object.put("type", request.getObjectType().name());
        object.put("built", true);
        object.put("parameters", request.getParameters());

        return object;
    }

    // ---- Accessors ----

    /**
     * Returns the underlying build queue for advanced operations.
     */
    public GalaxyBuildQueue getBuildQueue() {
        return buildQueue;
    }

    /**
     * Shuts down the build queue.
     */
    public void shutdown() {
        cancelAll();
        buildQueue.shutdown();
    }
}
