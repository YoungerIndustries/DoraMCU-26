package com.yisy.modules.doramcu.skin.dto;

import com.yisy.modules.doramcu.hex.Skin;

/**
 * Request body for creating a skin variant.
 */
public class SkinVariantRequest {

    private String id;
    private String baseSkinId;
    private String name;
    private String description;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private String textureReference;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBaseSkinId() {
        return baseSkinId;
    }

    public void setBaseSkinId(String baseSkinId) {
        this.baseSkinId = baseSkinId;
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
}
