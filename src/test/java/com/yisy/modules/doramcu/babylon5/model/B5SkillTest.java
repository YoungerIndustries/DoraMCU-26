package com.yisy.modules.doramcu.babylon5.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("B5Skill Tests")
class B5SkillTest {

    @Nested
    @DisplayName("Enum values")
    class EnumValues {

        @Test
        @DisplayName("All skill enum values exist")
        void allValues() {
            B5Skill[] values = B5Skill.values();
            assertEquals(37, values.length);
        }

        @Test
        @DisplayName("Combat skills")
        void combatSkills() {
            assertSame(B5Skill.MELEE, B5Skill.valueOf("MELEE"));
            assertSame(B5Skill.FIREARMS, B5Skill.valueOf("FIREARMS"));
            assertSame(B5Skill.SKIRMISH, B5Skill.valueOf("SKIRMISH"));
            assertSame(B5Skill.GUERRILLA, B5Skill.valueOf("GUERRILLA"));
            assertSame(B5Skill.HAND_TO_HAND, B5Skill.valueOf("HAND_TO_HAND"));
        }

        @Test
        @DisplayName("Leadership skills")
        void leadershipSkills() {
            assertSame(B5Skill.LEADERSHIP, B5Skill.valueOf("LEADERSHIP"));
            assertSame(B5Skill.TACTICS, B5Skill.valueOf("TACTICS"));
            assertSame(B5Skill.DIPLOMACY, B5Skill.valueOf("DIPLOMACY"));
        }

        @Test
        @DisplayName("Technical skills")
        void technicalSkills() {
            assertSame(B5Skill.ELECTRONICS, B5Skill.valueOf("ELECTRONICS"));
            assertSame(B5Skill.COMPUTERS, B5Skill.valueOf("COMPUTERS"));
            assertSame(B5Skill.ENGINEERING, B5Skill.valueOf("ENGINEERING"));
            assertSame(B5Skill.WEAPONS_SYSTEMS, B5Skill.valueOf("WEAPONS_SYSTEMS"));
            assertSame(B5Skill.SHIELD_SYSTEMS, B5Skill.valueOf("SHIELD_SYSTEMS"));
        }

        @Test
        @DisplayName("Navigation skills")
        void navigationSkills() {
            assertSame(B5Skill.NAVIGATION, B5Skill.valueOf("NAVIGATION"));
            assertSame(B5Skill.STARSHIPS, B5Skill.valueOf("STARSHIPS"));
            assertSame(B5Skill.PILOTING, B5Skill.valueOf("PILOTING"));
            assertSame(B5Skill.WORMHOLE_NAV, B5Skill.valueOf("WORMHOLE_NAV"));
        }

        @Test
        @DisplayName("Scientific skills")
        void scientificSkills() {
            assertSame(B5Skill.ASTRONOMY, B5Skill.valueOf("ASTRONOMY"));
            assertSame(B5Skill.GEOLOGY, B5Skill.valueOf("GEOLOGY"));
            assertSame(B5Skill.BIOLOGY, B5Skill.valueOf("BIOLOGY"));
            assertSame(B5Skill.CHEMISTRY, B5Skill.valueOf("CHEMISTRY"));
            assertSame(B5Skill.PHYSICS, B5Skill.valueOf("PHYSICS"));
            assertSame(B5Skill.XENOBIOLOGY, B5Skill.valueOf("XENOBIOLOGY"));
            assertSame(B5Skill.PSIONICS, B5Skill.valueOf("PSIONICS"));
            assertSame(B5Skill.SHADOW_LORE, B5Skill.valueOf("SHADOW_LORE"));
            assertSame(B5Skill.VORLON_LORE, B5Skill.valueOf("VORLON_LORE"));
        }

        @Test
        @DisplayName("Social skills")
        void socialSkills() {
            assertSame(B5Skill.LANGUAGE, B5Skill.valueOf("LANGUAGE"));
            assertSame(B5Skill.PSYCHOLOGY, B5Skill.valueOf("PSYCHOLOGY"));
            assertSame(B5Skill.FORGERY, B5Skill.valueOf("FORGERY"));
            assertSame(B5Skill.BARKEEPING, B5Skill.valueOf("BARKEEPING"));
            assertSame(B5Skill.POLITICS, B5Skill.valueOf("POLITICS"));
        }

        @Test
        @DisplayName("Survival skills")
        void survivalSkills() {
            assertSame(B5Skill.SURVIVAL, B5Skill.valueOf("SURVIVAL"));
            assertSame(B5Skill.MEDICAL, B5Skill.valueOf("MEDICAL"));
            assertSame(B5Skill.FIRST_AID, B5Skill.valueOf("FIRST_AID"));
        }

        @Test
        @DisplayName("Stealth skills")
        void stealthSkills() {
            assertSame(B5Skill.STEALTH, B5Skill.valueOf("STEALTH"));
            assertSame(B5Skill.INTRUSION_COUNTER, B5Skill.valueOf("INTRUSION_COUNTER"));
        }

        @Test
        @DisplayName("B5-specific skills")
        void b5SpecificSkills() {
            assertSame(B5Skill.ANCIENT_TECH, B5Skill.valueOf("ANCIENT_TECH"));
        }
    }

    @Nested
    @DisplayName("Skill properties")
    class SkillProperties {

        @Test
        @DisplayName("Each skill has a name")
        void names() {
            for (B5Skill skill : B5Skill.values()) {
                assertNotNull(skill.getName());
                assertFalse(skill.getName().isEmpty());
            }
        }

        @Test
        @DisplayName("Each skill has a description")
        void descriptions() {
            for (B5Skill skill : B5Skill.values()) {
                assertNotNull(skill.getDescription());
                assertFalse(skill.getDescription().isEmpty());
            }
        }

        @Test
        @DisplayName("Each skill has a category")
        void categories() {
            for (B5Skill skill : B5Skill.values()) {
                assertNotNull(skill.getCategory());
            }
        }

        @Test
        @DisplayName("Specific skill properties")
        void specificProperties() {
            assertEquals("Melee", B5Skill.MELEE.getName());
            assertEquals("Unarmed and melee weapon combat", B5Skill.MELEE.getDescription());
            assertEquals(B5Skill.SkillCategory.COMBAT, B5Skill.MELEE.getCategory());

            assertEquals("Psionics", B5Skill.PSIONICS.getName());
            assertEquals(B5Skill.SkillCategory.SCIENTIFIC, B5Skill.PSIONICS.getCategory());
        }
    }

    @Nested
    @DisplayName("SkillCategory enum")
    class SkillCategoryEnum {

        @Test
        @DisplayName("All 8 category values exist")
        void allCategories() {
            B5Skill.SkillCategory[] categories = B5Skill.SkillCategory.values();
            assertEquals(8, categories.length);
        }

        @Test
        @DisplayName("Category values")
        void categoryValues() {
            assertSame(B5Skill.SkillCategory.COMBAT, B5Skill.SkillCategory.valueOf("COMBAT"));
            assertSame(B5Skill.SkillCategory.LEADERSHIP, B5Skill.SkillCategory.valueOf("LEADERSHIP"));
            assertSame(B5Skill.SkillCategory.TECHNICAL, B5Skill.SkillCategory.valueOf("TECHNICAL"));
            assertSame(B5Skill.SkillCategory.NAVIGATION, B5Skill.SkillCategory.valueOf("NAVIGATION"));
            assertSame(B5Skill.SkillCategory.SCIENTIFIC, B5Skill.SkillCategory.valueOf("SCIENTIFIC"));
            assertSame(B5Skill.SkillCategory.SOCIAL, B5Skill.SkillCategory.valueOf("SOCIAL"));
            assertSame(B5Skill.SkillCategory.SURVIVAL, B5Skill.SkillCategory.valueOf("SURVIVAL"));
            assertSame(B5Skill.SkillCategory.STEALTH, B5Skill.SkillCategory.valueOf("STEALTH"));
        }

        @Test
        @DisplayName("Category names")
        void categoryNames() {
            assertEquals("Combat", B5Skill.SkillCategory.COMBAT.getName());
            assertEquals("Leadership", B5Skill.SkillCategory.LEADERSHIP.getName());
            assertEquals("Technical", B5Skill.SkillCategory.TECHNICAL.getName());
            assertEquals("Navigation", B5Skill.SkillCategory.NAVIGATION.getName());
            assertEquals("Scientific", B5Skill.SkillCategory.SCIENTIFIC.getName());
            assertEquals("Social", B5Skill.SkillCategory.SOCIAL.getName());
            assertEquals("Survival", B5Skill.SkillCategory.SURVIVAL.getName());
            assertEquals("Stealth", B5Skill.SkillCategory.STEALTH.getName());
        }

        @Test
        @DisplayName("Skills by category")
        void skillsByCategory() {
            List<B5Skill> combatSkills = Arrays.stream(B5Skill.values())
                    .filter(s -> s.getCategory() == B5Skill.SkillCategory.COMBAT)
                    .toList();
            assertEquals(5, combatSkills.size());

            List<B5Skill> scientificSkills = Arrays.stream(B5Skill.values())
                    .filter(s -> s.getCategory() == B5Skill.SkillCategory.SCIENTIFIC)
                    .toList();
            assertEquals(9, scientificSkills.size());
        }
    }
}
