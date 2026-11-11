package com.yisy.modules.doramcu.dto;

import java.time.LocalDateTime;

/**
 * Response DTO describing a single galaxy backup file.
 * <p>
 * Returned by the backup listing endpoint so admins can see what backups
 * exist for a galaxy before deciding which one to restore.
 * </p>
 */
public class GalaxyBackupInfoDto {

    private String galaxyName;
    private String backupFile;
    private LocalDateTime backupTime;
    private long sizeBytes;
    private int sectorCount;
    private int systemCount;
    private String gameType;
    private String difficultyLevel;

    public GalaxyBackupInfoDto() {
    }

    public GalaxyBackupInfoDto(String galaxyName, String backupFile, LocalDateTime backupTime,
                                long sizeBytes, int sectorCount, int systemCount,
                                String gameType, String difficultyLevel) {
        this.galaxyName = galaxyName;
        this.backupFile = backupFile;
        this.backupTime = backupTime;
        this.sizeBytes = sizeBytes;
        this.sectorCount = sectorCount;
        this.systemCount = systemCount;
        this.gameType = gameType;
        this.difficultyLevel = difficultyLevel;
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

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
}
