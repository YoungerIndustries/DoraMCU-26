package com.yisy.modules.doramcu.eve.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.yisy.modules.doramcu.neuron.service.CdcEntityListener;

/**
 * JPA entity representing an EVE Online corporation.
 * Tracks corporation name, ticker, CEO, and related metadata.
 */
@Entity
@Table(name = "eve_corporations")
@EntityListeners(CdcEntityListener.class)
public class EveCorporation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Full corporation name, e.g., "Younger Industries".
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Short ticker symbol, e.g., "YISY".
     */
    @Column(nullable = false, length = 10, unique = true)
    private String ticker;

    /**
     * CEO display name.
     */
    @Column(length = 100)
    private String ceoName;

    /**
     * CEO player ID reference.
     */
    @Column(length = 50)
    private String ceoPlayerId;

    /**
     * Description of the corporation.
     */
    @Column(length = 500)
    private String description;

    /**
     * Parent corporation ticker, if this is a subsidiary.
     */
    @Column(length = 10)
    private String parentTicker;

    /**
     * Corporation credit balance for galaxy creation and other paid services.
     */
    @Column
    private Long creditBalance;

    public EveCorporation() {
    }

    public EveCorporation(String name, String ticker, String ceoName) {
        this.name = name;
        this.ticker = ticker;
        this.ceoName = ceoName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getCeoName() {
        return ceoName;
    }

    public void setCeoName(String ceoName) {
        this.ceoName = ceoName;
    }

    public String getCeoPlayerId() {
        return ceoPlayerId;
    }

    public void setCeoPlayerId(String ceoPlayerId) {
        this.ceoPlayerId = ceoPlayerId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentTicker() {
        return parentTicker;
    }

    public void setParentTicker(String parentTicker) {
        this.parentTicker = parentTicker;
    }

    public Long getCreditBalance() {
        return creditBalance;
    }

    public void setCreditBalance(Long creditBalance) {
        this.creditBalance = creditBalance;
    }

    @Override
    public String toString() {
        return "EveCorporation{id=" + id + ", name='" + name + "', ticker='" + ticker +
                "', ceo='" + ceoName + "'}";
    }
}
