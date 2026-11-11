package com.yisy.modules.doraneuron.protocol.multigame;

/**
 * Request to register a new game on this Neuron.
 */
public class RegisterGameRequest {

    private String gameId;
    private String gameName;
    private String gameServerUrl;
    private String onnxModelPath;
    private int featureVectorSize = 64;
    private int outputActionSize = 17;
    private double confidenceThreshold = 0.6;
    private int inferenceThreads = 2;

    public RegisterGameRequest() {
    }

    public RegisterGameRequest(String gameId, String gameName, String gameServerUrl) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.gameServerUrl = gameServerUrl;
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
}
