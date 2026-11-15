package com.yisy.modules.doramcu.traveller.exploration;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA entity representing a character's current position in the Traveller universe.
 * <p>
 * Persists the current hex sector location for each character so positions
 * survive server restarts.
 * </p>
 */
@Entity
@Table(name = "traveller_character_position", uniqueConstraints = {
    @UniqueConstraint(columnNames = "character_id")
})
public class CharacterPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Character ID (unique per position record) */
    @Column(name = "character_id", nullable = false, unique = true, length = 100)
    private String characterId;

    /** Current hex sector coordinates (e.g., "1717") */
    @Column(nullable = false, length = 20)
    private String hexSector;

    /** Current star system name */
    @Column(length = 200)
    private String systemName;

    /** Last updated timestamp */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public CharacterPosition() {
    }

    public CharacterPosition(String characterId, String hexSector) {
        this.characterId = Objects.requireNonNull(characterId);
        this.hexSector = Objects.requireNonNull(hexSector);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCharacterId() { return characterId; }
    public void setCharacterId(String characterId) { this.characterId = Objects.requireNonNull(characterId); }

    public String getHexSector() { return hexSector; }
    public void setHexSector(String hexSector) { this.hexSector = Objects.requireNonNull(hexSector); }

    public String getSystemName() { return systemName; }
    public void setSystemName(String systemName) { this.systemName = systemName; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CharacterPosition that = (CharacterPosition) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CharacterPosition{" +
                "characterId='" + characterId + '\'' +
                ", hexSector='" + hexSector + '\'' +
                '}';
    }
}
