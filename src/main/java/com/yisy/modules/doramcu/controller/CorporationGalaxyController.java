package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.CreateGalaxyRequest;
import com.yisy.modules.doramcu.dto.GalaxyCreationCostDto;
import com.yisy.modules.doramcu.dto.GalaxyInfoDto;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.repository.EveCorporationRepository;
import com.yisy.modules.doramcu.service.GalaxyCreationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for galaxy creation from a player's corporation interface.
 * <p>
 * This controller provides the paid galaxy creation flow where a player's corporation
 * pays credits to create a new galaxy. The player becomes the sole Empire on the new
 * galaxy, similar to Earth2026 on the pregenerated Earth.
 * </p>
 * <p>
 * Endpoints:
 * </p>
 * <ul>
 *   <li><b>POST /api/v1/corporation/galaxies/estimate</b> — Get cost estimate for galaxy creation</li>
 *   <li><b>POST /api/v1/corporation/galaxies</b> — Create a new galaxy (paid)</li>
 *   <li><b>GET /api/v1/corporation/{corporationId}/balance</b> — Get corporation credit balance</li>
 * </ul>
 * <p>
 * The Galaxy Manager (corporation) decides what other AI Empires, Fleets, and Facilities
 * need to be created before players can start to use the galaxy for game play.
 * Unnecessary galaxy entities for game play can be created on the fly as necessary.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/corporation")
public class CorporationGalaxyController {

    private static final Logger log = LoggerFactory.getLogger(CorporationGalaxyController.class);

    private final GalaxyCreationService galaxyCreationService;
    private final EveCorporationRepository corporationRepository;

    public CorporationGalaxyController(GalaxyCreationService galaxyCreationService,
                                        EveCorporationRepository corporationRepository) {
        this.galaxyCreationService = galaxyCreationService;
        this.corporationRepository = corporationRepository;
    }

    /**
     * Gets a cost estimate for creating a new galaxy.
     * <p>
     * The cost is calculated based on the game module type, galaxy size,
     * celestial body probabilities, and difficulty level. The response includes
     * a detailed breakdown of all cost components.
     * </p>
     *
     * @param request the galaxy creation parameters
     * @return the cost estimate with total cost and breakdown
     */
    @PostMapping("/galaxies/estimate")
    public ResponseEntity<GalaxyCreationCostDto> estimateGalaxyCost(@RequestBody CreateGalaxyRequest request) {
        log.info("Cost estimate requested for galaxy: {}", request.getName());

        try {
            GalaxyCreationCostDto costDto = galaxyCreationService.estimateCost(request);
            return ResponseEntity.ok(costDto);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid galaxy creation parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new GalaxyCreationCostDto());
        }
    }

    /**
     * Gets a cost estimate for creating a new galaxy, including the corporation's balance.
     * <p>
     * This endpoint checks whether the corporation has sufficient credits to create
     * the galaxy and returns the balance information along with the cost estimate.
     * </p>
     *
     * @param request the galaxy creation parameters
     * @param corporationId the corporation ID to check balance for
     * @return the cost estimate with balance information
     */
    @PostMapping("/galaxies/estimate-with-balance")
    public ResponseEntity<GalaxyCreationCostDto> estimateGalaxyCostWithBalance(
            @RequestBody CreateGalaxyRequest request,
            @RequestParam Long corporationId) {
        log.info("Cost estimate with balance requested for galaxy: {} by corporation: {}",
                request.getName(), corporationId);

        try {
            // Validate corporation exists
            corporationRepository.findById(corporationId)
                    .orElseThrow(() -> new IllegalArgumentException("Corporation not found: " + corporationId));

            GalaxyCreationCostDto costDto = galaxyCreationService.estimateCostWithBalance(request, corporationId);
            return ResponseEntity.ok(costDto);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid galaxy creation parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new GalaxyCreationCostDto());
        }
    }

    /**
     * Creates a new galaxy as a paid function from the player's corporation.
     * <p>
     * This endpoint:
     * </p>
     * <ol>
     *   <li>Validates the player and corporation</li>
     *   <li>Calculates the cost based on the game module type and parameters</li>
     *   <li>Deducts the cost from the corporation's treasury</li>
     *   <li>Generates the galaxy with the player as the sole Empire</li>
     *   <li>Persists the galaxy to the database</li>
     * </ol>
     * <p>
     * The player is created as the only Empire, just like Earth2026 on the new
     * galaxy's pregenerated Earth. The Galaxy Manager decides what other AI Empires,
     * Fleets, and Facilities need to be created before players can start to use
     * the galaxy for game play.
     * </p>
     *
     * @param request the galaxy creation parameters
     * @param playerId the ID of the player requesting galaxy creation
     * @param corporationId the corporation ID paying for the galaxy
     * @return the created galaxy info or error
     */
    @PostMapping("/galaxies")
    public ResponseEntity<?> createGalaxy(
            @RequestBody CreateGalaxyRequest request,
            @RequestParam Long playerId,
            @RequestParam Long corporationId) {
        String name = request.getName();
        log.info("Galaxy creation requested by player {} for corporation {}: {}",
                playerId, corporationId, name);

        try {
            // Validate corporation exists
            EveCorporation corporation = corporationRepository.findById(corporationId)
                    .orElseThrow(() -> new IllegalArgumentException("Corporation not found: " + corporationId));

            GalaxyCreationService.PaidGalaxyCreationResult result =
                    galaxyCreationService.createGalaxyFromCorporation(request, playerId, corporationId);

            Map<String, Object> response = new HashMap<>();
            response.put("galaxy", toDto(result.getGalaxy()));
            response.put("creditsSpent", result.getCreditsSpent());
            response.put("corporationName", result.getCorporationName());
            response.put("message", "Galaxy '" + name + "' created successfully. " +
                    result.getCreditsSpent() + " credits deducted from " + result.getCorporationName());

            log.info("Galaxy '{}' created successfully by corporation {} ({} credits)",
                    name, corporation.getName(), result.getCreditsSpent());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (GalaxyCreationService.InsufficientFundsException e) {
            log.warn("Insufficient funds for galaxy creation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid galaxy creation request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("Failed to create galaxy '{}'", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create galaxy: " + e.getMessage()));
        }
    }

    /**
     * Gets the current credit balance of a corporation.
     *
     * @param corporationId the corporation ID
     * @return the credit balance information
     */
    @GetMapping("/{corporationId}/balance")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getCorporationBalance(@PathVariable Long corporationId) {
        log.info("Balance check for corporation: {}", corporationId);

        EveCorporation corporation = corporationRepository.findById(corporationId)
                .orElseThrow(() -> new IllegalArgumentException("Corporation not found: " + corporationId));

        long balance = galaxyCreationService.getCorporationCreditBalance(corporationId);

        Map<String, Object> response = new HashMap<>();
        response.put("corporationId", corporationId);
        response.put("corporationName", corporation.getName());
        response.put("ticker", corporation.getTicker());
        response.put("creditBalance", balance);

        return ResponseEntity.ok(response);
    }

    /**
     * Converts a Galaxy entity to a GalaxyInfoDto.
     */
    private GalaxyInfoDto toDto(com.yisy.modules.doramcu.model.Galaxy galaxy) {
        return new GalaxyInfoDto(
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
    }
}
