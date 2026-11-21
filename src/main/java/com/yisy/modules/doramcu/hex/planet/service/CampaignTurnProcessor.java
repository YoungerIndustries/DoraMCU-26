package com.yisy.modules.doramcu.hex.planet.service;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.DefaultPlanetSurfaceBuilder;
import com.yisy.modules.doramcu.hex.planet.Industry;
import com.yisy.modules.doramcu.hex.planet.Settlement;
import com.yisy.modules.doramcu.hex.planet.Station;
import com.yisy.modules.doramcu.hex.planet.SurfaceVehicle;
import com.yisy.modules.doramcu.hex.planet.TransportCampaignManager;
import com.yisy.modules.doramcu.hex.planet.TransportCorporation;
import com.yisy.modules.doramcu.hex.planet.ai.AiInfrastructureBuilder;
import com.yisy.modules.doramcu.hex.planet.cargo.CargoGenerationService;
import com.yisy.modules.doramcu.hex.planet.cargo.CargoGenerationService.CargoGenerationSummary;
import com.yisy.modules.doramcu.hex.planet.finance.TycoonFinancialService;
import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModule;
import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModuleService;
import com.yisy.modules.doramcu.hex.planet.military.MilitaryProductionChainService;
import com.yisy.modules.doramcu.hex.planet.military.MilitaryProductionChainService.ProductionTurnSummary;
import com.yisy.modules.doramcu.hex.planet.news.TycoonNewsService;
import com.yisy.modules.doramcu.hex.planet.schedule.TimetableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Processes a complete campaign turn, integrating all TTDL subsystems.
 * <p>
 * The turn processor:
 * <ul>
 *   <li>Generates cargo at industries and settlements</li>
 *   <li>Moves vehicles along routes and timetables</li>
 *   <li>Calculates financial interest</li>
 *   <li>Processes AI rival actions</li>
 *   <li>Updates vehicle reliability</li>
 *   <li>Processes military production (cargo → units)</li>
 *   <li>Generates news events</li>
 * </ul>
 * </p>
 */
@Service
public class CampaignTurnProcessor {

    private static final Logger log = LoggerFactory.getLogger(CampaignTurnProcessor.class);

    private final CargoGenerationService cargoGenerationService;
    private final TimetableService timetableService;
    private final TycoonFinancialService financialService;
    private final TycoonNewsService newsService;
    private final AiInfrastructureBuilder aiInfrastructureBuilder;
    private final MilitaryProductionChainService militaryProductionChainService;
    private final HullModuleService hullModuleService;

    public CampaignTurnProcessor(
            CargoGenerationService cargoGenerationService,
            TimetableService timetableService,
            TycoonFinancialService financialService,
            TycoonNewsService newsService,
            AiInfrastructureBuilder aiInfrastructureBuilder,
            MilitaryProductionChainService militaryProductionChainService,
            HullModuleService hullModuleService) {
        this.cargoGenerationService = cargoGenerationService;
        this.timetableService = timetableService;
        this.financialService = financialService;
        this.newsService = newsService;
        this.aiInfrastructureBuilder = aiInfrastructureBuilder;
        this.militaryProductionChainService = militaryProductionChainService;
        this.hullModuleService = hullModuleService;
    }

    /**
     * Processes a complete turn for the campaign.
     *
     * @param campaignManager the campaign manager
     * @param builder the planet surface builder
     * @param planetId the planet ID
     * @return the turn summary
     */
    public TurnSummary processTurn(TransportCampaignManager campaignManager,
                                    DefaultPlanetSurfaceBuilder builder,
                                    int planetId) {
        int turn = campaignManager.getCurrentTurn();
        log.info("Processing turn {} for campaign {}", turn, campaignManager.getCampaignId());

        Planet planet = new Planet(planetId);

        // Load data if needed
        List<Industry> industries = builder.getAllIndustries(planet);
        List<Settlement> settlements = builder.getAllSettlements(planet);
        List<Station> stations = builder.getAllStations(planet);
        List<SurfaceVehicle> vehicles = builder.getAllVehicles(planet);
        List<TransportCorporation> corporations = builder.getAllCorporations(planet);

        Map<String, Station> stationMap = new HashMap<>();
        for (Station station : stations) {
            stationMap.put(station.getId(), station);
        }

        // 1. Generate cargo and passengers at industries and settlements
        CargoGenerationSummary cargoSummary = cargoGenerationService.processTurn(
                industries, settlements, stations);

        // 1b. Generate cargo from HullModules (unified building system)
        List<HullModule> hullModules = hullModuleService.getModuleEntitiesByPlanet((long) planetId);
        int hullModuleCargo = 0;
        if (!hullModules.isEmpty()) {
            hullModuleCargo = cargoGenerationService.generateHullModuleCargo(hullModules, stationMap);
            if (hullModuleCargo > 0) {
                log.info("HullModules produced {} cargo units this turn", hullModuleCargo);
            }
        }
        // Update total cargo generated
        int totalCargoGenerated = cargoSummary.getCargoGenerated() + hullModuleCargo;

        // 2. Move vehicles along routes/timetables
        int vehiclesMoved = 0;
        for (SurfaceVehicle vehicle : vehicles) {
            if (!vehicle.isActive()) continue;
            if (vehicle.isBrokenDown()) continue;

            Station reached = timetableService.processTurn(vehicle, stationMap);
            if (reached != null) {
                vehiclesMoved++;
            }
        }

        // 3. Update vehicle reliability
        for (SurfaceVehicle vehicle : vehicles) {
            if (!vehicle.isActive()) continue;
            vehicle.updateReliability(turn);

            if (vehicle.isBrokenDown()) {
                newsService.onVehicleBreakdown(vehicle, vehicle.getOwnerEmpireId());
            }
        }

        // 4. Calculate financial interest
        long interestCollected = financialService.calculateInterest(corporations, turn);

        // 5. Process military production — consumes cargo from stations to produce units
        int militaryUnitsProduced = 0;
        try {
            ProductionTurnSummary militarySummary = militaryProductionChainService.processProductionTurn(
                    (long) planetId, industries, settlements, stations);
            militaryUnitsProduced = militarySummary.getUnitsProduced();
            if (militaryUnitsProduced > 0) {
                newsService.onMarketChange("Military Production",
                        militaryUnitsProduced + " units produced this turn", null);
            }
        } catch (Exception e) {
            log.warn("Military production failed for turn {}: {}", turn, e.getMessage());
        }

        // 6. Process AI rival actions
        for (TransportCorporation corporation : corporations) {
            if (corporation.getOwnerId().equals("player")) continue;
            aiInfrastructureBuilder.processTurn(corporation, planetId, builder, stations, turn);
        }

        // 7. Process campaign turn (contracts, objectives, etc.)
        campaignManager.processTurn();

        // 8. Generate news for significant events
        if (totalCargoGenerated > 0) {
            newsService.onMarketChange("Cargo",
                    totalCargoGenerated + " units produced this turn", null);
        }

        log.info("Turn {} complete: cargo={}, passengers={}, vehicles_moved={}, military_units={}, interest={}",
                turn, totalCargoGenerated, cargoSummary.getPassengersGenerated(),
                vehiclesMoved, militaryUnitsProduced, interestCollected);

        return new TurnSummary(turn, cargoSummary, vehiclesMoved, militaryUnitsProduced, interestCollected);
    }

    /**
     * Summary of a campaign turn.
     */
    public static class TurnSummary {
        private final int turn;
        private final int cargoGenerated;
        private final int passengersGenerated;
        private final int cargoDecayed;
        private final int vehiclesMoved;
        private final int militaryUnitsProduced;
        private final long interestCollected;

        TurnSummary(int turn, CargoGenerationSummary cargo,
                     int vehiclesMoved, int militaryUnitsProduced, long interestCollected) {
            this.turn = turn;
            this.cargoGenerated = cargo.getCargoGenerated();
            this.passengersGenerated = cargo.getPassengersGenerated();
            this.cargoDecayed = cargo.getCargoDecayed();
            this.vehiclesMoved = vehiclesMoved;
            this.militaryUnitsProduced = militaryUnitsProduced;
            this.interestCollected = interestCollected;
        }

        public int getTurn() { return turn; }
        public int getCargoGenerated() { return cargoGenerated; }
        public int getPassengersGenerated() { return passengersGenerated; }
        public int getCargoDecayed() { return cargoDecayed; }
        public int getVehiclesMoved() { return vehiclesMoved; }
        public int getMilitaryUnitsProduced() { return militaryUnitsProduced; }
        public long getInterestCollected() { return interestCollected; }

        @Override
        public String toString() {
            return String.format("TurnSummary{turn=%d, cargo=%d, passengers=%d, vehicles=%d, military=%d}",
                    turn, cargoGenerated, passengersGenerated, vehiclesMoved, militaryUnitsProduced);
        }
    }
}
