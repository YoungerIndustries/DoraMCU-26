package com.yisy.modules.doramcu.captainslog.controller;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacterTalent;
import com.yisy.modules.doramcu.captainslog.dto.CreateCharacterTalentRequest;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogService;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogTalentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST controller for Captain's Log character talent endpoints.
 * Extracted from CaptainLogController to reduce god class size.
 * <p>
 * Provides CRUD operations for character talents and talent suggestions.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/captains-log")
public class CaptainLogTalentController {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogTalentController.class);

    private final CaptainLogService captainLogService;

    public CaptainLogTalentController(CaptainLogService captainLogService) {
        this.captainLogService = captainLogService;
    }

    /**
     * Creates a new talent for a character.
     */
    @PostMapping("/characters/{characterId}/talents")
    public ResponseEntity<CaptainLogCharacterTalent> createTalent(@PathVariable Long characterId,
                                                                   @RequestBody CreateCharacterTalentRequest request) {
        log.debug("POST /api/v1/captains-log/characters/{}/talents - creating: {}", characterId, request.getName());
        CaptainLogCharacterTalent talent = captainLogService.getTalentService().createTalent(
                characterId, request.getName(), request.getDescription(),
                request.getMechanicalEffect(), request.getSource());
        return ResponseEntity.status(HttpStatus.CREATED).body(talent);
    }

    /**
     * Retrieves a talent by ID.
     */
    @GetMapping("/talents/{talentId}")
    public ResponseEntity<CaptainLogCharacterTalent> getTalent(@PathVariable Long talentId) {
        log.debug("GET /api/v1/captains-log/talents/{}", talentId);
        return captainLogService.getTalentService().getTalent(talentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Lists all talents for a character.
     */
    @GetMapping("/characters/{characterId}/talents")
    public ResponseEntity<List<CaptainLogCharacterTalent>> listTalents(@PathVariable Long characterId) {
        log.debug("GET /api/v1/captains-log/characters/{}/talents", characterId);
        return ResponseEntity.ok(captainLogService.getTalentService().listTalents(characterId));
    }

    /**
     * Lists all active talents for a character.
     */
    @GetMapping("/characters/{characterId}/talents/active")
    public ResponseEntity<List<CaptainLogCharacterTalent>> listActiveTalents(@PathVariable Long characterId) {
        log.debug("GET /api/v1/captains-log/characters/{}/talents/active", characterId);
        return ResponseEntity.ok(captainLogService.getTalentService().listActiveTalents(characterId));
    }

    /**
     * Deletes a talent.
     */
    @DeleteMapping("/talents/{talentId}")
    public ResponseEntity<Void> deleteTalent(@PathVariable Long talentId) {
        log.debug("DELETE /api/v1/captains-log/talents/{}", talentId);
        captainLogService.getTalentService().deleteTalent(talentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggles a talent's active status.
     */
    @PatchMapping("/talents/{talentId}/toggle")
    public ResponseEntity<CaptainLogCharacterTalent> toggleTalent(@PathVariable Long talentId) {
        log.debug("PATCH /api/v1/captains-log/talents/{}/toggle", talentId);
        return ResponseEntity.ok(captainLogService.getTalentService().toggleActive(talentId));
    }

    /**
     * Suggests talents based on a character's species.
     */
    @GetMapping("/talents/suggest/species/{species}")
    public ResponseEntity<List<CaptainLogTalentService.TalentDefinition>> suggestSpeciesTalents(@PathVariable String species) {
        log.debug("GET /api/v1/captains-log/talents/suggest/species/{}", species);
        return ResponseEntity.ok(captainLogService.getTalentService().suggestSpeciesTalents(species));
    }

    /**
     * Suggests lifepath talents.
     */
    @GetMapping("/talents/suggest/lifepath")
    public ResponseEntity<List<CaptainLogTalentService.TalentDefinition>> suggestLifepathTalents() {
        log.debug("GET /api/v1/captains-log/talents/suggest/lifepath");
        return ResponseEntity.ok(captainLogService.getTalentService().suggestLifepathTalents());
    }

    /**
     * Suggests career talents.
     */
    @GetMapping("/talents/suggest/career")
    public ResponseEntity<List<CaptainLogTalentService.TalentDefinition>> suggestCareerTalents() {
        log.debug("GET /api/v1/captains-log/talents/suggest/career");
        return ResponseEntity.ok(captainLogService.getTalentService().suggestCareerTalents());
    }

    /**
     * Suggests training talents.
     */
    @GetMapping("/talents/suggest/training")
    public ResponseEntity<List<CaptainLogTalentService.TalentDefinition>> suggestTrainingTalents() {
        log.debug("GET /api/v1/captains-log/talents/suggest/training");
        return ResponseEntity.ok(captainLogService.getTalentService().suggestTrainingTalents());
    }

    /**
     * Suggests all available talents for a character, excluding those they already have.
     */
    @GetMapping("/characters/{characterId}/talents/suggest")
    public ResponseEntity<List<CaptainLogTalentService.TalentDefinition>> suggestTalentsForCharacter(@PathVariable Long characterId) {
        log.debug("GET /api/v1/captains-log/characters/{}/talents/suggest", characterId);
        CaptainLogCharacter character = captainLogService.getCharacter(characterId)
                .orElseThrow(() -> new NoSuchElementException("Character not found: " + characterId));
        return ResponseEntity.ok(captainLogService.getTalentService().suggestTalentsForCharacter(character));
    }

    /**
     * Lists all available talent pools by source category.
     */
    @GetMapping("/talents/pools")
    public ResponseEntity<Map<String, List<CaptainLogTalentService.TalentDefinition>>> listTalentPools() {
        log.debug("GET /api/v1/captains-log/talents/pools");
        return ResponseEntity.ok(captainLogService.getTalentService().getAllTalentPools());
    }
}
