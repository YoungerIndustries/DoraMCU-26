package com.yisy.modules.doramcu.zero_zero_seven.controller;

import com.yisy.modules.doramcu.zero_zero_seven.dto.BondGameSessionDto;
import com.yisy.modules.doramcu.zero_zero_seven.generation.BondEmpireGenerator;
import com.yisy.modules.doramcu.zero_zero_seven.generation.BondMapGenerator;
import com.yisy.modules.doramcu.zero_zero_seven.model.BondEmpire;
import com.yisy.modules.doramcu.zero_zero_seven.model.BondGameSession;
import com.yisy.modules.doramcu.zero_zero_seven.model.BondTerritory;
import com.yisy.modules.doramcu.zero_zero_seven.operations.CovertOperationService;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondEmpireRepository;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondGameSessionRepository;
import com.yisy.modules.doramcu.zero_zero_seven.territory.TerritoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Main REST controller for 007 Cold War 4X RPG session management.
 * <p>
 * Handles game session creation, listing, retrieval, and turn advancement.
 * </p>
 */
@RestController
@RequestMapping("/api/zero-zero-seven")
public class BondController {

    private final BondGameSessionRepository gameSessionRepository;
    private final BondEmpireRepository empireRepository;
    private final BondEmpireGenerator empireGenerator;
    private final BondMapGenerator mapGenerator;
    private final CovertOperationService operationService;
    private final TerritoryService territoryService;

    public BondController(BondGameSessionRepository gameSessionRepository,
                          BondEmpireRepository empireRepository,
                          BondEmpireGenerator empireGenerator,
                          BondMapGenerator mapGenerator,
                          CovertOperationService operationService,
                          TerritoryService territoryService) {
        this.gameSessionRepository = gameSessionRepository;
        this.empireRepository = empireRepository;
        this.empireGenerator = empireGenerator;
        this.mapGenerator = mapGenerator;
        this.operationService = operationService;
        this.territoryService = territoryService;
    }

    /**
     * Create a new game session.
     */
    @PostMapping("/sessions")
    public ResponseEntity<BondGameSessionDto> createSession(@RequestParam(defaultValue = "Cold War Campaign") String name) {
        BondGameSession session = new BondGameSession();
        session.setName(name);
        session.setMapId(1);
        session.setMapSeed(System.currentTimeMillis());
        session = gameSessionRepository.save(session);

        // Generate empires and map
        empireGenerator.generateEmpires(session.getId());
        mapGenerator.generateMap(session.getId());

        return ResponseEntity.ok(new BondGameSessionDto(session));
    }

    /**
     * List all game sessions.
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<BondGameSessionDto>> listSessions() {
        List<BondGameSessionDto> sessions = gameSessionRepository.findAll().stream()
                .map(BondGameSessionDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get a game session by ID.
     */
    @GetMapping("/sessions/{id}")
    public ResponseEntity<BondGameSessionDto> getSession(@PathVariable Long id) {
        BondGameSession session = gameSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + id));
        return ResponseEntity.ok(new BondGameSessionDto(session));
    }

    /**
     * Advance to the next turn.
     */
    @PostMapping("/sessions/{id}/advance-turn")
    public ResponseEntity<BondGameSessionDto> advanceTurn(@PathVariable Long id) {
        BondGameSession session = gameSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + id));

        session.advanceTurn();
        operationService.advanceAllOperations(id);
        gameSessionRepository.save(session);

        return ResponseEntity.ok(new BondGameSessionDto(session));
    }

    /**
     * Get all empires for a session.
     */
    @GetMapping("/sessions/{id}/empires")
    public ResponseEntity<List<BondEmpire>> getEmpires(@PathVariable Long id) {
        List<BondEmpire> empires = empireRepository.findByGameSessionId(id);
        return ResponseEntity.ok(empires);
    }

    /**
     * Get all territories for a session.
     */
    @GetMapping("/sessions/{id}/territories")
    public ResponseEntity<List<BondTerritory>> getTerritories(@PathVariable Long id) {
        List<BondTerritory> territories = territoryService.getAllTerritories(id);
        return ResponseEntity.ok(territories);
    }
}
