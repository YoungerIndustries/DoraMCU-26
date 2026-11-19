package com.yisy.modules.doramcu.mechwarrior.controller;

import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorGameEngine;
import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorMech;
import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for MechWarrior FPS game operations.
 * <p>
 * Provides endpoints for:
 * <ul>
 *   <li>Session management (create, join, leave)</li>
 *   <li>Combat actions (fire weapons, jump jets)</li>
 *   <li>Game state queries (mech status, heat, components)</li>
 *   <li>Pilot progression (skills, experience, traits)</li>
 *   <li>Mech customization (weapons, armor, loadouts)</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/mechwarrior/fps")
public class MechWarriorFpsController {

    private static final Logger log = LoggerFactory.getLogger(MechWarriorFpsController.class);

    private final MechWarriorGameEngine gameEngine;

    public MechWarriorFpsController(MechWarriorGameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    // ==================== Session Management ====================

    /**
     * Create a new game session.
     */
    @PostMapping("/sessions")
    public ResponseEntity<MechWarriorSession> createSession(
            @RequestParam String mapName,
            @RequestParam(defaultValue = "8") int maxPlayers) {
        log.info("Creating session with map={} and maxPlayers={}", mapName, maxPlayers);
        String sessionId = UUID.randomUUID().toString();
        MechWarriorSession session = gameEngine.createSession(sessionId, mapName, maxPlayers);
        return ResponseEntity.ok(session);
    }

    /**
     * Get session details.
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<MechWarriorSession> getSession(@PathVariable String sessionId) {
        MechWarriorSession session = gameEngine.getSession(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    /**
     * List all active sessions.
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<MechWarriorSession>> listSessions() {
        return ResponseEntity.ok(gameEngine.getActiveSessions());
    }

    /**
     * Start a session.
     */
    @PostMapping("/sessions/{sessionId}/start")
    public ResponseEntity<Void> startSession(@PathVariable String sessionId) {
        MechWarriorSession session = gameEngine.getSession(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        session.startMatch();
        return ResponseEntity.ok().build();
    }

    /**
     * End a session.
     */
    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<Void> endSession(@PathVariable String sessionId) {
        MechWarriorSession session = gameEngine.getSession(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        session.endMatch();
        return ResponseEntity.ok().build();
    }

    /**
     * Remove a session.
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> removeSession(@PathVariable String sessionId) {
        gameEngine.removeSession(sessionId);
        return ResponseEntity.ok().build();
    }

    // ==================== Pilot Management ====================

    /**
     * Add a pilot with their mech to a session.
     */
    @PostMapping("/sessions/{sessionId}/pilots")
    public ResponseEntity<MechWarriorMech> addPilot(
            @PathVariable String sessionId,
            @RequestBody AddPilotRequest request) {
        MechPilot pilot = new MechPilot(request.pilotName());
        pilot.setId(request.pilotId());

        MechChassis chassis = new MechChassis(
                request.mechName(),
                request.mechDesignation(),
                request.mechClass(),
                request.tonnage()
        );
        chassis.setMaxSpeed(request.maxSpeed());
        chassis.setJumpJets(request.jumpJets());

        MechWarriorMech mech = gameEngine.addPilotToSession(sessionId, pilot, chassis);

        // Mount weapons if specified
        if (request.weapons() != null) {
            for (WeaponMountRequest weaponReq : request.weapons()) {
                MechWeapon weapon = new MechWeapon(
                        weaponReq.weaponName(),
                        weaponReq.weaponType(),
                        weaponReq.weaponSubType(),
                        weaponReq.damage(),
                        weaponReq.heatGenerated(),
                        weaponReq.cooldownTime()
                );
                weapon.setRange(weaponReq.range());
                mech.mountWeapon(weapon, weaponReq.location());
            }
        }

        return ResponseEntity.ok(mech);
    }

    /**
     * Get mech status for a pilot.
     */
    @GetMapping("/sessions/{sessionId}/pilots/{pilotId}/mech")
    public ResponseEntity<MechWarriorMech> getMechStatus(
            @PathVariable String sessionId,
            @PathVariable String pilotId) {
        MechWarriorSession session = gameEngine.getSession(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        MechWarriorMech mech = session.getMech(pilotId);
        if (mech == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mech);
    }

    // ==================== Combat Actions ====================

    /**
     * Fire weapons at a target.
     */
    @PostMapping("/sessions/{sessionId}/combat/fire")
    public ResponseEntity<CombatResult> fireWeapons(
            @PathVariable String sessionId,
            @RequestBody FireRequest request) {
        CombatResult result = gameEngine.fireWeapons(
                sessionId,
                request.shooterPilotId(),
                request.weaponIds(),
                request.targetLocation(),
                request.targetPilotId()
        );
        return ResponseEntity.ok(result);
    }

    /**
     * Use jump jets.
     */
    @PostMapping("/sessions/{sessionId}/movement/jump")
    public ResponseEntity<Void> useJumpJets(
            @PathVariable String sessionId,
            @RequestParam String pilotId,
            @RequestParam double distance) {
        gameEngine.useJumpJets(sessionId, pilotId, distance);
        return ResponseEntity.ok().build();
    }

    /**
     * Process a game tick.
     */
    @PostMapping("/sessions/{sessionId}/tick")
    public ResponseEntity<Void> processTick(@PathVariable String sessionId) {
        gameEngine.processTick(sessionId);
        return ResponseEntity.ok().build();
    }

    // ==================== Heat Management ====================

    /**
     * Toggle heat override.
     */
    @PostMapping("/sessions/{sessionId}/pilots/{pilotId}/heat/override")
    public ResponseEntity<Void> toggleHeatOverride(
            @PathVariable String sessionId,
            @PathVariable String pilotId,
            @RequestParam(defaultValue = "true") boolean enable) {
        MechWarriorSession session = gameEngine.getSession(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        MechWarriorMech mech = session.getMech(pilotId);
        if (mech == null) {
            return ResponseEntity.notFound().build();
        }
        mech.setHeatOverride(enable);
        return ResponseEntity.ok().build();
    }

    // ==================== Pilot Progression ====================

    /**
     * Get pilot stats.
     */
    @GetMapping("/sessions/{sessionId}/pilots/{pilotId}/stats")
    public ResponseEntity<MechPilot> getPilotStats(
            @PathVariable String sessionId,
            @PathVariable String pilotId) {
        MechWarriorSession session = gameEngine.getSession(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        MechWarriorMech mech = session.getMech(pilotId);
        if (mech == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mech.getPilot());
    }

    // ==================== Request DTOs ====================

    public record AddPilotRequest(
            String pilotId,
            String pilotName,
            String mechName,
            String mechDesignation,
            MechChassis.MechClass mechClass,
            int tonnage,
            double maxSpeed,
            int jumpJets,
            List<WeaponMountRequest> weapons
    ) {}

    public record WeaponMountRequest(
            String weaponName,
            MechWeapon.WeaponType weaponType,
            MechWeapon.WeaponSubType weaponSubType,
            int damage,
            int heatGenerated,
            double cooldownTime,
            int range,
            MechComponent.ComponentLocation location
    ) {}

    public record FireRequest(
            String shooterPilotId,
            List<String> weaponIds,
            MechComponent.ComponentLocation targetLocation,
            String targetPilotId
    ) {}
}
