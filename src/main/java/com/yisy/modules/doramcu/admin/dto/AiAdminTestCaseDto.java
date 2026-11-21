package com.yisy.modules.doramcu.admin.dto;

/**
 * DTO for AI admin test cases.
 */
public class AiAdminTestCaseDto {
    private String id;
    private String name;
    private String description;
    private String status;
    private long durationMs;

    public AiAdminTestCaseDto() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
}
