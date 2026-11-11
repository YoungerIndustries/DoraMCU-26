package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.service.Earth2026Service;
import com.yisy.modules.doramcu.gateway.PluginGateway;
import com.yisy.modules.doramcu.gateway.PluginDescriptor;
import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.model.PlayerCharacterRegistry;
import com.yisy.modules.doramcu.model.CharacterType;
import com.yisy.modules.doramcu.model.CharacterRole;
import com.yisy.modules.doramcu.repository.PlayerCharacterRegistryRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.unified.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for handling galaxy transitions from Earth 2026 to other game galaxies.
 * <p>
 * When a character on Earth chooses to transition to a game galaxy, this service
 * orchestrates the process: loading the target game's plugin via the Plugin Gateway,
 * creating or assigning the starting character for that game, updating the
 * PlayerCharacterRegistry, and marking the character as transitioned.
 * </p>
 */
@Service
public class GalaxyTransitionService {

    private static final Logger log = LoggerFactory.getLogger(GalaxyTransitionService.class);

    private final Earth2026Service earth2026Service;
    private final PlayerCharacterRegistryRepository registryRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final PluginGateway pluginGateway;

    public GalaxyTransitionService(Earth2026Service earth2026Service,
                                    PlayerCharacterRegistryRepository registryRepository,
                                    PlayerRepository playerRepository,
                                    UserRepository userRepository,
                                    PluginGateway pluginGateway) {
        this.earth2026Service = earth2026Service;
        this.registryRepository = registryRepository;
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
        this.pluginGateway = pluginGateway;
    }

    /**
     * Handles the complete transition flow for a character leaving Earth 2026.
     * <p>
     * Steps:
     * 1. Validate the character has a target galaxy
     * 2. Load the target game's plugin via the Plugin Gateway
     * 3. Create or update the PlayerCharacterRegistry entry
     * 4. Mark the Earth 2026 character as transitioned
     * 5. Update the User's galaxyName
     * </p>
     *
     * @param characterId the Earth 2026 character ID
     * @return the transitioned character
     */
    @Transactional
    public Earth2026Character transitionToGalaxy(Long characterId) {
        Earth2026Character character = earth2026Service.getCharacter(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        if (character.isHasTransitioned()) {
            throw new IllegalStateException("Character has already transitioned");
        }

        String targetGalaxy = character.getTargetGalaxy();
        if (targetGalaxy == null || targetGalaxy.trim().isEmpty()) {
            throw new IllegalStateException("No target galaxy selected");
        }

        log.info("Transitioning character {} from Earth 2026 to galaxy: {}", character.getName(), targetGalaxy);

        // Load the target game's plugin via the Plugin Gateway
        pluginGateway.getGameTypeForGalaxy(targetGalaxy).ifPresent(gameType -> {
            pluginGateway.loadPlugin(gameType);
            log.info("Loaded plugin {} for galaxy transition to {}", gameType, targetGalaxy);
        });

        // Determine the character type based on the target galaxy's plugin
        CharacterType charType = pluginGateway.getGameTypeForGalaxy(targetGalaxy)
                .map(this::mapGameTypeToCharacterType)
                .orElse(CharacterType.EARTH_2026);

        // Complete the transition in Earth2026Service
        earth2026Service.completeTransition(characterId);

        // Create registry entry linking the character to the target galaxy
        PlayerCharacterRegistry registry = new PlayerCharacterRegistry();
        if (character.getPlayerId() != null) {
            userRepository.findById(character.getPlayerId()).ifPresent(registry::setPlayer);
        }
        registry.setGalaxyName(targetGalaxy);
        registry.setCharacterType(charType);
        registry.setCharacterId(character.getId());
        registry.setCharacterName(character.getName());
        registry.setCharacterRole(CharacterRole.COMMANDER);
        registry.setHomeLocationId(character.getEarthLocation());
        registryRepository.save(registry);

        log.info("Character {} successfully transitioned to {} with plugin loaded", character.getName(), targetGalaxy);
        return character;
    }

    /**
     * Maps a GameType to a CharacterType for the PlayerCharacterRegistry.
     */
    private CharacterType mapGameTypeToCharacterType(GameType gameType) {
        return switch (gameType) {
            case TRAVELLER -> CharacterType.TRAVELLER;
            case CAPTAINS_LOG -> CharacterType.CAPTAINS_LOG;
            case DND_5E -> CharacterType.DND_5E;
            case EARTH_2026 -> CharacterType.EARTH_2026;
            default -> CharacterType.EARTH_2026;
        };
    }

    /**
     * Lists available game galaxies for transition.
     * <p>
     * The descriptions include the plugin that will be loaded for each galaxy.
     * </p>
     *
     * @return map of galaxy names to descriptions
     */
    public Map<String, String> getAvailableGalaxies() {
        return Map.of(
                GalaxyFactory.GAME_GALAXY_NAME, "Milky Way — Human/Interstellar Alliance (DoraMCU 4X plugin)",
                GalaxyFactory.VOID_GALAXY_NAME, "The Void — Alien races with AI empires (Traveller plugin)",
                GalaxyFactory.TEST_GALAXY_NAME, "Test Galaxy — Small galaxy for model development (DoraMCU 4X plugin)"
        );
    }

    /**
     * Lists available plugins that can be loaded via galaxy transition.
     *
     * @return list of plugin descriptors
     */
    public java.util.List<PluginDescriptor> getAvailablePlugins() {
        return pluginGateway.getAvailablePlugins();
    }
}
