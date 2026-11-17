package com.yisy.modules.doraai.dto;

import java.util.Map;

/**
 * Response for voice command interpretation.
 */
public class VoiceCommandResponse {

    /** Type of command: MOVEMENT, COMBAT, INTERACTION, QUERY, CHAT, UNKNOWN */
    private String commandType;
    private Map<String, Object> parameters;
    private boolean executed;
    private String message;
    private String transcribedText;

    public VoiceCommandResponse() {
    }

    public VoiceCommandResponse(String commandType, Map<String, Object> parameters, boolean executed, String message) {
        this.commandType = commandType;
        this.parameters = parameters;
        this.executed = executed;
        this.message = message;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTranscribedText() {
        return transcribedText;
    }

    public void setTranscribedText(String transcribedText) {
        this.transcribedText = transcribedText;
    }
}
