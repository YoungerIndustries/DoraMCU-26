package com.yisy.modules.doraneuron.protocol;

/**
 * Notification sent by the coordinator to a Neuron when a new ONNX model is available.
 *
 * The Neuron downloads the model from the provided URL and swaps it into the
 * local inference engine without downtime.
 * <p>
 * Supports version tracking and rollback notifications (Phase 18).
 * </p>
 */
public class ModelUpdateNotification {

    /** Version number of the new model. */
    private long version;

    /** URL from which to download the new ONNX model file. */
    private String url;

    /** SHA-256 hash of the model file for integrity verification. */
    private String sha256Hash;

    /** RSA signature of the model file for authenticity verification (Phase 8). */
    private String modelSignature;

    /** Previous version number (for rollback tracking, Phase 18). */
    private long previousVersion;

    /** Whether this update is a rollback (Phase 18). */
    private boolean isRollback;

    public ModelUpdateNotification() {
    }

    public ModelUpdateNotification(long version, String url, String sha256Hash) {
        this.version = version;
        this.url = url;
        this.sha256Hash = sha256Hash;
    }

    public ModelUpdateNotification(long version, String url, String sha256Hash, String modelSignature) {
        this.version = version;
        this.url = url;
        this.sha256Hash = sha256Hash;
        this.modelSignature = modelSignature;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public void setSha256Hash(String sha256Hash) {
        this.sha256Hash = sha256Hash;
    }

    public String getModelSignature() {
        return modelSignature;
    }

    public void setModelSignature(String modelSignature) {
        this.modelSignature = modelSignature;
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

    public void setRollback(boolean isRollback) {
        this.isRollback = isRollback;
    }

    @Override
    public String toString() {
        return "ModelUpdateNotification{" +
                "version=" + version +
                ", url='" + url + '\'' +
                ", sha256Hash='" + sha256Hash + '\'' +
                ", modelSignature='" + modelSignature + '\'' +
                ", previousVersion=" + previousVersion +
                ", isRollback=" + isRollback +
                '}';
    }
}
