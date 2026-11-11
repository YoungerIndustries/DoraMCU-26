package com.yisy.modules.doraneuron.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Security configuration properties for a DoraMCU Neuron node.
 *
 * All properties are prefixed with {@code neuron.security} in application.properties.
 * This configuration controls JWT authentication, rate limiting, and model signature
 * verification for secure inter-neuron communication.
 */
@ConfigurationProperties(prefix = "neuron.security")
public class NeuronSecurityConfig {

    // ── JWT Authentication ────────────────────────────────────────────────────

    /** Enable JWT authentication for API endpoints. */
    private boolean jwtEnabled = true;

    /** Secret key for JWT signing (minimum 256 bits for HS256). */
    private String jwtSecret = "CHANGE-THIS-TO-A-SECURE-RANDOM-KEY-IN-PRODUCTION";

    /** JWT issuer claim. */
    private String jwtIssuer = "DoraMCU-Neuron";

    /** JWT audience claim. */
    private String jwtAudience = "DoraMCU-Coordinator";

    /** Token expiration in milliseconds (default 1 hour). */
    private long tokenExpirationMs = 3600000;

    /** Refresh token expiration in milliseconds (default 24 hours). */
    private long refreshTokenExpirationMs = 86400000;

    // ── Rate Limiting ─────────────────────────────────────────────────────────

    /** Enable rate limiting for API endpoints. */
    private boolean rateLimitingEnabled = true;

    /** Maximum requests per window per IP. */
    private int maxRequestsPerWindow = 100;

    /** Rate limit window duration in seconds. */
    private int rateLimitWindowSeconds = 60;

    // ── Model Signature Verification ──────────────────────────────────────────

    /** Enable RSA signature verification for ONNX models. */
    private boolean modelSignatureEnabled = true;

    /** Path to the public key file for model signature verification. */
    private String modelPublicKeyPath;

    /** Enable mutual TLS authentication between neurons. */
    private boolean mtlsEnabled = false;

    // ── Getters and Setters ───────────────────────────────────────────────────

    public boolean isJwtEnabled() {
        return jwtEnabled;
    }

    public void setJwtEnabled(boolean jwtEnabled) {
        this.jwtEnabled = jwtEnabled;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public String getJwtIssuer() {
        return jwtIssuer;
    }

    public void setJwtIssuer(String jwtIssuer) {
        this.jwtIssuer = jwtIssuer;
    }

    public String getJwtAudience() {
        return jwtAudience;
    }

    public void setJwtAudience(String jwtAudience) {
        this.jwtAudience = jwtAudience;
    }

    public long getTokenExpirationMs() {
        return tokenExpirationMs;
    }

    public void setTokenExpirationMs(long tokenExpirationMs) {
        this.tokenExpirationMs = tokenExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    public void setRefreshTokenExpirationMs(long refreshTokenExpirationMs) {
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public boolean isRateLimitingEnabled() {
        return rateLimitingEnabled;
    }

    public void setRateLimitingEnabled(boolean rateLimitingEnabled) {
        this.rateLimitingEnabled = rateLimitingEnabled;
    }

    public int getMaxRequestsPerWindow() {
        return maxRequestsPerWindow;
    }

    public void setMaxRequestsPerWindow(int maxRequestsPerWindow) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
    }

    public int getRateLimitWindowSeconds() {
        return rateLimitWindowSeconds;
    }

    public void setRateLimitWindowSeconds(int rateLimitWindowSeconds) {
        this.rateLimitWindowSeconds = rateLimitWindowSeconds;
    }

    public boolean isModelSignatureEnabled() {
        return modelSignatureEnabled;
    }

    public void setModelSignatureEnabled(boolean modelSignatureEnabled) {
        this.modelSignatureEnabled = modelSignatureEnabled;
    }

    public String getModelPublicKeyPath() {
        return modelPublicKeyPath;
    }

    public void setModelPublicKeyPath(String modelPublicKeyPath) {
        this.modelPublicKeyPath = modelPublicKeyPath;
    }

    public boolean isMtlsEnabled() {
        return mtlsEnabled;
    }

    public void setMtlsEnabled(boolean mtlsEnabled) {
        this.mtlsEnabled = mtlsEnabled;
    }
}
