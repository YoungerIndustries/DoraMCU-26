package com.yisy.modules.doramcu.fps.model;

/**
 * WebRTC ICE candidate message for voice chat.
 */
public class IceCandidateMessage {

    private String candidate;
    private String sdpMid;
    private int sdpMLineIndex;
    private String from;
    private String to;

    public IceCandidateMessage() {}

    public IceCandidateMessage(String candidate, String sdpMid, int sdpMLineIndex, String from, String to) {
        this.candidate = candidate;
        this.sdpMid = sdpMid;
        this.sdpMLineIndex = sdpMLineIndex;
        this.from = from;
        this.to = to;
    }

    public String getCandidate() { return candidate; }
    public void setCandidate(String candidate) { this.candidate = candidate; }
    public String getSdpMid() { return sdpMid; }
    public void setSdpMid(String sdpMid) { this.sdpMid = sdpMid; }
    public int getSdpMLineIndex() { return sdpMLineIndex; }
    public void setSdpMLineIndex(int sdpMLineIndex) { this.sdpMLineIndex = sdpMLineIndex; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
}
