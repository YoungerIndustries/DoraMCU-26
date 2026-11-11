package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.dto.AddCampaignCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eCampaignRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eSessionRequest;
import com.yisy.modules.doramcu.dnd5e.model.*;
import com.yisy.modules.doramcu.dnd5e.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing D&D 5e campaigns and sessions.
 */
@Service
public class Dnd5eCampaignService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eCampaignService.class);

    private final Dnd5eCampaignRepository campaignRepository;
    private final Dnd5eSessionRepository sessionRepository;
    private final Dnd5eCharacterRepository characterRepository;

    public Dnd5eCampaignService(Dnd5eCampaignRepository campaignRepository,
                                Dnd5eSessionRepository sessionRepository,
                                Dnd5eCharacterRepository characterRepository) {
        this.campaignRepository = campaignRepository;
        this.sessionRepository = sessionRepository;
        this.characterRepository = characterRepository;
    }

    // ==================== Campaigns ====================

    @Transactional
    public Dnd5eCampaign createCampaign(CreateDnd5eCampaignRequest request) {
        Dnd5eCampaign campaign = new Dnd5eCampaign();
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setDmName(request.getDmName());
        campaign.setSettingName(request.getSettingName());
        campaign.setStartingLevel(request.getStartingLevel());
        campaign.setActive(true);

        Dnd5eCampaign saved = campaignRepository.save(campaign);
        log.info("Created campaign: {}", saved.getName());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Dnd5eCampaign> listCampaigns() {
        return campaignRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Dnd5eCampaign> listActiveCampaigns() {
        return campaignRepository.findByActive(true);
    }

    @Transactional(readOnly = true)
    public Dnd5eCampaign getCampaign(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found: " + id));
    }

    @Transactional
    public Dnd5eCampaign updateCampaign(Long id, CreateDnd5eCampaignRequest request) {
        Dnd5eCampaign campaign = getCampaign(id);

        if (request.getName() != null) {
            campaign.setName(request.getName());
        }
        if (request.getDescription() != null) {
            campaign.setDescription(request.getDescription());
        }
        if (request.getDmName() != null) {
            campaign.setDmName(request.getDmName());
        }
        if (request.getSettingName() != null) {
            campaign.setSettingName(request.getSettingName());
        }
        campaign.setStartingLevel(request.getStartingLevel());

        Dnd5eCampaign saved = campaignRepository.save(campaign);
        log.info("Updated campaign: {}", saved.getName());
        return saved;
    }

    @Transactional
    public void deleteCampaign(Long id) {
        if (!campaignRepository.existsById(id)) {
            throw new IllegalArgumentException("Campaign not found: " + id);
        }
        campaignRepository.deleteById(id);
        log.info("Deleted campaign: {}", id);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Dnd5eCampaign campaign = getCampaign(id);
        campaign.setActive(active);
        campaignRepository.save(campaign);
        log.info("Campaign {} set to active={}", id, active);
    }

    // ==================== Campaign Characters ====================

    @Transactional
    public Dnd5eCampaignCharacter addCharacterToCampaign(Long campaignId, AddCampaignCharacterRequest request) {
        Dnd5eCampaign campaign = getCampaign(campaignId);
        Dnd5eCharacter character = characterRepository.findById(request.getCharacterId())
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + request.getCharacterId()));

        Dnd5eCampaignCharacter cc = new Dnd5eCampaignCharacter();
        cc.setCampaign(campaign);
        cc.setCharacter(character);
        cc.setRole(request.getRole() != null ? request.getRole() : "Player Character");

        campaign.getCampaignCharacters().add(cc);
        campaignRepository.save(campaign);
        log.info("Added character {} to campaign {}", character.getName(), campaign.getName());
        return cc;
    }

    @Transactional
    public void removeCharacterFromCampaign(Long campaignId, Long characterId) {
        Dnd5eCampaign campaign = getCampaign(campaignId);
        campaign.getCampaignCharacters().removeIf(cc -> cc.getCharacter().getId().equals(characterId));
        campaignRepository.save(campaign);
        log.info("Removed character {} from campaign {}", characterId, campaign.getName());
    }

    // ==================== Sessions ====================

    @Transactional
    public Dnd5eSession createSession(Long campaignId, CreateDnd5eSessionRequest request) {
        Dnd5eCampaign campaign = getCampaign(campaignId);

        Dnd5eSession session = new Dnd5eSession();
        session.setCampaign(campaign);
        session.setSessionName(request.getSessionName());
        session.setSessionNumber(request.getSessionNumber());
        session.setSessionDate(request.getSessionDate() != null ? request.getSessionDate() : LocalDate.now());
        session.setNotes(request.getNotes());
        session.setXpAwarded(request.getXpAwarded());
        session.setDurationMinutes(request.getDurationMinutes());

        campaign.getSessions().add(session);
        Dnd5eCampaign saved = campaignRepository.save(campaign);
        log.info("Created session '{}' for campaign {}", session.getSessionName(), campaign.getName());
        return session;
    }

    @Transactional(readOnly = true)
    public List<Dnd5eSession> listSessions(Long campaignId) {
        getCampaign(campaignId); // Verify campaign exists
        return sessionRepository.findByCampaignIdOrderBySessionNumberDesc(campaignId);
    }

    @Transactional(readOnly = true)
    public Dnd5eSession getSession(Long campaignId, Long sessionId) {
        getCampaign(campaignId); // Verify campaign exists
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }

    @Transactional
    public Dnd5eSession updateSession(Long campaignId, Long sessionId, CreateDnd5eSessionRequest request) {
        Dnd5eSession session = getSession(campaignId, sessionId);

        if (request.getSessionName() != null) {
            session.setSessionName(request.getSessionName());
        }
        session.setSessionNumber(request.getSessionNumber());
        if (request.getSessionDate() != null) {
            session.setSessionDate(request.getSessionDate());
        }
        if (request.getNotes() != null) {
            session.setNotes(request.getNotes());
        }
        session.setXpAwarded(request.getXpAwarded());
        session.setDurationMinutes(request.getDurationMinutes());

        sessionRepository.save(session);
        log.info("Updated session {}", session.getSessionName());
        return session;
    }

    @Transactional
    public void deleteSession(Long campaignId, Long sessionId) {
        Dnd5eSession session = getSession(campaignId, sessionId);
        sessionRepository.delete(session);
        log.info("Deleted session {}", session.getSessionName());
    }
}
