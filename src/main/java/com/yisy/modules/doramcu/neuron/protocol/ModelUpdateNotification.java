package com.yisy.modules.doramcu.neuron.protocol;

/**
 * Notification sent by the coordinator to a Neuron when a new ONNX model is available.
 *
 * Mirror of dora-neuron's ModelUpdateNotification for use by the coordinator's
 * Kafka producer without depending on the dora-neuron module.
 */
public class ModelUpdateNotification {

    /** Version number of the new model. */
    private long version;

    /** URL from which to download the new ONNX model file. */
    private String url;

    /** SHA-256 hash of the model file for integrity verification. */
    private String sha256Hash;

    /** RSA signature of the model file for authenticity verification. */
    private String modelSignature;

    /** Previous version number (for rollback tracking). */
    private long previousVersion;

    /** Whether this update is a rollback. */
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

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getSha256Hash() { return sha256Hash; }
    public void setSha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; }

    public String getModelSignature() { return modelSignature; }
    public void setModelSignature(String modelSignature) { this.modelSignature = modelSignature; }

    public long getPreviousVersion() { return previousVersion; }
    public void setPreviousVersion(long previousVersion) { this.previousVersion = previousVersion; }

    public boolean isRollback() { return isRollback; }
    public void setRollback(boolean isRollback) { this.isRollback = isRollback; }
}
