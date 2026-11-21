package com.yisy.modules.doramcu.hex.planet.controller;

import com.yisy.modules.doramcu.gamecore.DungeonRoomService.DungeonRoom;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.planet.CampaignTradeRoute;
import com.yisy.modules.doramcu.hex.planet.CargoType;
import com.yisy.modules.doramcu.hex.planet.DeliveryContract;
import com.yisy.modules.doramcu.hex.planet.Industry;
import com.yisy.modules.doramcu.hex.planet.PlanetQuest;
import com.yisy.modules.doramcu.hex.planet.Settlement;
import com.yisy.modules.doramcu.hex.planet.Station;
import com.yisy.modules.doramcu.hex.planet.SurfaceVehicle;
import com.yisy.modules.doramcu.hex.planet.TerrainData;
import com.yisy.modules.doramcu.hex.planet.TransportCorporation;
import com.yisy.modules.doramcu.hex.planet.dto.ContractDto;
import com.yisy.modules.doramcu.hex.planet.dto.CorporationDto;
import com.yisy.modules.doramcu.hex.planet.dto.IndustryDto;
import com.yisy.modules.doramcu.hex.planet.dto.QuestDto;
import com.yisy.modules.doramcu.hex.planet.dto.SettlementDto;
import com.yisy.modules.doramcu.hex.planet.dto.StationDto;
import com.yisy.modules.doramcu.hex.planet.dto.TerrainHexDto;
import com.yisy.modules.doramcu.hex.planet.dto.TradeRouteDto;
import com.yisy.modules.doramcu.hex.planet.dto.VehicleDto;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Shared DTO mapping utilities for planet surface entities.
 * Used by both HexPlanetSurfaceController and HexPlanetEntityController
 * to eliminate duplicate mapping code.
 */
public class HexPlanetDtoMapper {

    private HexPlanetDtoMapper() {
        // Utility class
    }

    public static TerrainHexDto toTerrainHexDto(String hexKey, TerrainData td) {
        TerrainHexDto dto = new TerrainHexDto();
        String[] parts = hexKey.split(",");
        if (parts.length == 3) {
            dto.setX(Integer.parseInt(parts[0].trim()));
            dto.setY(Integer.parseInt(parts[1].trim()));
            dto.setZ(Integer.parseInt(parts[2].trim()));
        }
        dto.setTerrainType(td.getTerrainType() != null ? td.getTerrainType().name() : null);
        dto.setBiome(td.getBiome() != null ? td.getBiome().getName() : null);
        dto.setTemperature(td.getBiome().getTemperature());
        dto.setMoisture(td.getBiome().getMoisture());
        dto.setAltitude(td.getBiome().getAltitude());
        dto.setInfrastructure(td.getInfrastructure() != null ? td.getInfrastructure().name() : null);
        dto.setSettlementId(td.getSettlementId());
        dto.setIndustryId(td.getIndustryId());
        dto.setWater(!td.getTerrainType().isLand());
        return dto;
    }

    public static SettlementDto toSettlementDto(Settlement s) {
        SettlementDto dto = new SettlementDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        Hex3D center = s.getCenterHex();
        if (center != null) {
            dto.setCenterX(center.getX());
            dto.setCenterY(center.getY());
            dto.setCenterZ(center.getZ());
        }
        dto.setSize(s.getSize() != null ? s.getSize().name() : null);
        dto.setPopulation(s.getPopulation());
        dto.setPassengerProduction(s.getPassengerProduction());
        dto.setActive(s.isActive());
        dto.setOwnerEmpireId(s.getOwnerEmpireId());
        dto.setDungeonIds(s.getDungeonIds());
        dto.setHexes(s.getOccupiedHexes() != null ?
                s.getOccupiedHexes().stream().map(h -> h.getX() + "," + h.getY() + "," + h.getZ()).collect(Collectors.toList()) :
                Collections.emptyList());
        return dto;
    }

    public static IndustryDto toIndustryDto(Industry i) {
        IndustryDto dto = new IndustryDto();
        dto.setId(i.getId());
        dto.setType(i.getType() != null ? i.getType().name() : null);
        Hex3D pos = i.getLocation();
        if (pos != null) {
            dto.setX(pos.getX());
            dto.setY(pos.getY());
            dto.setZ(pos.getZ());
        }
        dto.setProductionRate((int) (i.getProductionLevel() * 100));
        Map<String, Integer> combinedInventory = new HashMap<>();
        if (i.getInputInventory() != null) {
            i.getInputInventory().forEach((k, v) -> combinedInventory.put("input_" + k.name(), v));
        }
        if (i.getOutputInventory() != null) {
            i.getOutputInventory().forEach((k, v) -> combinedInventory.put("output_" + k.name(), v));
        }
        dto.setInventory(combinedInventory);
        dto.setActive(i.isActive());
        dto.setOwnerEmpireId(i.getOwnerEmpireId());
        dto.setDungeonId(i.getDungeonId());
        return dto;
    }

    public static StationDto toStationDto(Station s) {
        StationDto dto = new StationDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        Hex3D pos = s.getPosition();
        if (pos != null) {
            dto.setX(pos.getX());
            dto.setY(pos.getY());
            dto.setZ(pos.getZ());
        }
        dto.setInfrastructureType(s.getInfrastructureType() != null ? s.getInfrastructureType().name() : null);
        dto.setConnectedSettlementId(s.getConnectedSettlementId());
        dto.setConnectedIndustryId(s.getConnectedIndustryId());
        dto.setOwnerEmpireId(s.getOwnerEmpireId());
        dto.setDungeonId(s.getDungeonId());
        dto.setConnections(s.getCatchmentArea() != null ?
                s.getCatchmentArea().stream().map(h -> h.getX() + "," + h.getY() + "," + h.getZ()).collect(Collectors.toList()) :
                Collections.emptyList());
        return dto;
    }

    public static VehicleDto toVehicleDto(SurfaceVehicle v) {
        VehicleDto dto = new VehicleDto();
        dto.setId(v.getId());
        dto.setType(v.getType() != null ? v.getType().name() : null);
        Hex3D pos = v.getCurrentPosition();
        if (pos != null) {
            dto.setX(pos.getX());
            dto.setY(pos.getY());
            dto.setZ(pos.getZ());
        }
        dto.setRouteStations(v.getRouteStationIds());
        Map<String, Integer> cargoMap = new HashMap<>();
        if (v.getCargoInventory() != null) {
            v.getCargoInventory().forEach((k, val) -> cargoMap.put(k.name(), val));
        }
        dto.setCargoInventory(cargoMap);
        dto.setPassengerCount(v.getPassengerCount());
        dto.setTotalDistance(v.getTotalDistance());
        dto.setTotalCargoDelivered(v.getTotalCargoDelivered());
        dto.setTotalPassengersDelivered(v.getTotalPassengersDelivered());
        dto.setActive(v.isActive());
        dto.setOwnerEmpireId(v.getOwnerEmpireId());
        return dto;
    }

    public static CorporationDto toCorporationDto(TransportCorporation c) {
        CorporationDto dto = new CorporationDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setOwnerEmpireId(c.getOwnerId());
        dto.setRank(c.getRank() != null ? c.getRank().name() : null);
        dto.setCredits(c.getCredits());
        dto.setReputation(c.getReputation());
        dto.setXp(c.getXp());
        dto.setXpToNextRank(c.getXpToNextRank());
        dto.setPlanetsOperated(c.getPlanetsOperated());
        dto.setTotalCargoDelivered(c.getTotalCargoDelivered());
        dto.setOwnedStationIds(c.getOwnedStationIds());
        dto.setOwnedVehicleIds(c.getOwnedVehicleIds());
        dto.setOwnedRouteIds(c.getOwnedRouteIds());
        dto.setActiveContractIds(c.getActiveContractIds());
        Map<String, Integer> cargoMap = new HashMap<>();
        if (c.getCargoInventory() != null) {
            c.getCargoInventory().forEach((k, v) -> cargoMap.put(k.name(), v));
        }
        dto.setCargoInventory(cargoMap);
        dto.setActiveQuestIds(c.getActiveQuestIds());
        dto.setCompletedQuestIds(c.getCompletedQuestIds());
        dto.setTotalAssetCount(c.getTotalAssetCount());
        return dto;
    }

    public static ContractDto toContractDto(DeliveryContract c) {
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

    public static QuestDto toQuestDto(PlanetQuest q) {
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

    public static TradeRouteDto toTradeRouteDto(CampaignTradeRoute r) {
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

    public static Map<String, Object> toRoomMap(DungeonRoom room) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", room.getId());
        map.put("name", room.getName());
        map.put("description", room.getDescription());
        map.put("dungeonId", room.getDungeonId());
        map.put("theme", room.getTheme());
        map.put("size", room.getSize());
        map.put("connections", room.getConnections());
        map.put("trap", room.getTrap());
        map.put("trapDc", room.getTrapDc());
        map.put("trapDamage", room.getTrapDamage());
        map.put("trapDamageExpr", room.getTrapDamageExpr());
        map.put("treasure", room.getTreasure());
        map.put("goldPieces", room.getGoldPieces());
        map.put("magicRarity", room.getMagicRarity());
        map.put("encounter", room.getEncounter());
        map.put("encounterDifficulty", room.getEncounterDifficulty());
        map.put("xpReward", room.getXpReward());
        map.put("minLevel", room.getMinLevel());
        map.put("maxLevel", room.getMaxLevel());
        map.put("discovered", room.isDiscovered());
        map.put("active", room.isActive());
        return map;
    }
}
