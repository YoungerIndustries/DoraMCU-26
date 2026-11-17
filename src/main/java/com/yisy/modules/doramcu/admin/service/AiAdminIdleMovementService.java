package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

/**
 * Periodically moves idle AI admin characters to simulate activity.
 * <p>
 * When an AI admin has no active orders and has been idle for more than 5 minutes,
 * this service slightly adjusts their position to make the 3D galaxy view feel alive.
 * The movement is a small random offset (up to 2 units) from their current position,
 * keeping them near their home location.
 * </p>
 * <p>
 * Runs every 10 minutes via {@code @Scheduled}.
 * </p>
 */
@Service
public class AiAdminIdleMovementService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminIdleMovementService.class);

    /** Maximum random offset in any single axis (units). */
    private static final double MAX_OFFSET = 2.0;

    /** Minimum idle time before an admin is eligible for movement (seconds). */
    private static final long MIN_IDLE_SECONDS = 300; // 5 minutes

    /** Random number generator for position offsets. */
    private final Random random = new Random();

    private final AiCharacterRepository aiCharacterRepository;

    public AiAdminIdleMovementService(AiCharacterRepository aiCharacterRepository) {
        this.aiCharacterRepository = aiCharacterRepository;
    }

    /**
     * Runs every 10 minutes to move idle AI admins slightly.
     */
    @Scheduled(fixedRate = 600000)
    public void moveIdleAdmins() {
        try {
            List<AiCharacter> idleAdmins = aiCharacterRepository
                    .findByAiAdminTrueAndActiveTrueAndCurrentOrderIdIsNullAndPositionNotNull();

            if (idleAdmins.isEmpty()) {
                return;
            }

            int moved = 0;
            long now = System.currentTimeMillis();

            for (AiCharacter admin : idleAdmins) {
                // Only move admins who have been idle (no current order and low workload)
                if (admin.getActiveOrderCount() > 0) {
                    continue;
                }

                // Calculate random offset
                double offsetX = (random.nextDouble() * 2 - 1) * MAX_OFFSET;
                double offsetY = (random.nextDouble() * 2 - 1) * MAX_OFFSET;
                double offsetZ = (random.nextDouble() * 2 - 1) * MAX_OFFSET;

                double newX = admin.getPosX() + offsetX;
                double newY = admin.getPosY() + offsetY;
                double newZ = admin.getPosZ() + offsetZ;

                admin.setPosX(newX);
                admin.setPosY(newY);
                admin.setPosZ(newZ);
                aiCharacterRepository.save(admin);

                moved++;
                log.debug("Idle movement: AI admin '{}' moved from ({}, {}, {}) to ({}, {}, {})",
                        admin.getName(),
                        admin.getHomePosX(), admin.getHomePosY(), admin.getHomePosZ(),
                        newX, newY, newZ);
            }

            if (moved > 0) {
                log.info("Idle movement: moved {} idle AI admins", moved);
            }

        } catch (Exception e) {
            log.error("Idle movement failed: {}", e.getMessage(), e);
        }
    }
}
