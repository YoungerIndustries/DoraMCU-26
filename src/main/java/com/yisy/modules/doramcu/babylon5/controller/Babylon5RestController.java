package com.yisy.modules.doramcu.babylon5.controller;

import com.yisy.modules.doramcu.babylon5.model.*;
import com.yisy.modules.doramcu.babylon5.service.Babylon5DatabaseService;
import com.yisy.modules.doramcu.babylon5.service.Babylon5EmpireBuilder;
import com.yisy.modules.doramcu.hex.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API for querying Babylon 5 universe data.
 * Endpoints at /api/v1/babylon5
 */
@RestController
@RequestMapping("/api/v1/babylon5")
public class Babylon5RestController {

    private final Babylon5DatabaseService service;
    private final Babylon5EmpireBuilder empireBuilder;

    public Babylon5RestController(Babylon5DatabaseService service, Babylon5EmpireBuilder empireBuilder) {
        this.service = service;
        this.empireBuilder = empireBuilder;
    }

    // ==========================================
    // Races
    // ==========================================

    @GetMapping("/races")
    public List<B5Race> getAllRaces() {
        return service.getAllRaces();
    }

    @GetMapping("/races/{id}")
    public ResponseEntity<B5Race> getRace(@PathVariable String id) {
        Optional<B5Race> race = service.getRace(id);
        return race.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // Characters
    // ==========================================

    @GetMapping("/characters")
    public List<B5Character> getAllCharacters(@RequestParam(required = false) String species) {
        if (species != null && !species.isEmpty()) {
            return service.getCharactersBySpecies(species);
        }
        return service.getAllCharacters();
    }

    @GetMapping("/characters/{id}")
    public ResponseEntity<B5Character> getCharacter(@PathVariable String id) {
        Optional<B5Character> character = service.getCharacter(id);
        return character.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // Storylines
    // ==========================================

    @GetMapping("/storylines")
    public List<B5Storyline> getAllStorylines() {
        return service.getAllStorylines();
    }

    @GetMapping("/storylines/{id}")
    public ResponseEntity<B5Storyline> getStoryline(@PathVariable String id) {
        Optional<B5Storyline> storyline = service.getStoryline(id);
        return storyline.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // Dialogues
    // ==========================================

    @GetMapping("/dialogues")
    public List<B5Dialogue> getAllDialogues() {
        return service.getAllDialogues();
    }

    // ==========================================
    // Locations
    // ==========================================

    @GetMapping("/locations")
    public List<B5Location> getAllLocations() {
        return service.getAllLocations();
    }

    @GetMapping("/locations/{id}")
    public ResponseEntity<B5Location> getLocation(@PathVariable String id) {
        Optional<B5Location> location = service.getLocation(id);
        return location.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // Galaxy Generation
    // ==========================================

    /**
     * Generates a Babylon 5-themed galaxy with empires based on B5 races.
     * <p>
     * Creates a galaxy using the BABYLON5 template and populates it with
     * one empire per B5 race, each with a capital planet and starting fleets.
     * </p>
     *
     * @return a summary of the generated galaxy and empires
     */
    @PostMapping("/galaxy/generate")
    public ResponseEntity<Map<String, Object>> generateGalaxy() {
        // Generate galaxy from BABYLON5 template
        Galaxy galaxy = GalaxyFactory.generateFromTemplate(
                "Babylon 5 Galaxy",
                GalaxyTemplate.BABYLON5,
                System.nanoTime()
        );

        // Create a game and register the galaxy
        Game game = new Game("Babylon 5 Campaign");
        game.addGalaxy(galaxy);

        // Build empires from B5 races
        List<Empire> empires = empireBuilder.buildEmpires(game, galaxy);

        // Build response summary
        Map<String, Object> response = new HashMap<>();
        response.put("galaxyName", galaxy.getName());
        response.put("galaxyId", galaxy.getId());
        response.put("empireCount", empires.size());
        response.put("empires", empires.stream().map(e -> {
            Map<String, Object> empireInfo = new HashMap<>();
            empireInfo.put("id", e.getId());
            empireInfo.put("name", e.getName());
            empireInfo.put("planetCount", e.getPlanetCount());
            empireInfo.put("capitalPlanetId", e.getCapitalPlanetId());
            return empireInfo;
        }).toList());

        return ResponseEntity.ok(response);
    }
}
