package com.yisy.modules.doramcu.warhammer40k.controller;

import com.yisy.modules.doramcu.warhammer40k.service.Warhammer40kService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Warhammer 40K game module.
 */
@RestController
@RequestMapping("/api/v1/warhammer40k")
public class Warhammer40kRestController {

    private final Warhammer40kService service;

    public Warhammer40kRestController(Warhammer40kService service) {
        this.service = service;
    }

    @GetMapping("/factions")
    public List<Faction> getFactions() {
        return service.getFactions();
    }

    @GetMapping("/factions/{id}")
    public ResponseEntity<Faction> getFaction(@PathVariable String id) {
        return service.getFaction(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/units")
    public List<Unit> getUnits() {
        return service.getUnits();
    }

    @GetMapping("/units/{id}")
    public ResponseEntity<Unit> getUnit(@PathVariable String id) {
        return service.getUnit(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/units/faction/{factionId}")
    public List<Unit> getUnitsByFaction(@PathVariable String factionId) {
        return service.getUnitsByFaction(factionId);
    }

    @GetMapping("/units/infantry")
    public List<Unit> getInfantry() {
        return service.getInfantry();
    }

    @GetMapping("/units/vehicles")
    public List<Unit> getVehicles() {
        return service.getVehicles();
    }

    @GetMapping("/scenarios")
    public List<Scenario> getScenarios() {
        return service.getScenarios();
    }

    @GetMapping("/scenarios/{id}")
    public ResponseEntity<Scenario> getScenario(@PathVariable String id) {
        return service.getScenario(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/lore")
    public Lore getLore() {
        return service.getLore();
    }

    @GetMapping("/combat-rules")
    public Map<String, Object> getCombatRules() {
        return service.getCombatRules();
    }
}
