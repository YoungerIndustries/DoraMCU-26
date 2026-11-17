package com.yisy.modules.doramcu.facebook;

import com.yisy.modules.doramcu.dto.auth.LoginResponse;
import com.yisy.modules.doramcu.facebook.dto.FacebookProfileData;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import com.yisy.modules.doramcu.service.auth.JwtService;
import com.yisy.modules.doramcu.service.players.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * REST API for Facebook integration.
 * <p>
 * Provides endpoints for:
 * <ul>
 *   <li>Facebook login — authenticate with a Facebook profile URL or ID</li>
 *   <li>Profile linking — link a Facebook profile to an existing player</li>
 *   <li>Profile lookup — fetch public Facebook profile data</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/facebook")
public class FacebookController {

    private static final Logger log = LoggerFactory.getLogger(FacebookController.class);

    private final FacebookService facebookService;
    private final JwtService jwtService;
    private final PlayerService playerService;
    private final PlayerRepository playerRepository;

    public FacebookController(
            FacebookService facebookService,
            JwtService jwtService,
            ObjectProvider<PlayerService> playerServiceProvider,
            ObjectProvider<PlayerRepository> playerRepositoryProvider) {
        this.facebookService = facebookService;
        this.jwtService = jwtService;
        this.playerService = playerServiceProvider.getIfAvailable();
        this.playerRepository = playerRepositoryProvider.getIfAvailable();
    }

    /**
     * Login with a Facebook profile URL or user ID.
     * <p>
     * Accepts either a profile URL or a Facebook user ID, fetches public profile data,
     * and returns a JWT token. If the player exists by Facebook ID, returns the existing
     * player. If the player exists by email (from Facebook), returns that player.
     * Otherwise, creates a new player with Facebook data.
     * </p>
     *
     * @param request map containing either "profileUrl" or "facebookId"
     * @return login response with JWT token and player info
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        if (!facebookService.isEnabled()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Facebook login is currently disabled"));
        }

        String profileUrl = request.get("profileUrl");
        String facebookId = request.get("facebookId");

        if ((profileUrl == null || profileUrl.isBlank()) && (facebookId == null || facebookId.isBlank())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Provide either 'profileUrl' or 'facebookId'"));
        }

        try {
            // Extract Facebook ID from URL or use provided ID
            String id = facebookId != null && !facebookId.isBlank()
                    ? facebookId
                    : facebookService.parseProfileUrl(profileUrl);

            // Fetch profile data from Facebook
            Optional<FacebookProfileData> profileOpt = facebookService.fetchProfile(id);
            if (profileOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Facebook profile not found. The profile may be private or the ID is invalid."));
            }

            FacebookProfileData profile = profileOpt.get();

            // Check if player already exists by Facebook ID
            if (playerService != null) {
                Optional<Player> existingPlayer = playerService.findByFacebookProfileId(profile.getFacebookId());
                if (existingPlayer.isPresent()) {
                    Player player = existingPlayer.get();
                    String token = jwtService.generateToken(player.getEmail());
                    LoginResponse response = new LoginResponse(token, player.getEmail(),
                            player.getLoginPin(), player.getEmpireName());
                    response.setAdmin(player.isAdmin());
                    log.info("Facebook login for existing player: {}", player.getEmail());
                    return ResponseEntity.ok(response);
                }
            }

            // Check if player exists by email from Facebook
            String email = profile.getEmail();
            if (email != null && !email.isBlank() && playerService != null) {
                Optional<Player> existingByEmail = playerService.findByEmail(email);
                if (existingByEmail.isPresent()) {
                    Player player = existingByEmail.get();
                    // Link Facebook to existing player
                    playerService.updateFromFacebook(player.getId(), profile);
                    String token = jwtService.generateToken(player.getEmail());
                    LoginResponse response = new LoginResponse(token, player.getEmail(),
                            player.getLoginPin(), player.getEmpireName());
                    response.setAdmin(player.isAdmin());
                    log.info("Facebook login linked to existing player by email: {}", player.getEmail());
                    return ResponseEntity.ok(response);
                }
            }

            // Create new player with Facebook data
            if (playerService != null && playerRepository != null) {
                // Generate an email from Facebook data if not available
                String playerEmail;
                if (email != null && !email.isBlank()) {
                    playerEmail = email;
                } else {
                    // Create a synthetic email from Facebook ID
                    playerEmail = "fb_" + profile.getFacebookId() + "@doramcu.local";
                }

                // Check if this synthetic email already exists
                if (playerService.findByEmail(playerEmail).isPresent()) {
                    // Find the player and link Facebook
                    Player existing = playerService.findByEmail(playerEmail).get();
                    playerService.updateFromFacebook(existing.getId(), profile);
                    String token = jwtService.generateToken(existing.getEmail());
                    LoginResponse response = new LoginResponse(token, existing.getEmail(),
                            existing.getLoginPin(), existing.getEmpireName());
                    response.setAdmin(existing.isAdmin());
                    return ResponseEntity.ok(response);
                }

                // Register new player
                Player player = playerService.registerPlayer(
                        playerEmail,
                        profile.getDisplayName() != null ? profile.getDisplayName() : "New Empire",
                        Player.GovernmentType.DEMOCRACY,
                        Player.Ethos.MATERIALIST
                );

                // Update with Facebook data
                playerService.updateFromFacebook(player.getId(), profile);

                String token = jwtService.generateToken(player.getEmail());
                LoginResponse response = new LoginResponse(token, player.getEmail(),
                        player.getLoginPin(), player.getEmpireName());
                response.setAdmin(player.isAdmin());
                log.info("Facebook login created new player: {}", player.getEmail());
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Player service is not available"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Facebook login failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Facebook login failed: " + e.getMessage()));
        }
    }

    /**
     * Link a Facebook profile to an existing player.
     * <p>
     * Fetches Facebook profile data and updates the player's profile fields,
     * filling in any missing information.
     * </p>
     *
     * @param playerId the player ID to link
     * @param request map containing either "profileUrl" or "facebookId"
     * @return success response with updated player info
     */
    @PutMapping("/players/{playerId}/link")
    public ResponseEntity<?> linkFacebookProfile(@PathVariable Long playerId,
                                                  @RequestBody Map<String, String> request) {
        if (!facebookService.isEnabled()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Facebook integration is currently disabled"));
        }

        if (playerService == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Player service is not available"));
        }

        String profileUrl = request.get("profileUrl");
        String facebookId = request.get("facebookId");

        if ((profileUrl == null || profileUrl.isBlank()) && (facebookId == null || facebookId.isBlank())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Provide either 'profileUrl' or 'facebookId'"));
        }

        try {
            // Extract Facebook ID
            String id = facebookId != null && !facebookId.isBlank()
                    ? facebookId
                    : facebookService.parseProfileUrl(profileUrl);

            // Fetch profile data
            Optional<FacebookProfileData> profileOpt = facebookService.fetchProfile(id);
            if (profileOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Facebook profile not found"));
            }

            FacebookProfileData profile = profileOpt.get();

            // Check if this Facebook ID is already linked to another player
            Optional<Player> existingByFacebook = playerService.findByFacebookProfileId(profile.getFacebookId());
            if (existingByFacebook.isPresent() && !existingByFacebook.get().getId().equals(playerId)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "This Facebook profile is already linked to another player"));
            }

            // Update player with Facebook data
            Optional<Player> updatedPlayer = playerService.updateFromFacebook(playerId, profile);
            if (updatedPlayer.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Player not found"));
            }

            log.info("Linked Facebook profile to player: {}", playerId);
            return ResponseEntity.ok(Map.of(
                    "message", "Facebook profile linked successfully",
                    "playerId", updatedPlayer.get().getId(),
                    "facebookId", profile.getFacebookId()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to link Facebook profile to player {}: {}", playerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to link Facebook profile: " + e.getMessage()));
        }
    }

    /**
     * Fetch public Facebook profile data for a given Facebook ID.
     * <p>
     * Returns the raw Facebook profile data without creating or modifying any player records.
     * </p>
     *
     * @param facebookId the Facebook user ID or username
     * @return Facebook profile data
     */
    @GetMapping("/profile/{facebookId}")
    public ResponseEntity<?> getFacebookProfile(@PathVariable String facebookId) {
        if (!facebookService.isEnabled()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Facebook integration is currently disabled"));
        }

        if (facebookId == null || facebookId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Facebook ID must not be blank"));
        }

        Optional<FacebookProfileData> profileOpt = facebookService.fetchProfile(facebookId);
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Facebook profile not found"));
        }

        return ResponseEntity.ok(profileOpt.get());
    }
}
