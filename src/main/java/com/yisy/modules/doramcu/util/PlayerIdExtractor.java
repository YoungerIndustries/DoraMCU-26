package com.yisy.modules.doramcu.util;

import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.model.user.User;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import com.yisy.modules.doramcu.repository.user.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility component that extracts a stable player ID from an authenticated user.
 * <p>
 * Looks up the actual player ID from the database by username. Falls back to a
 * stable hash only when no player record exists (e.g., unregistered users).
 * </p>
 */
@Component
public class PlayerIdExtractor {

    private static final Logger log = LoggerFactory.getLogger(PlayerIdExtractor.class);

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    public PlayerIdExtractor(UserRepository userRepository, PlayerRepository playerRepository) {
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
    }

    /**
     * Extract a player ID from the current authentication.
     *
     * @param authentication the Spring Security authentication object
     * @return the actual player ID from the database, or a stable hash as fallback
     */
    public Long extract(Authentication authentication) {
        String username = authentication.getName();
        
        // Try to find the actual player ID from the database
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            Optional<Player> playerOpt = playerRepository.findByUser(userOpt.get());
            if (playerOpt.isPresent()) {
                long playerId = playerOpt.get().getId();
                log.debug("Extracted playerId {} for username {} from database", playerId, username);
                return playerId;
            }
        }
        
        // Fallback: for users without a player record, use a stable hash
        long hashId = Math.abs((long) username.hashCode()) % 100000L;
        log.debug("No player record for username {}, using hash-based playerId {}", username, hashId);
        return hashId;
    }
}
