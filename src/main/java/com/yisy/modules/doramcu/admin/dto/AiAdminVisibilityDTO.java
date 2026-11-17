package com.yisy.modules.doramcu.admin.dto;

import java.time.LocalDateTime;

/**
 * DTO representing an AI admin's visibility information for the game world.
 * Used by the visibility endpoints to show AI admin positions and status.
 */
public class AiAdminVisibilityDTO {

    private Long id;
    private String name;
    private String role;
    private String empireName;
    private String galaxyName;
    private Double posX;
    private Double posY;
    private Double posZ;
    private String characterType;
    private Long currentOrderId;
    private Integer activeOrderCount;
    private Double distance;
    private Boolean isAvailable;
    private String status;
    private Integer totalExperience;
    private LocalDateTime lastSeenAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmpireName() { return empireName; }
    public void setEmpireName(String empireName) { this.empireName = empireName; }

    public String getGalaxyName() { return galaxyName; }
    public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }

    public Double getPosX() { return posX; }
    public void setPosX(Double posX) { this.posX = posX; }

    public Double getPosY() { return posY; }
    public void setPosY(Double posY) { this.posY = posY; }

    public Double getPosZ() { return posZ; }
    public void setPosZ(Double posZ) { this.posZ = posZ; }

    public String getCharacterType() { return characterType; }
    public void setCharacterType(String characterType) { this.characterType = characterType; }

    public Long getCurrentOrderId() { return currentOrderId; }
    public void setCurrentOrderId(Long currentOrderId) { this.currentOrderId = currentOrderId; }

    public Integer getActiveOrderCount() { return activeOrderCount; }
    public void setActiveOrderCount(Integer activeOrderCount) { this.activeOrderCount = activeOrderCount; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTotalExperience() { return totalExperience; }
    public void setTotalExperience(Integer totalExperience) { this.totalExperience = totalExperience; }

    public LocalDateTime getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(LocalDateTime lastSeenAt) { this.lastSeenAt = lastSeenAt; }
}
