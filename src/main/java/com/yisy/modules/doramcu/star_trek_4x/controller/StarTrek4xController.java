package com.yisy.modules.doramcu.star_trek_4x.controller;

import com.yisy.modules.doramcu.star_trek_4x.dto.GameSessionDto;
import com.yisy.modules.doramcu.star_trek_4x.model.GameSession;
import com.yisy.modules.doramcu.star_trek_4x.repository.GameSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Main REST API entry point for the Star Trek 4x game.
 * <p>
 * Manages game sessions, mode switching, and turn advancement.
 * </p>
 */
@RestController
@RequestMapping("/api/star-trek-4x")
public class StarTrek4xController {

    private static final Logger log = LoggerFactory.getLogger(StarTrek4xController.class);

    private final GameSessionRepository gameSessionRepository;

    public StarTrek4xController(GameSessionRepository gameSessionRepository) {
        this.gameSessionRepository = gameSessionRepository;
    }

    /**
     * Create a new 4x game session.
     */
    @PostMapping("/sessions")
    public ResponseEntity<GameSessionDto> createSession(@RequestParam(defaultValue = "New Game") String name,
                                                         @RequestParam(defaultValue = "0") long seed) {
        log.info("Creating new Star Trek 4x game session: {}", name);
        GameSession session = new GameSession();
        session.setName(name);
        session.setGalaxyId(1);
        session.setGalaxySeed(seed > 0 ? seed : System.currentTimeMillis());
        session = gameSessionRepository.save(session);
        return ResponseEntity.ok(new GameSessionDto(session));
    }

    /**
     * Get session state.
     */
    @GetMapping("/sessions/{id}")
    public ResponseEntity<GameSessionDto> getSession(@PathVariable Long id) {
        Optional<GameSession> session = gameSessionRepository.findById(id);
        return session.map(s -> ResponseEntity.ok(new GameSessionDto(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List all game sessions.
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<GameSessionDto>> listSessions() {
        List<GameSessionDto> sessions = gameSessionRepository.findAll().stream()
                .map(GameSessionDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(sessions);
    }

    /**
     * Advance to the next turn.
     */
    @PostMapping("/sessions/{id}/advance-turn")
    public ResponseEntity<GameSessionDto> advanceTurn(@PathVariable Long id) {
        GameSession session = gameSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + id));
        session.advanceTurn();
        GameSession saved = gameSessionRepository.save(session);
        return ResponseEntity.ok(new GameSessionDto(saved));
    }

    /**
     * Switch gameplay mode (Captain's Log or 4x).
     */
    @PostMapping("/sessions/{id}/switch-mode")
    public ResponseEntity<GameSessionDto> switchMode(@PathVariable Long id,
                                                      @RequestParam GameSession.GameMode mode) {
        GameSession session = gameSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + id));
        session.switchMode(mode);
        GameSession saved = gameSessionRepository.save(session);
        return ResponseEntity.ok(new GameSessionDto(saved));
    }
}
