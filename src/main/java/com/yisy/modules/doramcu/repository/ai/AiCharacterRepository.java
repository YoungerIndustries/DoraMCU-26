package com.yisy.modules.doramcu.repository.ai;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.model.misc.CharacterType;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiCharacterRepository extends JpaRepository<AiCharacter, Long> {

    Optional<AiCharacter> findByName(String name);

    List<AiCharacter> findByEmpireName(String empireName);

    List<AiCharacter> findByActive(boolean active);

    /**
     * Find hirable AI characters.
     */
    List<AiCharacter> findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus status);

    /**
     * Find AI characters by role.
     */
    List<AiCharacter> findByRole(String role);

    /**
     * Find AI characters with minimum experience.
     */
    List<AiCharacter> findByTotalExperienceGreaterThanEqualAndHirableTrue(int minExperience);

    /**
     * Count hirable AI characters.
     */
    long countByHirableTrue();

    /**
     * Find AI admin tester characters.
     */
    java.util.List<AiCharacter> findByAiAdminTrue();

    /**
     * Find AI admin characters that are active.
     */
    java.util.List<AiCharacter> findByAiAdminTrueAndActiveTrue();

    /**
     * Find AI admin characters that are active, filtered by empire name.
     */
    java.util.List<AiCharacter> findByAiAdminTrueAndActiveTrueAndEmpireName(String empireName);

    /**
     * Find available AI admin characters (active, idle with no current order).
     */
    java.util.List<AiCharacter> findByAiAdminTrueAndActiveTrueAndCurrentOrderIdIsNull();

    /**
     * Find available AI admin characters for a specific empire that are idle.
     */
    java.util.List<AiCharacter> findByAiAdminTrueAndActiveTrueAndEmpireNameAndCurrentOrderIdIsNull(
            String empireName);

    /**
     * Count AI admins with active orders (currentOrderId is NOT null).
     */
    long countByAiAdminTrueAndActiveTrueAndCurrentOrderIdNotNull();

    /**
     * Find AI characters by role that are active, ordered by total experience descending.
     */
    List<AiCharacter> findByRoleAndActiveTrueOrderByTotalExperienceDesc(String role);

    // --- Entity-linked AI character queries ---

    /**
     * Find AI characters by character type (SHIP, STATION, VEHICLE, FACILITY).
     */
    List<AiCharacter> findByCharacterType(AiCharacter.CharacterType characterType);

    /**
     * Find AI characters by character type that are active.
     */
    List<AiCharacter> findByCharacterTypeAndActiveTrue(AiCharacter.CharacterType characterType);

    /**
     * Find the AI character linked to a specific ship.
     */
    Optional<AiCharacter> findByCharacterTypeAndLinkedShipId(AiCharacter.CharacterType characterType, Long shipId);

    /**
     * Find the AI character linked to a specific station.
     */
    Optional<AiCharacter> findByCharacterTypeAndLinkedStationId(AiCharacter.CharacterType characterType, Long stationId);

    /**
     * Find the AI character linked to a specific vehicle.
     */
    Optional<AiCharacter> findByCharacterTypeAndLinkedVehicleId(AiCharacter.CharacterType characterType, Long vehicleId);

    /**
     * Find the AI character linked to a specific facility.
     */
    Optional<AiCharacter> findByCharacterTypeAndLinkedFacilityId(AiCharacter.CharacterType characterType, Long facilityId);

    /**
     * Find the AI character linked to a specific entity ID string (e.g., GroundVehicle string ID).
     */
    Optional<AiCharacter> findByCharacterTypeAndLinkedEntityId(AiCharacter.CharacterType characterType, String entityId);

    /**
     * Find all AI characters linked to entities in a given empire (active only).
     */
    List<AiCharacter> findByCharacterTypeInAndEmpireNameAndActiveTrue(
            java.util.List<AiCharacter.CharacterType> characterTypes, String empireName);

    /**
     * Find all AI characters linked to entities in a given empire (inactive only).
     */
    List<AiCharacter> findByCharacterTypeInAndEmpireNameAndActiveFalse(
            java.util.List<AiCharacter.CharacterType> characterTypes, String empireName);

    /**
     * Find all AI characters by character type (any empire, any active state).
     */
    List<AiCharacter> findByCharacterTypeIn(
            java.util.List<AiCharacter.CharacterType> characterTypes);

    /**
     * Find AI characters excluding entity-linked types (SHIP, STATION, VEHICLE, FACILITY).
     * Used for admin dashboard player counts that should only count human-like AI characters.
     */
    List<AiCharacter> findByCharacterTypeNotIn(
            java.util.List<AiCharacter.CharacterType> characterTypes);

    // --- 3D spatial position queries for AI admin visibility ---

    /**
     * Find all active AI admins in a specific galaxy.
     */
    List<AiCharacter> findByAiAdminTrueAndActiveTrueAndGalaxyName(String galaxyName);

    /**
     * Find active AI admins in a specific galaxy within a 3D position range (proximity query).
     */
    List<AiCharacter> findByAiAdminTrueAndActiveTrueAndGalaxyNameAndPosXBetweenAndPosYBetweenAndPosZBetween(
            String galaxyName, double minX, double maxX, double minY, double maxY, double minZ, double maxZ);

    /**
     * Find all AI characters (including non-admins) in a specific galaxy within a 3D position range.
     */
    List<AiCharacter> findByGalaxyNameAndPosXBetweenAndPosYBetweenAndPosZBetween(
            String galaxyName, double minX, double maxX, double minY, double maxY, double minZ, double maxZ);

    /**
     * Find AI admins with a valid 3D position set (all three coordinates not null).
     */
    @Query("SELECT ac FROM AiCharacter ac WHERE ac.aiAdmin = true AND ac.active = true AND ac.posX IS NOT NULL AND ac.posY IS NOT NULL AND ac.posZ IS NOT NULL")
    List<AiCharacter> findByAiAdminTrueAndActiveTrueAndPositionNotNull();

    /**
     * Find AI admins in a galaxy with a valid 3D position set.
     */
    @Query("SELECT ac FROM AiCharacter ac WHERE ac.aiAdmin = true AND ac.active = true AND ac.galaxyName = :galaxyName AND ac.posX IS NOT NULL AND ac.posY IS NOT NULL AND ac.posZ IS NOT NULL")
    List<AiCharacter> findByAiAdminTrueAndActiveTrueAndGalaxyNameAndPositionNotNull(@Param("galaxyName") String galaxyName);

    /**
     * Find idle AI admins (no current order) with a valid 3D position set.
     * Used by the idle movement service to find admins eligible for position updates.
     */
    @Query("SELECT ac FROM AiCharacter ac WHERE ac.aiAdmin = true AND ac.active = true AND ac.currentOrderId IS NULL AND ac.posX IS NOT NULL AND ac.posY IS NOT NULL AND ac.posZ IS NOT NULL")
    List<AiCharacter> findByAiAdminTrueAndActiveTrueAndCurrentOrderIdIsNullAndPositionNotNull();

    /**
     * Find all active AI admins ordered by total experience descending for leaderboard ranking.
     */
    List<AiCharacter> findByAiAdminTrueAndActiveTrueOrderByTotalExperienceDesc();
}
