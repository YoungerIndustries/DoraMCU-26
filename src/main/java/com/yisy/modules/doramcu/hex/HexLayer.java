package com.yisy.modules.doramcu.hex;

import java.util.*;

/**
 * Wraps an {@link M3space} cell and provides stacked layers (like onion skins).
 * <p>
 * A single hex cell can support multiple layers:
 * <pre>
 *   Layer N (outermost)  → Armor plating / exterior hull
 *   Layer N-1            → Structural frame
 *   ...
 *   Layer 1              → Interior bulkheads
 *   Layer 0 (innermost)  → Empty space / compartments
 * </pre>
 * <p>
 * Damage propagates inward through layers: when the outermost layer is destroyed,
 * the next layer becomes exposed and takes the remaining damage.
 * </p>
 * <p>
 * This class enables true multi-layer armor/hull modeling for both ships and buildings.
 * </p>
 */
public class HexLayer {

    private final M3space baseCell;
    private final List<LayerData> layers;
    private Compartment compartment;  // The compartment this cell belongs to (for interior cells)

    /**
     * Creates a new HexLayer wrapping the given M3space cell.
     *
     * @param baseCell the underlying 3D cell
     */
    public HexLayer(M3space baseCell) {
        this.baseCell = baseCell;
        this.layers = new ArrayList<>();
    }

    /**
     * Creates a new HexLayer with the given cell and initial layers.
     * Layers are ordered outermost → innermost.
     *
     * @param baseCell the underlying 3D cell
     * @param initialLayers the initial layer stack (outermost first)
     */
    public HexLayer(M3space baseCell, List<LayerData> initialLayers) {
        this.baseCell = baseCell;
        this.layers = new ArrayList<>(initialLayers != null ? initialLayers : Collections.emptyList());
    }

    // ---- Base cell accessors ----

    /** @return the underlying M3space cell */
    public M3space getBaseCell() {
        return baseCell;
    }

    // ---- Compartment integration ----

    /**
     * Returns the compartment this cell belongs to.
     * For interior cells, this links the layered hull to the compartment model.
     *
     * @return the associated compartment, or null if not set
     */
    public Compartment getCompartment() {
        return compartment;
    }

    /**
     * Sets the compartment this cell belongs to.
     * When layers are destroyed, the compartment integrity will be updated.
     *
     * @param compartment the compartment to associate with this cell
     */
    public void setCompartment(Compartment compartment) {
        this.compartment = compartment;
    }

    // ---- Layer management ----

    /**
     * Adds a new layer to the innermost position.
     *
     * @param layer the layer to add
     */
    public void addLayer(LayerData layer) {
        if (layer == null) {
            throw new IllegalArgumentException("Layer must not be null");
        }
        layers.add(layer);
    }

    /**
     * Adds a new layer at the specified position.
     * Position 0 = outermost, position size() = innermost.
     *
     * @param index the position to insert at
     * @param layer the layer to add
     */
    public void addLayerAt(int index, LayerData layer) {
        if (layer == null) {
            throw new IllegalArgumentException("Layer must not be null");
        }
        layers.add(Math.min(index, layers.size()), layer);
    }

    /**
     * Removes the layer at the specified position.
     *
     * @param index the position to remove
     * @return the removed layer
     */
    public LayerData removeLayer(int index) {
        return layers.remove(index);
    }

    /**
     * Clears all layers.
     */
    public void clearLayers() {
        layers.clear();
    }

    /**
     * @return the number of layers
     */
    public int getLayerCount() {
        return layers.size();
    }

    /**
     * Returns the layer at the specified depth.
     * Depth 0 = outermost layer, depth N = innermost layer.
     *
     * @param depth the layer depth (0 = outermost)
     * @return the layer at that depth, or null if out of range
     */
    public LayerData getLayer(int depth) {
        if (depth < 0 || depth >= layers.size()) {
            return null;
        }
        return layers.get(depth);
    }

    /**
     * @return unmodifiable list of layers (outermost → innermost)
     */
    public List<LayerData> getLayers() {
        return List.copyOf(layers);
    }

    /**
     * Returns the outermost intact layer.
     *
     * @return the outermost non-destroyed layer, or null if no layers
     */
    public LayerData getOutermostIntactLayer() {
        for (LayerData layer : layers) {
            if (!layer.isDestroyed()) {
                return layer;
            }
        }
        return null;
    }

    /**
     * Returns the number of destroyed layers from the outside.
     *
     * @return count of consecutive destroyed layers from the outermost
     */
    public int getDestroyedLayerCount() {
        int count = 0;
        for (LayerData layer : layers) {
            if (layer.isDestroyed()) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    // ---- Damage propagation ----

    /**
     * Applies damage starting from the outermost layer, propagating inward.
     * <p>
     * Damage is applied to the outermost intact layer. If that layer is destroyed,
     * the remaining damage propagates to the next layer, and so on.
     * When all layers are destroyed and this cell belongs to a compartment,
     * the compartment integrity is reduced accordingly.
     * </p>
     *
     * @param damage the total damage to apply
     * @return the remaining damage after all layers are exhausted (0 if fully absorbed)
     */
    public int damageOuter(int damage) {
        if (damage <= 0 || layers.isEmpty()) {
            return damage;
        }

        int layersDestroyedBefore = getDestroyedLayerCount();
        int remainingDamage = damage;
        for (int i = 0; i < layers.size() && remainingDamage > 0; i++) {
            LayerData layer = layers.get(i);
            if (layer.isDestroyed()) {
                continue;
            }

            boolean destroyed = layer.damage(remainingDamage);
            remainingDamage = Math.max(0, remainingDamage - layer.getMaxHitpoints() + layer.getHitpoints());

            if (destroyed && i < layers.size() - 1) {
                // Damage propagates to next layer
                // The remaining damage continues to the next iteration
            }
        }

        // If new layers were destroyed and this cell belongs to a compartment, update compartment integrity
        int layersDestroyedAfter = getDestroyedLayerCount();
        if (layersDestroyedAfter > layersDestroyedBefore && compartment != null) {
            int newLayersDestroyed = layersDestroyedAfter - layersDestroyedBefore;
            compartment.damageFromBreach(newLayersDestroyed);
        }

        return remainingDamage;
    }

    /**
     * Applies damage to a specific layer by depth.
     *
     * @param depth the layer depth (0 = outermost)
     * @param damage the damage amount
     * @return true if the layer was destroyed
     */
    public boolean damageLayer(int depth, int damage) {
        LayerData layer = getLayer(depth);
        if (layer == null) {
            return false;
        }
        return layer.damage(damage);
    }

    // ---- Health calculations ----

    /**
     * Returns the overall health ratio of all layers (0.0 to 1.0).
     *
     * @return average health ratio across all layers
     */
    public double getOverallHealthRatio() {
        if (layers.isEmpty()) {
            return 1.0;
        }
        double total = 0;
        for (LayerData layer : layers) {
            total += layer.getHealthRatio();
        }
        return total / layers.size();
    }

    /**
     * @return true if all layers are intact
     */
    public boolean isFullyIntact() {
        for (LayerData layer : layers) {
            if (!layer.isIntact()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if all layers are destroyed
     */
    public boolean isFullyDestroyed() {
        if (layers.isEmpty()) {
            return false;
        }
        for (LayerData layer : layers) {
            if (!layer.isDestroyed()) {
                return false;
            }
        }
        return true;
    }

    // ---- Repair ----

    /**
     * Repairs all layers by the given amount, starting from the innermost layer.
     *
     * @param amount the repair amount per layer
     */
    public void repairAll(int amount) {
        for (LayerData layer : layers) {
            layer.repair(amount);
        }
    }

    /**
     * Fully repairs all layers.
     */
    public void fullRepair() {
        for (LayerData layer : layers) {
            layer.fullRepair();
        }
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HexLayer{cell=").append(baseCell);
        sb.append(", layers=[");
        for (int i = 0; i < layers.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(layers.get(i));
        }
        sb.append("]}");
        return sb.toString();
    }
}
