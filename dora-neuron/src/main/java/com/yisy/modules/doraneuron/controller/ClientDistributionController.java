package com.yisy.modules.doraneuron.controller;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.dto.ClientDistributionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * Determines the player's platform and returns the appropriate client capabilities package.
 *
 * After login/registration the client calls this endpoint to receive the URLs for
 * PWA manifest, browser extension, game engine bundle, and AI models.
 */
@RestController
@RequestMapping("/api/v1/player")
public class ClientDistributionController {

    private static final Logger log = LoggerFactory.getLogger(ClientDistributionController.class);

    private final NeuronConfig config;

    public ClientDistributionController(NeuronConfig config) {
        this.config = config;
    }

    /**
     * Return platform-specific client distribution data.
     *
     * The client sends its detected platform and OS; the server responds with
     * the correct client type (PWA only or PWA + extension) and all required URLs.
     */
    @GetMapping("/platform")
    public ResponseEntity<ClientDistributionResponse> distributeClient(
            @RequestParam(value = "platform", defaultValue = "desktop") String platform,
            @RequestParam(value = "os", defaultValue = "windows") String os) {

        log.info("Client distribution request: platform={}, os={}", platform, os);

        ClientDistributionResponse response = new ClientDistributionResponse();
        response.setPlatform(platform);

        // Determine client type based on platform
        boolean isMobile = "mobile".equalsIgnoreCase(platform) || "android".equalsIgnoreCase(os) || "ios".equalsIgnoreCase(os);
        if (isMobile) {
            response.setClientType("pwa");
        } else {
            response.setClientType("pwa+extension");
        }

        // Set PWA manifest URL
        response.setPwaManifest("/manifest.json");

        // Set extension download URL (only for non-mobile platforms)
        if (!isMobile) {
            response.setExtensionDownloadUrl("/api/v1/clients/extension/chrome");
        }

        // Set game engine bundle URL
        response.setGameEngineBundle("/assets/game-engine/bundle.js");

        // Set AI models
        ClientDistributionResponse.AiModel travellerAi = new ClientDistributionResponse.AiModel();
        travellerAi.setName("traveller-ai");
        travellerAi.setUrl("/models/traveller.onnx");
        travellerAi.setVersion(1);
        response.getAiModels().add(travellerAi);

        ClientDistributionResponse.AiModel earth2026Ai = new ClientDistributionResponse.AiModel();
        earth2026Ai.setName("earth2026-ai");
        earth2026Ai.setUrl("/models/earth2026.onnx");
        earth2026Ai.setVersion(1);
        response.getAiModels().add(earth2026Ai);

        // Set game rules
        ClientDistributionResponse.GameRule travellerRules = new ClientDistributionResponse.GameRule();
        travellerRules.setSystem("traveller");
        travellerRules.setUrl("/assets/rules/traveller.json");
        response.getGameRules().add(travellerRules);

        ClientDistributionResponse.GameRule earth2026Rules = new ClientDistributionResponse.GameRule();
        earth2026Rules.setSystem("earth2026");
        earth2026Rules.setUrl("/assets/rules/earth2026.json");
        response.getGameRules().add(earth2026Rules);

        // Set WebSocket sync endpoint
        String coordinatorUrl = config.getCoordinatorUrl();
        if (coordinatorUrl != null && !coordinatorUrl.isEmpty()) {
            String baseUrl = coordinatorUrl.endsWith("/") ? coordinatorUrl.substring(0, coordinatorUrl.length() - 1) : coordinatorUrl;
            response.setSyncEndpoint("ws://" + baseUrl.replace("http://", "").replace("https://", "") + "/ws/game-sync");
        } else {
            response.setSyncEndpoint("ws://localhost:8090/ws/game-sync");
        }

        return ResponseEntity.ok(response);
    }
}
