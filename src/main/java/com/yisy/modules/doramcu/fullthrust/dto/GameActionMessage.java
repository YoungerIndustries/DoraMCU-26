package com.yisy.modules.doramcu.fullthrust.dto;
/**
 * DTO for WebSocket messages sent from client to server.
 */
public class GameActionMessage {
    private String type; // "JOIN", "LEAVE", "MOVE", "FIRE", "END_TURN", "CREATE_GAME", "ADD_PLAYER", "DEPLOY_SHIP", "START_GAME"
    private String sessionId;
    private String playerId;
    private String playerName;
    private String faction;
    private boolean isAI;
    private String aiDifficulty;
    private String shipId;
    private String shipClassId;
    private String shipName;
    private String targetId;
    private int weaponIndex;
    private int headingChange;
    private int thrust;
    private int startQ;
    private int startR;
    private String gameName;

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getFaction() { return faction; }
    public void setFaction(String faction) { this.faction = faction; }
    public boolean isAI() { return isAI; }
    public void setIsAI(boolean isAI) { this.isAI = isAI; }
    public String getAiDifficulty() { return aiDifficulty; }
    public void setAiDifficulty(String aiDifficulty) { this.aiDifficulty = aiDifficulty; }
    public String getShipId() { return shipId; }
    public void setShipId(String shipId) { this.shipId = shipId; }
    public String getShipClassId() { return shipClassId; }
    public void setShipClassId(String shipClassId) { this.shipClassId = shipClassId; }
    public String getShipName() { return shipName; }
    public void setShipName(String shipName) { this.shipName = shipName; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public int getWeaponIndex() { return weaponIndex; }
    public void setWeaponIndex(int weaponIndex) { this.weaponIndex = weaponIndex; }
    public int getHeadingChange() { return headingChange; }
    public void setHeadingChange(int headingChange) { this.headingChange = headingChange; }
    public int getThrust() { return thrust; }
    public void setThrust(int thrust) { this.thrust = thrust; }
    public int getStartQ() { return startQ; }
    public void setStartQ(int startQ) { this.startQ = startQ; }
    public int getStartR() { return startR; }
    public void setStartR(int startR) { this.startR = startR; }
    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }
}
