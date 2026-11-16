package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.HqPortal;
import com.yisy.modules.doramcu.earth2026.repository.HqPortalRepository;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing portal rooms within the Younger Industries HQ.
 * Each portal connects to a different game module galaxy.
 */
@Service
public class HqPortalService {

    private static final Logger log = LoggerFactory.getLogger(HqPortalService.class);

    private final HqPortalRepository hqPortalRepository;

    public HqPortalService(HqPortalRepository hqPortalRepository) {
        this.hqPortalRepository = hqPortalRepository;
    }

    /**
     * Get all portals for a specific HQ.
     */
    @Transactional(readOnly = true)
    public List<HqPortal> getPortalsByHq(Long hqId) {
        return hqPortalRepository.findByHqId(hqId);
    }

    /**
     * Get only active portals for a specific HQ.
     */
    @Transactional(readOnly = true)
    public List<HqPortal> getActivePortalsByHq(Long hqId) {
        return hqPortalRepository.findByHqIdAndActiveTrue(hqId);
    }

    /**
     * Get a specific portal by HQ and game type.
     */
    @Transactional(readOnly = true)
    public Optional<HqPortal> getPortalByGameType(Long hqId, GameType gameType) {
        return hqPortalRepository.findByHqIdAndGameType(hqId, gameType);
    }

    /**
     * Get all active portals across all HQs.
     */
    @Transactional(readOnly = true)
    public List<HqPortal> getAllActivePortals() {
        return hqPortalRepository.findByActiveTrue();
    }

    /**
     * Get a portal by ID.
     */
    @Transactional(readOnly = true)
    public Optional<HqPortal> getPortalById(Long id) {
        return hqPortalRepository.findById(id);
    }

    /**
     * Create a new portal.
     */
    @Transactional
    public HqPortal createPortal(HqPortal portal) {
        portal.setCreatedAt(LocalDateTime.now());
        portal.setUpdatedAt(LocalDateTime.now());
        log.info("Created HQ portal: {} -> {}", portal.getPortalName(), portal.getGameType());
        return hqPortalRepository.save(portal);
    }

    /**
     * Update an existing portal.
     */
    @Transactional
    public Optional<HqPortal> updatePortal(Long id, HqPortal updated) {
        return hqPortalRepository.findById(id).map(existing -> {
            existing.setPortalName(updated.getPortalName());
            existing.setDescription(updated.getDescription());
            existing.setBasementLevel(updated.getBasementLevel());
            existing.setActive(updated.isActive());
            existing.setRequiresLevel(updated.getRequiresLevel());
            existing.setRequiresClearance(updated.getRequiresClearance());
            existing.setPortalIcon(updated.getPortalIcon());
            existing.setUpdatedAt(LocalDateTime.now());
            log.info("Updated HQ portal: {}", existing.getPortalName());
            return hqPortalRepository.save(existing);
        });
    }

    /**
     * Toggle the active state of a portal.
     */
    @Transactional
    public Optional<HqPortal> togglePortalActive(Long id) {
        return hqPortalRepository.findById(id).map(portal -> {
            portal.setActive(!portal.isActive());
            portal.setUpdatedAt(LocalDateTime.now());
            log.info("Toggled HQ portal {} active state to: {}", portal.getPortalName(), portal.isActive());
            return hqPortalRepository.save(portal);
        });
    }

    /**
     * Delete a portal.
     */
    @Transactional
    public boolean deletePortal(Long id) {
        return hqPortalRepository.findById(id).map(portal -> {
            log.info("Deleting HQ portal: {}", portal.getPortalName());
            hqPortalRepository.delete(portal);
            return true;
        }).orElse(false);
    }
}
