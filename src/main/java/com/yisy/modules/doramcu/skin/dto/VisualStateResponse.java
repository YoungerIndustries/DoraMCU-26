package com.yisy.modules.doramcu.skin.dto;

import com.yisy.modules.doramcu.hex.Skin;

import com.yisy.modules.doramcu.hex.DamageTier;
import com.yisy.modules.doramcu.hex.SkinType;

import java.util.List;
import java.util.Map;

/**
 * Response body for the visual state of an asset, including skin, decals, and damage overlays.
 */
public class VisualStateResponse {

    private int assetId;
    private SkinType assetType;
    private String assetName;

    // Skin information
    private String skinId;
    private String skinName;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;

    // Decals on the asset
    private List<DecalResponse> decals;

    // Damage overlay per compartment: compartment name -> worst damage tier
    private Map<String, String> compartmentDamage;

    // Overall worst damage tier across all compartments
    private String overallWorstDamage;

    // Damage overlay description for the overall worst tier
    private String damageOverlayDescription;

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public SkinType getAssetType() {
        return assetType;
    }

    public void setAssetType(SkinType assetType) {
        this.assetType = assetType;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getSkinId() {
        return skinId;
    }

    public void setSkinId(String skinId) {
        this.skinId = skinId;
    }

    public String getSkinName() {
        return skinName;
    }

    public void setSkinName(String skinName) {
        this.skinName = skinName;
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

    public List<DecalResponse> getDecals() {
        return decals;
    }

    public void setDecals(List<DecalResponse> decals) {
        this.decals = decals;
    }

    public Map<String, String> getCompartmentDamage() {
        return compartmentDamage;
    }

    public void setCompartmentDamage(Map<String, String> compartmentDamage) {
        this.compartmentDamage = compartmentDamage;
    }

    public String getOverallWorstDamage() {
        return overallWorstDamage;
    }

    public void setOverallWorstDamage(String overallWorstDamage) {
        this.overallWorstDamage = overallWorstDamage;
    }

    public String getDamageOverlayDescription() {
        return damageOverlayDescription;
    }

    public void setDamageOverlayDescription(String damageOverlayDescription) {
        this.damageOverlayDescription = damageOverlayDescription;
    }
}
