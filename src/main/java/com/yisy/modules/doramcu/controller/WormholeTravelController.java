package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.WormholeTravelRequest;
import com.yisy.modules.doramcu.dto.WormholeTravelResponse;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.service.WormholeConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Player-facing API for traveling through wormholes.
 * <p>
 * A player initiates travel by providing their username. The system looks up
 * their current galaxy binding, finds the wormhole in that galaxy, and travels
 * to the connected galaxy.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/wormholes")
public class WormholeTravelController {

    private static final Logger log = LoggerFactory.getLogger(WormholeTravelController.class);

    private final WormholeConnectionService wormholeConnectionService;
    private final UserRepository userRepository;

    public WormholeTravelController(WormholeConnectionService wormholeConnectionService,
                                    UserRepository userRepository) {
        this.wormholeConnectionService = wormholeConnectionService;
        this.userRepository = userRepository;
    }

    /**
     * Initiates travel through a wormhole.
     * <p>
     * The player's current galaxy is looked up from their galaxy binding.
     * The wormhole in that galaxy is used to travel to the connected galaxy.
     * </p>
     *
     * @param request the travel request containing the player's username
     * @return the travel response with destination galaxy
     */
    @PostMapping("/travel")
    public ResponseEntity<?> travel(@RequestBody WormholeTravelRequest request) {
        String playerName = request.getPlayerName();
        if (playerName == null || playerName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Player name is required"));
        }

        // Look up player's current galaxy
        User user = userRepository.findByUsername(playerName)
                .orElseThrow(() -> new IllegalArgumentException("Player '" + playerName + "' not found"));

        String fromGalaxy = user.getGalaxyName();

        try {
            String toGalaxy = wormholeConnectionService.travelThroughWormhole(playerName, fromGalaxy);

            WormholeTravelResponse response = new WormholeTravelResponse();
            response.setPlayerName(playerName);
            response.setFromGalaxy(fromGalaxy);
            response.setToGalaxy(toGalaxy);
            response.setConverted(false);
            response.setConversionDetails(null);

            log.info("Player '{}' traveled from '{}' to '{}'", playerName, fromGalaxy, toGalaxy);
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets the connected galaxies for a player's current galaxy.
     *
     * @param playerName the player's username
     * @return list of connected galaxy names
     */
    @GetMapping("/connected")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<?> getConnectedGalaxies(@RequestParam String playerName) {
        User user = userRepository.findByUsername(playerName)
                .orElseThrow(() -> new IllegalArgumentException("Player '" + playerName + "' not found"));

        String galaxyName = user.getGalaxyName();
        var connected = wormholeConnectionService.getConnectedGalaxies(galaxyName);

        return ResponseEntity.ok(Map.of(
                "playerName", playerName,
                "currentGalaxy", galaxyName,
                "connectedGalaxies", connected
        ));
    }
}
