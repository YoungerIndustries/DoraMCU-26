package com.yisy.modules.doramcu.traveller.shipcustomization;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for ship customization in the Traveller module.
 * <p>
 * Endpoints at /api/v1/traveller/ship-customization for managing
 * ship components and custom ship designs.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/ship-customization")
public class ShipCustomizationController {

    private final ShipCustomizationService service;

    public ShipCustomizationController(ShipCustomizationService service) {
        this.service = service;
    }

    // ---- Component Endpoints ----

    /**
     * GET /components - List all available components.
     */
    @GetMapping("/components")
    public ResponseEntity<List<ShipComponent>> getAllComponents() {
        return ResponseEntity.ok(service.getAllComponents());
    }

    /**
     * GET /components/{type} - List components by type.
     */
    @GetMapping("/components/{type}")
    public ResponseEntity<List<ShipComponent>> getComponentsByType(@PathVariable ComponentType type) {
        return ResponseEntity.ok(service.getComponentsByType(type));
    }

    /**
     * GET /components/{type}/tl/{techLevel} - List components by type and tech level.
     */
    @GetMapping("/components/{type}/tl/{techLevel}")
    public ResponseEntity<List<ShipComponent>> getComponentsByTypeAndTech(
            @PathVariable ComponentType type, @PathVariable int techLevel) {
        return ResponseEntity.ok(service.getComponentsByTypeAndTech(type, techLevel));
    }

    // ---- Design Endpoints ----

    /**
     * POST /designs - Create a new custom ship design.
     */
    @PostMapping("/designs")
    public ResponseEntity<CustomShipDesign> createDesign(@RequestBody DesignRequest request) {
        CustomShipDesign design = service.createDesign(
                request.getName(),
                request.getOwnerId(),
                request.getTotalTonnage(),
                request.getTechLevel()
        );
        return ResponseEntity.ok(design);
    }

    /**
     * GET /designs/{id} - Get a design by ID.
     */
    @GetMapping("/designs/{id}")
    public ResponseEntity<CustomShipDesign> getDesign(@PathVariable Long id) {
        return service.getDesign(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /designs/owner/{ownerId} - Get all designs for an owner.
     */
    @GetMapping("/designs/owner/{ownerId}")
    public ResponseEntity<List<CustomShipDesign>> getDesignsByOwner(@PathVariable String ownerId) {
        return ResponseEntity.ok(service.getDesignsByOwner(ownerId));
    }

    /**
     * PUT /designs/{id} - Update a design's name or description.
     */
    @PutMapping("/designs/{id}")
    public ResponseEntity<CustomShipDesign> updateDesign(
            @PathVariable Long id, @RequestBody DesignUpdateRequest request) {
        CustomShipDesign design = service.updateDesign(id, request.getName(), request.getDescription());
        return ResponseEntity.ok(design);
    }

    /**
     * DELETE /designs/{id} - Delete a design.
     */
    @DeleteMapping("/designs/{id}")
    public ResponseEntity<Void> deleteDesign(@PathVariable Long id) {
        service.deleteDesign(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Component Slot Endpoints ----

    /**
     * POST /designs/{id}/components - Add a component to a design slot.
     */
    @PostMapping("/designs/{id}/components")
    public ResponseEntity<CustomShipDesign> addComponent(
            @PathVariable Long id, @RequestBody ComponentSlotRequest request) {
        CustomShipDesign design = service.addComponent(id, request.getSlotName(), request.getComponentId());
        return ResponseEntity.ok(design);
    }

    /**
     * DELETE /designs/{id}/components/{slotName} - Remove a component from a slot.
     */
    @DeleteMapping("/designs/{id}/components/{slotName}")
    public ResponseEntity<CustomShipDesign> removeComponent(
            @PathVariable Long id, @PathVariable String slotName) {
        CustomShipDesign design = service.removeComponent(id, slotName);
        return ResponseEntity.ok(design);
    }

    /**
     * POST /designs/{id}/validate - Validate a design and calculate derived stats.
     */
    @PostMapping("/designs/{id}/validate")
    public ResponseEntity<CustomShipDesign> validateDesign(@PathVariable Long id) {
        try {
            CustomShipDesign design = service.validateDesign(id);
            return ResponseEntity.ok(design);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ---- Request DTOs ----

    public static class DesignRequest {
        private String name;
        private String ownerId;
        private int totalTonnage;
        private int techLevel = 11;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
        public int getTotalTonnage() { return totalTonnage; }
        public void setTotalTonnage(int totalTonnage) { this.totalTonnage = totalTonnage; }
        public int getTechLevel() { return techLevel; }
        public void setTechLevel(int techLevel) { this.techLevel = techLevel; }
    }

    public static class DesignUpdateRequest {
        private String name;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class ComponentSlotRequest {
        private String slotName;
        private Long componentId;

        public String getSlotName() { return slotName; }
        public void setSlotName(String slotName) { this.slotName = slotName; }
        public Long getComponentId() { return componentId; }
        public void setComponentId(Long componentId) { this.componentId = componentId; }
    }
}
