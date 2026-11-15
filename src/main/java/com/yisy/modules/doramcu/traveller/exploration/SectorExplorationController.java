package com.yisy.modules.doramcu.traveller.exploration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/traveller/exploration")
public class SectorExplorationController {

    private final SectorExplorationService service;

    public SectorExplorationController(SectorExplorationService service) {
        this.service = service;
    }

    @GetMapping("/current-system")
    public ResponseEntity<StarSystem> getCurrentSystem(@RequestParam String characterId) {
        return service.getCurrentSystem(characterId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/navigate")
    public ResponseEntity<Map<String, Object>> navigate(
            @RequestParam String characterId,
            @RequestParam(defaultValue = "random") String direction) {
        SectorExplorationService.NavigationResult result = service.navigate(characterId, direction);
        return ResponseEntity.ok(Map.of(
                "success", result.isSuccess(),
                "message", result.getMessage(),
                "system", result.getSystem(),
                "hazard", result.getHazard()
        ));
    }

    @PostMapping("/land")
    public ResponseEntity<PlanetLanding> land(@RequestParam String characterId) {
        return ResponseEntity.ok(service.attemptLanding(characterId));
    }

    @GetMapping("/landings")
    public ResponseEntity<List<PlanetLanding>> getLandings(@RequestParam String characterId) {
        return ResponseEntity.ok(service.getLandingHistory(characterId));
    }

    @GetMapping("/trade-goods")
    public ResponseEntity<List<String>> getTradeGoods(@RequestParam String characterId) {
        return ResponseEntity.ok(service.getTradeGoods(characterId));
    }

    @GetMapping("/systems")
    public ResponseEntity<List<StarSystem>> getAllSystems() {
        return ResponseEntity.ok(service.getAllSystems());
    }
}
