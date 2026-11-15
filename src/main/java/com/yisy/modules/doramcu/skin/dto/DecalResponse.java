package com.yisy.modules.doramcu.skin.dto;

import com.yisy.modules.doramcu.hex.Skin;
import com.yisy.modules.doramcu.hex.Decal;

import com.yisy.modules.doramcu.hex.DecalType;

/**
 * Response body for a decal.
 */
public class DecalResponse {

    private String id;
    private DecalType type;
    private String content;
    private String position;
    private double size;
    private double rotation;
    private double opacity;
    private String color;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DecalType getType() {
        return type;
    }

    public void setType(DecalType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
