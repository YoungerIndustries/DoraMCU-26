package com.yisy.modules.doramcu.admin.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for submitting a new AI admin test order.
 */
public class AiAdminOrderRequest {

    @NotBlank(message = "Order text is required")
    private String orderText;

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
}
