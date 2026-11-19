package com.yisy.modules.doramcu.daoc.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DaocMilitaryBridge")
class DaocMilitaryBridgeTest {

    private DaocMilitaryBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new DaocMilitaryBridge();
    }

    @Nested
    @DisplayName("All Mappings")
    class AllMappings {

        @Test
        @DisplayName("returns non-empty mappings")
        void mappingsNotEmpty() {
            Map<String, GenericUnitType> mappings = bridge.getAllMappings();
            assertNotNull(mappings);
            assertTrue(mappings.size() > 40, "Should have 40+ mappings");
        }

        @Test
        @DisplayName("returns a copy of mappings")
        void returnsCopy() {
            Map<String, GenericUnitType> mappings1 = bridge.getAllMappings();
            Map<String, GenericUnitType> mappings2 = bridge.getAllMappings();
            assertNotSame(mappings1, mappings2);
            assertEquals(mappings1, mappings2);
        }
    }

    @Nested
    @DisplayName("Albion Classes")
    class AlbionClasses {

        @Test
        @DisplayName("Armsman maps to INFANTRY")
        void armsman() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("ARMSMAN"));
        }

        @Test
        @DisplayName("Paladin maps to SPECIAL_FORCES")
        void paladin() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("PALADIN"));
        }

        @Test
        @DisplayName("Wizard maps to SPECIAL_FORCES")
        void wizard() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("WIZARD"));
        }

        @Test
        @DisplayName("Sorcerer maps to SPECIAL_FORCES")
        void sorcerer() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("SORCERER"));
        }

        @Test
        @DisplayName("Cleric maps to SPECIAL_FORCES")
        void cleric() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("CLERIC"));
        }

        @Test
        @DisplayName("Friar maps to SPECIAL_FORCES")
        void friar() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("FRIAR"));
        }

        @Test
        @DisplayName("Cabalist maps to SPECIAL_FORCES")
        void cabalist() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("CABALIST"));
        }

        @Test
        @DisplayName("Heretic maps to SPECIAL_FORCES")
        void heretic() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("HERETIC"));
        }

        @Test
        @DisplayName("Necromancer maps to SPECIAL_FORCES")
        void necromancer() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("NECROMANCER"));
        }

        @Test
        @DisplayName("Infiltrator maps to SPECIAL_FORCES")
        void infiltrator() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("INFILTRATOR"));
        }

        @Test
        @DisplayName("Scout maps to INFANTRY")
        void scout() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("SCOUT"));
        }

        @Test
        @DisplayName("Reaver maps to INFANTRY")
        void reaver() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("REAVER"));
        }

        @Test
        @DisplayName("Mercenary maps to INFANTRY")
        void mercenary() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("MERCENARY"));
        }

        @Test
        @DisplayName("Minstrel maps to SPECIAL_FORCES")
        void minstrel() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("MINSTREL"));
        }

        @Test
        @DisplayName("Theurgist maps to SPECIAL_FORCES")
        void theurgist() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("THEURGIST"));
        }
    }

    @Nested
    @DisplayName("Hibernia Classes")
    class HiberniaClasses {

        @Test
        @DisplayName("Champion maps to SPECIAL_FORCES")
        void champion() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("CHAMPION"));
        }

        @Test
        @DisplayName("Druid maps to SPECIAL_FORCES")
        void druid() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("DRUID"));
        }

        @Test
        @DisplayName("Ranger maps to INFANTRY")
        void ranger() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("RANGER"));
        }

        @Test
        @DisplayName("Bard maps to SPECIAL_FORCES")
        void bard() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("BARD"));
        }

        @Test
        @DisplayName("Blademaster maps to INFANTRY")
        void blademaster() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("BLADEMASTER"));
        }

        @Test
        @DisplayName("Valewalker maps to INFANTRY")
        void valewalker() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("VALEWALKER"));
        }

        @Test
        @DisplayName("Vampiir maps to SPECIAL_FORCES")
        void vampiir() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("VAMPIIR"));
        }

        @Test
        @DisplayName("Nightshade maps to SPECIAL_FORCES")
        void nightshade() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("NIGHTSHADE"));
        }

        @Test
        @DisplayName("Animist maps to SPECIAL_FORCES")
        void animist() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("ANIMIST"));
        }

        @Test
        @DisplayName("Banshee maps to SPECIAL_FORCES")
        void banshee() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("BANSHEE"));
        }

        @Test
        @DisplayName("Eldritch maps to SPECIAL_FORCES")
        void eldritch() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("ELDITCH"));
        }

        @Test
        @DisplayName("Enchanter maps to SPECIAL_FORCES")
        void enchanter() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("ENCHANTER"));
        }

        @Test
        @DisplayName("Hero maps to INFANTRY")
        void hero() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("HERO"));
        }

        @Test
        @DisplayName("Mentalist maps to SPECIAL_FORCES")
        void mentalist() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("MENTALIST"));
        }

        @Test
        @DisplayName("Warden maps to INFANTRY")
        void warden() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("WARDEN"));
        }
    }

    @Nested
    @DisplayName("Midgard Classes")
    class MidgardClasses {

        @Test
        @DisplayName("Berserker maps to INFANTRY")
        void berserker() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("BERSERKER"));
        }

        @Test
        @DisplayName("Thane maps to INFANTRY")
        void thane() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("THANE"));
        }

        @Test
        @DisplayName("Runemaster maps to SPECIAL_FORCES")
        void runemaster() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("RUNEMASTER"));
        }

        @Test
        @DisplayName("Spiritmaster maps to SPECIAL_FORCES")
        void spiritmaster() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("SPIRITMASTER"));
        }

        @Test
        @DisplayName("Healer maps to SPECIAL_FORCES")
        void healer() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("HEALER"));
        }

        @Test
        @DisplayName("Hunter maps to INFANTRY")
        void hunter() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("HUNTER"));
        }

        @Test
        @DisplayName("Shadowblade maps to SPECIAL_FORCES")
        void shadowblade() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("SHADOWBLADE"));
        }

        @Test
        @DisplayName("Savage maps to INFANTRY")
        void savage() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("SAVAGE"));
        }

        @Test
        @DisplayName("Shaman maps to SPECIAL_FORCES")
        void shaman() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("SHAMAN"));
        }

        @Test
        @DisplayName("Skald maps to SPECIAL_FORCES")
        void skald() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("SKALD"));
        }

        @Test
        @DisplayName("Bonedancer maps to SPECIAL_FORCES")
        void bonedancer() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("BONEDANCER"));
        }
    }

    @Nested
    @DisplayName("Cross-Realm and Special")
    class CrossRealmAndSpecial {

        @Test
        @DisplayName("Mauler maps to SPECIAL_FORCES")
        void mauler() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("MAULER"));
        }

        @Test
        @DisplayName("Minotaur maps to INFANTRY")
        void minotaur() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("MINOTAUR"));
        }

        @Test
        @DisplayName("Legion maps to SPECIAL_FORCES")
        void legion() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("LEGION"));
        }

        @Test
        @DisplayName("Martyr maps to SPECIAL_FORCES")
        void martyr() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("MARTYR"));
        }

        @Test
        @DisplayName("Warrior maps to INFANTRY")
        void warrior() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("WARRIOR"));
        }

        @Test
        @DisplayName("Mage maps to SPECIAL_FORCES")
        void mage() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("MAGE"));
        }

        @Test
        @DisplayName("Assassin maps to SPECIAL_FORCES")
        void assassin() {
            assertEquals(GenericUnitType.SPECIAL_FORCES, bridge.mapToGenericType("ASSASSIN"));
        }
    }

    @Nested
    @DisplayName("Unknown Unit Types")
    class UnknownUnitTypes {

        @Test
        @DisplayName("unknown unit type defaults to INFANTRY")
        void unknownDefaultsToInfantry() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType("UNKNOWN_CLASS"));
        }

        @Test
        @DisplayName("empty string defaults to INFANTRY")
        void emptyStringDefaultsToInfantry() {
            assertEquals(GenericUnitType.INFANTRY, bridge.mapToGenericType(""));
        }
    }

    @Nested
    @DisplayName("Mapping Distribution")
    class MappingDistribution {

        @Test
        @DisplayName("SPECIAL_FORCES is the most common mapping")
        void specialForcesMostCommon() {
            Map<String, GenericUnitType> mappings = bridge.getAllMappings();
            long specialForces = mappings.values().stream()
                    .filter(t -> t == GenericUnitType.SPECIAL_FORCES).count();
            long infantry = mappings.values().stream()
                    .filter(t -> t == GenericUnitType.INFANTRY).count();

            assertTrue(specialForces > infantry,
                    "SPECIAL_FORCES should be more common than INFANTRY in DaOC");
        }

        @Test
        @DisplayName("all mappings are either INFANTRY or SPECIAL_FORCES")
        void onlyInfantryAndSpecialForces() {
            Map<String, GenericUnitType> mappings = bridge.getAllMappings();
            mappings.values().forEach(type -> {
                assertTrue(type == GenericUnitType.INFANTRY || type == GenericUnitType.SPECIAL_FORCES,
                        "Only INFANTRY and SPECIAL_FORCES should be used");
            });
        }
    }
}
