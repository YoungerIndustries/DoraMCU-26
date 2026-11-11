package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.PlayerCharacterEntryDto;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.service.PlayerCharacterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Player-facing REST API for managing the player character registry.
 */
@RestController
@RequestMapping("/api/v1/players/{playerId}/characters")
public class PlayerCharacterController {

    private final PlayerCharacterService playerCharacterService;
    private final UserRepository userRepository;

    public PlayerCharacterController(PlayerCharacterService playerCharacterService,
                                     UserRepository userRepository) {
        this.playerCharacterService = playerCharacterService;
        this.userRepository = userRepository;
    }

    /**
     * List all characters in the player's registry.
     */
    @GetMapping
    public ResponseEntity<List<PlayerCharacterEntryDto>> listAll(@PathVariable Long playerId) {
        User player = userRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
        List<PlayerCharacterEntryDto> characters = playerCharacterService.getAllCharacters(player);
        return ResponseEntity.ok(characters);
    }

    /**
     * List crew members only.
     */
    @GetMapping("/crew")
    public ResponseEntity<List<PlayerCharacterEntryDto>> listCrew(@PathVariable Long playerId,
                                                                   @PathVariable(required = false) String galaxyName) {
        User player = userRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        List<PlayerCharacterEntryDto> crew;
        if (galaxyName != null && !galaxyName.isBlank()) {
            crew = playerCharacterService.getCrewInGalaxy(player, galaxyName);
        } else {
            crew = playerCharacterService.getAllCharacters(player)
                    .stream()
                    .filter(c -> c.getCharacterRole().name().equals("CREW"))
                    .toList();
        }
        return ResponseEntity.ok(crew);
    }

    /**
     * List encountered AI characters.
     */
    @GetMapping("/encountered")
    public ResponseEntity<List<PlayerCharacterEntryDto>> listEncountered(@PathVariable Long playerId) {
        User player = userRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
        List<PlayerCharacterEntryDto> encountered = playerCharacterService.getAllCharacters(player)
                .stream()
                .filter(c -> c.getCharacterRole().name().equals("ENCOUNTERED"))
                .toList();
        return ResponseEntity.ok(encountered);
    }

    /**
     * Promote a character to crew role.
     */
    @PutMapping("/{registryId}/promote")
    public ResponseEntity<PlayerCharacterEntryDto> promoteToCrew(@PathVariable Long registryId) {
        PlayerCharacterEntryDto result = playerCharacterService.promoteToCrew(registryId);
        return ResponseEntity.ok(result);
    }

    /**
     * Demote a crew member to owned role.
     */
    @PutMapping("/{registryId}/demote")
    public ResponseEntity<PlayerCharacterEntryDto> demoteFromCrew(@PathVariable Long registryId) {
        PlayerCharacterEntryDto result = playerCharacterService.demoteFromCrew(registryId);
        return ResponseEntity.ok(result);
    }

    /**
     * Set a character as the player's commander.
     */
    @PutMapping("/{registryId}/commander")
    public ResponseEntity<PlayerCharacterEntryDto> setCommander(@PathVariable Long registryId) {
        PlayerCharacterEntryDto result = playerCharacterService.setCommander(registryId);
        return ResponseEntity.ok(result);
    }
}
