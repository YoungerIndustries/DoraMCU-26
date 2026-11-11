package com.yisy.modules.doramcu.dto;

import com.yisy.modules.doramcu.model.CharacterRole;
import com.yisy.modules.doramcu.model.CharacterType;

import java.time.LocalDateTime;

/**
 * Response DTO for a player character registry entry.
 */
public class PlayerCharacterEntryDto {

    private Long id;
    private Long playerId;
    private String playerName;
    private CharacterType characterType;
    private Long characterId;
    private String characterName;
    private CharacterRole characterRole;
    private String galaxyName;
    private boolean isAiControlled;
    private String homeLocationId;
    private String homeLocationType;
    private LocalDateTime createdAt;

    public PlayerCharacterEntryDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public CharacterType getCharacterType() {
        return characterType;
    }

    public void setCharacterType(CharacterType characterType) {
        this.characterType = characterType;
    }

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public CharacterRole getCharacterRole() {
        return characterRole;
    }

    public void setCharacterRole(CharacterRole characterRole) {
        this.characterRole = characterRole;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    public boolean isAiControlled() {
        return isAiControlled;
    }

    public void setAiControlled(boolean aiControlled) {
        isAiControlled = aiControlled;
    }

    public String getHomeLocationId() {
        return homeLocationId;
    }

    public void setHomeLocationId(String homeLocationId) {
        this.homeLocationId = homeLocationId;
    }

    public String getHomeLocationType() {
        return homeLocationType;
    }

    public void setHomeLocationType(String homeLocationType) {
        this.homeLocationType = homeLocationType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
