package com.yisy.modules.doramcu.service.ai;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for determining which AI admins are visible to a player
 * based on 3D proximity in the game world.
 */
@Service
public class AiAdminVisibilityService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminVisibilityService.class);
    
    /** Default proximity radius for "same space" detection (50 units) */
    public static final double DEFAULT_PROXIMITY_RADIUS = 50.0;

    private final AiCharacterRepository aiCharacterRepository;

    public AiAdminVisibilityService(AiCharacterRepository aiCharacterRepository) {
        this.aiCharacterRepository = aiCharacterRepository;
    }

    /**
     * Returns AI admins within the specified radius of a player's position.
     *
     * @param galaxyName the galaxy to search in
     * @param playerX player's X coordinate
     * @param playerY player's Y coordinate
     * @param playerZ player's Z coordinate
     * @param radius search radius in 3D units
     * @return list of AI admin info maps within range
     */
    public List<Map<String, Object>> getAiAdminsNearPlayer(String galaxyName, double playerX, double playerY, double playerZ, double radius) {
        if (galaxyName == null || galaxyName.isBlank()) {
            return List.of();
        }

        double minX = playerX - radius;
        double maxX = playerX + radius;
        double minY = playerY - radius;
        double maxY = playerY + radius;
        double minZ = playerZ - radius;
        double maxZ = playerZ + radius;

        List<AiCharacter> admins = aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndGalaxyNameAndPosXBetweenAndPosYBetweenAndPosZBetween(
                        galaxyName, minX, maxX, minY, maxY, minZ, maxZ);

        // Filter out admins without valid positions and compute exact distance
        return admins.stream()
                .filter(AiCharacter::hasPosition)
                .filter(admin -> admin.distanceTo(playerX, playerY, playerZ) <= radius)
                .map(admin -> toAdminInfo(admin, playerX, playerY, playerZ))
                .collect(Collectors.toList());
    }

    /**
     * Convenience method using the default proximity radius.
     */
    public List<Map<String, Object>> getAiAdminsInSameSpace(String galaxyName, double playerX, double playerY, double playerZ) {
        return getAiAdminsNearPlayer(galaxyName, playerX, playerY, playerZ, DEFAULT_PROXIMITY_RADIUS);
    }

    /**
     * Get all AI admins in a galaxy with their positions (for initial load).
     */
    public List<Map<String, Object>> getAllAiAdminsInGalaxy(String galaxyName) {
        if (galaxyName == null || galaxyName.isBlank()) {
            return List.of();
        }

        List<AiCharacter> admins = aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndGalaxyNameAndPositionNotNull(galaxyName);

        return admins.stream()
                .map(admin -> toAdminInfo(admin, 0, 0, 0))
                .collect(Collectors.toList());
    }

    private Map<String, Object> toAdminInfo(AiCharacter admin, double playerX, double playerY, double playerZ) {
        Map<String, Object> info = new java.util.HashMap<>();
        info.put("id", admin.getId());
        info.put("name", admin.getName());
        info.put("role", admin.getRole());
        info.put("empireName", admin.getEmpireName());
        info.put("galaxyName", admin.getGalaxyName());
        info.put("posX", admin.getPosX());
        info.put("posY", admin.getPosY());
        info.put("posZ", admin.getPosZ());
        info.put("characterType", admin.getCharacterType() != null ? admin.getCharacterType().name() : "HUMAN_LEADER");
        info.put("currentOrderId", admin.getCurrentOrderId());
        info.put("activeOrderCount", admin.getActiveOrderCount());
        
        double distance = admin.distanceTo(playerX, playerY, playerZ);
        info.put("distance", Math.round(distance * 100.0) / 100.0);
        
        return info;
    }
}
