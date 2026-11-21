package com.yisy.modules.doramcu.captainslog.controller;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogShipTalent;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogStarship;
import com.yisy.modules.doramcu.captainslog.dto.CreateShipTalentRequest;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogService;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogShipTalentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST controller for Captain's Log starship talent endpoints.
 * Extracted from CaptainLogController to reduce god class size.
 * <p>
 * Provides CRUD operations for starship talents and talent suggestions.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/captains-log")
public class CaptainLogShipTalentController {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogShipTalentController.class);

    private final CaptainLogService captainLogService;

    public CaptainLogShipTalentController(CaptainLogService captainLogService) {
        this.captainLogService = captainLogService;
    }

    /**
     * Creates a new talent for a starship.
     */
    @PostMapping("/starships/{starshipId}/talents")
    public ResponseEntity<CaptainLogShipTalent> createShipTalent(@PathVariable Long starshipId,
                                                                   @RequestBody CreateShipTalentRequest request) {
        log.debug("POST /api/v1/captains-log/starships/{}/talents - creating: {}", starshipId, request.getName());
        CaptainLogShipTalent talent = captainLogService.getShipTalentService().createTalent(
                starshipId, request.getName(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(talent);
    }

    /**
     * Retrieves a ship talent by ID.
     */
    @GetMapping("/starships/talents/{talentId}")
    public ResponseEntity<CaptainLogShipTalent> getShipTalent(@PathVariable Long talentId) {
        log.debug("GET /api/v1/captains-log/starships/talents/{}", talentId);
        return captainLogService.getShipTalentService().getTalent(talentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Lists all talents for a starship.
     */
    @GetMapping("/starships/{starshipId}/talents")
    public ResponseEntity<List<CaptainLogShipTalent>> listShipTalents(@PathVariable Long starshipId) {
        log.debug("GET /api/v1/captains-log/starships/{}/talents", starshipId);
        return ResponseEntity.ok(captainLogService.getShipTalentService().listTalents(starshipId));
    }

    /**
     * Deletes a ship talent.
     */
    @DeleteMapping("/starships/talents/{talentId}")
    public ResponseEntity<Void> deleteShipTalent(@PathVariable Long talentId) {
        log.debug("DELETE /api/v1/captains-log/starships/talents/{}", talentId);
        captainLogService.getShipTalentService().deleteTalent(talentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Suggests talents based on a starship's class.
     */
    @GetMapping("/starships/talents/suggest/class/{shipClass}")
    public ResponseEntity<List<CaptainLogShipTalentService.ShipTalentDefinition>> suggestShipClassTalents(@PathVariable String shipClass) {
        log.debug("GET /api/v1/captains-log/starships/talents/suggest/class/{}", shipClass);
        return ResponseEntity.ok(captainLogService.getShipTalentService().suggestClassTalents(shipClass));
    }

    /**
     * Suggests role-based ship talents.
     */
    @GetMapping("/starships/talents/suggest/role")
    public ResponseEntity<List<CaptainLogShipTalentService.ShipTalentDefinition>> suggestShipRoleTalents() {
        log.debug("GET /api/v1/captains-log/starships/talents/suggest/role");
        return ResponseEntity.ok(captainLogService.getShipTalentService().suggestRoleTalents());
    }

    /**
     * Suggests upgrade ship talents.
     */
    @GetMapping("/starships/talents/suggest/upgrade")
    public ResponseEntity<List<CaptainLogShipTalentService.ShipTalentDefinition>> suggestShipUpgradeTalents() {
        log.debug("GET /api/v1/captains-log/starships/talents/suggest/upgrade");
        return ResponseEntity.ok(captainLogService.getShipTalentService().suggestUpgradeTalents());
    }

    /**
     * Suggests all available talents for a starship, excluding those it already has.
     */
    @GetMapping("/starships/{starshipId}/talents/suggest")
    public ResponseEntity<List<CaptainLogShipTalentService.ShipTalentDefinition>> suggestTalentsForStarship(@PathVariable Long starshipId) {
        log.debug("GET /api/v1/captains-log/starships/{}/talents/suggest", starshipId);
        CaptainLogStarship starship = captainLogService.getStarship(starshipId)
                .orElseThrow(() -> new NoSuchElementException("Starship not found: " + starshipId));
        return ResponseEntity.ok(captainLogService.getShipTalentService().suggestTalentsForStarship(starship));
    }

    /**
     * Lists all available ship talent pools by category.
     */
    @GetMapping("/starships/talents/pools")
    public ResponseEntity<Map<String, List<CaptainLogShipTalentService.ShipTalentDefinition>>> listShipTalentPools() {
        log.debug("GET /api/v1/captains-log/starships/talents/pools");
        return ResponseEntity.ok(captainLogService.getShipTalentService().getAllTalentPools());
    }

    /**
     * Lists all available ship class names for talent lookup.
     */
    @GetMapping("/starships/talents/classes")
    public ResponseEntity<List<String>> listShipTalentClasses() {
        log.debug("GET /api/v1/captains-log/starships/talents/classes");
        return ResponseEntity.ok(captainLogService.getShipTalentService().getAvailableShipClasses());
    }
}
