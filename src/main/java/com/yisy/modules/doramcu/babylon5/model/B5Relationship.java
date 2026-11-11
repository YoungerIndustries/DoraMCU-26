package com.yisy.modules.doramcu.babylon5.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a relationship between two Babylon 5 characters.
 */
public class B5Relationship {

    @JsonProperty("character")
    private String characterId;

    @JsonProperty("type")
    private String type;

    public B5Relationship() {
    }

    public B5Relationship(String characterId, String type) {
        this.characterId = characterId;
        this.type = type;
    }

    public String getCharacterId() {
        return characterId;
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "B5Relationship{characterId='" + characterId + "', type='" + type + "'}";
    }
}
