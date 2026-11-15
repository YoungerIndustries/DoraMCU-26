package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.PlanetStructure;
import com.yisy.modules.doramcu.earth2026.service.PlanetStructureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for planet-level structures (mines, stores, stations, spaceports).
 */
@RestController
@RequestMapping("/api/earth2026/planet-structures")
public class PlanetStructureController {

    private final PlanetStructureService planetStructureService;

    public PlanetStructureController(PlanetStructureService planetStructureService) {
        this.planetStructureService = planetStructureService;
    }

    /**
     * Get all planet structures.
     */
    @GetMapping
    public List<PlanetStructure> getAllStructures() {
        return planetStructureService.getAllStructures();
    }

    /**
     * Get a planet structure by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlanetStructure> getStructureById(@PathVariable Long id) {
        return planetStructureService.getStructureById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all structures on a planet.
     */
    @GetMapping("/planet/{planetId}")
    public List<PlanetStructure> getStructuresByPlanet(@PathVariable Long planetId) {
        return planetStructureService.getStructuresByPlanet(planetId);
    }

    /**
     * Get all active structures on a planet.
     */
    @GetMapping("/planet/{planetId}/active")
    public List<PlanetStructure> getActiveStructuresByPlanet(@PathVariable Long planetId) {
        return planetStructureService.getActiveStructuresByPlanet(planetId);
    }

    /**
     * Get structures by planet and type.
     */
    @GetMapping("/planet/{planetId}/type/{structureType}")
    public List<PlanetStructure> getStructuresByPlanetAndType(
            @PathVariable Long planetId,
            @PathVariable PlanetStructure.StructureType structureType) {
        return planetStructureService.getStructuresByPlanetAndType(planetId, structureType);
    }

    /**
     * Get mines on a planet.
     */
    @GetMapping("/planet/{planetId}/mines")
    public List<PlanetStructure> getMinesByPlanet(@PathVariable Long planetId) {
        return planetStructureService.getMinesByPlanet(planetId);
    }

    /**
     * Get equipment stores on a planet.
     */
    @GetMapping("/planet/{planetId}/stores")
    public List<PlanetStructure> getEquipmentStoresByPlanet(@PathVariable Long planetId) {
        return planetStructureService.getEquipmentStoresByPlanet(planetId);
    }

    /**
     * Get transport stations on a planet.
     */
    @GetMapping("/planet/{planetId}/stations")
    public List<PlanetStructure> getTransportStationsByPlanet(@PathVariable Long planetId) {
        return planetStructureService.getTransportStationsByPlanet(planetId);
    }

    /**
     * Get spaceports on a planet.
     */
    @GetMapping("/planet/{planetId}/spaceports")
    public List<PlanetStructure> getSpaceportsByPlanet(@PathVariable Long planetId) {
        return planetStructureService.getSpaceportsByPlanet(planetId);
    }

    /**
     * Get factories on a planet.
     */
    @GetMapping("/planet/{planetId}/factories")
    public List<PlanetStructure> getFactoriesByPlanet(@PathVariable Long planetId) {
        return planetStructureService.getFactoriesByPlanet(planetId);
    }

    /**
     * Get structures within geographic bounds.
     */
    @GetMapping("/bounds")
    public List<PlanetStructure> getStructuresInBounds(
            @RequestParam Long planetId,
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon) {
        return planetStructureService.getStructuresInBounds(planetId, minLat, maxLat, minLon, maxLon);
    }

    /**
     * Create a new planet structure.
     */
    @PostMapping
    public PlanetStructure createStructure(@RequestBody PlanetStructure structure) {
        return planetStructureService.createStructure(structure);
    }

    /**
     * Update an existing planet structure.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlanetStructure> updateStructure(@PathVariable Long id, @RequestBody PlanetStructure updated) {
        return planetStructureService.updateStructure(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a planet structure.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStructure(@PathVariable Long id) {
        if (planetStructureService.deleteStructure(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Toggle the active state of a structure.
     */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<PlanetStructure> toggleActive(@PathVariable Long id) {
        return planetStructureService.toggleActive(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
