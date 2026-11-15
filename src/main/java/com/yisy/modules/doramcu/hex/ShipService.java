package com.yisy.modules.doramcu.hex;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import com.yisy.modules.doramcu.hex.dungeon.ShipModuleTemplate;
import com.yisy.modules.doramcu.hex.dungeon.ShipModuleTemplateRepository;
import com.yisy.modules.doramcu.techlevel.TechLevelService;

import java.util.*;

/**
 * Service for managing ship module templates and ship assembly.
 * <p>
 * Delegates all operations to {@link ShipBuilder}, which provides CRUD operations
 * for module templates, template browsing and filtering, ship creation from templates,
 * interior dungeon room generation, and pre-population of starter module templates.
 * </p>
 */
@Service
public class ShipService {

    private final ShipBuilder builder;

    /**
     * Creates a new ShipService.
     *
     * @param builder the ship builder service
     */
    public ShipService(ShipBuilder builder) {
        this.builder = builder;
    }

    // ---- Module Template CRUD (delegated to ShipBuilder) ----

    /**
     * Creates and saves a new module template.
     *
     * @param template the template to save
     * @return the saved template
     */
    public ShipModuleTemplate createModuleTemplate(ShipModuleTemplate template) {
        return builder.createModuleTemplate(template);
    }

    /**
     * Updates an existing module template.
     *
     * @param template the template with updated data
     * @return the updated template
     */
    public ShipModuleTemplate updateModuleTemplate(ShipModuleTemplate template) {
        return builder.updateModuleTemplate(template);
    }

    /**
     * Deletes a module template by ID.
     *
     * @param id the template ID
     */
    public void deleteModuleTemplate(Long id) {
        builder.deleteModuleTemplate(id);
    }

    /**
     * @return all module templates
     */
    public List<ShipModuleTemplate> getModuleTemplates() {
        return builder.getModuleTemplates();
    }

    /**
     * @param id the template ID
     * @return the template, or empty if not found
     */
    public Optional<ShipModuleTemplate> getModuleTemplate(Long id) {
        return builder.getModuleTemplate(id);
    }

    /**
     * Finds templates by ship module type.
     *
     * @param type the module type
     * @return list of matching templates
     */
    public List<ShipModuleTemplate> getModulesByType(ShipModuleTemplate.ShipModuleType type) {
        return builder.getModulesByType(type);
    }

    /**
     * Finds templates by tag.
     *
     * @param tag the tag to search for
     * @return list of matching templates
     */
    public List<ShipModuleTemplate> getModulesByTag(String tag) {
        return builder.getModulesByTag(tag);
    }

    /**
     * Finds templates compatible with the given ship type.
     *
     * @param shipType the ship type
     * @return list of compatible templates
     */
    public List<ShipModuleTemplate> getModulesForShipType(ShipType shipType) {
        return builder.getModulesForShipType(shipType);
    }

    /**
     * Finds templates by difficulty range.
     *
     * @param maxDifficulty the maximum difficulty
     * @return list of matching templates
     */
    public List<ShipModuleTemplate> getModulesByDifficulty(int maxDifficulty) {
        return builder.getModulesByDifficulty(maxDifficulty);
    }

    /**
     * Finds templates ordered by difficulty.
     *
     * @return all templates sorted by difficulty ascending
     */
    public List<ShipModuleTemplate> getModulesOrderedByDifficulty() {
        return builder.getModulesOrderedByDifficulty();
    }

    // ---- Ship Assembly (delegated to ShipBuilder) ----

    /**
     * Creates a ship from templates and connection definitions.
     *
     * @param name the ship name
     * @param shipType the ship type classification
     * @param templateIds list of template IDs to use
     * @param connections list of connection definitions
     * @return the assembled ship
     */
    public Ship createShip(String name, ShipType shipType,
                           List<Long> templateIds,
                           List<ShipBuilder.ConnectionDefinition> connections) {
        return builder.buildShip(name, shipType, templateIds);
    }

    /**
     * Creates a single-module ship from a template.
     *
     * @param name the ship name
     * @param shipType the ship type
     * @param templateId the template ID
     * @return the assembled ship
     */
    public Ship createShipFromTemplate(String name, ShipType shipType, Long templateId) {
        return builder.buildShip(name, shipType, Collections.singletonList(templateId));
    }

    // ---- Pre-population (delegated to ShipBuilder) ----

    /**
     * Pre-populates the database with starter ship module templates.
     * <p>
     * Creates templates for all 13 ship module types. These are only created if no templates exist yet.
     * </p>
     *
     * @return number of templates created
     */
    public int prepopulateTemplates() {
        return builder.prepopulateTemplates();
    }
}
