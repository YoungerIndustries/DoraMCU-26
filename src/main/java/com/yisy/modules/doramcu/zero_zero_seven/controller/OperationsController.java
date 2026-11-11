package com.yisy.modules.doramcu.zero_zero_seven.controller;

import com.yisy.modules.doramcu.zero_zero_seven.model.CovertOperation;
import com.yisy.modules.doramcu.zero_zero_seven.operations.AssassinationService;
import com.yisy.modules.doramcu.zero_zero_seven.operations.CovertOperationService;
import com.yisy.modules.doramcu.zero_zero_seven.operations.InfiltrationService;
import com.yisy.modules.doramcu.zero_zero_seven.operations.SabotageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for covert operations.
 */
@RestController
@RequestMapping("/api/zero-zero-seven/operations")
public class OperationsController {

    private final CovertOperationService operationService;
    private final SabotageService sabotageService;
    private final AssassinationService assassinationService;
    private final InfiltrationService infiltrationService;

    public OperationsController(CovertOperationService operationService,
                                 SabotageService sabotageService,
                                 AssassinationService assassinationService,
                                 InfiltrationService infiltrationService) {
        this.operationService = operationService;
        this.sabotageService = sabotageService;
        this.assassinationService = assassinationService;
        this.infiltrationService = infiltrationService;
    }

    @GetMapping
    public ResponseEntity<List<CovertOperation>> listOperations(@RequestParam Long gameSessionId) {
        return ResponseEntity.ok(operationService.getAllOperations(gameSessionId));
    }

    @GetMapping("/empire/{empireId}")
    public ResponseEntity<List<CovertOperation>> getEmpireOperations(@PathVariable Long empireId) {
        return ResponseEntity.ok(operationService.getOperationsByEmpire(empireId));
    }

    @PostMapping("/sabotage")
    public ResponseEntity<CovertOperation> launchSabotage(
            @RequestParam String name,
            @RequestParam Long ownerEmpireId,
            @RequestParam Long targetEmpireId,
            @RequestParam Long agentId,
            @RequestParam(defaultValue = "5") int difficulty) {
        return ResponseEntity.ok(sabotageService.launchSabotage(name, ownerEmpireId, targetEmpireId, agentId, difficulty));
    }

    @PostMapping("/assassination")
    public ResponseEntity<CovertOperation> launchAssassination(
            @RequestParam String name,
            @RequestParam Long ownerEmpireId,
            @RequestParam Long targetEmpireId,
            @RequestParam Long agentId,
            @RequestParam(defaultValue = "7") int difficulty) {
        return ResponseEntity.ok(assassinationService.launchAssassination(name, ownerEmpireId, targetEmpireId, agentId, difficulty));
    }

    @PostMapping("/infiltration")
    public ResponseEntity<CovertOperation> launchInfiltration(
            @RequestParam String name,
            @RequestParam Long ownerEmpireId,
            @RequestParam Long targetEmpireId,
            @RequestParam Long agentId,
            @RequestParam(defaultValue = "6") int difficulty) {
        return ResponseEntity.ok(infiltrationService.launchInfiltration(name, ownerEmpireId, targetEmpireId, agentId, difficulty));
    }
}
