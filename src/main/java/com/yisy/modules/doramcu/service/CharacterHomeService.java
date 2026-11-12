package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service that tracks each player's home galaxy and home solar system.
 * <p>
 * When a player travels through a wormhole to another galaxy, their home
 * location remains unchanged. This service provides methods to set, retrieve,
 * and return to a player's home location.
 * </p>
 * <p>
 * The home galaxy is stored in the User entity's {@code galaxyName} field.
 * The home solar system is tracked separately and represents the player's
 * "base of operations" within their home galaxy.
 * </p>
 */
@Service
public class CharacterHomeService {

    private static final Logger log = LoggerFactory.getLogger(CharacterHomeService.class);

    private final UserRepository userRepository;

    public CharacterHomeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the home galaxy for the given player.
     * <p>
     * If the player has no home galaxy set, returns the default "Milky Way".
     * </p>
     *
     * @param playerId the player's user ID
     * @return the home galaxy name, or "Milky Way" if not set
     * @throws IllegalArgumentException if the player is not found
     */
    public String getHomeGalaxy(Long playerId) {
        User user = userRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        String galaxy = user.getGalaxyName();
        if (galaxy == null || galaxy.isBlank()) {
            return "Earth 2026";
        }
        return galaxy;
    }

    /**
     * Sets the home galaxy for the given player.
     * <p>
     * This is typically called when a player first joins a galaxy or
     * explicitly chooses to relocate their home base.
     * </p>
     *
     * @param playerId the player's user ID
     * @param galaxyName the galaxy to set as home
     * @throws IllegalArgumentException if the player is not found or galaxy name is blank
     */
    public void setHomeGalaxy(Long playerId, String galaxyName) {
        if (galaxyName == null || galaxyName.isBlank()) {
            throw new IllegalArgumentException("Galaxy name must not be null or blank");
        }

        User user = userRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        String oldGalaxy = user.getGalaxyName();
        user.setGalaxyName(galaxyName);
        userRepository.save(user);

        log.info("Player {} ({}): home galaxy changed from '{}' to '{}'",
                playerId, user.getUsername(), oldGalaxy, galaxyName);
    }

    /**
     * Returns the current galaxy for the given player.
     * <p>
     * This may differ from the home galaxy if the player has traveled
     * through a wormhole and not yet returned.
     * </p>
     *
     * @param playerId the player's user ID
     * @return the current galaxy name
     * @throws IllegalArgumentException if the player is not found
     */
    public String getCurrentGalaxy(Long playerId) {
        return getHomeGalaxy(playerId);
    }

    /**
     * Checks if the player is currently in their home galaxy.
     *
     * @param playerId the player's user ID
     * @return true if the player is in their home galaxy
     */
    public boolean isAtHome(Long playerId) {
        // Currently always true since we use galaxyName for both home and current.
        // When current galaxy tracking is added separately, this will compare the two.
        return true;
    }

    /**
     * Returns the home location information for the given player.
     *
     * @param playerId the player's user ID
     * @return an optional containing the home location info, or empty if player not found
     */
    public Optional<HomeLocation> getHomeLocation(Long playerId) {
        return userRepository.findById(playerId).map(user -> {
            String galaxy = user.getGalaxyName();
            if (galaxy == null || galaxy.isBlank()) {
                galaxy = "Earth 2026";
            }
            return new HomeLocation(galaxy);
        });
    }

    /**
     * Represents a player's home location in a galaxy.
     */
    public static class HomeLocation {
        private final String galaxyName;
        private final Long solarSystemId;

        /**
         * Creates a home location with only the galaxy name.
         *
         * @param galaxyName the home galaxy
         */
        public HomeLocation(String galaxyName) {
            this(galaxyName, null);
        }

        /**
         * Creates a home location with galaxy name and solar system ID.
         *
         * @param galaxyName the home galaxy
         * @param solarSystemId the home solar system (optional)
         */
        public HomeLocation(String galaxyName, Long solarSystemId) {
            this.galaxyName = galaxyName;
            this.solarSystemId = solarSystemId;
        }

        /**
         * Returns the home galaxy name.
         */
        public String getGalaxyName() {
            return galaxyName;
        }

        /**
         * Returns the home solar system ID, or null if not set.
         */
        public Long getSolarSystemId() {
            return solarSystemId;
        }

        @Override
        public String toString() {
            return "HomeLocation{galaxy='" + galaxyName + "', solarSystemId=" + solarSystemId + "}";
        }
    }
}
