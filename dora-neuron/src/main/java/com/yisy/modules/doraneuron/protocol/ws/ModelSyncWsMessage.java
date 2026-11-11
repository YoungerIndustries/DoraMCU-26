package com.yisy.modules.doraneuron.protocol.ws;

/**
 * WebSocket model sync message sent from Coordinator to all Neurons.
 *
 * When the coordinator receives a new ONNX model from the DL service,
 * it broadcasts this message to all connected neurons so they can
 * download and swap the model in real time.
 */
public class ModelSyncWsMessage extends NeuronWsMessage {

    private static final long serialVersionUID = 1L;

    public static final String TYPE = "MODEL_SYNC";

    /** New model version number. */
    private long version;

    /** URL where the model file can be downloaded. */
    private String downloadUrl;

    /** SHA-256 hash for integrity verification. */
    private String sha256Hash;

    public ModelSyncWsMessage() {
        super(TYPE, null);
    }

    public ModelSyncWsMessage(long version, String downloadUrl, String sha256Hash) {
        super(TYPE, null);
        this.version = version;
        this.downloadUrl = downloadUrl;
        this.sha256Hash = sha256Hash;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public void setSha256Hash(String sha256Hash) {
        this.sha256Hash = sha256Hash;
    }
}
