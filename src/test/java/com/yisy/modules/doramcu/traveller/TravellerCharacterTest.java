package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TravellerCharacter")
class TravellerCharacterTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates character with id and name")
        void createsWithIdAndName() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            assertEquals(1, tc.getId());
            assertEquals("John Doe", tc.getName());
        }

        @Test
        @DisplayName("throws when name is null")
        void throwsWhenNameNull() {
            assertThrows(NullPointerException.class, () -> new TravellerCharacter(1, null));
        }

        @Test
        @DisplayName("starts at age 18")
        void startsAtAge18() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            assertEquals(18, tc.getAge());
        }

        @Test
        @DisplayName("starts alive")
        void startsAlive() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            assertTrue(tc.isAlive());
        }

        @Test
        @DisplayName("starts with TRAINING status")
        void startsTraining() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            assertEquals(CharacterStatus.TRAINING, tc.getStatus());
        }

        @Test
        @DisplayName("starts with zero wealth")
        void startsZeroWealth() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            assertEquals(0, tc.getWealth());
        }

        @Test
        @DisplayName("starts with empty skills")
        void startsEmptySkills() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            assertTrue(tc.getSkills().isEmpty());
        }

        @Test
        @DisplayName("starts with empty career tours")
        void startsEmptyTours() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            assertTrue(tc.getCareerTours().isEmpty());
        }

        @Test
        @DisplayName("starts as human")
        void startsAsHuman() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            assertEquals(AlienRace.HUMAN, tc.getRace());
        }

        @Test
        @DisplayName("abilities are all zero initially")
        void abilitiesZeroInitially() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            for (AbilityScore ability : AbilityScore.values()) {
                assertEquals(0, tc.getAbility(ability));
            }
        }
    }

    @Nested
    @DisplayName("setAbility")
    class SetAbility {

        @Test
        @DisplayName("sets ability score")
        void setsAbility() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");
            tc.setAbility(AbilityScore.END, 8);

            assertEquals(8, tc.getAbility(AbilityScore.END));
        }

        @Test
        @DisplayName("throws when below 2")
        void throwsBelowTwo() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            assertThrows(IllegalArgumentException.class, () -> tc.setAbility(AbilityScore.END, 1));
        }

        @Test
        @DisplayName("throws when above 12")
        void throwsAbove12() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            assertThrows(IllegalArgumentException.class, () -> tc.setAbility(AbilityScore.END, 13));
        }

        @Test
        @DisplayName("accepts boundary 2")
        void acceptsBoundaryTwo() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            assertDoesNotThrow(() -> tc.setAbility(AbilityScore.END, 2));
            assertEquals(2, tc.getAbility(AbilityScore.END));
        }

        @Test
        @DisplayName("accepts boundary 12")
        void acceptsBoundary12() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            assertDoesNotThrow(() -> tc.setAbility(AbilityScore.END, 12));
            assertEquals(12, tc.getAbility(AbilityScore.END));
        }
    }

    @Nested
    @DisplayName("addSkill")
    class AddSkill {

        @Test
        @DisplayName("adds a skill")
        void addsSkill() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");
            tc.addSkill(Skill.MELEE);

            assertTrue(tc.hasSkill(Skill.MELEE));
            assertEquals(1, tc.getSkills().size());
        }

        @Test
        @DisplayName("adds multiple skills")
        void addsMultipleSkills() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");
            tc.addSkill(Skill.MELEE);
            tc.addSkill(Skill.FIREARMS);
            tc.addSkill(Skill.COMPUTERS);

            assertEquals(3, tc.getSkills().size());
            assertTrue(tc.hasSkill(Skill.MELEE));
            assertTrue(tc.hasSkill(Skill.FIREARMS));
            assertTrue(tc.hasSkill(Skill.COMPUTERS));
        }

        @Test
        @DisplayName("throws when skill is null")
        void throwsWhenNull() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            assertThrows(NullPointerException.class, () -> tc.addSkill(null));
        }

        @Test
        @DisplayName("duplicate skill is not added again")
        void duplicateNotAdded() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");
            tc.addSkill(Skill.MELEE);
            tc.addSkill(Skill.MELEE);

            assertEquals(1, tc.getSkills().size());
        }
    }

    @Nested
    @DisplayName("addCareerTour")
    class AddCareerTour {

        @Test
        @DisplayName("adds career tour")
        void addsTour() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");
            tc.addCareerTour(new TravellerCharacter.CareerTour(
                    Career.SOLDIER, 4, new ArrayList<>(), "Private", 1000, false,
                    TravellerCharacter.RetirementType.NORMAL));

            assertEquals(1, tc.getCareerTours().size());
        }

        @Test
        @DisplayName("increases age")
        void increasesAge() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");
            tc.addCareerTour(new TravellerCharacter.CareerTour(
                    Career.SOLDIER, 4, new ArrayList<>(), "Private", 1000, false,
                    TravellerCharacter.RetirementType.NORMAL));

            assertEquals(22, tc.getAge());
        }

        @Test
        @DisplayName("adds skills from tour")
        void addsSkillsFromTour() {
            java.util.List<Skill> skills = new ArrayList<>();
            skills.add(Skill.MELEE);
            skills.add(Skill.FIREARMS);
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");
            tc.addCareerTour(new TravellerCharacter.CareerTour(
                    Career.SOLDIER, 4, skills, "Private", 1000, false,
                    TravellerCharacter.RetirementType.NORMAL));

            assertTrue(tc.hasSkill(Skill.MELEE));
            assertTrue(tc.hasSkill(Skill.FIREARMS));
        }

        @Test
        @DisplayName("adds wealth from tour")
        void addsWealth() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");
            tc.addCareerTour(new TravellerCharacter.CareerTour(
                    Career.SOLDIER, 4, new ArrayList<>(), "Private", 5000, false,
                    TravellerCharacter.RetirementType.NORMAL));

            assertEquals(5000, tc.getWealth());
        }

        @Test
        @DisplayName("throws when tour is null")
        void throwsWhenNull() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            assertThrows(NullPointerException.class, () -> tc.addCareerTour(null));
        }
    }

    @Nested
    @DisplayName("getName/setName")
    class GetNameSetName {

        @Test
        @DisplayName("updates name")
        void updatesName() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");
            tc.setName("Jane Doe");

            assertEquals("Jane Doe", tc.getName());
        }

        @Test
        @DisplayName("throws when name is null")
        void throwsWhenNull() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");

            assertThrows(NullPointerException.class, () -> tc.setName(null));
        }
    }

    @Nested
    @DisplayName("setAlive")
    class SetAlive {

        @Test
        @DisplayName("sets alive to false")
        void setsDead() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");
            tc.setAlive(false);

            assertFalse(tc.isAlive());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("includes name and abilities")
        void includesNameAndAbilities() {
            TravellerCharacter tc = new TravellerCharacter(1, "John Doe");
            tc.setAbility(AbilityScore.END, 8);

            String str = tc.toString();

            assertTrue(str.contains("John Doe"));
            assertTrue(str.contains("End=8"));
        }
    }

    @Nested
    @DisplayName("CareerTour")
    class CareerTour {

        @Test
        @DisplayName("returns career")
        void returnsCareer() {
            TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                    Career.SOLDIER, 4, new ArrayList<>(), "Private", 1000, false,
                    TravellerCharacter.RetirementType.NORMAL);

            assertEquals(Career.SOLDIER, tour.getCareer());
        }

        @Test
        @DisplayName("returns years served")
        void returnsYears() {
            TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                    Career.SOLDIER, 4, new ArrayList<>(), "Private", 1000, false,
                    TravellerCharacter.RetirementType.NORMAL);

            assertEquals(4, tour.getYearsServed());
        }

        @Test
        @DisplayName("returns rank")
        void returnsRank() {
            TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                    Career.SOLDIER, 4, new ArrayList<>(), "Captain", 1000, false,
                    TravellerCharacter.RetirementType.NORMAL);

            assertEquals("Captain", tour.getRank());
        }

        @Test
        @DisplayName("returns wealth earned")
        void returnsWealth() {
            TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                    Career.SOLDIER, 4, new ArrayList<>(), "Private", 5000, false,
                    TravellerCharacter.RetirementType.NORMAL);

            assertEquals(5000, tour.getWealthEarned());
        }

        @Test
        @DisplayName("returns retired status")
        void returnsRetired() {
            TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                    Career.SOLDIER, 4, new ArrayList<>(), "Private", 1000, true,
                    TravellerCharacter.RetirementType.HONORABLE);

            assertTrue(tour.isRetired());
        }

        @Test
        @DisplayName("returns retirement type")
        void returnsRetirementType() {
            TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                    Career.SOLDIER, 4, new ArrayList<>(), "Private", 1000, true,
                    TravellerCharacter.RetirementType.HONORABLE);

            assertEquals(TravellerCharacter.RetirementType.HONORABLE, tour.getRetirementType());
        }

        @Test
        @DisplayName("throws when career is null")
        void throwsWhenCareerNull() {
            assertThrows(NullPointerException.class, () -> new TravellerCharacter.CareerTour(
                    null, 4, new ArrayList<>(), "Private", 1000, false,
                    TravellerCharacter.RetirementType.NORMAL));
        }

        @Test
        @DisplayName("throws when skills are null")
        void throwsWhenSkillsNull() {
            assertThrows(NullPointerException.class, () -> new TravellerCharacter.CareerTour(
                    Career.SOLDIER, 4, null, "Private", 1000, false,
                    TravellerCharacter.RetirementType.NORMAL));
        }
    }

    @Nested
    @DisplayName("RetirementType")
    class RetirementType {

        @Test
        @DisplayName("has all retirement types")
        void hasAllTypes() {
            assertEquals(5, TravellerCharacter.RetirementType.values().length);
        }

        @Test
        @DisplayName("NORMAL type")
        void normal() {
            assertEquals("Normal", TravellerCharacter.RetirementType.NORMAL.getName());
        }

        @Test
        @DisplayName("HONORABLE type")
        void honorable() {
            assertEquals("Honorable", TravellerCharacter.RetirementType.HONORABLE.getName());
        }

        @Test
        @DisplayName("DISHONORABLE type")
        void dishonorable() {
            assertEquals("Dishonorable", TravellerCharacter.RetirementType.DISHONORABLE.getName());
        }

        @Test
        @DisplayName("MEDICAL type")
        void medical() {
            assertEquals("Medical", TravellerCharacter.RetirementType.MEDICAL.getName());
        }

        @Test
        @DisplayName("DEAD type")
        void dead() {
            assertEquals("Dead", TravellerCharacter.RetirementType.DEAD.getName());
        }
    }
}
