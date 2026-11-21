package com.yisy.modules.doramcu.dnd5e.controller;

import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eBackground;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eClass;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eClassFeature;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterBackground;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterFeat;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterToolProficiency;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingTool;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eFeat;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSpecies;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSubclass;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eBackgroundService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eClassService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCharacterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eFeatService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eSpeciesService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eToolProficiencyService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eToolProficiencyService.ToolCheckResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for D&D 5e Character-related endpoints.
 * Covers: Characters, Feats, Tool Proficiencies, Backgrounds, Classes, Species.
 */
@RestController
@RequestMapping("/api/v1/dnd5e")
public class Dnd5eCharacterController {

    private final Dnd5eCharacterService characterService;
    private final Dnd5eFeatService featService;
    private final Dnd5eToolProficiencyService toolProficiencyService;
    private final Dnd5eBackgroundService backgroundService;
    private final Dnd5eClassService classService;
    private final Dnd5eSpeciesService speciesService;

    public Dnd5eCharacterController(Dnd5eCharacterService characterService,
                                     Dnd5eFeatService featService,
                                     Dnd5eToolProficiencyService toolProficiencyService,
                                     Dnd5eBackgroundService backgroundService,
                                     Dnd5eClassService classService,
                                     Dnd5eSpeciesService speciesService) {
        this.characterService = characterService;
        this.featService = featService;
        this.toolProficiencyService = toolProficiencyService;
        this.backgroundService = backgroundService;
        this.classService = classService;
        this.speciesService = speciesService;
    }

    // ==================== Characters ====================

    @PostMapping("/characters")
    public Dnd5eCharacter createCharacter(@RequestBody CreateDnd5eCharacterRequest request) {
        return characterService.createCharacter(request);
    }

    @GetMapping("/characters")
    public List<Dnd5eCharacter> listCharacters() {
        return characterService.listCharacters();
    }

    @GetMapping("/characters/{id}")
    public Dnd5eCharacter getCharacter(@PathVariable Long id) {
        return characterService.getCharacter(id);
    }

    @PatchMapping("/characters/{id}")
    public Dnd5eCharacter updateCharacter(@PathVariable Long id, @RequestBody UpdateDnd5eCharacterRequest request) {
        return characterService.updateCharacter(id, request);
    }

    @DeleteMapping("/characters/{id}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable Long id) {
        characterService.deleteCharacter(id);
        return ResponseEntity.ok().build();
    }

    // ==================== Feats ====================

    @GetMapping("/feats")
    public List<Dnd5eFeat> listFeats() {
        return featService.listFeats();
    }

    @GetMapping("/feats/{id}")
    public Dnd5eFeat getFeat(@PathVariable Long id) {
        return featService.getFeat(id);
    }

    @GetMapping("/feats/name/{name}")
    public Dnd5eFeat getFeatByName(@PathVariable String name) {
        return featService.getFeatByName(name);
    }

    @GetMapping("/feats/category/{category}")
    public List<Dnd5eFeat> listFeatsByCategory(@PathVariable String category) {
        return featService.listFeatsByCategory(category);
    }

    @GetMapping("/feats/ability/{ability}")
    public List<Dnd5eFeat> listFeatsByAbility(@PathVariable String ability) {
        return featService.listFeatsByAbility(ability);
    }

    @GetMapping("/feats/search/{keyword}")
    public List<Dnd5eFeat> searchFeats(@PathVariable String keyword) {
        return featService.searchFeats(keyword);
    }

    @GetMapping("/feats/available/{characterId}")
    public List<Dnd5eFeat> listAvailableFeats(@PathVariable Long characterId) {
        return featService.listAvailableFeats(characterId);
    }

    @PostMapping("/feats")
    public Dnd5eFeat createFeat(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String description = (String) body.get("description");
        String category = (String) body.get("category");
        String abilityRequirement = (String) body.get("abilityRequirement");
        int abilityScoreRequired = (int) body.getOrDefault("abilityScoreRequired", 0);
        String prerequisite = (String) body.get("prerequisite");
        boolean repeatable = (boolean) body.getOrDefault("repeatable", false);
        int abilityIncrease = (int) body.getOrDefault("abilityIncrease", 0);
        String abilityIncreased = (String) body.get("abilityIncreased");
        return featService.createFeat(name, description, category, abilityRequirement,
                abilityScoreRequired, prerequisite, repeatable, abilityIncrease, abilityIncreased);
    }

    @DeleteMapping("/feats/{name}")
    public void deleteFeat(@PathVariable String name) {
        featService.deleteFeat(name);
    }

    @PostMapping("/characters/{characterId}/feats/{featName}")
    public Dnd5eCharacterFeat assignFeatToCharacter(@PathVariable Long characterId,
                                                     @PathVariable String featName) {
        return featService.assignFeatToCharacter(characterId, featName);
    }

    @DeleteMapping("/characters/{characterId}/feats/{featName}")
    public void removeFeatFromCharacter(@PathVariable Long characterId,
                                         @PathVariable String featName) {
        featService.removeFeatFromCharacter(characterId, featName);
    }

    @PatchMapping("/characters/{characterId}/feats/{featName}/toggle")
    public Dnd5eCharacterFeat toggleFeatActive(@PathVariable Long characterId,
                                                @PathVariable String featName,
                                                @RequestBody Map<String, Boolean> body) {
        boolean active = body.getOrDefault("active", true);
        return featService.toggleFeatActive(characterId, featName, active);
    }

    @GetMapping("/characters/{characterId}/feats")
    public List<Dnd5eCharacterFeat> listCharacterFeats(@PathVariable Long characterId) {
        return featService.listCharacterFeats(characterId);
    }

    @GetMapping("/characters/{characterId}/feats/active")
    public List<Dnd5eCharacterFeat> listActiveCharacterFeats(@PathVariable Long characterId) {
        return featService.listActiveCharacterFeats(characterId);
    }

    @GetMapping("/characters/{characterId}/feats/{featName}/has")
    public boolean hasFeat(@PathVariable Long characterId,
                            @PathVariable String featName) {
        return featService.hasFeat(characterId, featName);
    }

    // ==================== Tool Proficiencies ====================

    @GetMapping("/tools")
    public List<Dnd5eCraftingTool> listAllTools() {
        return toolProficiencyService.listAllTools();
    }

    @GetMapping("/tools/{name}")
    public Dnd5eCraftingTool getToolByName(@PathVariable String name) {
        return toolProficiencyService.getToolByName(name);
    }

    @GetMapping("/tools/type/{toolType}")
    public List<Dnd5eCraftingTool> listToolsByType(@PathVariable String toolType) {
        return toolProficiencyService.listToolsByType(toolType);
    }

    @PostMapping("/characters/{characterId}/tools/{toolName}")
    public Dnd5eCharacterToolProficiency addToolProficiency(@PathVariable Long characterId,
                                                             @PathVariable String toolName,
                                                             @RequestBody Map<String, Integer> body) {
        int level = body.getOrDefault("level", 1);
        return toolProficiencyService.addProficiency(characterId, toolName, level);
    }

    @DeleteMapping("/characters/{characterId}/tools/{toolName}")
    public ResponseEntity<Void> removeToolProficiency(@PathVariable Long characterId,
                                                       @PathVariable String toolName) {
        toolProficiencyService.removeProficiency(characterId, toolName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/characters/{characterId}/tools")
    public List<Dnd5eCharacterToolProficiency> listToolProficiencies(@PathVariable Long characterId) {
        return toolProficiencyService.listProficiencies(characterId);
    }

    @GetMapping("/characters/{characterId}/tools/active")
    public List<Dnd5eCharacterToolProficiency> listActiveToolProficiencies(@PathVariable Long characterId) {
        return toolProficiencyService.listActiveProficiencies(characterId);
    }

    @GetMapping("/characters/{characterId}/tools/{toolName}/proficient")
    public boolean isToolProficient(@PathVariable Long characterId,
                                     @PathVariable String toolName) {
        return toolProficiencyService.isProficient(characterId, toolName);
    }

    @GetMapping("/characters/{characterId}/tools/{toolName}/expertise")
    public boolean hasToolExpertise(@PathVariable Long characterId,
                                     @PathVariable String toolName) {
        return toolProficiencyService.hasExpertise(characterId, toolName);
    }

    @PatchMapping("/characters/{characterId}/tools/{toolName}/expertise")
    public Dnd5eCharacterToolProficiency setToolExpertise(@PathVariable Long characterId,
                                                           @PathVariable String toolName,
                                                           @RequestBody Map<String, Boolean> body) {
        boolean expertise = body.getOrDefault("expertise", true);
        return toolProficiencyService.setExpertise(characterId, toolName, expertise);
    }

    @PatchMapping("/characters/{characterId}/tools/{toolName}/toggle")
    public Dnd5eCharacterToolProficiency toggleToolActive(@PathVariable Long characterId,
                                                           @PathVariable String toolName) {
        return toolProficiencyService.toggleActive(characterId, toolName);
    }

    @PostMapping("/characters/{characterId}/tools/{toolName}/check")
    public ToolCheckResult makeToolCheck(@PathVariable Long characterId,
                                          @PathVariable String toolName,
                                          @RequestBody Map<String, Integer> body) {
        int dc = body.getOrDefault("dc", 10);
        return toolProficiencyService.makeToolCheck(characterId, toolName, dc);
    }

    // ==================== Backgrounds ====================

    // --- Background CRUD ---

    @GetMapping("/backgrounds")
    public List<Dnd5eBackground> listBackgrounds() {
        return backgroundService.listBackgrounds();
    }

    @GetMapping("/backgrounds/all")
    public List<Dnd5eBackground> listAllBackgrounds() {
        return backgroundService.listAllBackgrounds();
    }

    @GetMapping("/backgrounds/{id}")
    public Dnd5eBackground getBackground(@PathVariable Long id) {
        return backgroundService.getBackground(id);
    }

    @GetMapping("/backgrounds/name/{name}")
    public Dnd5eBackground getBackgroundByName(@PathVariable String name) {
        return backgroundService.getBackgroundByName(name);
    }

    @GetMapping("/backgrounds/category/{category}")
    public List<Dnd5eBackground> listBackgroundsByCategory(@PathVariable String category) {
        return backgroundService.listBackgroundsByCategory(category);
    }

    @GetMapping("/backgrounds/skill/{skill}")
    public List<Dnd5eBackground> listBackgroundsBySkill(@PathVariable String skill) {
        return backgroundService.listBackgroundsBySkill(skill);
    }

    @GetMapping("/backgrounds/search")
    public List<Dnd5eBackground> searchBackgrounds(@RequestParam String keyword) {
        return backgroundService.searchBackgrounds(keyword);
    }

    @GetMapping("/backgrounds/categories")
    public List<String> listBackgroundCategories() {
        return backgroundService.listCategories();
    }

    @PostMapping("/backgrounds")
    public Dnd5eBackground createBackground(@RequestParam String name,
                                             @RequestParam String description,
                                             @RequestParam(defaultValue = "Common") String category,
                                             @RequestParam String skillProficiencies,
                                             @RequestParam(required = false) String toolProficiencies,
                                             @RequestParam(required = false) String featureSlots,
                                             @RequestParam String feature,
                                             @RequestParam(required = false) String featureDescription,
                                             @RequestParam(required = false) String equipment) {
        return backgroundService.createBackground(name, description, category,
                skillProficiencies, toolProficiencies, featureSlots, feature,
                featureDescription, equipment);
    }

    @DeleteMapping("/backgrounds/{name}")
    public ResponseEntity<Void> deleteBackground(@PathVariable String name) {
        try {
            backgroundService.deleteBackground(name);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Proficiencies ---

    @GetMapping("/backgrounds/{backgroundName}/skill-proficiencies")
    public List<String> getBackgroundSkillProficiencies(@PathVariable String backgroundName) {
        return backgroundService.getSkillProficiencies(backgroundName);
    }

    @GetMapping("/backgrounds/{backgroundName}/tool-proficiencies")
    public List<String> getBackgroundToolProficiencies(@PathVariable String backgroundName) {
        return backgroundService.getToolProficiencies(backgroundName);
    }

    @GetMapping("/backgrounds/{backgroundName}/feature-slot-options")
    public List<String> getBackgroundFeatureSlotOptions(@PathVariable String backgroundName) {
        return backgroundService.getFeatureSlotOptions(backgroundName);
    }

    // --- Character Backgrounds ---

    @PostMapping("/characters/{characterId}/backgrounds")
    public ResponseEntity<Dnd5eCharacterBackground> assignBackgroundToCharacter(
            @PathVariable Long characterId,
            @RequestParam String backgroundName,
            @RequestParam(required = false) String featureSelections) {
        try {
            return ResponseEntity.ok(backgroundService.assignBackgroundToCharacter(characterId, backgroundName, featureSelections));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/characters/{characterId}/backgrounds/{backgroundName}")
    public ResponseEntity<Void> removeBackgroundFromCharacter(
            @PathVariable Long characterId,
            @PathVariable String backgroundName) {
        try {
            backgroundService.removeBackgroundFromCharacter(characterId, backgroundName);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/characters/{characterId}/backgrounds")
    public List<Dnd5eCharacterBackground> listCharacterBackgrounds(@PathVariable Long characterId) {
        return backgroundService.listCharacterBackgrounds(characterId);
    }

    @GetMapping("/characters/{characterId}/backgrounds/active")
    public ResponseEntity<Dnd5eCharacterBackground> getActiveCharacterBackground(@PathVariable Long characterId) {
        Dnd5eCharacterBackground bg = backgroundService.getActiveCharacterBackground(characterId);
        if (bg == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(bg);
    }

    // ==================== Classes ====================

    // --- Class CRUD ---

    @GetMapping("/classes")
    public List<Dnd5eClass> listClasses() {
        return classService.listClasses();
    }

    @GetMapping("/classes/{id}")
    public Dnd5eClass getClass(@PathVariable Long id) {
        return classService.getClass(id);
    }

    @GetMapping("/classes/name/{name}")
    public Dnd5eClass getClassByName(@PathVariable String name) {
        return classService.getClassByName(name);
    }

    @GetMapping("/classes/hitdie/{hitDie}")
    public List<Dnd5eClass> listClassesByHitDie(@PathVariable String hitDie) {
        return classService.listClassesByHitDie(hitDie);
    }

    @GetMapping("/classes/ability/{primaryAbility}")
    public List<Dnd5eClass> listClassesByPrimaryAbility(@PathVariable String primaryAbility) {
        return classService.listClassesByPrimaryAbility(primaryAbility);
    }

    @GetMapping("/classes/spellcasting")
    public List<Dnd5eClass> listSpellcastingClasses() {
        return classService.listSpellcastingClasses();
    }

    @GetMapping("/classes/spellcastingType/{spellcastingType}")
    public List<Dnd5eClass> listClassesBySpellcastingType(@PathVariable String spellcastingType) {
        return classService.listClassesBySpellcastingType(spellcastingType);
    }

    @GetMapping("/classes/search")
    public List<Dnd5eClass> searchClasses(@RequestParam String keyword) {
        return classService.searchClasses(keyword);
    }

    @PostMapping("/classes")
    public Dnd5eClass createClass(@RequestBody Dnd5eClass cls) {
        return classService.createClass(cls);
    }

    @PatchMapping("/classes/{id}")
    public Dnd5eClass updateClass(@PathVariable Long id, @RequestBody Dnd5eClass cls) {
        return classService.updateClass(id, cls);
    }

    @DeleteMapping("/classes/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        try {
            classService.deleteClass(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Subclasses ---

    @GetMapping("/subclasses")
    public List<Dnd5eSubclass> listSubclasses() {
        return classService.listSubclasses();
    }

    @GetMapping("/subclasses/class/{className}")
    public List<Dnd5eSubclass> listSubclassesByClass(@PathVariable String className) {
        return classService.listSubclassesByClass(className);
    }

    @GetMapping("/subclasses/name/{name}")
    public Dnd5eSubclass getSubclassByName(@PathVariable String name) {
        return classService.getSubclassByName(name);
    }

    @GetMapping("/subclasses/search")
    public List<Dnd5eSubclass> searchSubclasses(@RequestParam String keyword) {
        return classService.searchSubclasses(keyword);
    }

    @PostMapping("/subclasses")
    public Dnd5eSubclass createSubclass(@RequestBody Dnd5eSubclass subclass) {
        return classService.createSubclass(subclass);
    }

    @DeleteMapping("/subclasses/{id}")
    public ResponseEntity<Void> deleteSubclass(@PathVariable Long id) {
        try {
            classService.deleteSubclass(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Class Features ---

    @GetMapping("/classes/{className}/features")
    public List<Dnd5eClassFeature> listFeaturesByClass(@PathVariable String className) {
        return classService.listFeaturesByClass(className);
    }

    @GetMapping("/classes/{className}/features/level/{level}")
    public List<Dnd5eClassFeature> listFeaturesByClassAndLevel(@PathVariable String className, @PathVariable int level) {
        return classService.listFeaturesByClassAndLevel(className, level);
    }

    // ==================== Species ====================

    @PostMapping("/species")
    public Dnd5eSpecies createSpecies(@RequestBody Dnd5eSpecies species) {
        return speciesService.createSpecies(species);
    }

    @GetMapping("/species")
    public List<Dnd5eSpecies> listSpecies() {
        return speciesService.listSpecies();
    }

    @GetMapping("/species/{id}")
    public ResponseEntity<Dnd5eSpecies> getSpecies(@PathVariable Long id) {
        return speciesService.getSpecies(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/species/name/{name}")
    public ResponseEntity<Dnd5eSpecies> getSpeciesByName(@PathVariable String name) {
        return speciesService.getSpeciesByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/species/{id}")
    public ResponseEntity<Dnd5eSpecies> updateSpecies(@PathVariable Long id, @RequestBody Dnd5eSpecies updated) {
        try {
            return ResponseEntity.ok(speciesService.updateSpecies(id, updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/species/{id}")
    public ResponseEntity<Void> deleteSpecies(@PathVariable Long id) {
        speciesService.deleteSpecies(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/species/category/{category}")
    public List<Dnd5eSpecies> listSpeciesByCategory(@PathVariable String category) {
        return speciesService.listSpeciesByCategory(category);
    }

    @GetMapping("/species/size/{size}")
    public List<Dnd5eSpecies> listSpeciesBySize(@PathVariable String size) {
        return speciesService.listSpeciesBySize(size);
    }

    @GetMapping("/species/subspecies/{subspecies}")
    public List<Dnd5eSpecies> listSpeciesBySubspecies(@PathVariable String subspecies) {
        return speciesService.listSpeciesBySubspecies(subspecies);
    }

    @GetMapping("/species/darkvision")
    public List<Dnd5eSpecies> listSpeciesWithDarkvision() {
        return speciesService.listSpeciesWithDarkvision();
    }

    @GetMapping("/species/lifespan")
    public List<Dnd5eSpecies> listSpeciesByLifespan(@RequestParam int minLifespan) {
        return speciesService.listSpeciesByLifespan(minLifespan);
    }

    @GetMapping("/species/search")
    public List<Dnd5eSpecies> searchSpecies(@RequestParam String keyword) {
        return speciesService.searchSpecies(keyword);
    }

    @GetMapping("/species/trait/{trait}")
    public List<Dnd5eSpecies> listSpeciesByRacialTrait(@PathVariable String trait) {
        return speciesService.listSpeciesByRacialTrait(trait);
    }

    // --- Name Generation ---

    @GetMapping("/species/{speciesName}/generate-name")
    public Map<String, String> generateName(@PathVariable String speciesName,
                                             @RequestParam(defaultValue = "true") boolean male) {
        String first = speciesService.generateFirstName(speciesName, male);
        String last = speciesService.generateLastName(speciesName);
        String full = speciesService.generateFullName(speciesName, male);
        Map<String, String> result = new HashMap<>();
        result.put("firstName", first);
        result.put("lastName", last);
        result.put("fullName", full);
        return result;
    }

    @GetMapping("/species/{speciesName}/generate-title")
    public ResponseEntity<String> generateTitle(@PathVariable String speciesName,
                                                 @RequestParam(defaultValue = "leader") String role) {
        String title = speciesService.generateTitle(speciesName, role);
        if (title.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(title);
    }

    // --- Ability Score Increases ---

    @GetMapping("/species/{speciesName}/ability-increases")
    public ResponseEntity<Map<String, Integer>> getAbilityIncreases(@PathVariable String speciesName) {
        Map<String, Integer> increases = speciesService.getAbilityIncreases(speciesName);
        if (increases.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(increases);
    }

    @GetMapping("/species/{speciesName}/racial-traits")
    public ResponseEntity<List<String>> getRacialTraits(@PathVariable String speciesName) {
        List<String> traits = speciesService.getRacialTraits(speciesName);
        if (traits.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(traits);
    }

    // --- Reference Data ---

    @GetMapping("/species/categories")
    public List<String> listSpeciesCategories() {
        return speciesService.getCategories();
    }

    @GetMapping("/species/sizes")
    public List<String> listSpeciesSizes() {
        return speciesService.getSizes();
    }
}
