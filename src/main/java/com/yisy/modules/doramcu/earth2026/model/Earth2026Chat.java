package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a chat message in the Earth 2026 MMOG.
 * <p>
 * Messages can be location-based (public channel for all characters at a location)
 * or private (direct message between two characters). Messages are persisted
 * for history and support real-time delivery via SSE.
 * </p>
 */
@Entity
@Table(name = "earth2026_chat_messages", indexes = {
        @Index(name = "idx_earth2026_chat_sender", columnList = "senderId"),
        @Index(name = "idx_earth2026_chat_location", columnList = "locationId"),
        @Index(name = "idx_earth2026_chat_created", columnList = "createdAt")
})
public class Earth2026Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The character who sent the message.
     */
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /**
     * The recipient character for private messages; null for location-based messages.
     */
    @Column(name = "recipient_id")
    private Long recipientId;

    /**
     * The location where the message was sent; null for private messages.
     */
    @Column(name = "location_id")
    private Long locationId;

    /**
     * The party this message was sent to; null for non-party messages.
     */
    @Column(name = "party_id")
    private Long partyId;

    /**
     * The message content.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Message type: "public" for location-based, "private" for DM, "party" for party chat.
     */
    @Column(nullable = false, length = 10)
    private String messageType = "public";

    /**
     * When the message was sent.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Earth2026Chat() {
    }

    public Earth2026Chat(Long senderId, String content) {
        this.senderId = senderId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getPartyId() {
        return partyId;
    }

    public void setPartyId(Long partyId) {
        this.partyId = partyId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Earth2026Chat{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", messageType='" + messageType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
