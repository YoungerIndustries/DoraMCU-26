package com.yisy.modules.doraneuron.security;

import com.yisy.modules.doraneuron.config.NeuronSecurityConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Provides JWT token generation and validation for Neuron authentication.
 *
 * This class handles creating access tokens and refresh tokens, validating
 * token signatures, extracting claims, and checking token expiration.
 * All tokens use HS256 signing with the configured secret key.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final NeuronSecurityConfig securityConfig;
    private final SecretKey signingKey;

    public JwtTokenProvider(NeuronSecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
        this.signingKey = Keys.hmacShaKeyFor(
                securityConfig.getJwtSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Generate an access token for the given neuron ID.
     */
    public String generateAccessToken(String nodeId) {
        return generateToken(nodeId, securityConfig.getTokenExpirationMs());
    }

    /**
     * Generate a refresh token for the given neuron ID.
     */
    public String generateRefreshToken(String nodeId) {
        return generateToken(nodeId, securityConfig.getRefreshTokenExpirationMs());
    }

    /**
     * Generate a JWT token with the specified expiration.
     */
    private String generateToken(String nodeId, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("nodeId", nodeId);
        claims.put("type", "neuron");

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        claims.put("aud", securityConfig.getJwtAudience());

        return Jwts.builder()
                .claims(claims)
                .subject(nodeId)
                .issuer(securityConfig.getJwtIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validate a JWT token.
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String audience = claims.get("aud", String.class);
            return !isTokenExpired(claims) &&
                    securityConfig.getJwtIssuer().equals(claims.getIssuer()) &&
                    securityConfig.getJwtAudience().equals(audience);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract the neuron ID from a valid token.
     */
    public String extractNodeId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract a specific claim from the token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from the token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if a token is expired.
     */
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * Get the remaining validity of a token in milliseconds.
     */
    public long getRemainingValidity(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0;
        }
    }
}
