package com.yisy.modules.doramcu.earth2026.model;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.gamecore.GameType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a portal room within the Younger Industries HQ.
 * <p>
 * Each portal connects the HQ basement to a specific game module galaxy,
 * allowing players to transition from Earth 2026 to other game systems.
 * The HQ serves as the central transportation hub for the entire DoraMCU universe.
 * </p>
 */
@Entity
@Table(name = "hq_portals", indexes = {
        @Index(name = "idx_hq_portals_hq", columnList = "hq_id"),
        @Index(name = "idx_hq_portals_game_type", columnList = "game_type"),
        @Index(name = "idx_hq_portals_active", columnList = "active")
})
public class HqPortal {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "earth2026_seq")
    @SequenceGenerator(name = "earth2026_seq", sequenceName = "earth2026_sequence", allocationSize = 1)
    private Long id;

    /**
     * The HQ building this portal belongs to.
     */
    @Column(name = "hq_id", nullable = false)
    private Long hqId;

    /**
     * Target game module this portal leads to.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "game_type", nullable = false, length = 50)
    private GameType gameType;

    /**
     * Display name of the portal (e.g., "Traveller Spinward Gate").
     */
    @Column(name = "portal_name", nullable = false, length = 200)
    private String portalName;

    /**
     * Flavor text describing the portal and its destination.
     */
    @Column(length = 1000)
    private String description;

    /**
     * Which basement level the portal is located on (0 = ground, negative = basement).
     */
    @Column(name = "basement_level", nullable = false)
    private int basementLevel = 0;

    /**
     * Whether the portal is currently operational and can be used.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Minimum character level required to use this portal.
     */
    @Column(name = "requires_level", nullable = false)
    private int requiresLevel = 1;

    /**
     * Security clearance required to access this portal (optional).
     */
    @Column(name = "requires_clearance", length = 100)
    private String requiresClearance;

    /**
     * Visual icon or emoji for the portal door.
     */
    @Column(name = "portal_icon", length = 50)
    private String portalIcon;

    /**
     * Visual style theme for the portal door (e.g., "transport-tube", "dungeon-door", "warp-gate").
     * Determines the CSS class and visual appearance of the portal card.
     */
    @Column(name = "portal_style", length = 50)
    private String portalStyle;

    /**
     * When the portal was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Last update time.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public HqPortal() {
    }

    public HqPortal(Long hqId, GameType gameType, String portalName) {
        this.hqId = hqId;
        this.gameType = gameType;
        this.portalName = portalName;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHqId() {
        return hqId;
    }

    public void setHqId(Long hqId) {
        this.hqId = hqId;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public String getPortalName() {
        return portalName;
    }

    public void setPortalName(String portalName) {
        this.portalName = portalName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBasementLevel() {
        return basementLevel;
    }

    public void setBasementLevel(int basementLevel) {
        this.basementLevel = basementLevel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getRequiresLevel() {
        return requiresLevel;
    }

    public void setRequiresLevel(int requiresLevel) {
        this.requiresLevel = requiresLevel;
    }

    public String getRequiresClearance() {
        return requiresClearance;
    }

    public void setRequiresClearance(String requiresClearance) {
        this.requiresClearance = requiresClearance;
    }

    public String getPortalIcon() {
        return portalIcon;
    }

    public void setPortalIcon(String portalIcon) {
        this.portalIcon = portalIcon;
    }

    public String getPortalStyle() {
        return portalStyle;
    }

    public void setPortalStyle(String portalStyle) {
        this.portalStyle = portalStyle;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "HqPortal{id=" + id + ", gameType=" + gameType + ", portalName='" + portalName +
                "', active=" + active + ", basementLevel=" + basementLevel + "}";
    }
}
