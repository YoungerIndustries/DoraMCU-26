package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.PortalTravel;
import com.yisy.modules.doramcu.earth2026.model.TravelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortalTravelRepository extends JpaRepository<PortalTravel, Long> {

    @Query("SELECT pt FROM PortalTravel pt WHERE pt.characterId = :characterId ORDER BY pt.traveledAt DESC")
    List<PortalTravel> findByCharacterId(@Param("characterId") Long characterId);

    @Query("SELECT pt FROM PortalTravel pt WHERE pt.characterId = :characterId AND pt.portalId = :portalId ORDER BY pt.traveledAt DESC")
    List<PortalTravel> findByCharacterIdAndPortalId(@Param("characterId") Long characterId, @Param("portalId") Long portalId);

    @Query("SELECT pt FROM PortalTravel pt WHERE pt.characterId = :characterId AND pt.portalId = :portalId AND pt.cooldownExpiresAt > :now ORDER BY pt.traveledAt DESC")
    Optional<PortalTravel> findActiveCooldown(@Param("characterId") Long characterId, @Param("portalId") Long portalId, @Param("now") LocalDateTime now);

    @Query("SELECT pt FROM PortalTravel pt WHERE pt.corporationId = :corporationId ORDER BY pt.traveledAt DESC")
    List<PortalTravel> findByCorporationId(@Param("corporationId") Long corporationId);

    @Query("SELECT pt FROM PortalTravel pt WHERE pt.portalId = :portalId ORDER BY pt.traveledAt DESC")
    List<PortalTravel> findByPortalId(@Param("portalId") Long portalId);

    @Query("SELECT pt FROM PortalTravel pt WHERE pt.status = :status ORDER BY pt.traveledAt DESC")
    List<PortalTravel> findByStatus(@Param("status") TravelStatus status);

    @Query("SELECT pt FROM PortalTravel pt WHERE pt.cooldownExpiresAt IS NOT NULL AND pt.cooldownExpiresAt < :now")
    List<PortalTravel> findExpiredCooldowns(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(pt) FROM PortalTravel pt WHERE pt.characterId = :characterId")
    long countByCharacterId(@Param("characterId") Long characterId);
}
