package com.yisy.modules.doramcu.hex.dungeon;

import com.yisy.modules.doramcu.hex.HullModule;
import com.yisy.modules.doramcu.hex.M3space;
import com.yisy.modules.doramcu.hex.ShapeDefinition;
import jakarta.persistence.*;

import java.util.*;

/**
 * JPA entity representing a reusable dungeon module template.
 * <p>
 * Extends {@link ModuleTemplate} for shared template properties and behavior.
 * Specializes for dungeon rooms and corridors via the {@link DungeonRoom} type field.
 * </p>
 * <p>
 * Templates are stored in the database and loaded by the {@link DungeonBuilder}
 * to instantiate {@link HullModule} instances for dungeon assembly.
 * </p>
 */
@Entity
@Table(name = "dungeon_module_templates", indexes = {
        @Index(name = "idx_dungeon_module_type", columnList = "type"),
        @Index(name = "idx_dungeon_module_name", columnList = "name")
})
public class DungeonModuleTemplate extends ModuleTemplate {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DungeonRoom type;

    /**
     * @return the dungeon room type for this template
     */
    public DungeonRoom getType() {
        return type;
    }

    /**
     * Sets the dungeon room type.
     *
     * @param type the room type
     */
    public void setType(DungeonRoom type) {
        this.type = type;
    }

    /**
     * Converts the serialized shape cells JSON to a {@link com.yisy.modules.doramcu.hex.MaskShape}.
     * <p>
     * Delegates to {@link ModuleTemplate#toShapeDefinition()} for the common parsing logic.
     * </p>
     *
     * @return a MaskShape representing the module's footprint
     * @throws IllegalArgumentException if shapeCellsJson is null or empty
     */
    public ShapeDefinition toMaskShape() {
        return toShapeDefinition();
    }

    /**
     * Serializes a set of M3space cells to a JSON array string.
     *
     * @param cells the cells to serialize
     * @return JSON array string, e.g., '["0,0,0","1,0,0"]'
     */
    public static String serializeCells(Set<M3space> cells) {
        return ModuleTemplate.serializeCells(cells);
    }

    @Override
    public String toString() {
        return String.format("DungeonModuleTemplate{id=%d, name='%s', type=%s, dimensions=%dx%dx%d}",
                getId(), getName(), type, getWidth(), getHeight(), getDepth());
    }
}
