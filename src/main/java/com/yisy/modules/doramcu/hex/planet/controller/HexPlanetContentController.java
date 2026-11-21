package com.yisy.modules.doramcu.hex.planet.controller;

import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.CargoType;
import com.yisy.modules.doramcu.hex.planet.CampaignTradeRoute;
import com.yisy.modules.doramcu.hex.planet.DefaultPlanetSurfaceBuilder;
import com.yisy.modules.doramcu.hex.planet.DeliveryContract;
import com.yisy.modules.doramcu.hex.planet.PlanetQuest;
import com.yisy.modules.doramcu.hex.planet.PlanetSurfacePersistenceService;
import com.yisy.modules.doramcu.hex.planet.dto.ContractDto;
import com.yisy.modules.doramcu.hex.planet.dto.QuestDto;
import com.yisy.modules.doramcu.hex.planet.dto.TradeRouteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for planet contracts, quests, and trade routes.
 * Extracted from HexPlanetSurfaceController to reduce god class size.
 */
@RestController
@RequestMapping("/api/v1/planets")
public class HexPlanetContentController {

    private static final Logger log = LoggerFactory.getLogger(HexPlanetContentController.class);

    private final DefaultPlanetSurfaceBuilder planetSurfaceBuilder;
    private final PlanetSurfacePersistenceService persistenceService;

    public HexPlanetContentController(
            DefaultPlanetSurfaceBuilder planetSurfaceBuilder,
            PlanetSurfacePersistenceService persistenceService) {
        this.planetSurfaceBuilder = planetSurfaceBuilder;
        this.persistenceService = persistenceService;
    }

    /**
     * Get all contracts on a planet.
     */
    @GetMapping("/{planetId}/contracts")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<ContractDto>> getContracts(@PathVariable int planetId) {
        log.info("GET /api/v1/planets/{}/contracts", planetId);

        Planet planet = new Planet(planetId);
        List<DeliveryContract> contracts = planetSurfaceBuilder.getAllContracts(planet);

        if (contracts.isEmpty()) {
            if (persistenceService.hasPlanetData(planetId)) {
                persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
                contracts = planetSurfaceBuilder.getAllContracts(planet);
            }
        }

        List<ContractDto> dtos = contracts.stream()
                .map(this::toContractDto)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get all quests on a planet.
     */
    @GetMapping("/{planetId}/quests")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<QuestDto>> getQuests(@PathVariable int planetId) {
        log.info("GET /api/v1/planets/{}/quests", planetId);

        Planet planet = new Planet(planetId);
        List<PlanetQuest> quests = planetSurfaceBuilder.getAllQuests(planet);

        if (quests.isEmpty()) {
            if (persistenceService.hasPlanetData(planetId)) {
                persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
                quests = planetSurfaceBuilder.getAllQuests(planet);
            }
        }

        List<QuestDto> dtos = quests.stream()
                .map(this::toQuestDto)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get all trade routes on a planet.
     */
    @GetMapping("/{planetId}/routes")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<TradeRouteDto>> getTradeRoutes(@PathVariable int planetId) {
        log.info("GET /api/v1/planets/{}/routes", planetId);

        Planet planet = new Planet(planetId);
        List<CampaignTradeRoute> routes = planetSurfaceBuilder.getAllTradeRoutes(planet);

        if (routes.isEmpty()) {
            if (persistenceService.hasPlanetData(planetId)) {
                persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
                routes = planetSurfaceBuilder.getAllTradeRoutes(planet);
            }
        }

        List<TradeRouteDto> dtos = routes.stream()
                .map(this::toTradeRouteDto)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private ContractDto toContractDto(DeliveryContract c) {
        ContractDto dto = new ContractDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setType(c.getType().name());
        dto.setCargoType(c.getCargoType().name());
        dto.setCargoAmount(c.getCargoAmount());
        dto.setFromStationId(c.getFromStationId());
        dto.setToStationId(c.getToStationId());
        dto.setPayCredits(c.getPayCredits());
        dto.setXpReward(c.getXpReward());
        dto.setReputationReward(c.getReputationReward());
        dto.setStatus(c.getStatus().name());
        dto.setIssuerEmpireId(c.getIssuerEmpireId());
        dto.setAssignedCorporationId(c.getAssignedCorporationId());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setExpiresAt(c.getExpiresAt());
        dto.setCompletedAt(c.getCompletedAt());
        dto.setTotalValue(c.getTotalValue());
        return dto;
    }

    private QuestDto toQuestDto(PlanetQuest q) {
        QuestDto dto = new QuestDto();
        dto.setId(q.getId());
        dto.setTitle(q.getTitle());
        dto.setDescription(q.getDescription());
        dto.setType(q.getType().name());
        dto.setDifficulty(q.getDifficulty());
        dto.setCreditsReward(q.getCreditsReward());
        dto.setXpReward(q.getXpReward());
        dto.setReputationReward(q.getReputationReward());
        dto.setStatus(q.getStatus().name());
        dto.setIssuerName(q.getIssuerName());
        dto.setIssuerEmpireId(q.getIssuerEmpireId());
        dto.setObjectives(q.getObjectives());
        dto.setRequiredStationIds(q.getRequiredStationIds());
        dto.setAssignedCorporationId(q.getAssignedCorporationId());
        dto.setCreatedAt(q.getCreatedAt());
        dto.setExpiresAt(q.getExpiresAt());
        dto.setCompletedAt(q.getCompletedAt());
        return dto;
    }

    private TradeRouteDto toTradeRouteDto(CampaignTradeRoute r) {
        TradeRouteDto dto = new TradeRouteDto();
        dto.setId(r.getId());
        dto.setName(r.getName());
        dto.setOwnerCorporationId(r.getOwnerCorporationId());
        dto.setStationIds(r.getStationIds());
        dto.setVehicleIds(r.getVehicleIds());
        List<String> cargoNames = new ArrayList<>();
        for (CargoType ct : r.getCargoTypes()) {
            cargoNames.add(ct.name());
        }
        dto.setCargoTypes(cargoNames);
        dto.setStatus(r.getStatus().name());
        dto.setTotalDistance(r.getTotalDistance());
        dto.setDailyIncome(r.getDailyIncome());
        dto.setTotalIncome(r.getTotalIncome());
        dto.setCargoDeliveredToday(r.getCargoDeliveredToday());
        dto.setStationCount(r.getStationCount());
        dto.setVehicleCount(r.getVehicleCount());
        dto.setOriginStationId(r.getOriginStationId());
        dto.setDestinationStationId(r.getDestinationStationId());
        return dto;
    }
}
