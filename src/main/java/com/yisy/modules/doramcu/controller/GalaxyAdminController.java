package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.dto.CreateGalaxyRequest;
import com.yisy.modules.doramcu.dto.GalaxyAssessmentDto;
import com.yisy.modules.doramcu.dto.GalaxyAssessmentDto.FindingDto;
import com.yisy.modules.doramcu.dto.GalaxyBuildStatusDto;
import com.yisy.modules.doramcu.dto.GalaxyComparisonDto;
import com.yisy.modules.doramcu.dto.GalaxyComparisonSummaryDto;
import com.yisy.modules.doramcu.dto.GalaxyDashboardDto;
import com.yisy.modules.doramcu.dto.GalaxyDashboardSummaryDto;
import com.yisy.modules.doramcu.dto.GalaxyHealthDto;
import com.yisy.modules.doramcu.dto.GalaxyInfoDto;
import com.yisy.modules.doramcu.dto.GalaxyTemplateDto;
import com.yisy.modules.doramcu.dto.WorkOrderDto;
import com.yisy.modules.doramcu.dto.WorkforceReportDto;
import com.yisy.modules.doramcu.event.GalaxyEvent;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.event.GalaxyEventType;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.service.BackgroundGalaxyBuilder;
import com.yisy.modules.doramcu.service.GalaxyBackupService;
import com.yisy.modules.doramcu.service.GalaxyBuildStatus;
import com.yisy.modules.doramcu.service.GalaxyComparisonService;
import com.yisy.modules.doramcu.service.GalaxyOverseerService;
import com.yisy.modules.doramcu.service.GalaxyOverseerService.GalaxyAssessment;
import com.yisy.modules.doramcu.service.GalaxyOverseerService.WorkforceReport;
import com.yisy.modules.doramcu.service.GalaxyProgressService;
import com.yisy.modules.doramcu.service.GalaxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

/**
 * Admin API for galaxy management.
 * <p>
 * Provides endpoints to list, create, and delete galaxies.
 * Supports custom galaxy creation with configurable generation parameters.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin/galaxies")
public class GalaxyAdminController {

    private static final Logger log = LoggerFactory.getLogger(GalaxyAdminController.class);

    private final GalaxyService galaxyService;
    private final GalaxyProgressService galaxyProgressService;
    private final BackgroundGalaxyBuilder backgroundGalaxyBuilder;
    private final GalaxyOverseerService galaxyOverseerService;
    private final GalaxyEventService galaxyEventService;
    private final PlayerRepository playerRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final AiAdminService aiAdminService;
    private final GalaxyBackupService galaxyBackupService;
    private final GalaxyComparisonService galaxyComparisonService;

    public GalaxyAdminController(GalaxyService galaxyService,
                                 GalaxyProgressService galaxyProgressService,
                                 BackgroundGalaxyBuilder backgroundGalaxyBuilder,
                                 GalaxyOverseerService galaxyOverseerService,
                                 GalaxyEventService galaxyEventService,
                                 PlayerRepository playerRepository,
                                 AiCharacterRepository aiCharacterRepository,
                                 AiAdminService aiAdminService,
                                 GalaxyBackupService galaxyBackupService,
                                 GalaxyComparisonService galaxyComparisonService) {
        this.galaxyService = galaxyService;
        this.galaxyProgressService = galaxyProgressService;
        this.backgroundGalaxyBuilder = backgroundGalaxyBuilder;
        this.galaxyOverseerService = galaxyOverseerService;
        this.galaxyEventService = galaxyEventService;
        this.playerRepository = playerRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.aiAdminService = aiAdminService;
        this.galaxyBackupService = galaxyBackupService;
        this.galaxyComparisonService = galaxyComparisonService;
    }

    /**
     * Lists all galaxies with their generation parameters.
     *
     * @return list of galaxy info DTOs
     */
    @GetMapping
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<GalaxyInfoDto>> listGalaxies() {
        log.info("Admin requested galaxy list");
        List<GalaxyInfoDto> galaxies = galaxyService.getAllGalaxies().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(galaxies);
    }

    /**
     * Gets a single galaxy by name with full generation parameters.
     *
     * @param name the galaxy name
     * @return galaxy info DTO or 404 if not found
     */
    @GetMapping("/{name}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<GalaxyInfoDto> getGalaxy(@PathVariable String name) {
        log.info("Admin requested galaxy: {}", name);
        Optional<com.yisy.modules.doramcu.model.Galaxy> galaxy = galaxyService.getGalaxyByName(name);
        if (galaxy.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GalaxyInfoDto());
        }
        return ResponseEntity.ok(toDto(galaxy.get()));
    }

    /**
     * Creates a new custom galaxy with the given configuration.
     * <p>
     * If no parameters are provided, the default template is used.
     * A random seed is generated if not specified.
     * </p>
     *
     * @param request the galaxy creation parameters
     * @return the created galaxy info DTO or error
     */
    @PostMapping
    public ResponseEntity<?> createGalaxy(@RequestBody CreateGalaxyRequest request) {
        String name = request.getName();
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Galaxy name is required"));
        }

        if (galaxyService.hasGalaxy(name)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Galaxy '" + name + "' already exists"));
        }

        log.info("Admin requested creation of galaxy: {}", name);

        try {
            // Build config from request, using defaults for missing values
            GalaxyGenerationConfig.Builder configBuilder = GalaxyGenerationConfig.builder();

            if (request.getSystemCount() != null) {
                configBuilder.systemCount(request.getSystemCount());
            }
            if (request.getPlanetsPerSystem() != null) {
                configBuilder.planetsPerSystem(request.getPlanetsPerSystem());
            }
            if (request.getMoonChance() != null) {
                configBuilder.moonChance(request.getMoonChance());
            }
            if (request.getAsteroidBeltChance() != null) {
                configBuilder.asteroidBeltChance(request.getAsteroidBeltChance());
            }
            if (request.getGasGiantChance() != null) {
                configBuilder.gasGiantChance(request.getGasGiantChance());
            }
            if (request.getNebulaChance() != null) {
                configBuilder.nebulaChance(request.getNebulaChance());
            }
            if (request.getBlackHoleChance() != null) {
                configBuilder.blackHoleChance(request.getBlackHoleChance());
            }
            if (request.getCometChance() != null) {
                configBuilder.cometChance(request.getCometChance());
            }
            if (request.getSpaceStationChance() != null) {
                configBuilder.spaceStationChance(request.getSpaceStationChance());
            }
            if (request.getHyperlaneDensity() != null) {
                configBuilder.hyperlaneDensity(request.getHyperlaneDensity());
            }
            if (request.getGameType() != null) {
                configBuilder.gameType(request.getGameType());
            }
            if (request.getDifficultyLevel() != null) {
                configBuilder.difficultyLevel(request.getDifficultyLevel());
            }

            GalaxyGenerationConfig config = configBuilder.build();
            long seed = request.getSeed() != null ? request.getSeed() : System.currentTimeMillis();

            // Build AI-controlled empires from existing players
            Map<Integer, Empire> empires = buildEmpiresFromPlayers();

            // Set progress listener
            galaxyService.setProgressListener(galaxyProgressService);

            // Generate and persist
            GalaxyService.GalaxyGenerationResult result = galaxyService.generateAndSaveGalaxyWithEmpires(
                    name, empires,
                    config.getSystemCount(), config.getPlanetsPerSystem(),
                    config.getMoonChance(), config.getAsteroidBeltChance(),
                    config.getGasGiantChance(), config.getNebulaChance(),
                    config.getBlackHoleChance(), config.getCometChance(),
                    config.getSpaceStationChance(), config.getHyperlaneDensity(),
                    seed
            );

            com.yisy.modules.doramcu.model.Galaxy dbGalaxy = result.getDatabaseGalaxy();
            dbGalaxy.setSeed(seed);
            if (request.getDescription() != null) {
                dbGalaxy.setDescription(request.getDescription());
            }
            if (request.getTheme() != null) {
                dbGalaxy.setTheme(request.getTheme());
            }

            log.info("Galaxy '{}' created successfully with seed {}", name, seed);

            // Publish GALAXY_CREATED event
            galaxyEventService.publish(GalaxyEventType.GALAXY_CREATED, name, dbGalaxy.getId(),
                    "template", "custom",
                    "seed", String.valueOf(seed),
                    "systemCount", String.valueOf(config.getSystemCount()));

            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(dbGalaxy));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid galaxy creation parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to create galaxy '{}'", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create galaxy: " + e.getMessage()));
        }
    }

    /**
     * Deletes a galaxy by name.
     * <p>
     * WARNING: This also deletes all solar systems, stars, planets, and celestial bodies
     * belonging to the galaxy.
     * </p>
     *
     * @param name the galaxy name
     * @return success message or error
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteGalaxy(@PathVariable String name) {
        log.info("Admin requested deletion of galaxy: {}", name);

        Optional<com.yisy.modules.doramcu.model.Galaxy> galaxy = galaxyService.getGalaxyByName(name);
        if (galaxy.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        try {
            galaxyService.deleteGalaxy(name);
            log.info("Galaxy '{}' deleted successfully", name);
            return ResponseEntity.ok(Map.of("message", "Galaxy '" + name + "' deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete galaxy '{}'", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete galaxy: " + e.getMessage()));
        }
    }

    /**
     * Regenerates an existing galaxy from its stored generation parameters.
     * <p>
     * Deletes the existing galaxy and regenerates it with the same seed and config.
     * </p>
     *
     * @param name the galaxy name
     * @return the regenerated galaxy info DTO or error
     */
    @PostMapping("/{name}/regenerate")
    public ResponseEntity<?> regenerateGalaxy(@PathVariable String name) {
        log.info("Admin requested regeneration of galaxy: {}", name);

        Optional<com.yisy.modules.doramcu.model.Galaxy> existing = galaxyService.getGalaxyByName(name);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        com.yisy.modules.doramcu.model.Galaxy gal = existing.get();
        if (gal.getSeed() == null || gal.getSystemCount() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Galaxy '" + name + "' has no stored generation parameters"));
        }

        try {
            // Delete existing galaxy
            galaxyService.deleteGalaxy(name);

            // Rebuild config from stored parameters
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .systemCount(gal.getSystemCount())
                    .planetsPerSystem(gal.getPlanetsPerSystem() != null ? gal.getPlanetsPerSystem() : GalaxyGenerationConfig.DEFAULT_PLANETS_PER_SYSTEM)
                    .moonChance(gal.getMoonChance() != null ? gal.getMoonChance() : GalaxyGenerationConfig.DEFAULT_MOON_CHANCE)
                    .asteroidBeltChance(gal.getAsteroidBeltChance() != null ? gal.getAsteroidBeltChance() : GalaxyGenerationConfig.DEFAULT_ASTEROID_BELT_CHANCE)
                    .gasGiantChance(gal.getGasGiantChance() != null ? gal.getGasGiantChance() : GalaxyGenerationConfig.DEFAULT_GAS_GIANT_CHANCE)
                    .nebulaChance(gal.getNebulaChance() != null ? gal.getNebulaChance() : GalaxyGenerationConfig.DEFAULT_NEBULA_CHANCE)
                    .blackHoleChance(gal.getBlackHoleChance() != null ? gal.getBlackHoleChance() : GalaxyGenerationConfig.DEFAULT_BLACK_HOLE_CHANCE)
                    .cometChance(gal.getCometChance() != null ? gal.getCometChance() : GalaxyGenerationConfig.DEFAULT_COMET_CHANCE)
                    .spaceStationChance(gal.getSpaceStationChance() != null ? gal.getSpaceStationChance() : GalaxyGenerationConfig.DEFAULT_SPACE_STATION_CHANCE)
                    .hyperlaneDensity(gal.getHyperlaneDensity())
                    .build();

            Map<Integer, Empire> empires = buildEmpiresFromPlayers();
            galaxyService.setProgressListener(galaxyProgressService);

            GalaxyService.GalaxyGenerationResult result = galaxyService.generateAndSaveGalaxyWithEmpires(
                    name, empires,
                    config.getSystemCount(), config.getPlanetsPerSystem(),
                    config.getMoonChance(), config.getAsteroidBeltChance(),
                    config.getGasGiantChance(), config.getNebulaChance(),
                    config.getBlackHoleChance(), config.getCometChance(),
                    config.getSpaceStationChance(), config.getHyperlaneDensity(),
                    gal.getSeed()
            );

            com.yisy.modules.doramcu.model.Galaxy dbGalaxy = result.getDatabaseGalaxy();
            dbGalaxy.setSeed(gal.getSeed());
            if (gal.getDescription() != null) {
                dbGalaxy.setDescription(gal.getDescription());
            }
            if (gal.getGameType() != null) {
                dbGalaxy.setGameType(gal.getGameType());
            }
            if (gal.getDifficultyLevel() != null) {
                dbGalaxy.setDifficultyLevel(gal.getDifficultyLevel());
            }
            if (gal.getTheme() != null) {
                dbGalaxy.setTheme(gal.getTheme());
            }

            log.info("Galaxy '{}' regenerated successfully", name);
            return ResponseEntity.ok(toDto(dbGalaxy));

        } catch (Exception e) {
            log.error("Failed to regenerate galaxy '{}'", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to regenerate galaxy: " + e.getMessage()));
        }
    }

    /**
     * Lists all available galaxy templates with their metadata.
     * <p>
     * Admins can use this endpoint to discover available templates before
     * creating a galaxy from a template.
     * </p>
     *
     * @return list of template metadata DTOs
     */
    @GetMapping("/templates")
    public ResponseEntity<List<GalaxyTemplateDto>> listTemplates() {
        log.info("Admin requested template list");
        List<GalaxyTemplateDto> templates = Arrays.stream(GalaxyTemplate.getAllTemplates())
                .map(this::toTemplateDto)
                .toList();
        return ResponseEntity.ok(templates);
    }

    /**
     * Creates a new galaxy from a predefined template.
     * <p>
     * The template provides the base configuration. Any non-null fields in the
     * request body override the template's defaults for those parameters.
     * </p>
     *
     * @param request the galaxy creation parameters (name required, template required)
     * @return the created galaxy info DTO or error
     */
    @PostMapping("/from-template")
    public ResponseEntity<?> createGalaxyFromTemplate(@RequestBody CreateGalaxyRequest request) {
        String name = request.getName();
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Galaxy name is required"));
        }

        String templateName = request.getTemplate();
        if (templateName == null || templateName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Template name is required for /from-template endpoint"));
        }

        if (galaxyService.hasGalaxy(name)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Galaxy '" + name + "' already exists"));
        }

        log.info("Admin requested creation of galaxy '{}' from template '{}'", name, templateName);

        try {
            // Validate template exists
            GalaxyTemplate template = GalaxyTemplate.findByName(templateName);
            log.info("Resolved template: {} ({})", template.name(), template.getDisplayName());

            long seed = request.getSeed() != null ? request.getSeed() : System.currentTimeMillis();

            // Build AI-controlled empires from existing players
            Map<Integer, Empire> empires = buildEmpiresFromPlayers();

            // Set progress listener
            galaxyService.setProgressListener(galaxyProgressService);

            // Generate and persist from template
            GalaxyService.GalaxyGenerationResult result = galaxyService.generateAndSaveGalaxyFromTemplate(
                    name, empires, templateName, seed,
                    request.getSystemCount(), request.getPlanetsPerSystem(),
                    request.getMoonChance(), request.getAsteroidBeltChance(),
                    request.getGasGiantChance(), request.getNebulaChance(),
                    request.getBlackHoleChance(), request.getCometChance(),
                    request.getSpaceStationChance(), request.getHyperlaneDensity()
            );

            com.yisy.modules.doramcu.model.Galaxy dbGalaxy = result.getDatabaseGalaxy();
            dbGalaxy.setSeed(seed);
            if (request.getDescription() != null) {
                dbGalaxy.setDescription(request.getDescription());
            }
            if (request.getTheme() != null) {
                dbGalaxy.setTheme(request.getTheme());
            }

            log.info("Galaxy '{}' created from template '{}' with seed {}", name, templateName, seed);

            // Publish GALAXY_CREATED event
            galaxyEventService.publish(GalaxyEventType.GALAXY_CREATED, name, dbGalaxy.getId(),
                    "template", templateName,
                    "seed", String.valueOf(seed),
                    "systemCount", String.valueOf(dbGalaxy.getSystemCount() != null ? dbGalaxy.getSystemCount() : 0));

            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(dbGalaxy));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid template or parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to create galaxy '{}' from template '{}'", name, templateName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create galaxy: " + e.getMessage()));
        }
    }

    /**
     * Returns the default galaxy template configuration.
     *
     * @return the default config as a map
     */
    @GetMapping("/template/default")
    public ResponseEntity<Map<String, Object>> getDefaultTemplate() {
        GalaxyGenerationConfig config = GalaxyFactory.defaultConfig();
        Map<String, Object> template = new HashMap<>();
        template.put("systemCount", config.getSystemCount());
        template.put("planetsPerSystem", config.getPlanetsPerSystem());
        template.put("moonChance", config.getMoonChance());
        template.put("asteroidBeltChance", config.getAsteroidBeltChance());
        template.put("gasGiantChance", config.getGasGiantChance());
        template.put("nebulaChance", config.getNebulaChance());
        template.put("blackHoleChance", config.getBlackHoleChance());
        template.put("cometChance", config.getCometChance());
        template.put("spaceStationChance", config.getSpaceStationChance());
        template.put("hyperlaneDensity", config.getHyperlaneDensity());
        template.put("sectorSize", config.getSectorSize());
        template.put("subSectorDimension", config.getSubSectorDimension());
        template.put("civilizationDensity", config.getCivilizationDensity());
        template.put("resourceDistribution", config.getResourceDistribution());
        template.put("galaxyShape", config.getGalaxyShape());
        template.put("namingConvention", config.getNamingConvention());
        template.put("empireCount", config.getEmpireCount());
        template.put("difficultyLevel", config.getDifficultyLevel());
        template.put("gameType", config.getGameType());
        return ResponseEntity.ok(template);
    }

    private GalaxyInfoDto toDto(com.yisy.modules.doramcu.model.Galaxy galaxy) {
        GalaxyInfoDto dto = new GalaxyInfoDto(
                galaxy.getId(),
                galaxy.getName(),
                galaxy.getTotalSectors(),
                galaxy.getHyperlaneDensity(),
                galaxy.getSeed(),
                galaxy.getSystemCount(),
                galaxy.getPlanetsPerSystem(),
                galaxy.getMoonChance(),
                galaxy.getAsteroidBeltChance(),
                galaxy.getGasGiantChance(),
                galaxy.getNebulaChance(),
                galaxy.getBlackHoleChance(),
                galaxy.getCometChance(),
                galaxy.getSpaceStationChance(),
                galaxy.getCreatedAt(),
                galaxy.getDescription(),
                galaxy.getGameType(),
                galaxy.getDifficultyLevel(),
                galaxy.getTheme()
        );
        dto.setGalaxyOverseerId(galaxy.getGalaxyOverseerId());
        return dto;
    }

    private Map<Integer, Empire> buildEmpiresFromPlayers() {
        Map<Integer, Empire> empires = new HashMap<>();
        for (Player player : playerRepository.findAll()) {
            int empireId = player.getId().intValue();
            String empireName = player.getEmpireName();
            Empire empire = new Empire(empireId, empireName, -1);
            empires.put(empireId, empire);
        }
        return empires;
    }

    private GalaxyTemplateDto toTemplateDto(GalaxyTemplate template) {
        GalaxyGenerationConfig config = template.getConfig();
        return new GalaxyTemplateDto(
                template.name(),
                template.getDisplayName(),
                template.getDescription(),
                config.getGameType(),
                config.getDifficultyLevel(),
                config.getSectorSize(),
                config.getEmpireCount(),
                config.getGalaxyShape(),
                config.getNamingConvention(),
                config.getHyperlaneDensity(),
                config.getCivilizationDensity(),
                config.getResourceDistribution()
        );
    }

    // ---- Build Status Endpoints ----

    /**
     * Returns the current background build status for a galaxy.
     * <p>
     * If no build has ever started for this galaxy, returns a status with
     * completion percentage derived from the persisted galaxy data.
     * </p>
     *
     * @param name the galaxy name
     * @return build status DTO or 404 if galaxy not found
     */
    @GetMapping("/{name}/build-status")
    public ResponseEntity<?> getBuildStatus(@PathVariable String name) {
        log.info("Admin requested build status for galaxy: {}", name);

        if (!galaxyService.hasGalaxy(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        GalaxyBuildStatus status = backgroundGalaxyBuilder.getBuildStatus(name);
        if (status == null) {
            // No build has started — return a status based on DB data
            int completionPercent = galaxyService.getGalaxyCompletionPercent(name);
            GalaxyBuildStatusDto dto = new GalaxyBuildStatusDto();
            dto.setGalaxyName(name);
            dto.setRunning(false);
            dto.setCompleted(completionPercent >= 100);
            dto.setFailed(false);
            dto.setCompletionPercent(completionPercent);
            dto.setSystemsGenerated(0);
            dto.setPhase(completionPercent >= 100 ? "Complete" : "Not started");
            dto.setErrorMessage(null);
            dto.setStartTime(0);
            dto.setEndTime(0);
            dto.setElapsedMs(0);
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.ok(toBuildStatusDto(status));
    }

    /**
     * Streams real-time build progress for a galaxy via SSE.
     * <p>
     * Clients subscribe to this endpoint to receive progress updates
     * during background galaxy generation. The stream completes when
     * the build finishes or after a timeout.
     * </p>
     *
     * @param name the galaxy name
     * @return SSE emitter for progress events
     */
    @GetMapping(value = "/{name}/build-progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getBuildProgress(@PathVariable String name) {
        log.info("Client subscribed to build progress for galaxy: {}", name);
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30 min timeout

        // Send initial status
        GalaxyBuildStatus status = backgroundGalaxyBuilder.getBuildStatus(name);
        if (status != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("status")
                        .data(toBuildStatusDto(status)));
            } catch (Exception e) {
                log.warn("Failed to send initial status for galaxy '{}': {}", name, e.getMessage());
            }
        } else {
            int completionPercent = galaxyService.getGalaxyCompletionPercent(name);
            try {
                emitter.send(SseEmitter.event()
                        .name("status")
                        .data(Map.of(
                                "galaxyName", name,
                                "completionPercent", completionPercent,
                                "phase", completionPercent >= 100 ? "Complete" : "Not started"
                        )));
            } catch (Exception e) {
                log.warn("Failed to send initial status for galaxy '{}': {}", name, e.getMessage());
            }
        }

        // Poll for updates every 5 seconds
        java.util.concurrent.ScheduledExecutorService scheduler =
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        java.util.concurrent.ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            GalaxyBuildStatus currentStatus = backgroundGalaxyBuilder.getBuildStatus(name);
            if (currentStatus != null) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("progress")
                            .data(toBuildStatusDto(currentStatus)));
                    if (currentStatus.isCompleted() || currentStatus.isFailed()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            // ignore
                        }
                        emitter.complete();
                        scheduler.shutdown();
                    }
                } catch (Exception e) {
                    log.warn("Failed to send progress for galaxy '{}': {}", name, e.getMessage());
                    emitter.complete();
                    scheduler.shutdown();
                }
            }
        }, 5, 5, java.util.concurrent.TimeUnit.SECONDS);

        emitter.onCompletion(() -> {
            log.debug("SSE stream completed for galaxy build progress: {}", name);
            scheduler.shutdown();
        });
        emitter.onTimeout(() -> {
            log.debug("SSE stream timed out for galaxy build progress: {}", name);
            scheduler.shutdown();
        });
        emitter.onError((e) -> {
            log.warn("SSE stream error for galaxy build progress: {}", name, e);
            scheduler.shutdown();
        });

        return emitter;
    }

    /**
     * Starts background generation of remaining sectors for a galaxy.
     * <p>
     * If a build is already running, returns the current build status.
     * If the galaxy is already 100% complete, returns a message indicating
     * no build is needed.
     * </p>
     *
     * @param name the galaxy name
     * @return build status DTO or error
     */
    @PostMapping("/{name}/start-build")
    public ResponseEntity<?> startBuild(@PathVariable String name) {
        log.info("Admin requested start of background build for galaxy: {}", name);

        if (!galaxyService.hasGalaxy(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        int completionPercent = galaxyService.getGalaxyCompletionPercent(name);
        if (completionPercent >= 100) {
            return ResponseEntity.ok(
                    Map.of("message", "Galaxy '" + name + "' is already 100% complete"));
        }

        GalaxyBuildStatus status = backgroundGalaxyBuilder.startBackgroundGeneration(name);
        return ResponseEntity.ok(toBuildStatusDto(status));
    }

    /**
     * Cancels the currently running background build for a galaxy.
     *
     * @param name the galaxy name
     * @return success message or error
     */
    @PostMapping("/{name}/cancel-build")
    public ResponseEntity<?> cancelBuild(@PathVariable String name) {
        log.info("Admin requested cancel of background build for galaxy: {}", name);

        GalaxyBuildStatus status = backgroundGalaxyBuilder.getBuildStatus(name);
        if (status == null || !status.isRunning()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No running build found for galaxy '" + name + "'"));
        }

        boolean cancelled = backgroundGalaxyBuilder.cancelCurrentBuild();
        if (cancelled) {
            return ResponseEntity.ok(Map.of("message", "Build for galaxy '" + name + "' cancelled"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to cancel build for galaxy '" + name + "'"));
        }
    }

    private GalaxyBuildStatusDto toBuildStatusDto(GalaxyBuildStatus status) {
        return new GalaxyBuildStatusDto(
                status.getGalaxyName(),
                status.isRunning(),
                status.isCompleted(),
                status.isFailed(),
                status.getCompletionPercent(),
                status.getSystemsGenerated(),
                status.getPhase(),
                status.getErrorMessage(),
                status.getStartTime(),
                status.getEndTime(),
                status.getElapsedTimeMs()
        );
    }

    // ---- Health Monitoring Endpoints ----

    /**
     * Returns a unified health view for a galaxy.
     * <p>
     * Combines assessment results, workforce data, build status, and completion
     * percentage into a single dashboard-friendly response.
     * </p>
     *
     * @param name the galaxy name
     * @return unified health DTO or 404 if galaxy not found
     */
    @GetMapping("/{name}/health")
    public ResponseEntity<?> getHealth(@PathVariable String name) {
        log.info("Admin requested health view for galaxy: {}", name);

        if (!galaxyService.hasGalaxy(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        Optional<com.yisy.modules.doramcu.model.Galaxy> galaxyOpt = galaxyService.getGalaxyByName(name);
        if (galaxyOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        com.yisy.modules.doramcu.model.Galaxy galaxy = galaxyOpt.get();
        GalaxyAssessment assessment = galaxyOverseerService.assessGalaxyNeeds(galaxy);
        WorkforceReport workforce = galaxyOverseerService.monitorWorkforce(galaxy.getId());

        GalaxyHealthDto dto = new GalaxyHealthDto();
        dto.setGalaxyName(name);
        dto.setGalaxyId(galaxy.getId());
        dto.setHealthScore(assessment.getHealthScore());
        dto.setHealthStatus(GalaxyHealthDto.deriveHealthStatus(assessment.getHealthScore()));
        dto.setPlayable(galaxyService.isGalaxyPlayable(name));
        dto.setCompletionPercent(galaxyService.getGalaxyCompletionPercent(name));
        dto.setSolarSystemCount(assessment.getSolarSystemCount());
        dto.setExpectedMaxSystems(assessment.getExpectedMaxSystems());
        dto.setAiAgentCount(workforce.getTotalAiAgents());
        dto.setOverstaffed(workforce.isOverstaffed());

        // Check if build is running
        GalaxyBuildStatus buildStatus = backgroundGalaxyBuilder.getBuildStatus(name);
        dto.setBuildRunning(buildStatus != null && buildStatus.isRunning());

        // Count findings by severity
        int critical = 0, high = 0, medium = 0;
        for (GalaxyAssessment.Finding f : assessment.getFindings()) {
            switch (f.getSeverity()) {
                case CRITICAL: critical++; break;
                case HIGH: high++; break;
                case MEDIUM: medium++; break;
                default: break;
            }
        }
        dto.setCriticalFindings(critical);
        dto.setHighFindings(high);
        dto.setMediumFindings(medium);
        dto.setTopRecommendations(assessment.getRecommendations());

        return ResponseEntity.ok(dto);
    }

    /**
     * Returns the detailed galaxy assessment from the Galaxy Overseer.
     * <p>
     * Evaluates the galaxy against the Earth 2026 rules database and returns
     * health score, findings with severity, and recommendations.
     * </p>
     *
     * @param name the galaxy name
     * @return assessment DTO or 404 if galaxy not found
     */
    @GetMapping("/{name}/assessment")
    public ResponseEntity<?> getAssessment(@PathVariable String name) {
        log.info("Admin requested assessment for galaxy: {}", name);

        if (!galaxyService.hasGalaxy(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        Optional<com.yisy.modules.doramcu.model.Galaxy> galaxyOpt = galaxyService.getGalaxyByName(name);
        if (galaxyOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        GalaxyAssessment assessment = galaxyOverseerService.assessGalaxyNeeds(galaxyOpt.get());
        return ResponseEntity.ok(toAssessmentDto(assessment));
    }

    /**
     * Returns the workforce monitoring report for a galaxy.
     * <p>
     * Shows AI agent counts broken down by role, staffing status,
     * and experience metrics.
     * </p>
     *
     * @param name the galaxy name
     * @return workforce report DTO or 404 if galaxy not found
     */
    @GetMapping("/{name}/workforce")
    public ResponseEntity<?> getWorkforce(@PathVariable String name) {
        log.info("Admin requested workforce report for galaxy: {}", name);

        Optional<com.yisy.modules.doramcu.model.Galaxy> galaxyOpt = galaxyService.getGalaxyByName(name);
        if (galaxyOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        WorkforceReport report = galaxyOverseerService.monitorWorkforce(galaxyOpt.get().getId());
        return ResponseEntity.ok(toWorkforceDto(report));
    }

    /**
     * Returns a summary health view for all galaxies.
     * <p>
     * Used by the admin dashboard to show a quick overview of all galaxy
     * health scores, completion percentages, and staffing status.
     * </p>
     *
     * @return list of health summary DTOs
     */
    @GetMapping("/health-summary")
    public ResponseEntity<List<GalaxyHealthDto>> getAllHealthSummary() {
        log.info("Admin requested health summary for all galaxies");
        List<GalaxyHealthDto> summaries = new ArrayList<>();

        for (com.yisy.modules.doramcu.model.Galaxy galaxy : galaxyService.getAllGalaxies()) {
            GalaxyAssessment assessment = galaxyOverseerService.assessGalaxyNeeds(galaxy);
            WorkforceReport workforce = galaxyOverseerService.monitorWorkforce(galaxy.getId());

            GalaxyHealthDto dto = new GalaxyHealthDto();
            dto.setGalaxyName(galaxy.getName());
            dto.setGalaxyId(galaxy.getId());
            dto.setHealthScore(assessment.getHealthScore());
            dto.setHealthStatus(GalaxyHealthDto.deriveHealthStatus(assessment.getHealthScore()));
            dto.setPlayable(galaxyService.isGalaxyPlayable(galaxy.getName()));
            dto.setCompletionPercent(galaxyService.getGalaxyCompletionPercent(galaxy.getName()));
            dto.setSolarSystemCount(assessment.getSolarSystemCount());
            dto.setExpectedMaxSystems(assessment.getExpectedMaxSystems());
            dto.setAiAgentCount(workforce.getTotalAiAgents());
            dto.setOverstaffed(workforce.isOverstaffed());

            GalaxyBuildStatus buildStatus = backgroundGalaxyBuilder.getBuildStatus(galaxy.getName());
            dto.setBuildRunning(buildStatus != null && buildStatus.isRunning());

            int critical = 0, high = 0, medium = 0;
            for (GalaxyAssessment.Finding f : assessment.getFindings()) {
                switch (f.getSeverity()) {
                    case CRITICAL: critical++; break;
                    case HIGH: high++; break;
                    case MEDIUM: medium++; break;
                    default: break;
                }
            }
            dto.setCriticalFindings(critical);
            dto.setHighFindings(high);
            dto.setMediumFindings(medium);
            dto.setTopRecommendations(assessment.getRecommendations());
            summaries.add(dto);
        }

        return ResponseEntity.ok(summaries);
    }

    /**
     * Triggers an immediate assessment and work order creation for a galaxy.
     * <p>
     * Forces the Galaxy Overseer to re-evaluate the galaxy and create
     * work orders for any issues found.
     * </p>
     *
     * @param name the galaxy name
     * @return assessment DTO with orders created count
     */
    @PostMapping("/{name}/assess")
    public ResponseEntity<?> triggerAssessment(@PathVariable String name) {
        log.info("Admin triggered assessment for galaxy: {}", name);

        if (!galaxyService.hasGalaxy(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        Optional<com.yisy.modules.doramcu.model.Galaxy> galaxyOpt = galaxyService.getGalaxyByName(name);
        if (galaxyOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        List<String> orders = galaxyOverseerService.createWorkOrdersForGalaxy(galaxyOpt.get());
        GalaxyAssessment assessment = galaxyOverseerService.assessGalaxyNeeds(galaxyOpt.get());

        GalaxyAssessmentDto dto = toAssessmentDto(assessment);
        return ResponseEntity.ok(Map.of(
                "assessment", dto,
                "ordersCreated", orders.size(),
                "orders", orders
        ));
    }

    // ---- Galaxy Event Stream Endpoints ----

    /**
     * Streams all galaxy lifecycle events via SSE.
     * <p>
     * Clients subscribe to this endpoint to receive real-time events for all galaxies,
     * including build progress, health changes, character transitions, and work orders.
     * On subscription, the client receives the recent event history (last 100 events).
     * </p>
     *
     * @param clientId unique client identifier
     * @return SSE emitter for galaxy events
     */
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToEvents(@RequestParam(defaultValue = "dashboard") String clientId) {
        log.info("Client '{}' subscribed to all galaxy events", clientId);
        return galaxyEventService.subscribeAll(clientId);
    }

    /**
     * Streams galaxy lifecycle events for a specific galaxy via SSE.
     * <p>
     * On subscription, the client receives the recent event history for the specified galaxy.
     * </p>
     *
     * @param name the galaxy name
     * @param clientId unique client identifier
     * @return SSE emitter for galaxy events
     */
    @GetMapping(value = "/{name}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToGalaxyEvents(@PathVariable String name,
                                              @RequestParam(defaultValue = "dashboard") String clientId) {
        log.info("Client '{}' subscribed to galaxy events for: {}", clientId, name);
        return galaxyEventService.subscribeGalaxy(clientId, name);
    }

    /**
     * Returns the recent galaxy event history.
     * <p>
     * Useful for clients that want to catch up on missed events without maintaining
     * an SSE connection. Returns the last 100 events, optionally filtered by galaxy name.
     * </p>
     *
     * @param galaxyName optional galaxy name filter
     * @return list of recent events
     */
    @GetMapping("/events/history")
    public ResponseEntity<List<Map<String, Object>>> getEventHistory(
            @RequestParam(required = false) String galaxyName) {
        log.info("Admin requested event history{}", galaxyName != null ? " for galaxy: " + galaxyName : "");
        List<GalaxyEvent> events = galaxyEventService.getEventHistory(galaxyName);
        List<Map<String, Object>> eventMaps = new ArrayList<>();
        for (GalaxyEvent event : events) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", event.getType().name());
            map.put("galaxyName", event.getGalaxyName());
            if (event.getGalaxyId() != null) {
                map.put("galaxyId", event.getGalaxyId());
            }
            map.put("timestamp", event.getTimestamp().toString());
            if (!event.getPayload().isEmpty()) {
                map.put("payload", event.getPayload());
            }
            eventMaps.add(map);
        }
        return ResponseEntity.ok(eventMaps);
    }

    /**
     * Returns the number of currently connected event subscribers.
     */
    @GetMapping("/events/subscribers")
    public ResponseEntity<Map<String, Object>> getEventSubscriberCount() {
        return ResponseEntity.ok(Map.of("subscribers", galaxyEventService.getSubscriberCount()));
    }

    // ---- Galaxy Dashboard Endpoints ----

    /**
     * Returns a comprehensive dashboard view for a single galaxy.
     * <p>
     * Aggregates health assessment, build status, workforce report, work orders,
     * recent events, and player population into a single response so admin dashboards
     * need only one API call to get the full picture of a galaxy's state.
     * </p>
     *
     * @param name the galaxy name
     * @return comprehensive dashboard DTO or 404 if galaxy not found
     */
    @GetMapping("/{name}/dashboard")
    public ResponseEntity<?> getDashboard(@PathVariable String name) {
        log.info("Admin requested dashboard for galaxy: {}", name);

        if (!galaxyService.hasGalaxy(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        Optional<com.yisy.modules.doramcu.model.Galaxy> galaxyOpt = galaxyService.getGalaxyByName(name);
        if (galaxyOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        com.yisy.modules.doramcu.model.Galaxy galaxy = galaxyOpt.get();
        GalaxyDashboardDto dto = new GalaxyDashboardDto();

        // ---- Galaxy Identity ----
        dto.setGalaxyName(name);
        dto.setGalaxyId(galaxy.getId());
        dto.setDescription(galaxy.getDescription());
        dto.setGameType(galaxy.getGameType());
        dto.setDifficultyLevel(galaxy.getDifficultyLevel());
        dto.setTheme(galaxy.getTheme());
        dto.setCreatedAt(galaxy.getCreatedAt());

        // ---- Health Section ----
        GalaxyAssessment assessment = galaxyOverseerService.assessGalaxyNeeds(galaxy);
        dto.setHealthScore(assessment.getHealthScore());
        dto.setHealthStatus(GalaxyHealthDto.deriveHealthStatus(assessment.getHealthScore()));
        dto.setPlayable(galaxyService.isGalaxyPlayable(name));
        dto.setTopRecommendations(assessment.getRecommendations());
        dto.setFindings(toFindingDtoList(assessment.getFindings()));

        int critical = 0, high = 0, medium = 0;
        for (GalaxyAssessment.Finding f : assessment.getFindings()) {
            switch (f.getSeverity()) {
                case CRITICAL: critical++; break;
                case HIGH: high++; break;
                case MEDIUM: medium++; break;
                default: break;
            }
        }
        dto.setCriticalFindings(critical);
        dto.setHighFindings(high);
        dto.setMediumFindings(medium);

        // ---- Build Section ----
        dto.setCompletionPercent(galaxyService.getGalaxyCompletionPercent(name));
        dto.setSolarSystemCount(assessment.getSolarSystemCount());
        dto.setExpectedMaxSystems(assessment.getExpectedMaxSystems());

        GalaxyBuildStatus buildStatus = backgroundGalaxyBuilder.getBuildStatus(name);
        if (buildStatus != null) {
            dto.setBuildRunning(buildStatus.isRunning());
            dto.setBuildPhase(buildStatus.getPhase());
            dto.setBuildErrorMessage(buildStatus.getErrorMessage());
            dto.setBuildElapsedMs(buildStatus.getElapsedTimeMs());
        } else {
            dto.setBuildRunning(false);
            dto.setBuildPhase(dto.getCompletionPercent() >= 100 ? "Complete" : "Not started");
        }

        // ---- Workforce Section ----
        WorkforceReport workforce = galaxyOverseerService.monitorWorkforce(galaxy.getId());
        dto.setAiAgentCount(workforce.getTotalAiAgents());
        dto.setOverstaffed(workforce.isOverstaffed());
        dto.setWorkforceRecommendation(workforce.getRecommendation());

        Map<String, Integer> byRole = new HashMap<>();
        byRole.put("GalaxyOverseer", workforce.getGalaxyOverseerCount());
        byRole.put("PlanetOverseer", workforce.getPlanetOverseerCount());
        byRole.put("Leader", workforce.getLeaderCount());
        byRole.put("Governor", workforce.getGovernorCount());
        byRole.put("Other", workforce.getOtherCount());
        dto.setAiAgentsByRole(byRole);

        // ---- Work Orders Section ----
        String prefix = "GalaxyOverseer:" + name;
        List<AiAdminOrder> orders = aiAdminService.getOrdersByPrefix(prefix);
        dto.setWorkOrderTotal(orders.size());

        long pendingCount = 0, runningCount = 0, completedCount = 0, failedCount = 0;
        for (AiAdminOrder order : orders) {
            switch (order.getStatus()) {
                case PENDING: pendingCount++; break;
                case RUNNING: runningCount++; break;
                case COMPLETED: completedCount++; break;
                case FAILED: failedCount++; break;
            }
        }
        dto.setWorkOrderPending((int) pendingCount);
        dto.setWorkOrderRunning((int) runningCount);
        dto.setWorkOrderCompleted((int) completedCount);
        dto.setWorkOrderFailed((int) failedCount);

        // Recent work orders (last 5, most recent first)
        List<WorkOrderDto> recentOrders = new ArrayList<>(orders)
                .stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(5)
                .map(this::toWorkOrderDto)
                .toList();
        dto.setRecentWorkOrders(recentOrders);

        // ---- Events Section ----
        List<GalaxyEvent> events = galaxyEventService.getEventHistory(name);
        List<Map<String, Object>> recentEvents = events.stream()
                .limit(10)
                .map(event -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", event.getType().name());
                    map.put("galaxyName", event.getGalaxyName());
                    if (event.getGalaxyId() != null) {
                        map.put("galaxyId", event.getGalaxyId());
                    }
                    map.put("timestamp", event.getTimestamp().toString());
                    if (!event.getPayload().isEmpty()) {
                        map.put("payload", event.getPayload());
                    }
                    return map;
                })
                .toList();
        dto.setRecentEvents(recentEvents);

        // ---- Player Population Section ----
        List<Player> allPlayers = playerRepository.findAll();
        List<AiCharacter> allAiCharacters = aiCharacterRepository.findAll();

        // Count AI characters as AI players, human players as those with email
        int aiPlayerCount = allAiCharacters.size();
        int humanPlayerCount = (int) allPlayers.stream().filter(p -> p.getEmail() != null).count();
        dto.setAiPlayerCount(aiPlayerCount);
        dto.setHumanPlayerCount(humanPlayerCount);
        dto.setPlayerCount(aiPlayerCount + humanPlayerCount);

        // Collect player names
        List<String> playerNames = new ArrayList<>();
        for (Player p : allPlayers) {
            if (p.getDisplayName() != null && !p.getDisplayName().isBlank()) {
                playerNames.add(p.getDisplayName());
            } else if (p.getEmpireName() != null) {
                playerNames.add(p.getEmpireName());
            }
        }
        for (AiCharacter ac : allAiCharacters) {
            playerNames.add(ac.getName());
        }
        dto.setPlayerNames(playerNames);

        return ResponseEntity.ok(dto);
    }

    /**
     * Returns a dashboard summary for all galaxies.
     * <p>
     * Provides a condensed overview of each galaxy's state, including health score,
     * completion percentage, build status, AI agent count, player count, and
     * pending work orders. Used by the admin dashboard to show all galaxies at a glance.
     * </p>
     *
     * @return list of dashboard summary DTOs
     */
    @GetMapping("/dashboard")
    public ResponseEntity<List<GalaxyDashboardSummaryDto>> getAllDashboards() {
        log.info("Admin requested dashboard for all galaxies");
        List<GalaxyDashboardSummaryDto> summaries = new ArrayList<>();

        List<Player> allPlayers = playerRepository.findAll();
        List<AiCharacter> allAiCharacters = aiCharacterRepository.findAll();
        int totalPlayers = allPlayers.size() + allAiCharacters.size();

        for (com.yisy.modules.doramcu.model.Galaxy galaxy : galaxyService.getAllGalaxies()) {
            String name = galaxy.getName();
            GalaxyAssessment assessment = galaxyOverseerService.assessGalaxyNeeds(galaxy);
            WorkforceReport workforce = galaxyOverseerService.monitorWorkforce(galaxy.getId());

            GalaxyDashboardSummaryDto dto = new GalaxyDashboardSummaryDto();
            dto.setGalaxyName(name);
            dto.setGalaxyId(galaxy.getId());
            dto.setGameType(galaxy.getGameType());
            dto.setHealthScore(assessment.getHealthScore());
            dto.setHealthStatus(GalaxyHealthDto.deriveHealthStatus(assessment.getHealthScore()));
            dto.setPlayable(galaxyService.isGalaxyPlayable(name));
            dto.setCompletionPercent(galaxyService.getGalaxyCompletionPercent(name));

            GalaxyBuildStatus buildStatus = backgroundGalaxyBuilder.getBuildStatus(name);
            dto.setBuildRunning(buildStatus != null && buildStatus.isRunning());

            dto.setAiAgentCount(workforce.getTotalAiAgents());
            dto.setPlayerCount(totalPlayers);
            dto.setCreatedAt(galaxy.getCreatedAt());

            // Count critical findings
            int critical = 0;
            for (GalaxyAssessment.Finding f : assessment.getFindings()) {
                if (f.getSeverity() == GalaxyAssessment.Severity.CRITICAL) {
                    critical++;
                }
            }
            dto.setCriticalFindings(critical);

            // Count pending work orders
            String prefix = "GalaxyOverseer:" + name;
            List<AiAdminOrder> pendingOrders = aiAdminService.getOrdersByPrefixAndStatus(
                    prefix, AiAdminOrder.OrderStatus.PENDING);
            dto.setPendingWorkOrders(pendingOrders.size());

            summaries.add(dto);
        }

        return ResponseEntity.ok(summaries);
    }

    private List<FindingDto> toFindingDtoList(List<GalaxyAssessment.Finding> findings) {
        List<FindingDto> dtoList = new ArrayList<>();
        for (GalaxyAssessment.Finding f : findings) {
            dtoList.add(new FindingDto(f.getSeverity().name(), f.getDescription(), f.getRecommendedAction()));
        }
        return dtoList;
    }

    // ---- Galaxy Work Orders Endpoints ----

    /**
     * Lists all work orders created by the Galaxy Overseer for a specific galaxy.
     * <p>
     * Work orders are created when the Galaxy Overseer assesses a galaxy and finds
     * issues that need attention (missing Earth hub, low sector count, understaffing, etc.).
     * </p>
     *
     * @param name the galaxy name
     * @return list of work order DTOs
     */
    @GetMapping("/{name}/work-orders")
    public ResponseEntity<?> getWorkOrders(@PathVariable String name) {
        log.info("Admin requested work orders for galaxy: {}", name);

        if (!galaxyService.hasGalaxy(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        String prefix = "GalaxyOverseer:" + name;
        List<AiAdminOrder> orders = aiAdminService.getOrdersByPrefix(prefix);
        List<WorkOrderDto> dtos = new ArrayList<>();
        for (AiAdminOrder order : orders) {
            dtos.add(toWorkOrderDto(order));
        }
        return ResponseEntity.ok(dtos);
    }

    /**
     * Lists pending work orders for a specific galaxy.
     * <p>
     * Pending orders are those that have not yet been processed by the AI Admin system.
     * </p>
     *
     * @param name the galaxy name
     * @return list of pending work order DTOs
     */
    @GetMapping("/{name}/work-orders/pending")
    public ResponseEntity<?> getPendingWorkOrders(@PathVariable String name) {
        log.info("Admin requested pending work orders for galaxy: {}", name);

        if (!galaxyService.hasGalaxy(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        String prefix = "GalaxyOverseer:" + name;
        List<AiAdminOrder> orders = aiAdminService.getOrdersByPrefixAndStatus(prefix, AiAdminOrder.OrderStatus.PENDING);
        List<WorkOrderDto> dtos = new ArrayList<>();
        for (AiAdminOrder order : orders) {
            dtos.add(toWorkOrderDto(order));
        }
        return ResponseEntity.ok(dtos);
    }

    /**
     * Gets a specific work order by ID with its associated test report.
     * <p>
     * Returns the order details along with the test report summary if the order
     * has been completed and a report was generated.
     * </p>
     *
     * @param name the galaxy name (for validation)
     * @param orderId the work order ID
     * @return work order DTO with report summary or 404
     */
    @GetMapping("/{name}/work-orders/{orderId}")
    public ResponseEntity<?> getWorkOrder(@PathVariable String name,
                                          @PathVariable Long orderId) {
        log.info("Admin requested work order {} for galaxy: {}", orderId, name);

        if (!galaxyService.hasGalaxy(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Galaxy '" + name + "' not found"));
        }

        Optional<AiAdminOrder> orderOpt = aiAdminService.getOrder(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Work order " + orderId + " not found"));
        }

        AiAdminOrder order = orderOpt.get();
        // Verify this order belongs to the galaxy
        String prefix = "GalaxyOverseer:" + name;
        if (!order.getSubmittedBy().contains(prefix)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Work order " + orderId + " does not belong to galaxy '" + name + "'"));
        }

        WorkOrderDto dto = toWorkOrderDto(order);

        // Attach report summary if available
        Optional<AiAdminTestReport> reportOpt = aiAdminService.getReportForOrder(orderId);
        reportOpt.ifPresent(report -> {
            dto.setPassedTests(report.getPassed());
            dto.setFailedTests(report.getFailed());
            dto.setTotalTests(report.getTotalTests());
        });

        return ResponseEntity.ok(dto);
    }

    // ---- Galaxy Backup & Restore ----

    /**
     * Lists all available backups for a galaxy.
     *
     * @param name the galaxy name
     * @return list of backup info maps sorted by backup time (newest first)
     */
    @GetMapping("/{name}/backups")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> listBackups(@PathVariable String name) {
        log.info("Admin requested backup list for galaxy: {}", name);
        List<Map<String, Object>> backups = galaxyBackupService.listBackups(name);
        return ResponseEntity.ok(backups);
    }

    /**
     * Creates a backup of the specified galaxy.
     *
     * @param name the galaxy name
     * @return backup result with file path, size, and statistics
     */
    @PostMapping("/{name}/backup")
    public ResponseEntity<Map<String, Object>> createBackup(@PathVariable String name) {
        log.info("Admin requested backup for galaxy: {}", name);
        try {
            Map<String, Object> result = galaxyBackupService.createBackup(name);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Backup failed: " + e.getMessage()));
        }
    }

    /**
     * Restores a galaxy from a backup file.
     * <p>
     * Request body: { "backupFile": "Milky_Way-20261110-180000.json" }
     * </p>
     *
     * @param name       the galaxy name
     * @param request    the restore request containing the backup file name
     * @return restore result with statistics
     */
    @PostMapping("/{name}/restore")
    public ResponseEntity<Map<String, Object>> restoreBackup(@PathVariable String name,
                                                              @RequestBody Map<String, String> request) {
        String backupFile = request.get("backupFile");
        if (backupFile == null || backupFile.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "backupFile is required in request body"));
        }

        log.info("Admin requested restore for galaxy '{}' from backup: {}", name, backupFile);
        try {
            Map<String, Object> result = galaxyBackupService.restoreBackup(name, backupFile);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Restore failed: " + e.getMessage()));
        }
    }

    /**
     * Deletes a specific backup file.
     *
     * @param name       the galaxy name
     * @param backupFile the backup file name to delete
     * @return 200 OK if deleted, 404 if not found
     */
    @DeleteMapping("/{name}/backups/{backupFile}")
    public ResponseEntity<Map<String, Object>> deleteBackup(@PathVariable String name,
                                                             @PathVariable String backupFile) {
        log.info("Admin requested delete backup '{}' for galaxy: {}", backupFile, name);
        boolean deleted = galaxyBackupService.deleteBackup(name, backupFile);
        if (deleted) {
            return ResponseEntity.ok(Map.of("deleted", true, "backupFile", backupFile));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Backup file not found: " + backupFile));
        }
    }

    // ---- Galaxy Comparison Endpoints ----

    /**
     * Compares two galaxies side by side.
     * <p>
     * Returns detailed comparison with health scores, build progress, workforce,
     * work orders, player population, deltas, and human-readable insights.
     * </p>
     *
     * @param nameA first galaxy name
     * @param nameB second galaxy name
     * @return detailed comparison DTO
     */
    @GetMapping("/compare/{nameA}/{nameB}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<GalaxyComparisonDto> compareGalaxies(@PathVariable String nameA,
                                                                @PathVariable String nameB) {
        log.info("Admin requested galaxy comparison: {} vs {}", nameA, nameB);
        try {
            GalaxyComparisonDto comparison = galaxyComparisonService.compareGalaxies(nameA, nameB);
            return ResponseEntity.ok(comparison);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    /**
     * Returns a comparison grid of all galaxies ranked by combined score.
     * <p>
     * Provides a compact overview of all galaxies sorted by a weighted score
     * combining health, completion, staffing, and work orders.
     * </p>
     *
     * @return list of galaxy comparison summaries ranked by combined score
     */
    @GetMapping("/compare-all")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<GalaxyComparisonSummaryDto>> compareAllGalaxies() {
        log.info("Admin requested comparison of all galaxies");
        List<GalaxyComparisonSummaryDto> summaries = galaxyComparisonService.compareAllGalaxies();
        return ResponseEntity.ok(summaries);
    }

    /**
     * Returns a dashboard summary of work orders across all galaxies.
     * <p>
     * Aggregates work order counts by status for all galaxies managed by the Galaxy Overseer.
     * </p>
     *
     * @return summary map with counts by status and per-galaxy breakdown
     */
    @GetMapping("/work-orders/summary")
    public ResponseEntity<Map<String, Object>> getWorkOrdersSummary() {
        log.info("Admin requested work orders summary");

        List<AiAdminOrder> allOrders = aiAdminService.getAllOrders();
        long total = allOrders.size();
        long pending = allOrders.stream().filter(o -> o.getStatus() == AiAdminOrder.OrderStatus.PENDING).count();
        long running = allOrders.stream().filter(o -> o.getStatus() == AiAdminOrder.OrderStatus.RUNNING).count();
        long completed = allOrders.stream().filter(o -> o.getStatus() == AiAdminOrder.OrderStatus.COMPLETED).count();
        long failed = allOrders.stream().filter(o -> o.getStatus() == AiAdminOrder.OrderStatus.FAILED).count();

        // Per-galaxy breakdown — extract galaxy name from "GalaxyOverseer:galaxyName" prefix
        Map<String, Map<String, Long>> byGalaxy = new HashMap<>();
        for (AiAdminOrder order : allOrders) {
            String submittedBy = order.getSubmittedBy();
            if (submittedBy.startsWith("GalaxyOverseer:")) {
                String galaxyName = submittedBy.substring("GalaxyOverseer:".length());
                byGalaxy.computeIfAbsent(galaxyName, k -> new HashMap<>())
                        .merge(order.getStatus().name(), 1L, Long::sum);
            }
        }

        return ResponseEntity.ok(Map.of(
                "total", total,
                "pending", pending,
                "running", running,
                "completed", completed,
                "failed", failed,
                "byGalaxy", byGalaxy
        ));
    }

    // ---- DTO Converters ----

    private GalaxyAssessmentDto toAssessmentDto(GalaxyAssessment assessment) {
        GalaxyAssessmentDto dto = new GalaxyAssessmentDto();
        dto.setGalaxyId(assessment.getGalaxyId());
        dto.setGalaxyName(assessment.getGalaxyName());
        dto.setHealthScore(assessment.getHealthScore());
        dto.setSolarSystemCount(assessment.getSolarSystemCount());
        dto.setExpectedMaxSystems(assessment.getExpectedMaxSystems());
        dto.setAiAgentCount(assessment.getAiAgentCount());
        dto.setRecommendedAiAgents(assessment.getRecommendedAiAgents());
        dto.setMetadataComplete(assessment.isMetadataComplete());
        dto.setHyperlaneDensity(assessment.getHyperlaneDensity());
        dto.setRecommendations(assessment.getRecommendations());

        List<FindingDto> findingDtos = new ArrayList<>();
        for (GalaxyAssessment.Finding f : assessment.getFindings()) {
            findingDtos.add(new FindingDto(f.getSeverity().name(), f.getDescription(), f.getRecommendedAction()));
        }
        dto.setFindings(findingDtos);
        return dto;
    }

    private WorkforceReportDto toWorkforceDto(WorkforceReport report) {
        WorkforceReportDto dto = new WorkforceReportDto();
        dto.setGalaxyId(report.getGalaxyId());
        dto.setGalaxyName(report.getGalaxyName());
        dto.setTotalAiAgents(report.getTotalAiAgents());
        dto.setGalaxyOverseerCount(report.getGalaxyOverseerCount());
        dto.setPlanetOverseerCount(report.getPlanetOverseerCount());
        dto.setLeaderCount(report.getLeaderCount());
        dto.setGovernorCount(report.getGovernorCount());
        dto.setOtherCount(report.getOtherCount());
        dto.setEstimatedPlanets(report.getEstimatedPlanets());
        dto.setRecommendedMaxAgents(report.getRecommendedMaxAgents());
        dto.setIsOverstaffed(report.isOverstaffed());
        dto.setTotalWorkforceExperience(report.getTotalWorkforceExperience());
        dto.setAverageExperience(report.getAverageExperience());
        dto.setRecommendation(report.getRecommendation());
        return dto;
    }

    private WorkOrderDto toWorkOrderDto(AiAdminOrder order) {
        WorkOrderDto dto = new WorkOrderDto();
        dto.setId(order.getId());
        dto.setSubmittedBy(order.getSubmittedBy());
        dto.setOrderText(order.getOrderText());
        dto.setStatus(order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setCompletedAt(order.getCompletedAt());
        return dto;
    }
}
