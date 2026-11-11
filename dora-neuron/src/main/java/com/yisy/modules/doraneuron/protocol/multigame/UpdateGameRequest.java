package com.yisy.modules.doraneuron.protocol.multigame;

/**
 * Request to update an existing game's configuration.
 */
public class UpdateGameRequest {

    private String gameName;
    private String gameServerUrl;
    private String onnxModelPath;
    private int featureVectorSize;
    private int outputActionSize;
    private double confidenceThreshold;
    private int inferenceThreads;
    private boolean pauseGame;

    public UpdateGameRequest() {
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

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

    public boolean isPauseGame() {
        return pauseGame;
    }

    public void setPauseGame(boolean pauseGame) {
        this.pauseGame = pauseGame;
    }
}
