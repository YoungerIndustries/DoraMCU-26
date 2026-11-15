package com.yisy.modules.doramcu.hex.planet.dto;

import com.yisy.modules.doramcu.hex.planet.TerrainData;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing the full surface data of a planet.
 * <p>
 * Returned by the planet surface API to game clients for 3D rendering.
 * Includes terrain hexes, settlements, industries, stations, and vehicles.
 * </p>
 */
public class PlanetSurfaceDto {

    private int planetId;
    private String planetName;
    private List<TerrainHexDto> terrainHexes;
    private List<SettlementDto> settlements;
    private List<IndustryDto> industries;
    private List<StationDto> stations;
    private List<VehicleDto> vehicles;
    private List<CorporationDto> corporations;
    private List<ContractDto> contracts;
    private List<QuestDto> quests;
    private List<TradeRouteDto> tradeRoutes;
    private SummaryDto summary;

    public PlanetSurfaceDto() {
    }

    public PlanetSurfaceDto(int planetId, String planetName) {
        this.planetId = planetId;
        this.planetName = planetName;
    }

    public int getPlanetId() {
        return planetId;
    }

    public void setPlanetId(int planetId) {
        this.planetId = planetId;
    }

    public String getPlanetName() {
        return planetName;
    }

    public void setPlanetName(String planetName) {
        this.planetName = planetName;
    }

    public List<TerrainHexDto> getTerrainHexes() {
        return terrainHexes;
    }

    public void setTerrainHexes(List<TerrainHexDto> terrainHexes) {
        this.terrainHexes = terrainHexes;
    }

    public List<SettlementDto> getSettlements() {
        return settlements;
    }

    public void setSettlements(List<SettlementDto> settlements) {
        this.settlements = settlements;
    }

    public List<IndustryDto> getIndustries() {
        return industries;
    }

    public void setIndustries(List<IndustryDto> industries) {
        this.industries = industries;
    }

    public List<StationDto> getStations() {
        return stations;
    }

    public void setStations(List<StationDto> stations) {
        this.stations = stations;
    }

    public List<VehicleDto> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<VehicleDto> vehicles) {
        this.vehicles = vehicles;
    }

    public List<CorporationDto> getCorporations() {
        return corporations;
    }

    public void setCorporations(List<CorporationDto> corporations) {
        this.corporations = corporations;
    }

    public List<ContractDto> getContracts() {
        return contracts;
    }

    public void setContracts(List<ContractDto> contracts) {
        this.contracts = contracts;
    }

    public List<QuestDto> getQuests() {
        return quests;
    }

    public void setQuests(List<QuestDto> quests) {
        this.quests = quests;
    }

    public List<TradeRouteDto> getTradeRoutes() {
        return tradeRoutes;
    }

    public void setTradeRoutes(List<TradeRouteDto> tradeRoutes) {
        this.tradeRoutes = tradeRoutes;
    }

    public SummaryDto getSummary() {
        return summary;
    }

    public void setSummary(SummaryDto summary) {
        this.summary = summary;
    }

    /**
     * Summary counts of objects on the planet surface.
     */
    public static class SummaryDto {
        private long terrainCells;
        private long settlements;
        private long industries;
        private long stations;
        private long vehicles;
        private long corporations;
        private long interiors;
        private long contracts;
        private long quests;
        private long tradeRoutes;

        public long getTerrainCells() {
            return terrainCells;
        }

        public void setTerrainCells(long terrainCells) {
            this.terrainCells = terrainCells;
        }

        public long getSettlements() {
            return settlements;
        }

        public void setSettlements(long settlements) {
            this.settlements = settlements;
        }

        public long getIndustries() {
            return industries;
        }

        public void setIndustries(long industries) {
            this.industries = industries;
        }

        public long getStations() {
            return stations;
        }

        public void setStations(long stations) {
            this.stations = stations;
        }

        public long getVehicles() {
            return vehicles;
        }

        public void setVehicles(long vehicles) {
            this.vehicles = vehicles;
        }

        public long getInteriors() {
            return interiors;
        }

        public void setInteriors(long interiors) {
            this.interiors = interiors;
        }

        public long getCorporations() {
            return corporations;
        }

        public void setCorporations(long corporations) {
            this.corporations = corporations;
        }

        public long getContracts() {
            return contracts;
        }

        public void setContracts(long contracts) {
            this.contracts = contracts;
        }

        public long getQuests() {
            return quests;
        }

        public void setQuests(long quests) {
            this.quests = quests;
        }

        public long getTradeRoutes() {
            return tradeRoutes;
        }

        public void setTradeRoutes(long tradeRoutes) {
            this.tradeRoutes = tradeRoutes;
        }
    }
}
