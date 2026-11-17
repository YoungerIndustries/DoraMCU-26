package com.yisy.modules.doramcu.admin.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for submitting a new AI admin test order.
 * Supports optional target location fields for order-aware positioning.
 */
public class AiAdminOrderRequest {

    @NotBlank(message = "Order text is required")
    private String orderText;

    /**
     * Target galaxy for the AI admin to move to (optional).
     */
    private String targetGalaxy;

    /**
     * Target X coordinate (optional).
     */
    private Double targetPosX;

    /**
     * Target Y coordinate (optional).
     */
    private Double targetPosY;

    /**
     * Target Z coordinate (optional).
     */
    private Double targetPosZ;

    public AiAdminOrderRequest() {
    }

    public AiAdminOrderRequest(String orderText) {
        this.orderText = orderText;
    }

    public String getOrderText() {
        return orderText;
    }

    public void setOrderText(String orderText) {
        this.orderText = orderText;
    }

    public String getTargetGalaxy() {
        return targetGalaxy;
    }

    public void setTargetGalaxy(String targetGalaxy) {
        this.targetGalaxy = targetGalaxy;
    }

    public Double getTargetPosX() {
        return targetPosX;
    }

    public void setTargetPosX(Double targetPosX) {
        this.targetPosX = targetPosX;
    }

    public Double getTargetPosY() {
        return targetPosY;
    }

    public void setTargetPosY(Double targetPosY) {
        this.targetPosY = targetPosY;
    }

    public Double getTargetPosZ() {
        return targetPosZ;
    }

    public void setTargetPosZ(Double targetPosZ) {
        this.targetPosZ = targetPosZ;
    }
}
