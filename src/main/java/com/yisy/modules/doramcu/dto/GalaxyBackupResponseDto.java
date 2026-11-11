package com.yisy.modules.doramcu.dto;

import java.time.LocalDateTime;

/**
 * Response DTO returned when a galaxy backup is created or restored.
 * <p>
 * Contains the backup file path, timing, and a summary of what was backed up.
 * </p>
 */
public class GalaxyBackupResponseDto {

    private String galaxyName;
    private String backupFile;
    private LocalDateTime backupTime;
    private long sizeBytes;
    private int sectorCount;
    private int systemCount;
    private String status;
    private String message;

    public GalaxyBackupResponseDto() {
    }

    public GalaxyBackupResponseDto(String galaxyName, String backupFile, LocalDateTime backupTime,
                                    long sizeBytes, int sectorCount, int systemCount,
                                    String status, String message) {
        this.galaxyName = galaxyName;
        this.backupFile = backupFile;
        this.backupTime = backupTime;
        this.sizeBytes = sizeBytes;
        this.sectorCount = sectorCount;
        this.systemCount = systemCount;
        this.status = status;
        this.message = message;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    public String getBackupFile() {
        return backupFile;
    }

    public void setBackupFile(String backupFile) {
        this.backupFile = backupFile;
    }

    public LocalDateTime getBackupTime() {
        return backupTime;
    }

    public void setBackupTime(LocalDateTime backupTime) {
        this.backupTime = backupTime;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public int getSectorCount() {
        return sectorCount;
    }

    public void setSectorCount(int sectorCount) {
        this.sectorCount = sectorCount;
    }

    public int getSystemCount() {
        return systemCount;
    }

    public void setSystemCount(int systemCount) {
        this.systemCount = systemCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
