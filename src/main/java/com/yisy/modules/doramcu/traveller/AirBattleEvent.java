package com.yisy.modules.doramcu.traveller;

/**
 * Individual event during an air battle.
 */
public class AirBattleEvent {
    private int round;
    private String message;
    private long timestamp;

    public AirBattleEvent() {}

    public AirBattleEvent(int round, String message) {
        this.round = round;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public int getRound() { return round; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}
