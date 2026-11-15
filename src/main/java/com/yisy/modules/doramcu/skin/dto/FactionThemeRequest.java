package com.yisy.modules.doramcu.skin.dto;

import com.yisy.modules.doramcu.hex.Skin;

/**
 * Request body for creating or updating a faction skin theme.
 */
public class FactionThemeRequest {

    private String factionId;
    private String name;
    private String defaultSkinId;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private String emblemSymbol;

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
}
