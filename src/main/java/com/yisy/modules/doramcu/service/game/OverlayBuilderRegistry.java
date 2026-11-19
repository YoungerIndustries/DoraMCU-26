package com.yisy.modules.doramcu.service.game;
import com.yisy.modules.doramcu.gamecore.AiNpcPopulator;
import com.yisy.modules.doramcu.gamecore.GameOverlayBuilder;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry that maps {@link GameType} to {@link GameOverlayBuilder} and {@link AiNpcPopulator}.
 * <p>
 * Game modules register their overlay builders and NPC populators at startup.
 * The {@link com.yisy.modules.doramcu.config.MultiGalaxyBuildQueue} uses this registry
 * to look up game-specific builders for each galaxy's {@link GameType}.
 * </p>
 * <p>
 * Auto-registers all builders and populators found in the Spring context at {@code @PostConstruct}.
 * </p>
 */
@Component
public class OverlayBuilderRegistry {

    private static final Logger log = LoggerFactory.getLogger(OverlayBuilderRegistry.class);

    private final Map<GameType, GameOverlayBuilder> overlayBuilders = new HashMap<>();
    private final Map<GameType, AiNpcPopulator> npcPopulators = new HashMap<>();

    private final List<GameOverlayBuilder> allBuilders;
    private final List<AiNpcPopulator> allPopulators;

    /**
     * Constructor injects all registered builders and populators from Spring.
     *
     * @param allBuilders   list of all GameOverlayBuilder beans (may be empty)
     * @param allPopulators list of all AiNpcPopulator beans (may be empty)
     */
    public OverlayBuilderRegistry(
            List<GameOverlayBuilder> allBuilders,
            List<AiNpcPopulator> allPopulators) {
        this.allBuilders = allBuilders != null ? allBuilders : List.of();
        this.allPopulators = allPopulators != null ? allPopulators : List.of();
    }

    @PostConstruct
    public void registerAll() {
        for (GameOverlayBuilder builder : allBuilders) {
            registerBuilder(builder);
        }
        for (AiNpcPopulator populator : allPopulators) {
            registerPopulator(populator);
        }
        log.info("OverlayBuilderRegistry initialized with {} overlay builders and {} NPC populators",
                overlayBuilders.size(), npcPopulators.size());
    }

    /**
     * Registers a game overlay builder for a specific game type.
     *
     * @param builder the builder to register
     */
    public void registerBuilder(GameOverlayBuilder builder) {
        GameType gameType = builder.getGameType();
        overlayBuilders.put(gameType, builder);
        log.info("Registered overlay builder: {} for game type: {}", builder.getClass().getSimpleName(), gameType);
    }

    /**
     * Registers an AI NPC populator for a specific game type.
     *
     * @param populator the populator to register
     */
    public void registerPopulator(AiNpcPopulator populator) {
        GameType gameType = populator.getGameType();
        npcPopulators.put(gameType, populator);
        log.info("Registered NPC populator: {} for game type: {}", populator.getClass().getSimpleName(), gameType);
    }

    /**
     * Looks up the overlay builder for a game type.
     *
     * @param gameType the game type
     * @return optional overlay builder
     */
    public Optional<GameOverlayBuilder> getOverlayBuilder(GameType gameType) {
        return Optional.ofNullable(overlayBuilders.get(gameType));
    }

    /**
     * Looks up the NPC populator for a game type.
     *
     * @param gameType the game type
     * @return optional NPC populator
     */
    public Optional<AiNpcPopulator> getNpcPopulator(GameType gameType) {
        return Optional.ofNullable(npcPopulators.get(gameType));
    }

    /**
     * Returns whether an overlay builder is registered for the given game type.
     *
     * @param gameType the game type
     * @return true if a builder is registered
     */
    public boolean hasOverlayBuilder(GameType gameType) {
        return overlayBuilders.containsKey(gameType);
    }

    /**
     * Returns whether an NPC populator is registered for the given game type.
     *
     * @param gameType the game type
     * @return true if a populator is registered
     */
    public boolean hasNpcPopulator(GameType gameType) {
        return npcPopulators.containsKey(gameType);
    }
}
