package com.yisy.modules.doramcu.hex.planet.npc;

import com.yisy.modules.doramcu.hex.Hex3D;

import java.util.UUID;

/**
 * Represents an NPC in the tycoon world.
 * <p>
 * NPCs include:
 * <ul>
 *   <li>Vehicle operators (train drivers, bus drivers, pilots)</li>
 *   <li>Factory workers</li>
 *   <li>Station staff (station masters, cargo handlers)</li>
 *   <li>Shop owners</li>
 * </ul>
 * </p>
 */
public class TycoonNPC {

    public enum NpcType {
        /** Train driver */
        TRAIN_DRIVER,
        /** Bus driver */
        BUS_DRIVER,
        /** Truck driver */
        TRUCK_DRIVER,
        /** Ship captain */
        SHIP_CAPTAIN,
        /** Aircraft pilot */
        PILOT,
        /** Factory worker */
        FACTORY_WORKER,
        /** Station master */
        STATION_MASTER,
        /** Cargo handler */
        CARGO_HANDLER,
        /** Shop owner */
        SHOP_OWNER
    }

    private final String id;
    private final NpcType type;
    private final String name;
    private final String entityId;
    private Hex3D position;
    private final boolean active;

    /**
     * Creates an NPC.
     *
     * @param type the NPC type
     * @param name the NPC's name
     * @param entityId the entity ID this NPC is associated with (vehicle, industry, station)
     * @param position the NPC's current position
     */
    public TycoonNPC(NpcType type, String name, String entityId, Hex3D position) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.name = name;
        this.entityId = entityId;
        this.position = position;
        this.active = true;
    }

    /** @return the unique NPC ID */
    public String getId() {
        return id;
    }

    /** @return the NPC type */
    public NpcType getType() {
        return type;
    }

    /** @return the NPC's name */
    public String getName() {
        return name;
    }

    /** @return the entity ID this NPC is associated with */
    public String getEntityId() {
        return entityId;
    }

    /** @return the NPC's current position */
    public Hex3D getPosition() {
        return position;
    }

    /**
     * Updates the NPC's position.
     *
     * @param position the new position
     */
    public void setPosition(Hex3D position) {
        this.position = position;
    }

    /** @return whether the NPC is active */
    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return String.format("TycoonNPC{name=%s, type=%s, entity=%s}", name, type, entityId);
    }
}
