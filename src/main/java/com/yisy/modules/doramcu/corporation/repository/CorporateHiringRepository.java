package com.yisy.modules.doramcu.corporation.repository;

import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorporateHiringRepository extends JpaRepository<CorporateHiring, Long> {

    /**
     * Find all active hires for a corporation.
     */
    List<CorporateHiring> findByCorporationIdAndStatus(Long corporationId, CorporateHiring.Status status);

    /**
     * Find a hiring record by AI character ID.
     */
    Optional<CorporateHiring> findByAiCharacterIdAndStatus(Long aiCharacterId, CorporateHiring.Status status);

    /**
     * Find hires by corporation and position.
     */
    List<CorporateHiring> findByCorporationIdAndPositionAndStatus(
            Long corporationId, CorporatePosition position, CorporateHiring.Status status);

    /**
     * Check if a position is already filled for a corporation.
     */
    boolean existsByCorporationIdAndPositionAndStatus(
            Long corporationId, CorporatePosition position, CorporateHiring.Status status);
}
