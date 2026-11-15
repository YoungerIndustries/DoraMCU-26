package com.yisy.modules.doramcu.earth2026.survival.dto;

import com.yisy.modules.doramcu.earth2026.survival.SurvivalResource;

import java.util.List;

/**
 * DTO representing the result of a scavenging action.
 */
public class ScavengeResultDto {

    private String message;
    private List<SurvivalResource> resourcesFound;
    private int totalItems;
    private String location;
    private boolean success;

    public ScavengeResultDto() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<SurvivalResource> getResourcesFound() {
        return resourcesFound;
    }

    public void setResourcesFound(List<SurvivalResource> resourcesFound) {
        this.resourcesFound = resourcesFound;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
