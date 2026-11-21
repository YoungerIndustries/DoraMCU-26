package com.yisy.modules.doramcu.hex.chat;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable chat message representing a single chat entry in a game session.
 * <p>
 * Supports channels (general, tactics, ooc), message types (text, system, whisper),
 * and optional target player for whispers.
 * </p>
 */
public class ChatMessage {

    public enum Channel {
        GENERAL("general"),
        TACTICS("tactics"),
        OOC("ooc");

        private final String name;

        Channel(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Channel fromName(String name) {
            for (Channel c : values()) {
                if (c.name.equalsIgnoreCase(name)) {
                    return c;
                }
            }
            return GENERAL;
        }
    }

    public enum MessageType {
        TEXT,
        SYSTEM,
        WHISPER
    }

    private final String id;
    private final String sessionId;
    private final long senderId;
    private final String senderName;
    private final String content;
    private final Channel channel;
    private final MessageType type;
    private final Long targetPlayerId;
    private final Instant timestamp;

    private ChatMessage(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        this.sessionId = builder.sessionId;
        this.senderId = builder.senderId;
        this.senderName = builder.senderName != null ? builder.senderName : "Player" + builder.senderId;
        this.content = builder.content;
        this.channel = builder.channel != null ? builder.channel : Channel.GENERAL;
        this.type = builder.type != null ? builder.type : MessageType.TEXT;
        this.targetPlayerId = builder.targetPlayerId;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public Channel getChannel() {
        return channel;
    }

    public MessageType getType() {
        return type;
    }

    public Long getTargetPlayerId() {
        return targetPlayerId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "[" + channel.getName() + "] " + senderName + ": " + content;
    }

    public static class Builder {
        private String id;
        private String sessionId;
        private long senderId;
        private String senderName;
        private String content;
        private Channel channel;
        private MessageType type;
        private Long targetPlayerId;
        private Instant timestamp;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder senderId(long senderId) {
            this.senderId = senderId;
            return this;
        }

        public Builder senderName(String senderName) {
            this.senderName = senderName;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder channel(Channel channel) {
            this.channel = channel;
            return this;
        }

        public Builder type(MessageType type) {
            this.type = type;
            return this;
        }

        public Builder targetPlayerId(Long targetPlayerId) {
            this.targetPlayerId = targetPlayerId;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ChatMessage build() {
            return new ChatMessage(this);
        }
    }
}
