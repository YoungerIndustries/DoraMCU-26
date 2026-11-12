package com.yisy.modules.doramcu.zero_zero_seven.controller;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondDiplomaticRelation;
import com.yisy.modules.doramcu.zero_zero_seven.diplomacy.DiplomacyService;
import com.yisy.modules.doramcu.zero_zero_seven.diplomacy.TreatyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for diplomatic relations.
 */
@RestController("bondDiplomacyController")
@RequestMapping("/api/zero-zero-seven/diplomacy")
public class DiplomacyController {

    private final DiplomacyService diplomacyService;
    private final TreatyService treatyService;

    public DiplomacyController(DiplomacyService diplomacyService,
                               TreatyService treatyService) {
        this.diplomacyService = diplomacyService;
        this.treatyService = treatyService;
    }

    @GetMapping
    public ResponseEntity<List<BondDiplomaticRelation>> listRelations(@RequestParam Long gameSessionId) {
        return ResponseEntity.ok(diplomacyService.getAllRelations(gameSessionId));
    }

    @GetMapping("/empire/{empireName}")
    public ResponseEntity<List<BondDiplomaticRelation>> getEmpireRelations(@PathVariable String empireName) {
        return ResponseEntity.ok(diplomacyService.getRelationsForEmpire(empireName));
    }

    @PostMapping("/declare-war")
    public ResponseEntity<Void> declareWar(
            @RequestParam String empire1,
            @RequestParam String empire2,
            @RequestParam Long gameSessionId) {
        diplomacyService.declareWar(empire1, empire2, gameSessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/peace")
    public ResponseEntity<Void> establishPeace(
            @RequestParam String empire1,
            @RequestParam String empire2,
            @RequestParam Long gameSessionId) {
        diplomacyService.establishPeace(empire1, empire2, gameSessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/alliance")
    public ResponseEntity<Void> createAlliance(
            @RequestParam String empire1,
            @RequestParam String empire2,
            @RequestParam Long gameSessionId) {
        treatyService.createAlliance(empire1, empire2, gameSessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/trade-agreement")
    public ResponseEntity<Void> createTradeAgreement(
            @RequestParam String empire1,
            @RequestParam String empire2,
            @RequestParam Long gameSessionId) {
        treatyService.createTradeAgreement(empire1, empire2, gameSessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/non-aggression-pact")
    public ResponseEntity<Void> createNonAggressionPact(
            @RequestParam String empire1,
            @RequestParam String empire2,
            @RequestParam Long gameSessionId) {
        treatyService.createNonAggressionPact(empire1, empire2, gameSessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/break-treaty")
    public ResponseEntity<Void> breakTreaty(
            @RequestParam String empire1,
            @RequestParam String empire2,
            @RequestParam String treatyType,
            @RequestParam Long gameSessionId) {
        treatyService.breakTreaty(empire1, empire2, treatyType, gameSessionId);
        return ResponseEntity.ok().build();
    }
}
