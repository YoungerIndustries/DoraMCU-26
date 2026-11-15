package com.yisy.modules.doraneuron.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Response that tells the client which capabilities to download based on the detected platform.
 */
public class ClientDistributionResponse {

    private String platform;
    private String clientType;
    private String pwaManifest;
    private String extensionDownloadUrl;
    private String gameEngineBundle;
    private List<AiModel> aiModels = new ArrayList<>();
    private List<GameRule> gameRules = new ArrayList<>();
    private String syncEndpoint;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getPwaManifest() {
        return pwaManifest;
    }

    public void setPwaManifest(String pwaManifest) {
        this.pwaManifest = pwaManifest;
    }

    public String getExtensionDownloadUrl() {
        return extensionDownloadUrl;
    }

    public void setExtensionDownloadUrl(String extensionDownloadUrl) {
        this.extensionDownloadUrl = extensionDownloadUrl;
    }

    public String getGameEngineBundle() {
        return gameEngineBundle;
    }

    public void setGameEngineBundle(String gameEngineBundle) {
        this.gameEngineBundle = gameEngineBundle;
    }

    public List<AiModel> getAiModels() {
        return aiModels;
    }

    public void setAiModels(List<AiModel> aiModels) {
        this.aiModels = aiModels;
    }

    public List<GameRule> getGameRules() {
        return gameRules;
    }

    public void setGameRules(List<GameRule> gameRules) {
        this.gameRules = gameRules;
    }

    public String getSyncEndpoint() {
        return syncEndpoint;
    }

    public void setSyncEndpoint(String syncEndpoint) {
        this.syncEndpoint = syncEndpoint;
    }

    /**
     * AI model reference for client-side inference.
     */
    public static class AiModel {
        private String name;
        private String url;
        private int version;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }
    }

    /**
     * Game rule reference for client-side rule execution.
     */
    public static class GameRule {
        private String system;
        private String url;

        public String getSystem() {
            return system;
        }

        public void setSystem(String system) {
            this.system = system;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
