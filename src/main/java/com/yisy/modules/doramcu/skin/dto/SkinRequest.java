package com.yisy.modules.doramcu.skin.dto;

import com.yisy.modules.doramcu.hex.Skin;
import com.yisy.modules.doramcu.hex.DamageOverlay;

import com.yisy.modules.doramcu.hex.DamageTier;
import com.yisy.modules.doramcu.hex.SkinType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Request body for creating or updating a skin.
 */
public class SkinRequest {

    private String id;
    private SkinType type;
    private String name;
    private String description;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private String textureReference;
    private String factionId;
    private List<DecalRequest> decals;
    private Map<String, String> damageOverlay;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SkinType getType() {
        return type;
    }

    public void setType(SkinType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getTextureReference() {
        return textureReference;
    }

    public void setTextureReference(String textureReference) {
        this.textureReference = textureReference;
    }

    public String getFactionId() {
        return factionId;
    }

    public void setFactionId(String factionId) {
        this.factionId = factionId;
    }

    public List<DecalRequest> getDecals() {
        return decals;
    }

    public void setDecals(List<DecalRequest> decals) {
        this.decals = decals;
    }

    public Map<String, String> getDamageOverlay() {
        return damageOverlay;
    }

    public void setDamageOverlay(Map<String, String> damageOverlay) {
        this.damageOverlay = damageOverlay;
    }
}
