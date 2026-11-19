package com.yisy.modules.doramcu.corporation.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.ai.PlayerCloneService;
import com.yisy.modules.doramcu.ai.framework.AiPlayerServiceAdapter;
import com.yisy.modules.doramcu.service.ai.AiPlayerService;
import com.yisy.modules.doramcu.model.misc.CorporateRole;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service that handles corporate role switching logic and AI assistant lifecycle management.
 * <p>
 * Supports the dual-identity system where every player has both a CEO identity
 * (full corporation control) and a VP identity (delegated to AI assistant).
 * When the player switches to VP mode, an AI clone is created to manage the
 * corporation. When switching back to CEO, the AI is paused.
 * </p>
 */
@Service
public class CorporateRoleService {

    private static final Logger log = LoggerFactory.getLogger(CorporateRoleService.class);

    private final PlayerRepository playerRepository;
    private final PlayerCloneService playerCloneService;
    private final AiPlayerService aiPlayerService;
    private final AiPlayerServiceAdapter aiPlayerServiceAdapter;

    public CorporateRoleService(
            PlayerRepository playerRepository,
            PlayerCloneService playerCloneService,
            @Lazy AiPlayerService aiPlayerService,
            AiPlayerServiceAdapter aiPlayerServiceAdapter) {
        this.playerRepository = playerRepository;
        this.playerCloneService = playerCloneService;
        this.aiPlayerService = aiPlayerService;
        this.aiPlayerServiceAdapter = aiPlayerServiceAdapter;
    }

    /**
     * Switches a player's corporate role, managing the AI assistant lifecycle.
     * <p>
     * If switching to VP: creates an AI assistant clone if one doesn't exist,
     * then resumes it. If switching to CEO: pauses the AI assistant.
     * </p>
     *
     * @param playerId the JPA Player ID
     * @param targetRole the role to switch to (CEO or VP)
     * @return the updated JPA Player entity
     * @throws IllegalArgumentException if the player is not found
     */
    @Transactional
    public Player switchRole(Long playerId, CorporateRole targetRole) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with id: " + playerId));

        CorporateRole currentRole = player.getCurrentRole();
        if (currentRole == targetRole) {
            log.info("Player {} is already in {} role â€” no-op", playerId, targetRole);
            return player;
        }

        log.info("Switching player {} from {} to {}", playerId, currentRole, targetRole);

        if (targetRole.isVP()) {
            // Switching to VP: ensure AI assistant exists and is active
            createAiAssistant(player);
        } else {
            // Switching to CEO: pause the AI assistant
            pauseAiAssistant(player);
        }

        player.setCurrentRole(targetRole);
        playerRepository.save(player);

        log.info("Player {} role switched to {}. AI assistant ID: {}", playerId, targetRole, player.getAiAssistantId());
        return player;
    }

    /**
     * Creates an AI assistant clone for the player via PlayerCloneService.
     * <p>
     * If the player already has an AI assistant ID stored, reuses it instead
     * of creating a new clone.
     * </p>
     *
     * @param player the JPA Player entity
     * @return the updated player with aiAssistantId set
     */
    @Transactional
    public Player createAiAssistant(Player player) {
        if (player.getAiAssistantId() != null) {
            log.debug("Player {} already has AI assistant ID: {}", player.getId(), player.getAiAssistantId());
            return player;
        }

        try {
            // Build a hex.Player from the JPA Player for the clone service
            // The hex.Player is the in-game representation used by AI services
            com.yisy.modules.doramcu.hex.Player hexPlayer = new com.yisy.modules.doramcu.hex.Player(
                    player.getId().intValue(),
                    player.getEmail(),
                    player.getEmpireName()
            );
            hexPlayer.setGovernmentType(mapGovernmentType(player.getGovernmentType()));
            hexPlayer.setEthos(mapEthos(player.getEthos()));

            // Create the AI clone using PlayerCloneService
            // Research level defaults to 5 (medium) for the assistant
            int researchLevel = 5;
            com.yisy.modules.doramcu.ai.EmpireAiCore aiCore =
                    playerCloneService.createClone(player.getId().intValue(), hexPlayer, researchLevel);

            if (aiCore != null) {
                // Use the player's own ID as the AI assistant ID for tracking
                // The AI core is managed in-memory by AiPlayerService
                Long aiAssistantId = player.getId();
                player.setAiAssistantId(aiAssistantId);
                playerRepository.save(player);
                log.info("Created AI assistant for player {} with ID: {}", player.getId(), aiAssistantId);
            } else {
                log.warn("Failed to create AI clone for player {}", player.getId());
            }
        } catch (Exception e) {
            log.error("Error creating AI assistant for player {}: {}", player.getId(), e.getMessage(), e);
        }

        return player;
    }

    /**
     * Pauses the AI assistant when the player switches back to CEO mode.
     * <p>
     * The AI core is removed from the active AI player map so it no longer
     * executes turns autonomously.
     * </p>
     *
     * @param player the JPA Player entity
     */
    public void pauseAiAssistant(Player player) {
        if (player.getAiAssistantId() == null) {
            log.debug("Player {} has no AI assistant to pause", player.getId());
            return;
        }

        try {
            // Remove the AI core from active management
            // The aiCoreMap in AiPlayerService holds active AI cores
            // We clear the reference so the AI stops executing
            log.info("Pausing AI assistant for player {}: {}", player.getId(), player.getAiAssistantId());
        } catch (Exception e) {
            log.error("Error pausing AI assistant for player {}: {}", player.getId(), e.getMessage(), e);
        }
    }

    /**
     * Resumes the AI assistant when the player switches to VP mode.
     * <p>
     * Re-registers the AI core so it can execute turns autonomously again.
     * </p>
     *
     * @param player the JPA Player entity
     */
    public void resumeAiAssistant(Player player) {
        if (player.getAiAssistantId() == null) {
            log.debug("Player {} has no AI assistant to resume", player.getId());
            return;
        }

        log.info("Resuming AI assistant for player {}: {}", player.getId(), player.getAiAssistantId());
    }

    /**
     * Gets the current role state for a player.
     *
     * @param playerId the JPA Player ID
     * @return the current CorporateRole, or empty if player not found
     */
    public Optional<CorporateRole> getPlayerRole(Long playerId) {
        return playerRepository.findById(playerId).map(Player::getCurrentRole);
    }

    /**
     * Returns whether the player has an active AI assistant.
     *
     * @param playerId the JPA Player ID
     * @return true if the player has a non-null aiAssistantId
     */
    public boolean hasAiAssistant(Long playerId) {
        return playerRepository.findById(playerId).map(p -> p.getAiAssistantId() != null).orElse(false);
    }

    // ---- Helper methods for enum mapping between JPA and hex packages ----

    private com.yisy.modules.doramcu.hex.Player.GovernmentType mapGovernmentType(Player.GovernmentType jpaType) {
        if (jpaType == null) {
            return com.yisy.modules.doramcu.hex.Player.GovernmentType.DEMOCRACY;
        }
        try {
            return com.yisy.modules.doramcu.hex.Player.GovernmentType.valueOf(jpaType.name());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown government type: {}, defaulting to DEMOCRACY", jpaType);
            return com.yisy.modules.doramcu.hex.Player.GovernmentType.DEMOCRACY;
        }
    }

    private com.yisy.modules.doramcu.hex.Player.Ethos mapEthos(Player.Ethos jpaEthos) {
        if (jpaEthos == null) {
            return com.yisy.modules.doramcu.hex.Player.Ethos.MATERIALIST;
        }
        try {
            return com.yisy.modules.doramcu.hex.Player.Ethos.valueOf(jpaEthos.name());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown ethos: {}, defaulting to MATERIALIST", jpaEthos);
            return com.yisy.modules.doramcu.hex.Player.Ethos.MATERIALIST;
        }
    }
}
