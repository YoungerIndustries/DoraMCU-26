package com.yisy.modules.doraneuron.model.multigame;

import java.util.Objects;

/**
 * Describes a single game profile registered on this Neuron.
 *
 * Each game has its own ONNX model, game server URL, feature vector size,
 * and confidence threshold. The Neuron maintains one InferenceEngine per
 * active game, enabling isolated AI inference for each game.
 */
public class GameProfile {

    /** Unique game identifier (e.g. "doramcu", "chess", "rts-alpha"). */
    private String gameId;

    /** Human-readable game name. */
    private String gameName;

    /** URL of the game server for this game. */
    private String gameServerUrl;

    /** Path to the ONNX model file for this game. */
    private String onnxModelPath;

    /** Size of the input feature vector (e.g. 64 for DoraMCU). */
    private int featureVectorSize = 64;

    /** Size of the output action space (e.g. 17 for DoraMCU). */
    private int outputActionSize = 17;

    /** Minimum confidence threshold for AI decisions. */
    private double confidenceThreshold = 0.6;

    /** Number of threads for inference. */
    private int inferenceThreads = 2;

    /** Current status of the game profile. */
    private GameStatus status = GameStatus.LOADING;

    /** Current ONNX model version. */
    private long modelVersion = 0;

    /** Timestamp when the profile was registered. */
    private long registeredAt;

    /** Timestamp of the last model update. */
    private long lastModelUpdate;

    /** Total AI predictions made for this game. */
    private long totalPredictions = 0;

    /** Total experience samples collected for this game. */
    private long totalExperienceCollected = 0;

    public GameProfile() {
        this.registeredAt = System.currentTimeMillis();
    }

    public GameProfile(String gameId, String gameName, String gameServerUrl) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.gameServerUrl = gameServerUrl;
        this.registeredAt = System.currentTimeMillis();
        this.status = GameStatus.LOADING;
    }

    /**
     * Check if this game profile is ready to serve AI requests.
     */
    public boolean isReady() {
        return status == GameStatus.ACTIVE && onnxModelPath != null && !onnxModelPath.isEmpty();
    }

    /**
     * Mark the game as active (model loaded and ready).
     */
    public void activate() {
        this.status = GameStatus.ACTIVE;
        this.lastModelUpdate = System.currentTimeMillis();
    }

    /**
     * Increment the prediction counter.
     */
    public void incrementPredictions() {
        this.totalPredictions++;
    }

    /**
     * Increment the experience counter.
     */
    public void incrementExperience() {
        this.totalExperienceCollected++;
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameServerUrl() {
        return gameServerUrl;
    }

    public void setGameServerUrl(String gameServerUrl) {
        this.gameServerUrl = gameServerUrl;
    }

    public String getOnnxModelPath() {
        return onnxModelPath;
    }

    public void setOnnxModelPath(String onnxModelPath) {
        this.onnxModelPath = onnxModelPath;
    }

    public int getFeatureVectorSize() {
        return featureVectorSize;
    }

    public void setFeatureVectorSize(int featureVectorSize) {
        this.featureVectorSize = featureVectorSize;
    }

    public int getOutputActionSize() {
        return outputActionSize;
    }

    public void setOutputActionSize(int outputActionSize) {
        this.outputActionSize = outputActionSize;
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public int getInferenceThreads() {
        return inferenceThreads;
    }

    public void setInferenceThreads(int inferenceThreads) {
        this.inferenceThreads = inferenceThreads;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public long getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(long modelVersion) {
        this.modelVersion = modelVersion;
    }

    public long getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(long registeredAt) {
        this.registeredAt = registeredAt;
    }

    public long getLastModelUpdate() {
        return lastModelUpdate;
    }

    public void setLastModelUpdate(long lastModelUpdate) {
        this.lastModelUpdate = lastModelUpdate;
    }

    public long getTotalPredictions() {
        return totalPredictions;
    }

    public void setTotalPredictions(long totalPredictions) {
        this.totalPredictions = totalPredictions;
    }

    public long getTotalExperienceCollected() {
        return totalExperienceCollected;
    }

    public void setTotalExperienceCollected(long totalExperienceCollected) {
        this.totalExperienceCollected = totalExperienceCollected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameProfile that = (GameProfile) o;
        return Objects.equals(gameId, that.gameId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId);
    }

    @Override
    public String toString() {
        return "GameProfile{gameId='" + gameId + "', name='" + gameName +
                "', status=" + status + ", modelVersion=" + modelVersion + "}";
    }
}
