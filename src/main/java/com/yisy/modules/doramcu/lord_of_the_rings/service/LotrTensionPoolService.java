package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrTensionPool;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrTensionPoolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Tension Pools.
 * <p>
 * Tension pools track narrative tension in a campaign. As tension builds,
 * the world becomes more dangerous. When tension reaches maximum, a crisis occurs.
 * </p>
 */
@Service
public class LotrTensionPoolService {

    private static final Logger log = LoggerFactory.getLogger(LotrTensionPoolService.class);

    private final LotrTensionPoolRepository tensionPoolRepository;

    public LotrTensionPoolService(LotrTensionPoolRepository tensionPoolRepository) {
        this.tensionPoolRepository = tensionPoolRepository;
    }

    /**
     * Get all tension pools.
     */
    @Transactional(readOnly = true)
    public List<LotrTensionPool> findAll() {
        return tensionPoolRepository.findAll();
    }

    /**
     * Get tension pool for a campaign.
     */
    @Transactional(readOnly = true)
    public Optional<LotrTensionPool> findByCampaignId(Long campaignId) {
        return tensionPoolRepository.findAll().stream()
                .filter(tp -> tp.getCampaignId().equals(campaignId) && tp.isActive())
                .findFirst();
    }

    /**
     * Get tension pool by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrTensionPool> findById(Long id) {
        return tensionPoolRepository.findById(id);
    }

    /**
     * Create or get tension pool for a campaign.
     */
    @Transactional
    public LotrTensionPool createOrUpdate(Long campaignId, int maximum) {
        Optional<LotrTensionPool> existing = findByCampaignId(campaignId);
        if (existing.isPresent()) {
            LotrTensionPool tp = existing.get();
            tp.setMaximum(maximum);
            return tensionPoolRepository.save(tp);
        }

        LotrTensionPool tp = new LotrTensionPool();
        tp.setCampaignId(campaignId);
        tp.setCurrent(0);
        tp.setMaximum(maximum);
        tp.setActive(true);

        LotrTensionPool saved = tensionPoolRepository.save(tp);
        log.info("Created tension pool for campaign {}", campaignId);
        return saved;
    }

    /**
     * Increase tension in the pool.
     */
    @Transactional
    public String increaseTension(Long campaignId, int amount) {
        LotrTensionPool tp = getOrCreateTensionPool(campaignId);

        int newCurrent = Math.min(tp.getMaximum(), tp.getCurrent() + amount);
        int added = newCurrent - tp.getCurrent();
        tp.setCurrent(newCurrent);
        tensionPoolRepository.save(tp);

        if (newCurrent >= tp.getMaximum()) {
            return String.format("Tension maxed out! +{} points. A crisis is imminent!", added);
        }
        return String.format("Tension increases by {}. Current: {}/{}", added, tp.getCurrent(), tp.getMaximum());
    }

    /**
     * Decrease tension in the pool.
     */
    @Transactional
    public String decreaseTension(Long campaignId, int amount) {
        LotrTensionPool tp = getOrCreateTensionPool(campaignId);

        int newCurrent = Math.max(0, tp.getCurrent() - amount);
        int reduced = tp.getCurrent() - newCurrent;
        tp.setCurrent(newCurrent);
        tensionPoolRepository.save(tp);

        return String.format("Tension decreases by {}. Current: {}/{}", reduced, tp.getCurrent(), tp.getMaximum());
    }

    /**
     * Reset tension pool.
     */
    @Transactional
    public String resetTension(Long campaignId) {
        LotrTensionPool tp = getOrCreateTensionPool(campaignId);

        int previous = tp.getCurrent();
        tp.setCurrent(0);
        tensionPoolRepository.save(tp);

        return String.format("Tension reset from {} to 0.", previous);
    }

    /**
     * Get the current tension percentage.
     */
    @Transactional(readOnly = true)
    public double getTensionPercentage(Long campaignId) {
        LotrTensionPool tp = tensionPoolRepository.findAll().stream()
                .filter(t -> t.getCampaignId().equals(campaignId) && t.isActive())
                .findFirst()
                .orElse(null);

        if (tp == null || tp.getMaximum() == 0) return 0.0;
        return (double) tp.getCurrent() / tp.getMaximum() * 100.0;
    }

    /**
     * Delete tension pool.
     */
    @Transactional
    public void deleteTensionPool(Long id) {
        if (!tensionPoolRepository.existsById(id)) {
            throw new IllegalArgumentException("Tension pool not found: " + id);
        }
        tensionPoolRepository.deleteById(id);
        log.info("Deleted tension pool: {}", id);
    }

    // --- Helper Methods ---

    private LotrTensionPool getOrCreateTensionPool(Long campaignId) {
        Optional<LotrTensionPool> existing = findByCampaignId(campaignId);
        if (existing.isPresent()) {
            return existing.get();
        }

        LotrTensionPool tp = new LotrTensionPool();
        tp.setCampaignId(campaignId);
        tp.setCurrent(0);
        tp.setMaximum(10);
        tp.setActive(true);
        return tensionPoolRepository.save(tp);
    }
}
