package com.yisy.modules.doramcu.hex.planet.dto;

import java.util.List;

/**
 * DTO representing a transport station on a planet surface.
 * <p>
 * Includes the station's position, type (bus stop, rail station, boat dock, airport),
 * connections, and reference to interior dungeon rooms for 3D exploration.
 * </p>
 */
public class StationDto {

    private String id;
    private String name;
    private int x;
    private int y;
    private int z;
    private String infrastructureType;
    private String connectedSettlementId;
    private String connectedIndustryId;
    private String ownerEmpireId;
    private String dungeonId;
    private List<String> connections;

    public StationDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getInfrastructureType() {
        return infrastructureType;
    }

    public void setInfrastructureType(String infrastructureType) {
        this.infrastructureType = infrastructureType;
    }

    public String getConnectedSettlementId() {
        return connectedSettlementId;
    }

    public void setConnectedSettlementId(String connectedSettlementId) {
        this.connectedSettlementId = connectedSettlementId;
    }

    public String getConnectedIndustryId() {
        return connectedIndustryId;
    }

    public void setConnectedIndustryId(String connectedIndustryId) {
        this.connectedIndustryId = connectedIndustryId;
    }

    public String getOwnerEmpireId() {
        return ownerEmpireId;
    }

    public void setOwnerEmpireId(String ownerEmpireId) {
        this.ownerEmpireId = ownerEmpireId;
    }

    public String getDungeonId() {
        return dungeonId;
    }

    public void setDungeonId(String dungeonId) {
        this.dungeonId = dungeonId;
    }

    public List<String> getConnections() {
        return connections;
    }

    public void setConnections(List<String> connections) {
        this.connections = connections;
    }
}
