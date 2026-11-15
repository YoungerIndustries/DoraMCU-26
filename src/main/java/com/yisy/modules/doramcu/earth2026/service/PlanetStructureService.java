package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.PlanetStructure;
import com.yisy.modules.doramcu.earth2026.repository.PlanetStructureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing planet-level structures (mines, stores, stations, spaceports).
 */
@Service
public class PlanetStructureService {

    private static final Logger log = LoggerFactory.getLogger(PlanetStructureService.class);

    private final PlanetStructureRepository planetStructureRepository;

    public PlanetStructureService(PlanetStructureRepository planetStructureRepository) {
        this.planetStructureRepository = planetStructureRepository;
    }

    /**
     * Get all planet structures.
     */
    @Transactional(readOnly = true)
    public List<PlanetStructure> getAllStructures() {
        return planetStructureRepository.findAll();
    }

    /**
     * Get a planet structure by ID.
     */
    @Transactional(readOnly = true)
    public Optional<PlanetStructure> getStructureById(Long id) {
        return planetStructureRepository.findById(id);
    }

    /**
     * Get all structures on a planet.
     */
    @Transactional(readOnly = true)
    public List<PlanetStructure> getStructuresByPlanet(Long planetId) {
        return planetStructureRepository.findByPlanetId(planetId);
    }

    /**
     * Get all active structures on a planet.
     */
    @Transactional(readOnly = true)
    public List<PlanetStructure> getActiveStructuresByPlanet(Long planetId) {
        return planetStructureRepository.findByPlanetIdAndActive(planetId, true);
    }

    /**
     * Get structures by planet and type.
     */
    @Transactional(readOnly = true)
    public List<PlanetStructure> getStructuresByPlanetAndType(Long planetId, PlanetStructure.StructureType structureType) {
        return planetStructureRepository.findByPlanetIdAndStructureType(planetId, structureType);
    }

    /**
     * Get active structures by planet and type.
     */
    @Transactional(readOnly = true)
    public List<PlanetStructure> getActiveStructuresByPlanetAndType(Long planetId, PlanetStructure.StructureType structureType) {
        return planetStructureRepository.findByPlanetIdAndStructureTypeAndActive(planetId, structureType, true);
    }

    /**
     * Get structures within geographic bounds.
     */
    @Transactional(readOnly = true)
    public List<PlanetStructure> getStructuresInBounds(Long planetId, double minLat, double maxLat, double minLon, double maxLon) {
        return planetStructureRepository.findByGeographicBounds(planetId, minLat, maxLat, minLon, maxLon);
    }

    /**
     * Get all mines on a planet.
     */
    @Transactional(readOnly = true)
    public List<PlanetStructure> getMinesByPlanet(Long planetId) {
        return planetStructureRepository.findMinesByPlanet(planetId);
    }

    /**
     * Get all equipment stores on a planet.
     */
    @Transactional(readOnly = true)
    public List<PlanetStructure> getEquipmentStoresByPlanet(Long planetId) {
        return planetStructureRepository.findEquipmentStoresByPlanet(planetId);
    }

    /**
     * Get all transport stations on a planet.
     */
    @Transactional(readOnly = true)
    public List<PlanetStructure> getTransportStationsByPlanet(Long planetId) {
        return planetStructureRepository.findTransportStationsByPlanet(planetId);
    }

    /**
     * Get all spaceports on a planet.
     */
    @Transactional(readOnly = true)
    public List<PlanetStructure> getSpaceportsByPlanet(Long planetId) {
        return planetStructureRepository.findSpaceportsByPlanet(planetId);
    }

    /**
     * Get all factories on a planet.
     */
    @Transactional(readOnly = true)
    public List<PlanetStructure> getFactoriesByPlanet(Long planetId) {
        return planetStructureRepository.findFactoriesByPlanet(planetId);
    }

    /**
     * Create a new planet structure.
     */
    @Transactional
    public PlanetStructure createStructure(PlanetStructure structure) {
        log.info("Creating planet structure: {} at ({}, {})", structure.getName(),
                structure.getLatitude(), structure.getLongitude());
        return planetStructureRepository.save(structure);
    }

    /**
     * Update an existing planet structure.
     */
    @Transactional
    public Optional<PlanetStructure> updateStructure(Long id, PlanetStructure updated) {
        return planetStructureRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setStructureType(updated.getStructureType());
            existing.setElevation(updated.getElevation());
            existing.setResourcesJson(updated.getResourcesJson());
            existing.setCapacity(updated.getCapacity());
            existing.setActive(updated.isActive());
            existing.setDescription(updated.getDescription());
            log.info("Updated planet structure: {}", existing.getName());
            return planetStructureRepository.save(existing);
        });
    }

    /**
     * Delete a planet structure by ID.
     */
    @Transactional
    public boolean deleteStructure(Long id) {
        return planetStructureRepository.findById(id).map(structure -> {
            log.info("Deleting planet structure: {}", structure.getName());
            planetStructureRepository.delete(structure);
            return true;
        }).orElse(false);
    }

    /**
     * Toggle the active state of a structure.
     */
    @Transactional
    public Optional<PlanetStructure> toggleActive(Long id) {
        return planetStructureRepository.findById(id).map(structure -> {
            structure.setActive(!structure.isActive());
            log.info("Toggled planet structure {} active state to: {}", structure.getName(), structure.isActive());
            return planetStructureRepository.save(structure);
        });
    }
}
