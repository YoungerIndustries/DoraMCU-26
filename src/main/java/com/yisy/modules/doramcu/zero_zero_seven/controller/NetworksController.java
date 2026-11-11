package com.yisy.modules.doramcu.zero_zero_seven.controller;

import com.yisy.modules.doramcu.zero_zero_seven.model.SpyNetwork;
import com.yisy.modules.doramcu.zero_zero_seven.networks.SafehouseService;
import com.yisy.modules.doramcu.zero_zero_seven.networks.SpyNetworkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for spy network management.
 */
@RestController
@RequestMapping("/api/zero-zero-seven/networks")
public class NetworksController {

    private final SpyNetworkService spyNetworkService;
    private final SafehouseService safehouseService;

    public NetworksController(SpyNetworkService spyNetworkService,
                               SafehouseService safehouseService) {
        this.spyNetworkService = spyNetworkService;
        this.safehouseService = safehouseService;
    }

    @GetMapping
    public ResponseEntity<List<SpyNetwork>> listNetworks(@RequestParam Long empireId) {
        return ResponseEntity.ok(spyNetworkService.getAllNetworks(empireId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<SpyNetwork>> listActiveNetworks(@RequestParam Long empireId) {
        return ResponseEntity.ok(spyNetworkService.getActiveNetworks(empireId));
    }

    @PostMapping("/establish")
    public ResponseEntity<SpyNetwork> establishNetwork(
            @RequestParam String name,
            @RequestParam Long locationId,
            @RequestParam Long ownerEmpireId,
            @RequestParam Long gameSessionId) {
        return ResponseEntity.ok(spyNetworkService.establishNetwork(name, locationId, ownerEmpireId, gameSessionId));
    }

    @PostMapping("/{networkId}/upgrade")
    public ResponseEntity<SpyNetwork> upgradeNetwork(@PathVariable Long networkId) {
        return ResponseEntity.ok(spyNetworkService.upgradeNetwork(networkId));
    }

    @PostMapping("/{networkId}/safehouse")
    public ResponseEntity<SpyNetwork> buildSafehouse(@PathVariable Long networkId) {
        return ResponseEntity.ok(safehouseService.buildSafehouse(networkId));
    }

    @GetMapping("/safehouses")
    public ResponseEntity<Integer> getSafehouses(@RequestParam Long empireId) {
        return ResponseEntity.ok(safehouseService.getTotalSafehouses(empireId));
    }
}
