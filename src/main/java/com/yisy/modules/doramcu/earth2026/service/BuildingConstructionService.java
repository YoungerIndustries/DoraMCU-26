package com.yisy.modules.doramcu.earth2026.service;
import com.yisy.modules.doramcu.model.misc.Planet;


import com.yisy.modules.doramcu.corporation.service.CorporationEconomyService;
import com.yisy.modules.doramcu.earth2026.model.PlanetStructure;
import com.yisy.modules.doramcu.earth2026.repository.PlanetStructureRepository;
import com.yisy.modules.doramcu.earth2026.tycoon.CorporationTycoonService;
import com.yisy.modules.doramcu.earth2026.tycoon.TycoonBuilding;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.repository.misc.EveCorporationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified service for building construction, upgrades, and production chain management.
 * <p>
 * Integrates with CorporationTycoonService for game state and PlanetStructure for world placement.
 * Supports all building types: mines, factories, depots, hotels, warehouses, etc.
 * </p>
 */
@Service
public class BuildingConstructionService {

    private static final Logger log = LoggerFactory.getLogger(BuildingConstructionService.class);

    private final PlanetStructureRepository planetStructureRepository;
    private final EveCorporationRepository corporationRepository;
    private final CorporationEconomyService economyService;
    private final CorporationTycoonService tycoonService;

    public BuildingConstructionService(
            PlanetStructureRepository planetStructureRepository,
            EveCorporationRepository corporationRepository,
            CorporationEconomyService economyService,
            CorporationTycoonService tycoonService) {
        this.planetStructureRepository = planetStructureRepository;
        this.corporationRepository = corporationRepository;
        this.economyService = economyService;
        this.tycoonService = tycoonService;
    }

    /**
     * Construct a new building for a corporation on a planet.
     *
     * @param corporationId the corporation ID
     * @param buildingType the building type
     * @param planetId the planet ID
     * @param name building name
     * @param latitude geographic latitude
     * @param longitude geographic longitude
     * @return the created PlanetStructure
     */
    @Transactional
    public PlanetStructure constructBuilding(Long corporationId, TycoonBuilding.BuildingType buildingType,
                                             Long planetId, String name, Double latitude, Double longitude) {
        // Check corporation exists and has funds
        EveCorporation corporation = corporationRepository.findById(corporationId)
                .orElseThrow(() -> new IllegalArgumentException("Corporation not found: " + corporationId));

        long cost = getConstructionCost(buildingType);
        long balance = corporation.getCreditBalance() != null ? corporation.getCreditBalance() : 0L;
        if (balance < cost) {
            throw new IllegalArgumentException(
                    "Insufficient funds. Required: " + cost + ", Available: " + balance);
        }

        // Deduct construction cost
        economyService.debitCorporation(corporationId, cost,
                com.yisy.modules.doramcu.corporation.model.CorporateFinanceTransaction.TransactionType.EXPENSE,
                "Construction: " + buildingType.getDisplayName(), null);

        // Create PlanetStructure
        PlanetStructure structure = new PlanetStructure();
        structure.setName(name);
        structure.setStructureType(toStructureType(buildingType));
        structure.setPlanetId(planetId);
        structure.setLatitude(latitude);
        structure.setLongitude(longitude);
        structure.setCorporationId(corporationId);
        structure.setBuildingLevel(1);
        structure.setActive(true);
        structure.setProductionOutput(generateProductionOutput(buildingType, 1));
        structure.setDescription(buildingType.getDescription());
        structure = planetStructureRepository.save(structure);

        // Also create in tycoon game state
        tycoonService.constructBuilding(corporationId, buildingType);

        log.info("Constructed {} for corporation {} on planet {}. Cost: {}",
                buildingType.getDisplayName(), corporationId, planetId, cost);

        return structure;
    }

    /**
     * Upgrade an existing building.
     *
     * @param structureId the structure ID
     * @param corporationId the corporation ID (for authorization)
     * @return the upgraded structure
     */
    @Transactional
    public PlanetStructure upgradeBuilding(Long structureId, Long corporationId) {
        PlanetStructure structure = planetStructureRepository.findById(structureId)
                .orElseThrow(() -> new IllegalArgumentException("Structure not found: " + structureId));

        if (!structure.getCorporationId().equals(corporationId)) {
            throw new IllegalArgumentException("Structure does not belong to this corporation");
        }

        if (!structure.isActive()) {
            throw new IllegalArgumentException("Structure is not active");
        }

        int currentLevel = structure.getBuildingLevel() != null ? structure.getBuildingLevel() : 1;
        if (currentLevel >= 10) {
            throw new IllegalArgumentException("Structure is already at maximum level");
        }

        long upgradeCost = getUpgradeCost(structure.getStructureType(), currentLevel);
        EveCorporation corporation = corporationRepository.findById(corporationId)
                .orElseThrow(() -> new IllegalArgumentException("Corporation not found: " + corporationId));

        long balance = corporation.getCreditBalance() != null ? corporation.getCreditBalance() : 0L;
        if (balance < upgradeCost) {
            throw new IllegalArgumentException(
                    "Insufficient funds for upgrade. Required: " + upgradeCost + ", Available: " + balance);
        }

        // Deduct upgrade cost
        economyService.debitCorporation(corporationId, upgradeCost,
                com.yisy.modules.doramcu.corporation.model.CorporateFinanceTransaction.TransactionType.EXPENSE,
                "Upgrade: " + structure.getName(), structureId);

        // Update structure
        int newLevel = currentLevel + 1;
        structure.setBuildingLevel(newLevel);
        structure.setProductionOutput(generateProductionOutput(fromStructureType(structure.getStructureType()), newLevel));
        structure = planetStructureRepository.save(structure);

        log.info("Upgraded {} to level {}. Cost: {}", structure.getName(), newLevel, upgradeCost);

        return structure;
    }

    /**
     * Get all buildings owned by a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of structures
     */
    @Transactional(readOnly = true)
    public List<PlanetStructure> getCorporationBuildings(Long corporationId) {
        return planetStructureRepository.findByCorporationId(corporationId);
    }

    /**
     * Get buildings on a specific planet.
     *
     * @param planetId the planet ID
     * @return list of structures
     */
    @Transactional(readOnly = true)
    public List<PlanetStructure> getPlanetBuildings(Long planetId) {
        return planetStructureRepository.findByPlanetId(planetId);
    }

    /**
     * Get construction cost for a building type.
     */
    public long getConstructionCost(TycoonBuilding.BuildingType type) {
        switch (type) {
            case HEADQUARTERS: return 2000;
            case RESEARCH_LAB: return 5000;
            case MANUFACTURING_PLANT: return 8000;
            case TRADING_POST: return 3000;
            case SECURITY_OFFICE: return 4000;
            case DATA_CENTER: return 6000;
            case RECRUITING_OFFICE: return 3500;
            case BANK_BRANCH: return 10000;
            case MEDIA_OFFICE: return 4500;
            case PORT_FACILITY: return 7000;
            case MINE: return 9000;
            case VEHICLE_DEPOT: return 6000;
            case HOTEL: return 8000;
            case WAREHOUSE: return 5000;
            case POWER_PLANT: return 12000;
            case COMMUNICATION_TOWER: return 7000;
            case AGRICULTURAL_COMPLEX: return 6500;
            default: return 5000;
        }
    }

    /**
     * Get upgrade cost for a structure at a given level.
     */
    public long getUpgradeCost(PlanetStructure.StructureType type, int currentLevel) {
        return (long) (getConstructionCost(fromStructureType(type)) * Math.pow(1.5, currentLevel));
    }

    /**
     * Generate JSON production output string for a building type and level.
     */
    public String generateProductionOutput(TycoonBuilding.BuildingType type, int level) {
        TycoonBuilding building = new TycoonBuilding(type);
        building.setLevel(level);

        Map<String, Object> output = new HashMap<>();
        output.put("creditsPerHour", building.getCreditsPerHour());
        output.put("influencePerHour", building.getInfluencePerHour());
        output.put("techPerHour", building.getTechPerHour());
        output.put("materialsPerHour", building.getMaterialsPerHour());
        output.put("employeeSlots", building.getEmployeeSlots());
        output.put("level", level);

        // Simple JSON serialization
        return "{" +
                "\"creditsPerHour\":" + building.getCreditsPerHour() + "," +
                "\"influencePerHour\":" + building.getInfluencePerHour() + "," +
                "\"techPerHour\":" + building.getTechPerHour() + "," +
                "\"materialsPerHour\":" + building.getMaterialsPerHour() + "," +
                "\"employeeSlots\":" + building.getEmployeeSlots() + "," +
                "\"level\":" + level +
                "}";
    }

    /**
     * Convert TycoonBuilding.BuildingType to PlanetStructure.StructureType.
     */
    private PlanetStructure.StructureType toStructureType(TycoonBuilding.BuildingType type) {
        switch (type) {
            case MINE: return PlanetStructure.StructureType.MINE;
            case MANUFACTURING_PLANT: return PlanetStructure.StructureType.FACTORY;
            case VEHICLE_DEPOT: return PlanetStructure.StructureType.TRANSPORT_STATION;
            case PORT_FACILITY: return PlanetStructure.StructureType.SPACEPORT;
            case HOTEL:
            case HEADQUARTERS: return PlanetStructure.StructureType.RESIDENCE;
            default: return PlanetStructure.StructureType.FACTORY;
        }
    }

    /**
     * Convert PlanetStructure.StructureType to TycoonBuilding.BuildingType.
     */
    private TycoonBuilding.BuildingType fromStructureType(PlanetStructure.StructureType type) {
        switch (type) {
            case MINE: return TycoonBuilding.BuildingType.MINE;
            case FACTORY: return TycoonBuilding.BuildingType.MANUFACTURING_PLANT;
            case TRANSPORT_STATION: return TycoonBuilding.BuildingType.VEHICLE_DEPOT;
            case SPACEPORT: return TycoonBuilding.BuildingType.PORT_FACILITY;
            case RESIDENCE: return TycoonBuilding.BuildingType.HOTEL;
            default: return TycoonBuilding.BuildingType.HEADQUARTERS;
        }
    }

    /**
     * Get building statistics for a corporation.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getBuildingStats(Long corporationId) {
        List<PlanetStructure> buildings = getCorporationBuildings(corporationId);

        long totalCreditsPerHour = 0;
        long totalMaterialsPerHour = 0;
        long totalTechPerHour = 0;
        long totalInfluencePerHour = 0;
        int totalEmployeeSlots = 0;

        for (PlanetStructure structure : buildings) {
            if (structure.isActive() && structure.getProductionOutput() != null) {
                // Parse production output JSON (simple parsing)
                String output = structure.getProductionOutput();
                totalCreditsPerHour += extractJsonLong(output, "creditsPerHour");
                totalMaterialsPerHour += extractJsonLong(output, "materialsPerHour");
                totalTechPerHour += extractJsonLong(output, "techPerHour");
                totalInfluencePerHour += extractJsonLong(output, "influencePerHour");
                totalEmployeeSlots += extractJsonInt(output, "employeeSlots");
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBuildings", buildings.size());
        stats.put("activeBuildings", buildings.stream().filter(PlanetStructure::isActive).count());
        stats.put("totalCreditsPerHour", totalCreditsPerHour);
        stats.put("totalMaterialsPerHour", totalMaterialsPerHour);
        stats.put("totalTechPerHour", totalTechPerHour);
        stats.put("totalInfluencePerHour", totalInfluencePerHour);
        stats.put("totalEmployeeSlots", totalEmployeeSlots);
        return stats;
    }

    /**
     * Simple JSON field extraction (no external library dependency).
     */
    private long extractJsonLong(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int idx = json.indexOf(pattern);
            if (idx >= 0) {
                int start = idx + pattern.length();
                int end = json.indexOf(",", start);
                if (end < 0) end = json.indexOf("}", start);
                if (end < 0) end = json.length();
                return Long.parseLong(json.substring(start, end).trim());
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return 0;
    }

    private int extractJsonInt(String json, String key) {
        return (int) extractJsonLong(json, key);
    }
}
