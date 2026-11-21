package com.yisy.modules.doramcu.hex.planet.controller;

import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.DefaultPlanetSurfaceBuilder;
import com.yisy.modules.doramcu.hex.planet.PlanetSurfacePersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for planet turn processing.
 * Extracted from HexPlanetSurfaceController to reduce god class size.
 */
@RestController
@RequestMapping("/api/v1/planets")
public class HexPlanetTurnController {

    private static final Logger log = LoggerFactory.getLogger(HexPlanetTurnController.class);

    private final DefaultPlanetSurfaceBuilder planetSurfaceBuilder;
    private final PlanetSurfacePersistenceService persistenceService;

    public HexPlanetTurnController(
            DefaultPlanetSurfaceBuilder planetSurfaceBuilder,
            PlanetSurfacePersistenceService persistenceService) {
        this.planetSurfaceBuilder = planetSurfaceBuilder;
        this.persistenceService = persistenceService;
    }

    /**
     * Process one economy turn for a planet.
     * <p>
     * Advances the simulation by one turn: settlements grow, industries produce,
     * vehicles move, contracts expire, quests progress, trade routes generate
     * revenue, and corporations accumulate credits and XP.
     * </p>
     *
     * @param planetId the planet ID
     * @return summary of what happened during the turn
     */
    @PostMapping("/{planetId}/turn")
    public ResponseEntity<TurnSummaryDto> processTurn(@PathVariable int planetId) {
        log.info("POST /api/v1/planets/{}/turn", planetId);

        Planet planet = new Planet(planetId);

        // Check if planet exists
        if (planetSurfaceBuilder.getAllTerrainData().isEmpty()) {
            if (!persistenceService.hasPlanetData(planetId)) {
                return ResponseEntity.notFound().build();
            }
            persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
        }

        // Process the turn
        Map<String, Long> turnSummary = planetSurfaceBuilder.processTurn(planet);

        // Save state after turn processing
        persistenceService.savePlanetSurface(planet, planetSurfaceBuilder);

        // Build response DTO
        TurnSummaryDto dto = new TurnSummaryDto();
        dto.setPlanetId(planetId);
        dto.setContractsExpired(turnSummary.getOrDefault("contractsExpired", 0L));
        dto.setQuestsExpired(turnSummary.getOrDefault("questsExpired", 0L));
        dto.setTotalRevenue(turnSummary.getOrDefault("totalRevenue", 0L));
        dto.setCargoDelivered(turnSummary.getOrDefault("cargoDelivered", 0L));
        dto.setSettlementCount(planetSurfaceBuilder.getAllSettlements(planet).size());
        dto.setIndustryCount(planetSurfaceBuilder.getAllIndustries(planet).size());
        dto.setVehicleCount(planetSurfaceBuilder.getAllVehicles(planet).size());
        dto.setContractCount(planetSurfaceBuilder.getAllContracts(planet).size());
        dto.setQuestCount(planetSurfaceBuilder.getAllQuests(planet).size());
        dto.setTradeRouteCount(planetSurfaceBuilder.getAllTradeRoutes(planet).size());

        log.info("Turn processed for planet {}: {}", planetId, dto);
        return ResponseEntity.ok(dto);
    }

    /**
     * DTO returned by the POST /turn endpoint summarizing turn results.
     */
    public static class TurnSummaryDto {
        private int planetId;
        private long contractsExpired;
        private long questsExpired;
        private long totalRevenue;
        private long cargoDelivered;
        private int settlementCount;
        private int industryCount;
        private int vehicleCount;
        private int contractCount;
        private int questCount;
        private int tradeRouteCount;

        public int getPlanetId() {
            return planetId;
        }

        public void setPlanetId(int planetId) {
            this.planetId = planetId;
        }

        public long getContractsExpired() {
            return contractsExpired;
        }

        public void setContractsExpired(long contractsExpired) {
            this.contractsExpired = contractsExpired;
        }

        public long getQuestsExpired() {
            return questsExpired;
        }

        public void setQuestsExpired(long questsExpired) {
            this.questsExpired = questsExpired;
        }

        public long getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(long totalRevenue) {
            this.totalRevenue = totalRevenue;
        }

        public long getCargoDelivered() {
            return cargoDelivered;
        }

        public void setCargoDelivered(long cargoDelivered) {
            this.cargoDelivered = cargoDelivered;
        }

        public int getSettlementCount() {
            return settlementCount;
        }

        public void setSettlementCount(int settlementCount) {
            this.settlementCount = settlementCount;
        }

        public int getIndustryCount() {
            return industryCount;
        }

        public void setIndustryCount(int industryCount) {
            this.industryCount = industryCount;
        }

        public int getVehicleCount() {
            return vehicleCount;
        }

        public void setVehicleCount(int vehicleCount) {
            this.vehicleCount = vehicleCount;
        }

        public int getContractCount() {
            return contractCount;
        }

        public void setContractCount(int contractCount) {
            this.contractCount = contractCount;
        }

        public int getQuestCount() {
            return questCount;
        }

        public void setQuestCount(int questCount) {
            this.questCount = questCount;
        }

        public int getTradeRouteCount() {
            return tradeRouteCount;
        }

        public void setTradeRouteCount(int tradeRouteCount) {
            this.tradeRouteCount = tradeRouteCount;
        }

        @Override
        public String toString() {
            return "TurnSummaryDto{" +
                    "planetId=" + planetId +
                    ", contractsExpired=" + contractsExpired +
                    ", questsExpired=" + questsExpired +
                    ", totalRevenue=" + totalRevenue +
                    ", cargoDelivered=" + cargoDelivered +
                    '}';
        }
    }
}
