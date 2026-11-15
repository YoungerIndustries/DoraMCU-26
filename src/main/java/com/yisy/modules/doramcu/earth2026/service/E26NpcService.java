package com.yisy.modules.doramcu.earth2026.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.model.E26Npc;
import com.yisy.modules.doramcu.earth2026.repository.E26NpcRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * Service for Earth 2026 NPC interactions.
 */
@Service
public class E26NpcService {

    private final E26NpcRepository repository;

    @Autowired
    public E26NpcService(E26NpcRepository repository) {
        this.repository = repository;
    }

    /**
     * Find all NPCs.
     */
    public List<E26Npc> findAll() {
        return repository.findAll();
    }

    /**
     * Find NPC by ID.
     */
    public Optional<E26Npc> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * Find NPCs by type.
     */
    public List<E26Npc> findByNpcType(String npcType) {
        return repository.findByNpcType(npcType);
    }

    /**
     * Find NPCs by occupation.
     */
    public List<E26Npc> findByOccupation(String occupation) {
        return repository.findByOccupation(occupation);
    }

    /**
     * Find NPCs by disposition.
     */
    public List<E26Npc> findByDisposition(String disposition) {
        return repository.findByDisposition(disposition);
    }

    /**
     * Find NPCs by type and disposition.
     */
    public List<E26Npc> findByNpcTypeAndDisposition(String npcType, String disposition) {
        return repository.findByNpcTypeAndDisposition(npcType, disposition);
    }

    /**
     * Find villain NPCs.
     */
    public List<E26Npc> findVillains() {
        return repository.findByIsVillainAndActive(true, true);
    }

    /**
     * Find ally NPCs.
     */
    public List<E26Npc> findAllies() {
        return repository.findByIsAllyAndActive(true, true);
    }

    /**
     * Find NPCs by threat level range.
     */
    public List<E26Npc> findByThreatLevelRange(int minTL, int maxTL) {
        return repository.findByThreatLevelRange(minTL, maxTL);
    }

    /**
     * Find NPCs by disposition score range.
     */
    public List<E26Npc> findByDispositionScoreRange(int minScore, int maxScore) {
        return repository.findByDispositionScoreRange(minScore, maxScore);
    }

    /**
     * Save an NPC.
     */
    public E26Npc save(E26Npc npc) {
        return repository.save(npc);
    }
}
