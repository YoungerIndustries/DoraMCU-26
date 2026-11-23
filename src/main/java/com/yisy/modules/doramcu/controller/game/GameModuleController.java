package com.yisy.modules.doramcu.controller.game;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.hex.dungeon.Dungeon;

import com.yisy.modules.doramcu.dto.game.GameModuleDto;
import com.yisy.modules.doramcu.dto.character.MechanicDto;
import com.yisy.modules.doramcu.gamecore.GameModule;
import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.MechanicRegistry;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.module.ModuleInfo;
import com.yisy.modules.doramcu.gamecore.module.ModuleLifecycleManager;
import com.yisy.modules.doramcu.gamecore.module.ModuleHealthStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller that provides game module metadata for the unified UI launcher.
 * Uses the GameModuleRegistry dynamically instead of hardcoded data.
 */
@RestController
@RequestMapping("/api/v1/games")
public class GameModuleController {

    private final GameModuleRegistry gameModuleRegistry;
    private final MechanicRegistry mechanicRegistry;
    private final ModuleLifecycleManager moduleLifecycleManager;

    @Autowired
    public GameModuleController(GameModuleRegistry gameModuleRegistry, MechanicRegistry mechanicRegistry,
                                 ModuleLifecycleManager moduleLifecycleManager) {
        this.gameModuleRegistry = gameModuleRegistry;
        this.mechanicRegistry = mechanicRegistry;
        this.moduleLifecycleManager = moduleLifecycleManager;
    }

    /**
     * Returns a list of all registered game modules with metadata.
     *
     * @return List of game module DTOs
     */
    @GetMapping("/list")
    public List<GameModuleDto> listGameModules() {
        List<GameModuleDto> modules = new ArrayList<>();

        for (GameModule module : gameModuleRegistry.getAllModules()) {
            String id = module.getGameType().name().toLowerCase().replace('_', '-');
            Set<String> mechanics = module.getMechanics().stream()
                    .map(MechanicType::name)
                    .collect(Collectors.toSet());
            List<String> availableServices = getAvailableServices(module);

            GameModuleDto dto = new GameModuleDto();
            dto.setId(id);
            dto.setName(module.getModuleName());
            dto.setEnabled(module.isEnabled());
            dto.setMechanics(mechanics);
            dto.setAvailableServices(availableServices);
            modules.add(dto);
        }

        return modules;
    }

    /**
     * Returns detailed info for a single game module by type.
     *
     * @param gameType the game type identifier (e.g., "traveller", "dnd-5e")
     * @return module DTO or 404
     */
    @GetMapping("/{gameType}")
    public GameModuleDto getGameModule(@PathVariable String gameType) {
        GameType type = GameType.valueOf(gameType.toUpperCase().replace('-', '_'));
        return gameModuleRegistry.getModule(type).map(module -> {
            String id = module.getGameType().name().toLowerCase().replace('_', '-');
            Set<String> mechanics = module.getMechanics().stream()
                    .map(MechanicType::name)
                    .collect(Collectors.toSet());
            List<String> availableServices = getAvailableServices(module);

            GameModuleDto dto = new GameModuleDto();
            dto.setId(id);
            dto.setName(module.getModuleName());
            dto.setEnabled(module.isEnabled());
            dto.setMechanics(mechanics);
            dto.setAvailableServices(availableServices);
            return dto;
        }).orElse(null);
    }

    /**
     * Returns all mechanics and which modules provide them.
     *
     * @return List of mechanic DTOs
     */
    @GetMapping("/mechanics")
    public List<MechanicDto> listMechanics() {
        List<MechanicDto> mechanics = new ArrayList<>();

        for (MechanicType mechanicType : MechanicType.values()) {
            List<String> providingModules = gameModuleRegistry.getModulesForMechanic(mechanicType).stream()
                    .map(GameModule::getModuleName)
                    .collect(Collectors.toList());

            MechanicDto dto = new MechanicDto();
            dto.setType(mechanicType.name());
            dto.setDisplayName(mechanicType.name());
            dto.setDescription(mechanicType.getDescription() != null ? mechanicType.getDescription() : "");
            dto.setProvidingModules(providingModules);
            mechanics.add(dto);
        }

        return mechanics;
    }

    /**
     * Returns modules that provide a specific mechanic.
     *
     * @param mechanicType the mechanic type (e.g., "COMBAT", "ECONOMY")
     * @return List of module DTOs providing this mechanic
     */
    @GetMapping("/mechanics/{mechanicType}")
    public List<GameModuleDto> getModulesByMechanic(@PathVariable String mechanicType) {
        MechanicType type = MechanicType.valueOf(mechanicType.toUpperCase());
        List<GameModuleDto> modules = new ArrayList<>();

        for (GameModule module : gameModuleRegistry.getModulesForMechanic(type)) {
            String id = module.getGameType().name().toLowerCase().replace('_', '-');
            Set<String> mechanics = module.getMechanics().stream()
                    .map(MechanicType::name)
                    .collect(Collectors.toSet());
            List<String> availableServices = getAvailableServices(module);

            GameModuleDto dto = new GameModuleDto();
            dto.setId(id);
            dto.setName(module.getModuleName());
            dto.setEnabled(module.isEnabled());
            dto.setMechanics(mechanics);
            dto.setAvailableServices(availableServices);
            modules.add(dto);
        }

        return modules;
    }

    /**
     * Checks which service getters return non-null for a module.
     */
    private List<String> getAvailableServices(GameModule module) {
        List<String> services = new ArrayList<>();
        if (module.getDiceService() != null) services.add("dice");
        if (module.getCombatService() != null) services.add("combat");
        if (module.getInventoryService() != null) services.add("inventory");
        if (module.getCraftingService() != null) services.add("crafting");
        if (module.getDungeonRoomService() != null) services.add("dungeon");
        if (module.getSpellService() != null) services.add("spell");
        if (module.getPsionicsService() != null) services.add("psionics");
        if (module.getEncounterService() != null) services.add("encounter");
        if (module.getCharacterService() != null) services.add("character");
        if (module.getCampaignService() != null) services.add("campaign");
        if (module.getRulesService() != null) services.add("rules");
        if (module.getTechLevelAdapter() != null) services.add("techLevel");
        if (module.getRealEstateService() != null) services.add("realEstate");
        if (module.getBuildingInstanceService() != null) services.add("buildingInstance");
        if (module.getGroundCombatAdapter() != null) services.add("groundCombat");
        if (module.getRulesEngine() != null) services.add("rulesEngine");
        return services;
    }

    // ========== Admin Endpoints for Hot-Swap Module Management ==========

    /**
     * Deploy a new module JAR by path.
     * POST /api/v1/modules/admin/deploy?jarPath=/path/to/module.jar
     */
    @PostMapping("/modules/admin/deploy")
    public ResponseEntity<?> deployModule(@RequestParam String jarPath) {
        Path path = Paths.get(jarPath);
        java.util.Optional<ModuleInfo> loaded = moduleLifecycleManager.loadModule(path);
        if (loaded.isPresent()) {
            return ResponseEntity.ok(toDto(loaded.get()));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Failed to deploy module: " + jarPath));
    }

    /**
     * Deploy a new module JAR by file upload.
     * POST /api/v1/modules/admin/deploy/upload
     */
    @PostMapping("/modules/admin/deploy/upload")
    public ResponseEntity<?> deployModuleUpload(@RequestParam("file") MultipartFile file) {
        try {
            String modulesDir = moduleLifecycleManager.getModulesDir();
            Path target = Paths.get(modulesDir).resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            java.util.Optional<ModuleInfo> loaded = moduleLifecycleManager.loadModule(target);
            if (loaded.isPresent()) {
                return ResponseEntity.ok(toDto(loaded.get()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to deploy uploaded module"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Reload an existing module.
     * POST /api/v1/modules/admin/{gameType}/reload
     */
    @PostMapping("/modules/admin/{gameType}/reload")
    public ResponseEntity<?> reloadModule(@PathVariable String gameType) {
        GameType type = GameType.valueOf(gameType.toUpperCase().replace('-', '_'));
        ModuleInfo info = moduleLifecycleManager.reloadModule(type);
        if (info != null) {
            return ResponseEntity.ok(toDto(info));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Module not found or reload failed: " + gameType));
    }

    /**
     * Unload a module.
     * DELETE /api/v1/modules/admin/{gameType}
     */
    @DeleteMapping("/modules/admin/{gameType}")
    public ResponseEntity<?> unloadModule(@PathVariable String gameType) {
        GameType type = GameType.valueOf(gameType.toUpperCase().replace('-', '_'));
        boolean unloaded = moduleLifecycleManager.unloadModule(type);
        if (unloaded) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Module " + gameType + " unloaded"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Module not found: " + gameType));
    }

    /**
     * Toggle a module's enabled state.
     * PUT /api/v1/modules/admin/{gameType}/toggle
     * Body: {"enabled": true}
     */
    @PutMapping("/modules/admin/{gameType}/toggle")
    public ResponseEntity<?> toggleModule(@PathVariable String gameType, @RequestBody Map<String, Boolean> body) {
        GameType type = GameType.valueOf(gameType.toUpperCase().replace('-', '_'));
        boolean enabled = body.getOrDefault("enabled", true);
        ModuleInfo info = enabled ?
                moduleLifecycleManager.enableModule(type) :
                moduleLifecycleManager.disableModule(type);
        if (info != null) {
            return ResponseEntity.ok(toDto(info));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Module not found: " + gameType));
    }

    /**
     * Health status of all dynamically loaded modules.
     * GET /api/v1/modules/admin/health
     */
    @GetMapping("/modules/admin/health")
    public List<GameModuleDto> healthCheck() {
        List<GameModuleDto> dtos = new ArrayList<>();
        for (var entry : moduleLifecycleManager.getAllModuleInfo().entrySet()) {
            ModuleInfo info = entry.getValue();
            dtos.add(toDto(info));
        }
        return dtos;
    }

    /**
     * Detailed info for a specific module.
     * GET /api/v1/modules/admin/{gameType}/info
     */
    @GetMapping("/modules/admin/{gameType}/info")
    public ResponseEntity<?> moduleInfo(@PathVariable String gameType) {
        GameType type = GameType.valueOf(gameType.toUpperCase().replace('-', '_'));
        ModuleInfo info = moduleLifecycleManager.getModuleInfo(type);
        if (info != null) {
            return ResponseEntity.ok(toDto(info));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Convert ModuleInfo to GameModuleDto.
     */
    private GameModuleDto toDto(ModuleInfo info) {
        GameModuleDto dto = new GameModuleDto();
        dto.setId(info.getGameType().name().toLowerCase().replace('_', '-'));
        dto.setName(info.getName());
        dto.setVersion(info.getVersion());
        dto.setEnabled(info.isEnabled());
        dto.setHealth(info.getHealth() != null ? info.getHealth().name() : null);
        if (info.getGameModule() != null) {
            dto.setMechanics(info.getGameModule().getMechanics().stream()
                    .map(MechanicType::name).collect(Collectors.toSet()));
            dto.setAvailableServices(getAvailableServices(info.getGameModule()));
        }
        return dto;
    }
}
