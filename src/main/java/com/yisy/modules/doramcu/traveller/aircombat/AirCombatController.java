package com.yisy.modules.doramcu.traveller.aircombat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/traveller/air-combat")
public class AirCombatController {

    private final AirCombatService service;

    public AirCombatController(AirCombatService service) {
        this.service = service;
    }

    @GetMapping("/aircraft")
    public ResponseEntity<List<AircraftDesign>> getAllAircraft() {
        return ResponseEntity.ok(service.getAllAircraft());
    }

    @GetMapping("/aircraft/{type}")
    public ResponseEntity<List<AircraftDesign>> getAircraftByType(@PathVariable String type) {
        return ResponseEntity.ok(service.getAircraftByType(type));
    }

    @GetMapping("/missions")
    public ResponseEntity<List<AirCombatMission>> getMissions(@RequestParam String characterId) {
        return ResponseEntity.ok(service.getMissions(characterId));
    }

    @PostMapping("/missions")
    public ResponseEntity<AirCombatMission> createMission(
            @RequestParam String characterId,
            @RequestParam(required = false) String missionType) {
        return ResponseEntity.ok(service.createMission(characterId, missionType));
    }

    @PostMapping("/missions/{id}/resolve")
    public ResponseEntity<AirCombatService.MissionResult> resolveMission(@PathVariable Long id) {
        return ResponseEntity.ok(service.resolveMission(id));
    }
}
