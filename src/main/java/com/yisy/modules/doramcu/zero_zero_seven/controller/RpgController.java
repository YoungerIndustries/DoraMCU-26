package com.yisy.modules.doramcu.zero_zero_seven.controller;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondAgent;
import com.yisy.modules.doramcu.zero_zero_seven.networks.RecruitmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for agent management and missions.
 */
@RestController
@RequestMapping("/api/zero-zero-seven/agents")
public class RpgController {

    private final RecruitmentService recruitmentService;

    public RpgController(RecruitmentService recruitmentService) {
        this.recruitmentService = recruitmentService;
    }

    @GetMapping
    public ResponseEntity<List<BondAgent>> listAgents(@RequestParam Long empireId) {
        return ResponseEntity.ok(recruitmentService.getAgentsByEmpire(empireId));
    }

    @GetMapping("/available")
    public ResponseEntity<List<BondAgent>> listAvailableAgents(@RequestParam Long empireId) {
        return ResponseEntity.ok(recruitmentService.getAvailableAgents(empireId));
    }

    @PostMapping("/recruit")
    public ResponseEntity<BondAgent> recruitAgent(
            @RequestParam String name,
            @RequestParam String codeName,
            @RequestParam Long ownerEmpireId,
            @RequestParam Long gameSessionId) {
        return ResponseEntity.ok(recruitmentService.recruitAgent(name, codeName, ownerEmpireId, gameSessionId));
    }

    @PostMapping("/recruit-custom")
    public ResponseEntity<BondAgent> recruitAgentWithSkills(
            @RequestParam String name,
            @RequestParam String codeName,
            @RequestParam Long ownerEmpireId,
            @RequestParam Long gameSessionId,
            @RequestParam(defaultValue = "5") int stealth,
            @RequestParam(defaultValue = "5") int charisma,
            @RequestParam(defaultValue = "5") int combat,
            @RequestParam(defaultValue = "5") int technical) {
        return ResponseEntity.ok(recruitmentService.recruitAgentWithSkills(
                name, codeName, ownerEmpireId, gameSessionId, stealth, charisma, combat, technical));
    }
}
