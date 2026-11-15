package com.yisy.modules.doraneuron.controller;

import com.yisy.modules.doraneuron.dto.PlayerLoginRequest;
import com.yisy.modules.doraneuron.dto.PlayerRegistrationRequest;
import com.yisy.modules.doraneuron.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles player authentication requests — login and registration.
 *
 * These endpoints are publicly accessible so that new and returning players
 * can authenticate through the neuron portal before receiving client capabilities.
 */
@RestController
@RequestMapping("/api/v1/player")
public class PlayerAuthController {

    private static final Logger log = LoggerFactory.getLogger(PlayerAuthController.class);

    private final JwtTokenProvider jwtTokenProvider;

    public PlayerAuthController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Register a new player account.
     *
     * In a production system this would delegate to the DoraMCU OnboardingController
     * or a dedicated user service. For now it creates a JWT directly.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody PlayerRegistrationRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "email is required"));
        }
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username is required"));
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "password is required"));
        }

        log.info("Player registration: {} (platform: {}, os: {})",
                request.getUsername(), request.getPlatform(), request.getOs());

        // Use email as the subject for the JWT token
        String accessToken = jwtTokenProvider.generateAccessToken(request.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(request.getEmail());

        return ResponseEntity.ok(Map.of(
                "message", "Registration successful",
                "playerId", request.getEmail(),
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "expiresIn", jwtTokenProvider.getRemainingValidity(accessToken)
        ));
    }

    /**
     * Authenticate an existing player and return JWT tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody PlayerLoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "email is required"));
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "password is required"));
        }

        log.info("Player login attempt: {}", request.getEmail());

        // In production, verify credentials against the user database
        // For now, accept any non-empty credentials and issue tokens
        String accessToken = jwtTokenProvider.generateAccessToken(request.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(request.getEmail());

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "playerId", request.getEmail(),
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "expiresIn", jwtTokenProvider.getRemainingValidity(accessToken)
        ));
    }
}
