package com.yisy.modules.doraneuron.model;

/**
 * Protocol DTO for model version information exchanged between the coordinator
 * and neurons.
 *
 * Used for version comparison, A/B test assignment, and rollback notifications.
 */
public class ModelVersionInfo {

    /** Model version number */
    private long version;

    /** SHA-256 hash of the model file */
    private String sha256Hash;

    /** Model status (ACTIVE, TESTING, ARCHIVED, ROLLED_BACK) */
    private String status;

    /** Download URL for this model version */
    private String downloadUrl;

    /** Previous version number (for rollback tracking) */
    private long previousVersion;

    /** Whether this update is a rollback */
    private boolean isRollback;

    /** File size in bytes */
    private long fileSize;

    public ModelVersionInfo() {
    }

    public ModelVersionInfo(long version, String sha256Hash, String status) {
        this.version = version;
        this.sha256Hash = sha256Hash;
        this.status = status;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public void setSha256Hash(String sha256Hash) {
        this.sha256Hash = sha256Hash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getPreviousVersion() {
        return previousVersion;
    }

    public void setPreviousVersion(long previousVersion) {
        this.previousVersion = previousVersion;
    }

    public boolean isRollback() {
        return isRollback;
    }

    public void setRollback(boolean rollback) {
        isRollback = rollback;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
