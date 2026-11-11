package com.yisy.modules.doramcu.dnd5e.dto;

/**
 * Response DTO for a D&D 5e crafting check result.
 */
public class CraftingCheckResponse {

    private Long projectId;
    private int roll;
    private int modifier;
    private int total;
    private int dc;
    private boolean success;
    private String result;
    private int hoursWorked;
    private int hoursRequired;
    private int goldSpent;
    private int goldRequired;
    private String status;
    private boolean completed;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public int getRoll() { return roll; }
    public void setRoll(int roll) { this.roll = roll; }

    public int getModifier() { return modifier; }
    public void setModifier(int modifier) { this.modifier = modifier; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getDc() { return dc; }
    public void setDc(int dc) { this.dc = dc; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public int getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(int hoursWorked) { this.hoursWorked = hoursWorked; }

    public int getHoursRequired() { return hoursRequired; }
    public void setHoursRequired(int hoursRequired) { this.hoursRequired = hoursRequired; }

    public int getGoldSpent() { return goldSpent; }
    public void setGoldSpent(int goldSpent) { this.goldSpent = goldSpent; }

    public int getGoldRequired() { return goldRequired; }
    public void setGoldRequired(int goldRequired) { this.goldRequired = goldRequired; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
