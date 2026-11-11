package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.WormholeConnectionDto;
import com.yisy.modules.doramcu.dto.WormholeInfoDto;
import com.yisy.modules.doramcu.dto.WormholePairRequest;
import com.yisy.modules.doramcu.service.WormholeConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin API for managing wormhole connections between galaxies.
 * <p>
 * Provides endpoints to create wormhole pairs, list all connections,
 * get individual wormhole details, and toggle wormhole state.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin/wormholes")
public class WormholeController {

    private static final Logger log = LoggerFactory.getLogger(WormholeController.class);

    private final WormholeConnectionService wormholeConnectionService;

    public WormholeController(WormholeConnectionService wormholeConnectionService) {
        this.wormholeConnectionService = wormholeConnectionService;
    }

    /**
     * Creates a wormhole pair connecting two galaxies.
     *
     * @param request the pair request with galaxy names
     * @return the created connection DTO
     */
    @PostMapping("/pair")
    public ResponseEntity<?> createWormholePair(@RequestBody WormholePairRequest request) {
        try {
            WormholeConnectionDto connection = wormholeConnectionService.createWormholePair(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(connection);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lists all wormholes in the system.
     *
     * @return list of all wormhole info DTOs
     */
    @GetMapping
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<WormholeInfoDto>> listWormholes() {
        log.info("Admin requested wormhole list");
        List<WormholeInfoDto> wormholes = wormholeConnectionService.listAllWormholes();
        return ResponseEntity.ok(wormholes);
    }

    /**
     * Gets the wormhole for a specific galaxy.
     *
     * @param galaxyName the galaxy name
     * @return wormhole info DTO or 404
     */
    @GetMapping("/{galaxyName}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<?> getWormhole(@PathVariable String galaxyName) {
        log.info("Admin requested wormhole for galaxy: {}", galaxyName);
        var optional = wormholeConnectionService.getWormhole(galaxyName);
        if (optional.isPresent()) {
            return ResponseEntity.ok(optional.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "No wormhole found in galaxy '" + galaxyName + "'"));
    }

    /**
     * Toggles the active state of a wormhole.
     *
     * @param galaxyName the galaxy containing the wormhole
     * @return the updated wormhole info DTO
     */
    @PutMapping("/{galaxyName}/toggle")
    public ResponseEntity<?> toggleWormhole(@PathVariable String galaxyName) {
        try {
            log.info("Admin toggled wormhole for galaxy: {}", galaxyName);
            WormholeInfoDto dto = wormholeConnectionService.toggleWormhole(galaxyName);
            return ResponseEntity.ok(dto);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lists all wormhole connections (pairs).
     *
     * @return list of all wormhole connection DTOs
     */
    @GetMapping("/connections")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<WormholeConnectionDto>> listConnections() {
        log.info("Admin requested wormhole connections");
        List<WormholeConnectionDto> connections = wormholeConnectionService.listAllConnections();
        return ResponseEntity.ok(connections);
    }
}
