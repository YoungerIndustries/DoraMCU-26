package com.yisy.modules.doramcu.traveller.service;

import com.yisy.modules.doramcu.gamecore.BuildingInstanceService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Traveller adapter for the BuildingInstanceService interface.
 * <p>
 * Provides stub implementations. Future enhancements could add Traveller-themed
 * buildings like space stations, corporate offices, and port facilities.
 * </p>
 */
@Service
public class TravellerBuildingInstanceService implements BuildingInstanceService {

    @Override
    public BuildingInstance createBuilding(String buildingType, String name, int upperFloors, int basementLevels) {
        return null;
    }

    @Override
    public BuildingInstance getBuilding(Long id) {
        return null;
    }

    @Override
    public List<BuildingInstance> listBuildings() {
        return List.of();
    }

    @Override
    public List<BuildingInstance> listBuildingsByType(String buildingType) {
        return List.of();
    }

    @Override
    public List<FloorInstance> getFloors(Long buildingId) {
        return List.of();
    }

    @Override
    public FloorInstance getFloor(Long buildingId, int floorLevel) {
        return null;
    }

    @Override
    public DungeonInstance configureBasement(Long buildingId, int basementLevel, String templateId) {
        return null;
    }

    @Override
    public List<DungeonInstance> getDungeonInstances(Long buildingId) {
        return List.of();
    }

    @Override
    public DungeonInstance getDungeonInstance(Long id) {
        return null;
    }

    @Override
    public boolean removeDungeonInstance(Long dungeonInstanceId) {
        return false;
    }

    @Override
    public List<DungeonInstanceTemplate> listDungeonTemplates() {
        return List.of();
    }

    @Override
    public DungeonInstanceTemplate getDungeonTemplate(String templateId) {
        return null;
    }
}
