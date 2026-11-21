package com.yisy.modules.doramcu.earth2026.controller;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Contract;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Country;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Economy;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Job;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Planet;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Region;
import com.yisy.modules.doramcu.earth2026.service.CountryDiplomacyService;
import com.yisy.modules.doramcu.earth2026.service.EspionageService;
import com.yisy.modules.doramcu.earth2026.service.G20CountryInitializer;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.fourx.FourxDiplomaticRelation;
import com.yisy.modules.doramcu.fourx.FourxFaction;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.PlanetPopulationProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * REST API for Earth 2026 planet regions, countries, economy, jobs, contracts,
 * and AI character assignments.
 * <p>
 * This controller exposes the PlanetService methods for managing the complete
 * planet hierarchy: Planet → Countries → Regions → Locations → Structures.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/earth2026/regions")
public class Earth2026PlanetController {

    private static final Logger log = LoggerFactory.getLogger(Earth2026PlanetController.class);

    private static final long EMITTER_TIMEOUT = 30 * 60 * 1000L; // 30 minutes

    private final PlanetService planetService;
    private final G20CountryInitializer g20Initializer;
    private final CountryDiplomacyService diplomacyService;
    private final EspionageService espionageService;

    /**
     * Dedicated thread pool for async planet creation via SSE streaming.
     * Each planet creation runs in its own thread so the HTTP thread returns immediately.
     */
    private final ExecutorService planetCreationExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "planet-creation-sse");
        t.setDaemon(true);
        return t;
    });

    public Earth2026PlanetController(PlanetService planetService, G20CountryInitializer g20Initializer,
                                     CountryDiplomacyService diplomacyService, EspionageService espionageService) {
        this.planetService = planetService;
        this.g20Initializer = g20Initializer;
        this.diplomacyService = diplomacyService;
        this.espionageService = espionageService;
    }

    // ------------------------------------------------------------------
    // Planet operations
    // ------------------------------------------------------------------

    /**
     * GET /api/v1/earth2026/regions/planets
     * List all available planets.
     */
    @GetMapping("/planets")
    public ResponseEntity<List<Earth2026Planet>> getAllPlanets() {
        return ResponseEntity.ok(planetService.getAllPlanets());
    }

    /**
     * GET /api/v1/earth2026/regions/planets/{id}
     * Get a planet by ID.
     */
    @GetMapping("/planets/{id}")
    public ResponseEntity<?> getPlanet(@PathVariable Long id) {
        return planetService.getPlanet(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/earth2026/regions/planets/name/{name}
     * Get a planet by name.
     */
    @GetMapping("/planets/name/{name}")
    public ResponseEntity<?> getPlanetByName(@PathVariable String name) {
        return planetService.getPlanet(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Country operations
    // ------------------------------------------------------------------

    /**
     * GET /api/v1/earth2026/regions/countries
     * List all countries on a planet.
     */
    @GetMapping("/countries")
    public ResponseEntity<List<Earth2026Country>> getCountries(
            @RequestParam Long planetId) {
        return ResponseEntity.ok(planetService.getCountries(planetId));
    }

    /**
     * GET /api/v1/earth2026/regions/countries/paginated
     * List countries on a planet with pagination.
     */
    @GetMapping("/countries/paginated")
    public ResponseEntity<Page<Earth2026Country>> getCountriesPaginated(
            @RequestParam Long planetId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(planetService.getCountries(planetId, pageable));
    }

    /**
     * GET /api/v1/earth2026/regions/countries/{id}
     * Get a country by ID.
     */
    @GetMapping("/countries/{id}")
    public ResponseEntity<?> getCountry(@PathVariable Long id) {
        return planetService.getCountry(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/earth2026/regions/countries/iso/{isoCode}
     * Get a country by ISO code.
     */
    @GetMapping("/countries/iso/{isoCode}")
    public ResponseEntity<?> getCountryByIsoCode(@PathVariable String isoCode) {
        return planetService.getCountryByIsoCode(isoCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/earth2026/regions/countries/continent/{continent}
     * Get countries by continent.
     */
    @GetMapping("/countries/continent/{continent}")
    public ResponseEntity<List<Earth2026Country>> getCountriesByContinent(
            @PathVariable String continent) {
        return ResponseEntity.ok(planetService.getCountriesByContinent(continent));
    }

    /**
     * GET /api/v1/earth2026/regions/countries/g20
     * List all G20 country configurations (pre-defined templates).
     */
    @GetMapping("/countries/g20")
    public ResponseEntity<List<G20CountryInitializer.G20CountryConfig>> getG20Countries() {
        return ResponseEntity.ok(g20Initializer.getG20Countries());
    }

    /**
     * POST /api/v1/earth2026/regions/countries/init-g20
     * Initialize G20 countries on a specific planet.
     */
    @PostMapping("/countries/init-g20")
    public ResponseEntity<?> initializeG20Countries(@RequestParam Long planetId) {
        try {
            List<Earth2026Country> created = g20Initializer.initializeG20OnPlanet(planetId);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("created", created.size());
            response.put("countries", created.stream().map(c -> Map.of(
                    "id", c.getId(),
                    "name", c.getName(),
                    "isoCode", c.getIsoCode()
            )).toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to initialize G20 countries: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------------------------------------------------------
    // Region operations
    // ------------------------------------------------------------------

    /**
     * GET /api/v1/earth2026/regions/countries/{countryId}/regions
     * List all regions in a country.
     */
    @GetMapping("/countries/{countryId}/regions")
    public ResponseEntity<List<Earth2026Region>> getRegions(@PathVariable Long countryId) {
        return ResponseEntity.ok(planetService.getRegions(countryId));
    }

    /**
     * GET /api/v1/earth2026/regions/countries/{countryId}/regions/paginated
     * List regions in a country with pagination.
     */
    @GetMapping("/countries/{countryId}/regions/paginated")
    public ResponseEntity<Page<Earth2026Region>> getRegionsPaginated(
            @PathVariable Long countryId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(planetService.getRegions(countryId, pageable));
    }

    /**
     * GET /api/v1/earth2026/regions/regions/{id}
     * Get a region by ID.
     */
    @GetMapping("/regions/{id}")
    public ResponseEntity<?> getRegion(@PathVariable Long id) {
        return planetService.getRegion(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Economy operations
    // ------------------------------------------------------------------

    /**
     * GET /api/v1/earth2026/regions/economy/{planetId}
     * Get the economy for a planet.
     */
    @GetMapping("/economy/{planetId}")
    public ResponseEntity<?> getEconomy(@PathVariable Long planetId) {
        return planetService.getEconomy(planetId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Job operations
    // ------------------------------------------------------------------

    /**
     * GET /api/v1/earth2026/regions/jobs
     * List all jobs on a planet.
     */
    @GetMapping("/jobs")
    public ResponseEntity<List<Earth2026Job>> getJobs(@RequestParam Long planetId) {
        return ResponseEntity.ok(planetService.getJobs(planetId));
    }

    /**
     * GET /api/v1/earth2026/regions/jobs/paginated
     * List jobs on a planet with pagination.
     */
    @GetMapping("/jobs/paginated")
    public ResponseEntity<Page<Earth2026Job>> getJobsPaginated(
            @RequestParam Long planetId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(planetService.getJobs(planetId, pageable));
    }

    /**
     * GET /api/v1/earth2026/regions/jobs/{id}
     * Get a job by ID.
     */
    @GetMapping("/jobs/{id}")
    public ResponseEntity<?> getJob(@PathVariable Long id) {
        return planetService.getJob(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/earth2026/regions/jobs/category/{category}
     * Get jobs by category.
     */
    @GetMapping("/jobs/category/{category}")
    public ResponseEntity<List<Earth2026Job>> getJobsByCategory(
            @RequestParam Long planetId,
            @PathVariable String category) {
        return ResponseEntity.ok(planetService.getJobsByCategory(planetId, category));
    }

    // ------------------------------------------------------------------
    // Contract operations
    // ------------------------------------------------------------------

    /**
     * GET /api/v1/earth2026/regions/contracts
     * List all available contracts on a planet.
     */
    @GetMapping("/contracts")
    public ResponseEntity<List<Earth2026Contract>> getAvailableContracts(
            @RequestParam Long planetId) {
        return ResponseEntity.ok(planetService.getAvailableContracts(planetId));
    }

    /**
     * GET /api/v1/earth2026/regions/contracts/paginated
     * List available contracts on a planet with pagination.
     */
    @GetMapping("/contracts/paginated")
    public ResponseEntity<Page<Earth2026Contract>> getAvailableContractsPaginated(
            @RequestParam Long planetId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(planetService.getAvailableContracts(planetId, pageable));
    }

    /**
     * GET /api/v1/earth2026/regions/contracts/{id}
     * Get a contract by ID.
     */
    @GetMapping("/contracts/{id}")
    public ResponseEntity<?> getContract(@PathVariable Long id) {
        return planetService.getContract(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/earth2026/regions/contracts/character/{characterId}
     * Get contracts assigned to a character.
     */
    @GetMapping("/contracts/character/{characterId}")
    public ResponseEntity<List<Earth2026Contract>> getContractsByCharacter(
            @PathVariable Long characterId) {
        return ResponseEntity.ok(planetService.getContractsByCharacter(characterId));
    }

    // ------------------------------------------------------------------
    // AI Character operations
    // ------------------------------------------------------------------

    /**
     * GET /api/v1/earth2026/regions/countries/{countryId}/ai-leader
     * Get the AI character that controls a country.
     */
    @GetMapping("/countries/{countryId}/ai-leader")
    public ResponseEntity<?> getCountryAiLeader(@PathVariable Long countryId) {
        return planetService.getCountryAiLeader(countryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/earth2026/regions/regions/{regionId}/ai-governor
     * Get the AI character that governs a region.
     */
    @GetMapping("/regions/{regionId}/ai-governor")
    public ResponseEntity<?> getRegionAiGovernor(@PathVariable Long regionId) {
        return planetService.getRegionAiGovernor(regionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/earth2026/regions/planets/{planetId}/ai-leaders
     * Get all AI characters assigned to countries on a planet.
     */
    @GetMapping("/planets/{planetId}/ai-leaders")
    public ResponseEntity<List<AiCharacter>> getAllCountryAiLeaders(@PathVariable Long planetId) {
        return ResponseEntity.ok(planetService.getAllCountryAiLeaders(planetId));
    }

    /**
     * GET /api/v1/earth2026/regions/planets/{planetId}/ai-leaders/paginated
     * Get AI characters assigned to countries on a planet with pagination.
     */
    @GetMapping("/planets/{planetId}/ai-leaders/paginated")
    public ResponseEntity<Page<AiCharacter>> getAllCountryAiLeadersPaginated(
            @PathVariable Long planetId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(planetService.getAllCountryAiLeaders(planetId, pageable));
    }

    /**
     * GET /api/v1/earth2026/regions/countries/{countryId}/ai-governors
     * Get all AI characters assigned to regions in a country.
     */
    @GetMapping("/countries/{countryId}/ai-governors")
    public ResponseEntity<List<AiCharacter>> getAllRegionAiGovernors(@PathVariable Long countryId) {
        return ResponseEntity.ok(planetService.getAllRegionAiGovernors(countryId));
    }

    /**
     * GET /api/v1/earth2026/regions/countries/{countryId}/ai-governors/paginated
     * Get AI characters assigned to regions in a country with pagination.
     */
    @GetMapping("/countries/{countryId}/ai-governors/paginated")
    public ResponseEntity<Page<AiCharacter>> getAllRegionAiGovernorsPaginated(
            @PathVariable Long countryId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(planetService.getAllRegionAiGovernors(countryId, pageable));
    }

    /**
     * POST /api/v1/earth2026/regions/countries/{countryId}/ai-leader
     * Assign an AI character as the leader of a country.
     */
    @PostMapping("/countries/{countryId}/ai-leader")
    public ResponseEntity<?> assignCountryAiLeader(
            @PathVariable Long countryId,
            @RequestBody Map<String, Object> body) {
        Long aiCharacterId = ((Number) body.get("aiCharacterId")).longValue();
        return planetService.assignCountryAiLeader(countryId, aiCharacterId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/earth2026/regions/regions/{regionId}/ai-governor
     * Assign an AI character as the governor of a region.
     */
    @PostMapping("/regions/{regionId}/ai-governor")
    public ResponseEntity<?> assignRegionAiGovernor(
            @PathVariable Long regionId,
            @RequestBody Map<String, Object> body) {
        Long aiCharacterId = ((Number) body.get("aiCharacterId")).longValue();
        return planetService.assignRegionAiGovernor(regionId, aiCharacterId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Planet summary
    // ------------------------------------------------------------------

    /**
     * GET /api/v1/earth2026/regions/planets/{planetId}/summary
     * Get a lightweight summary of a planet including metadata and counts.
     * <p>
     * This endpoint returns only planet metadata and entity counts to avoid
     * loading all sub-resources into memory. Use the paginated endpoints
     * to fetch individual resources on demand:
     * </p>
     * <ul>
     *   <li>{@code GET /countries/paginated?planetId=X} — paginated countries</li>
     *   <li>{@code GET /countries/{id}/regions/paginated} — paginated regions</li>
     *   <li>{@code GET /jobs/paginated?planetId=X} — paginated jobs</li>
     *   <li>{@code GET /contracts/paginated?planetId=X} — paginated contracts</li>
     *   <li>{@code GET /planets/{id}/ai-leaders/paginated} — paginated AI leaders</li>
     * </ul>
     */
    @GetMapping("/planets/{planetId}/summary")
    public ResponseEntity<Map<String, Object>> getPlanetSummary(@PathVariable Long planetId) {
        Map<String, Object> summary = new HashMap<>();

        planetService.getPlanet(planetId).ifPresent(planet -> {
            summary.put("planet", planet);

            // Use count queries instead of loading all entities into memory
            summary.put("countryCount", planetService.countCountries(planetId));
            summary.put("jobCount", planetService.countJobs(planetId));
            summary.put("contractCount", planetService.countAvailableContracts(planetId));

            // Economy is a single entity — safe to load
            planetService.getEconomy(planetId).ifPresent(economy -> summary.put("economy", economy));
        });

        if (summary.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(summary);
    }

    // ------------------------------------------------------------------
    // SSE Streaming: Planet Creation
    // ------------------------------------------------------------------

    /**
     * POST /api/v1/earth2026/regions/planets/stream
     * Create a planet asynchronously and stream progress via Server-Sent Events.
     * <p>
     * The planet is built in phases (foundation, countries, regions, AI leaders,
     * AI governors, player characters, completion) and each phase is streamed
     * back as an SSE event. This prevents memory overrun on large planets because
     * the client receives data incrementally as the planet is built.
     * </p>
     * <p>
     * Event types:
     * </p>
     * <ul>
     *   <li>{@code planet_created} — basic planet metadata</li>
     *   <li>{@code country_batch} — batch of countries with leaders</li>
     *   <li>{@code region_batch} — batch of regions with governors</li>
     *   <li>{@code ai_leader} — individual AI leader assigned</li>
     *   <li>{@code ai_governor} — individual AI governor assigned</li>
     *   <li>{@code player_characters} — registered player characters</li>
     *   <li>{@code log} — log message</li>
     *   <li>{@code complete} — final population summary</li>
     *   <li>{@code error} — error during population</li>
     * </ul>
     *
     * @param request the planet population request
     * @return SSE emitter for real-time progress events
     */
    @PostMapping(value = "/planets/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter createPlanetStream(@RequestBody PlanetPopulationRequest request) {
        log.info("Starting streamed planet creation for: {} ({})", request.getPlanetName(), request.getGameModuleName());

        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);

        // Run planet population asynchronously so the HTTP thread returns immediately
        planetCreationExecutor.submit(() -> {
            try {
                // Send start event
                sendSseEvent(emitter, "start", Map.of(
                        "planetName", request.getPlanetName(),
                        "game", request.getGameModuleName()
                ));

                // Wrap the SSE emitter as a PlanetPopulationProgress callback
                PlanetPopulationProgress sseProgress = new PlanetPopulationProgress() {
                    @Override
                    public void onPlanetCreated(Earth2026Planet planet) {
                        try {
                            sendSseEvent(emitter, "planet_created", Map.of(
                                    "planetId", planet.getId(),
                                    "name", planet.getName(),
                                    "type", planet.getPlanetType(),
                                    "techLevel", planet.getBaseTechLevel() != null ? planet.getBaseTechLevel().getLevel() : null,
                                    "timePeriod", planet.getTimePeriod()
                            ));
                        } catch (IOException e) {
                            log.warn("Failed to send planet_created event: {}", e.getMessage());
                        }
                    }

                    @Override
                    public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {
                        try {
                            List<Map<String, Object>> countryList = new java.util.ArrayList<>();
                            for (Earth2026Country c : countries) {
                                Map<String, Object> cm = new HashMap<>();
                                cm.put("id", c.getId());
                                cm.put("name", c.getName());
                                cm.put("isoCode", c.getIsoCode());
                                cm.put("continent", c.getContinent());
                                cm.put("leaderName", c.getLeaderName());
                                cm.put("leaderTitle", c.getLeaderTitle());
                                countryList.add(cm);
                            }
                            sendSseEvent(emitter, "country_batch", Map.of(
                                    "countries", countryList,
                                    "batchStart", countryIndex,
                                    "totalCountries", totalCountries,
                                    "batchSize", countries.size()
                            ));
                        } catch (IOException e) {
                            log.warn("Failed to send country_batch event: {}", e.getMessage());
                        }
                    }

                    @Override
                    public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {
                        try {
                            List<Map<String, Object>> regionList = new java.util.ArrayList<>();
                            for (Earth2026Region r : regions) {
                                Map<String, Object> rm = new HashMap<>();
                                rm.put("id", r.getId());
                                rm.put("name", r.getName());
                                rm.put("countryId", countryId);
                                rm.put("governorName", r.getGovernorName());
                                rm.put("governorTitle", r.getGovernorTitle());
                                regionList.add(rm);
                            }
                            sendSseEvent(emitter, "region_batch", Map.of(
                                    "regions", regionList,
                                    "countryId", countryId,
                                    "batchStart", regionIndex,
                                    "totalRegions", totalRegions,
                                    "batchSize", regions.size()
                            ));
                        } catch (IOException e) {
                            log.warn("Failed to send region_batch event: {}", e.getMessage());
                        }
                    }

                    @Override
                    public void onAiLeaderAssigned(AiCharacter leader, Long countryId, boolean isNew) {
                        try {
                            sendSseEvent(emitter, "ai_leader", Map.of(
                                    "aiCharacterId", leader.getId(),
                                    "name", leader.getName(),
                                    "role", leader.getRole(),
                                    "countryId", countryId,
                                    "isNew", isNew
                            ));
                        } catch (IOException e) {
                            log.warn("Failed to send ai_leader event: {}", e.getMessage());
                        }
                    }

                    @Override
                    public void onAiGovernorAssigned(AiCharacter governor, Long regionId, boolean isNew) {
                        try {
                            sendSseEvent(emitter, "ai_governor", Map.of(
                                    "aiCharacterId", governor.getId(),
                                    "name", governor.getName(),
                                    "role", governor.getRole(),
                                    "regionId", regionId,
                                    "isNew", isNew
                            ));
                        } catch (IOException e) {
                            log.warn("Failed to send ai_governor event: {}", e.getMessage());
                        }
                    }

                    @Override
                    public void onPlayerCharactersRegistered(Map<String, String> registeredPlayerCharacters) {
                        try {
                            sendSseEvent(emitter, "player_characters", Map.of(
                                    "characters", registeredPlayerCharacters,
                                    "count", registeredPlayerCharacters.size()
                            ));
                        } catch (IOException e) {
                            log.warn("Failed to send player_characters event: {}", e.getMessage());
                        }
                    }

                    @Override
                    public void onLog(String message) {
                        try {
                            sendSseEvent(emitter, "log", Map.of("message", message));
                        } catch (IOException e) {
                            log.warn("Failed to send log event: {}", e.getMessage());
                        }
                    }

                    @Override
                    public void onComplete(PlanetPopulationProgress.PopulationSummary summary) {
                        try {
                            Map<String, Object> data = new HashMap<>();
                            data.put("planetFound", summary.isPlanetFound());
                            data.put("totalGameCharacters", summary.getTotalGameCharacters());
                            data.put("countriesWithLeaders", summary.getCountriesWithLeaders());
                            data.put("regionsWithGovernors", summary.getRegionsWithGovernors());
                            data.put("aiCharactersReused", summary.getAiCharactersReused());
                            data.put("newAiCharactersGenerated", summary.getNewAiCharactersGenerated());
                            data.put("playerCharactersRegistered", summary.getPlayerCharactersRegistered());
                            sendSseEvent(emitter, "complete", data);
                            emitter.complete();
                            log.info("Streamed planet creation completed successfully");
                        } catch (IOException e) {
                            log.warn("Failed to send complete event: {}", e.getMessage());
                            try {
                                emitter.complete();
                            } catch (Exception ex) {
                                log.warn("Failed to complete emitter: {}", ex.getMessage());
                            }
                        }
                    }
                };

                // Execute the phased planet population
                planetService.populatePlanetWithGameData(request, sseProgress);

            } catch (Exception e) {
                log.error("Error during streamed planet creation", e);
                try {
                    sendSseEvent(emitter, "error", Map.of(
                            "message", e.getMessage(),
                            "type", e.getClass().getSimpleName()
                    ));
                } catch (IOException ex) {
                    log.warn("Failed to send error event: {}", ex.getMessage());
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * Send an SSE event safely, handling disconnected clients.
     */
    private void sendSseEvent(SseEmitter emitter, String name, Object data) throws IOException {
        emitter.send(SseEmitter.event()
                .name(name)
                .data(data));
    }

    // ========== Diplomacy Endpoints ==========

    /**
     * Get all diplomacy factions (G20 countries).
     */
    @GetMapping("/diplomacy/factions")
    public ResponseEntity<List<FourxFaction>> getDiplomacyFactions() {
        return ResponseEntity.ok(diplomacyService.getAllFactions());
    }

    /**
     * Get diplomatic relations for a country.
     */
    @GetMapping("/diplomacy/relations/{countryIso}")
    public ResponseEntity<List<FourxDiplomaticRelation>> getDiplomacyRelations(@PathVariable String countryIso) {
        return ResponseEntity.ok(diplomacyService.getRelations(countryIso));
    }

    /**
     * Declare war on another country.
     */
    @PostMapping("/diplomacy/declare-war")
    public ResponseEntity<Map<String, Object>> declareWar(@RequestBody Map<String, String> request) {
        String attacker = request.get("attackerIso");
        String defender = request.get("defenderIso");
        if (attacker == null || defender == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "attackerIso and defenderIso are required"));
        }
        FourxDiplomaticRelation relation = diplomacyService.declareWar(attacker, defender);
        return ResponseEntity.ok(Map.of(
                "attacker", attacker,
                "defender", defender,
                "atWar", relation.isAtWar(),
                "relationScore", relation.getRelationScore()
        ));
    }

    /**
     * Establish peace between two countries.
     */
    @PostMapping("/diplomacy/establish-peace")
    public ResponseEntity<Map<String, Object>> establishPeace(@RequestBody Map<String, String> request) {
        String f1 = request.get("faction1Iso");
        String f2 = request.get("faction2Iso");
        if (f1 == null || f2 == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "faction1Iso and faction2Iso are required"));
        }
        FourxDiplomaticRelation relation = diplomacyService.establishPeace(f1, f2);
        return ResponseEntity.ok(Map.of(
                "faction1", f1,
                "faction2", f2,
                "atWar", relation.isAtWar(),
                "relationScore", relation.getRelationScore()
        ));
    }

    /**
     * Add a treaty between two countries.
     */
    @PostMapping("/diplomacy/add-treaty")
    public ResponseEntity<Map<String, Object>> addTreaty(@RequestBody Map<String, String> request) {
        String f1 = request.get("faction1Iso");
        String f2 = request.get("faction2Iso");
        String treatyType = request.getOrDefault("treatyType", "TRADE");
        if (f1 == null || f2 == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "faction1Iso and faction2Iso are required"));
        }
        FourxDiplomaticRelation relation = diplomacyService.addTreaty(f1, f2, treatyType);
        return ResponseEntity.ok(Map.of(
                "faction1", f1,
                "faction2", f2,
                "treatyType", treatyType,
                "treaties", relation.getActiveTreaties(),
                "relationScore", relation.getRelationScore()
        ));
    }

    /**
     * Generate a random diplomatic event for a country.
     */
    @PostMapping("/diplomacy/event/{countryIso}")
    public ResponseEntity<Map<String, Object>> generateDiplomaticEvent(@PathVariable String countryIso) {
        Map<String, Object> event = diplomacyService.generateDiplomaticEvent(countryIso);
        return ResponseEntity.ok(event);
    }

    /**
     * Check if two countries are at war.
     */
    @GetMapping("/diplomacy/is-at-war")
    public ResponseEntity<Map<String, Object>> isAtWar(
            @RequestParam String faction1, @RequestParam String faction2) {
        boolean atWar = diplomacyService.isAtWar(faction1, faction2);
        return ResponseEntity.ok(Map.of(
                "faction1", faction1,
                "faction2", faction2,
                "atWar", atWar
        ));
    }

    // ------------------------------------------------------------------
    // Espionage operations
    // ------------------------------------------------------------------

    /**
     * Deploy a spy to a target country.
     */
    @PostMapping("/espionage/deploy-spy")
    public ResponseEntity<EspionageService.SpyDeploymentResult> deploySpy(
            @RequestParam String deployerIso,
            @RequestParam String targetIso,
            @RequestParam(defaultValue = "INFILTRATOR") EspionageService.SpyType spyType) {
        EspionageService.SpyDeploymentResult result = espionageService.deploySpy(deployerIso, targetIso, spyType);
        return ResponseEntity.ok(result);
    }

    /**
     * Execute sabotage operation on a target country.
     */
    @PostMapping("/espionage/sabotage")
    public ResponseEntity<EspionageService.SabotageResult> executeSabotage(
            @RequestParam String targetIso,
            @RequestParam(defaultValue = "INDUSTRY") EspionageService.SabotageType sabotageType) {
        EspionageService.SabotageResult result = espionageService.executeSabotage(targetIso, sabotageType);
        return ResponseEntity.ok(result);
    }

    /**
     * Gather intelligence on a target country.
     */
    @GetMapping("/espionage/intelligence/{targetIso}")
    public ResponseEntity<EspionageService.IntelReport> gatherIntelligence(@PathVariable String targetIso) {
        EspionageService.IntelReport report = espionageService.gatherIntelligence(targetIso);
        return ResponseEntity.ok(report);
    }

    /**
     * Run counter-espionage operation to detect and capture enemy spies.
     */
    @PostMapping("/espionage/counter-espionage/{countryIso}")
    public ResponseEntity<EspionageService.CounterEspionageResult> runCounterEspionage(@PathVariable String countryIso) {
        EspionageService.CounterEspionageResult result = espionageService.runCounterEspionage(countryIso);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all active spies deployed by a country.
     */
    @GetMapping("/espionage/spies/deployer/{deployerIso}")
    public ResponseEntity<List<EspionageService.Spy>> getSpiesByDeployer(@PathVariable String deployerIso) {
        List<EspionageService.Spy> spies = espionageService.getSpiesByDeployer(deployerIso);
        return ResponseEntity.ok(spies);
    }

    /**
     * Get all spies deployed in a target country.
     */
    @GetMapping("/espionage/spies/target/{targetIso}")
    public ResponseEntity<List<EspionageService.Spy>> getSpiesInTarget(@PathVariable String targetIso) {
        List<EspionageService.Spy> spies = espionageService.getSpiesInTarget(targetIso);
        return ResponseEntity.ok(spies);
    }

    /**
     * Get intelligence reports for a target country.
     */
    @GetMapping("/espionage/intelligence-reports/{targetIso}")
    public ResponseEntity<List<EspionageService.IntelReport>> getIntelligenceReports(@PathVariable String targetIso) {
        List<EspionageService.IntelReport> reports = espionageService.getIntelligenceReports(targetIso);
        return ResponseEntity.ok(reports);
    }
}
