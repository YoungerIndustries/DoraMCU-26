package com.yisy.modules.doramcu.skin.dto;

import com.yisy.modules.doramcu.hex.Skin;

import com.yisy.modules.doramcu.hex.SkinType;

/**
 * Request body for assigning a skin to an asset.
 */
public class SkinAssignRequest {

    private int assetId;
    private SkinType assetType;
    private String skinId;

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

    public String getSkinId() {
        return skinId;
    }

    public void setSkinId(String skinId) {
        this.skinId = skinId;
    }
}
