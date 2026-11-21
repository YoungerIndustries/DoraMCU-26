package com.yisy.modules.doramcu.fullthrust.model;

/**
 * Model class for event log entries.
 */
public class EventLog {

    private Long id;
    private int turnNumber;
    private String message;

    public EventLog() {}

    public EventLog(int turnNumber, String message) {
        this.turnNumber = turnNumber;
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
