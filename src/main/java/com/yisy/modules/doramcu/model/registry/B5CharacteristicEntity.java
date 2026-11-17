package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for Babylon 5 specific characteristics.
 * <p>
 * Covers ST, DX, EN, IN, ED, and SO.
 * </p>
 */
@Entity
@Table(name = "b5_characteristic", indexes = {
        @Index(name = "idx_b5c_char_code", columnList = "char_code")
})
public class B5CharacteristicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "char_code", nullable = false, unique = true, length = 10)
    private String charCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    public B5CharacteristicEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCharCode() {
        return charCode;
    }

    public void setCharCode(String charCode) {
        this.charCode = charCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
