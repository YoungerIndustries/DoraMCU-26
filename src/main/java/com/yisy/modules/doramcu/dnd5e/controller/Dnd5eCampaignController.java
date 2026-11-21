package com.yisy.modules.doramcu.dnd5e.controller;

import com.yisy.modules.doramcu.dnd5e.dto.AddCampaignCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eCampaignRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eSessionRequest;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaign;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaignCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaignTemplate;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSession;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCampaignService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCampaignTemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for D&D 5e Campaign-related endpoints.
 * Covers: Campaigns, Campaign Characters, Sessions, Campaign Templates.
 */
@RestController
@RequestMapping("/api/v1/dnd5e")
public class Dnd5eCampaignController {

    private final Dnd5eCampaignService campaignService;
    private final Dnd5eCampaignTemplateService campaignTemplateService;

    public Dnd5eCampaignController(Dnd5eCampaignService campaignService,
                                    Dnd5eCampaignTemplateService campaignTemplateService) {
        this.campaignService = campaignService;
        this.campaignTemplateService = campaignTemplateService;
    }

    // ==================== Campaigns ====================

    @PostMapping("/campaigns")
    public Dnd5eCampaign createCampaign(@RequestBody CreateDnd5eCampaignRequest request) {
        return campaignService.createCampaign(request);
    }

    @GetMapping("/campaigns")
    public List<Dnd5eCampaign> listCampaigns() {
        return campaignService.listCampaigns();
    }

    @GetMapping("/campaigns/active")
    public List<Dnd5eCampaign> listActiveCampaigns() {
        return campaignService.listActiveCampaigns();
    }

    @GetMapping("/campaigns/{id}")
    public Dnd5eCampaign getCampaign(@PathVariable Long id) {
        return campaignService.getCampaign(id);
    }

    @PatchMapping("/campaigns/{id}")
    public Dnd5eCampaign updateCampaign(@PathVariable Long id, @RequestBody CreateDnd5eCampaignRequest request) {
        return campaignService.updateCampaign(id, request);
    }

    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/campaigns/{id}/active")
    public Dnd5eCampaign setCampaignActive(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean active = body.getOrDefault("active", true);
        campaignService.setActive(id, active);
        return campaignService.getCampaign(id);
    }

    // ==================== Campaign Characters ====================

    @PostMapping("/campaigns/{id}/characters")
    public Dnd5eCampaignCharacter addCharacterToCampaign(@PathVariable Long id, @RequestBody AddCampaignCharacterRequest request) {
        return campaignService.addCharacterToCampaign(id, request);
    }

    @DeleteMapping("/campaigns/{campaignId}/characters/{characterId}")
    public ResponseEntity<Void> removeCharacterFromCampaign(@PathVariable Long campaignId, @PathVariable Long characterId) {
        campaignService.removeCharacterFromCampaign(campaignId, characterId);
        return ResponseEntity.ok().build();
    }

    // ==================== Sessions ====================

    @PostMapping("/campaigns/{id}/sessions")
    public Dnd5eSession createSession(@PathVariable Long id, @RequestBody CreateDnd5eSessionRequest request) {
        return campaignService.createSession(id, request);
    }

    @GetMapping("/campaigns/{id}/sessions")
    public List<Dnd5eSession> listSessions(@PathVariable Long id) {
        return campaignService.listSessions(id);
    }

    @GetMapping("/campaigns/{campaignId}/sessions/{sessionId}")
    public Dnd5eSession getSession(@PathVariable Long campaignId, @PathVariable Long sessionId) {
        return campaignService.getSession(campaignId, sessionId);
    }

    @PatchMapping("/campaigns/{campaignId}/sessions/{sessionId}")
    public Dnd5eSession updateSession(@PathVariable Long campaignId, @PathVariable Long sessionId, @RequestBody CreateDnd5eSessionRequest request) {
        return campaignService.updateSession(campaignId, sessionId, request);
    }

    @DeleteMapping("/campaigns/{campaignId}/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long campaignId, @PathVariable Long sessionId) {
        campaignService.deleteSession(campaignId, sessionId);
        return ResponseEntity.ok().build();
    }

    // ==================== Campaign Templates ====================

    @PostMapping("/campaign-templates")
    public Dnd5eCampaignTemplate createCampaignTemplate(@RequestBody Dnd5eCampaignTemplate template) {
        return campaignTemplateService.createTemplate(template);
    }

    @GetMapping("/campaign-templates")
    public List<Dnd5eCampaignTemplate> listCampaignTemplates() {
        return campaignTemplateService.listTemplates();
    }

    @GetMapping("/campaign-templates/active")
    public List<Dnd5eCampaignTemplate> listActiveCampaignTemplates() {
        return campaignTemplateService.listActiveTemplates();
    }

    @GetMapping("/campaign-templates/{id}")
    public ResponseEntity<Dnd5eCampaignTemplate> getCampaignTemplate(@PathVariable Long id) {
        return campaignTemplateService.getTemplate(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/campaign-templates/name/{name}")
    public ResponseEntity<Dnd5eCampaignTemplate> getCampaignTemplateByName(@PathVariable String name) {
        return campaignTemplateService.getTemplateByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/campaign-templates/{id}")
    public ResponseEntity<Dnd5eCampaignTemplate> updateCampaignTemplate(@PathVariable Long id, @RequestBody Dnd5eCampaignTemplate updated) {
        try {
            return ResponseEntity.ok(campaignTemplateService.updateTemplate(id, updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/campaign-templates/{id}")
    public ResponseEntity<Void> deleteCampaignTemplate(@PathVariable Long id) {
        campaignTemplateService.deleteTemplate(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/campaign-templates/{id}/toggle-active")
    public ResponseEntity<Void> toggleCampaignTemplateActive(@PathVariable Long id) {
        try {
            campaignTemplateService.toggleTemplateActive(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/campaign-templates/theme/{theme}")
    public List<Dnd5eCampaignTemplate> listCampaignTemplatesByTheme(@PathVariable String theme) {
        return campaignTemplateService.listTemplatesByTheme(theme);
    }

    @GetMapping("/campaign-templates/level/{partyLevel}")
    public List<Dnd5eCampaignTemplate> listCampaignTemplatesByPartyLevel(@PathVariable int partyLevel) {
        return campaignTemplateService.listTemplatesByPartyLevel(partyLevel);
    }

    @GetMapping("/campaign-templates/level-range")
    public List<Dnd5eCampaignTemplate> listCampaignTemplatesByLevelRange(@RequestParam int minLevel, @RequestParam int maxLevel) {
        return campaignTemplateService.listTemplatesByLevelRange(minLevel, maxLevel);
    }

    @GetMapping("/campaign-templates/search")
    public List<Dnd5eCampaignTemplate> searchCampaignTemplates(@RequestParam String keyword) {
        return campaignTemplateService.searchTemplates(keyword);
    }

    @GetMapping("/campaign-templates/{id}/summary")
    public ResponseEntity<Map<String, Object>> getCampaignTemplateSummary(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(campaignTemplateService.getTemplateSummary(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/campaign-templates/themes")
    public List<String> listCampaignTemplateThemes() {
        return campaignTemplateService.listAllThemes();
    }
}
