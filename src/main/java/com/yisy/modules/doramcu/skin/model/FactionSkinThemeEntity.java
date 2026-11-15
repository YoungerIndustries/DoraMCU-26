package com.yisy.modules.doramcu.skin.model;

import com.yisy.modules.doramcu.hex.Skin;

import jakarta.persistence.*;

/**
 * JPA entity representing a faction's default skin theme.
 * <p>
 * A faction skin theme defines the default appearance for all units belonging to
 * a faction, including the default skin reference, color palette, and emblem symbol.
 * </p>
 */
@Entity
@Table(name = "faction_skin_themes", indexes = {
        @Index(name = "idx_faction_theme_id", columnList = "factionId", unique = true)
})
public class FactionSkinThemeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "faction_id", nullable = false, length = 100, unique = true)
    private String factionId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "default_skin_id", length = 100)
    private String defaultSkinId = "";

    @Column(name = "primary_color", nullable = false, length = 20)
    private String primaryColor = "#808080";

    @Column(name = "secondary_color", nullable = false, length = 20)
    private String secondaryColor = "#606060";

    @Column(name = "accent_color", nullable = false, length = 20)
    private String accentColor = "#4040ff";

    @Column(name = "emblem_symbol", length = 100)
    private String emblemSymbol = "";

    public FactionSkinThemeEntity() {
    }

    public FactionSkinThemeEntity(String factionId) {
        this.factionId = factionId;
        this.name = factionId;
    }

    // ---- Accessors ----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFactionId() {
        return factionId;
    }

    public void setFactionId(String factionId) {
        this.factionId = factionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultSkinId() {
        return defaultSkinId;
    }

    public void setDefaultSkinId(String defaultSkinId) {
        this.defaultSkinId = defaultSkinId;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public String getEmblemSymbol() {
        return emblemSymbol;
    }

    public void setEmblemSymbol(String emblemSymbol) {
        this.emblemSymbol = emblemSymbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactionSkinThemeEntity that = (FactionSkinThemeEntity) o;
        return factionId != null && factionId.equals(that.factionId);
    }

    @Override
    public int hashCode() {
        return factionId != null ? factionId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "FactionSkinThemeEntity{" +
                "id=" + id +
                ", factionId='" + factionId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
