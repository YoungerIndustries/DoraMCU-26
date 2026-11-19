package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCampaign;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCampaignCharacter;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacter;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrSession;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrCampaignCharacterRepository;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrCampaignRepository;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for Lord of the Rings campaign management.
 */
@Service
public class LotrCampaignService {

    private static final Logger log = LoggerFactory.getLogger(LotrCampaignService.class);

    private final LotrCampaignRepository repository;
    private final LotrCampaignCharacterRepository ccRepository;
    private final LotrSessionRepository sessionRepository;

    public LotrCampaignService(LotrCampaignRepository repository, LotrCampaignCharacterRepository ccRepository,
                               LotrSessionRepository sessionRepository) {
        this.repository = repository;
        this.ccRepository = ccRepository;
        this.sessionRepository = sessionRepository;
    }

    public List<LotrCampaign> findAll() {
        return repository.findAll();
    }

    public List<LotrCampaign> findActive() {
        return repository.findByActiveTrue();
    }

    public LotrCampaign findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found: " + id));
    }

    @Transactional
    public LotrCampaign createCampaign(String name, String description, String narratorName, String questType) {
        LotrCampaign campaign = new LotrCampaign();
        campaign.setName(name);
        campaign.setDescription(description);
        campaign.setNarratorName(narratorName);
        campaign.setQuestType(questType);
        campaign.setHopeLevel(5);
        campaign.setShadowLevel(0);
        campaign.setActive(true);
        log.info("Created LOTR campaign: {}", name);
        return repository.save(campaign);
    }

    @Transactional
    public LotrCampaign save(LotrCampaign campaign) {
        return repository.save(campaign);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public LotrCampaignCharacter addCharacterToCampaign(Long campaignId, Long characterId) {
        LotrCampaign campaign = findById(campaignId);
        LotrCampaignCharacter cc = new LotrCampaignCharacter();
        cc.setCampaign(campaign);
        cc.setCharacter(new LotrCharacter());
        cc.setCharacter(null); // Will be set by JPA
        ccRepository.save(cc);
        return cc;
    }

    public List<LotrCampaignCharacter> getCharactersInCampaign(Long campaignId) {
        return ccRepository.findByCampaignId(campaignId);
    }

    // --- Session management ---

    @Transactional
    public LotrSession createSession(Long campaignId, String sessionName, int sessionNumber,
                                      LocalDate sessionDate, String notes) {
        LotrCampaign campaign = findById(campaignId);
        LotrSession session = new LotrSession();
        session.setCampaign(campaign);
        session.setSessionName(sessionName);
        session.setSessionNumber(sessionNumber);
        session.setSessionDate(sessionDate != null ? sessionDate : LocalDate.now());
        session.setNotes(notes);
        session.setXpAwarded(0);
        session.setCorruptionChange(0);
        session.setHopeChange(0);
        session.setShadowChange(0);
        log.info("Created LOTR session: {} for campaign: {}", sessionName, campaign.getName());
        return sessionRepository.save(session);
    }

    public List<LotrSession> listSessions(Long campaignId) {
        return sessionRepository.findByCampaignId(campaignId);
    }

    public LotrSession findSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }
}
