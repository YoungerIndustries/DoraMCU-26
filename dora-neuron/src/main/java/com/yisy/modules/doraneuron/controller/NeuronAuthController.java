package com.yisy.modules.doraneuron.controller;

import com.yisy.modules.doraneuron.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles authentication requests for Neuron nodes.
 *
 * This controller provides endpoints for obtaining and refreshing JWT tokens.
 * It is publicly accessible (no authentication required) to allow new neurons
 * to authenticate with the coordinator.
 */
@RestController
@RequestMapping("/api/auth")
public class NeuronAuthController {

    private static final Logger log = LoggerFactory.getLogger(NeuronAuthController.class);

    private final JwtTokenProvider jwtTokenProvider;

    public NeuronAuthController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Request a new access token for a neuron.
     * The neuron must provide its nodeId and a shared secret.
     */
    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> requestToken(@RequestBody Map<String, String> request) {
        String nodeId = request.get("nodeId");
        String secret = request.get("secret");

        if (nodeId == null || nodeId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "nodeId is required"));
        }

        if (secret == null || secret.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "secret is required"));
        }

        // In production, verify the secret against a stored value
        // For now, we accept any non-empty secret as a placeholder
        log.info("Token request for neuron: {}", nodeId);

        String accessToken = jwtTokenProvider.generateAccessToken(nodeId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(nodeId);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "expiresIn", jwtTokenProvider.getRemainingValidity(accessToken)
        ));
    }

    /**
     * Refresh an access token using a valid refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refreshToken is required"));
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
        }

        String nodeId = jwtTokenProvider.extractNodeId(refreshToken);
        String newAccessToken = jwtTokenProvider.generateAccessToken(nodeId);

        log.info("Token refreshed for neuron: {}", nodeId);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "expiresIn", jwtTokenProvider.getRemainingValidity(newAccessToken)
        ));
    }

    /**
     * Validate a token without using it.
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "token is required"));
        }

        boolean valid = jwtTokenProvider.validateToken(token);
        String nodeId = valid ? jwtTokenProvider.extractNodeId(token) : null;
        long remaining = valid ? jwtTokenProvider.getRemainingValidity(token) : 0;

        return ResponseEntity.ok(Map.of(
                "valid", valid,
                "nodeId", nodeId,
                "remainingValidityMs", remaining
        ));
    }
}
