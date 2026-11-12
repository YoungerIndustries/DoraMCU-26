package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.traveller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CharacterConversionService")
class CharacterConversionServiceTest {

    @Nested
    @DisplayName("normalizeAttribute")
    class NormalizeAttribute {

        @Test
        @DisplayName("maps minimum Traveller score 2 to 0")
        void mapsMinimum() {
            assertEquals(0, CharacterConversionService.normalizeAttribute(2));
        }

        @Test
        @DisplayName("maps maximum Traveller score 12 to 5")
        void mapsMaximum() {
            assertEquals(5, CharacterConversionService.normalizeAttribute(12));
        }

        @Test
        @DisplayName("maps middle score 7 to 3")
        void mapsMiddle() {
            assertEquals(3, CharacterConversionService.normalizeAttribute(7));
        }

        @Test
        @DisplayName("maps score 3 to 1")
        void mapsThree() {
            assertEquals(1, CharacterConversionService.normalizeAttribute(3));
        }

        @Test
        @DisplayName("maps score 4 to 1")
        void mapsFour() {
            assertEquals(1, CharacterConversionService.normalizeAttribute(4));
        }

        @Test
        @DisplayName("maps score 5 to 2")
        void mapsFive() {
            assertEquals(2, CharacterConversionService.normalizeAttribute(5));
        }

        @Test
        @DisplayName("maps score 6 to 2")
        void mapsSix() {
            assertEquals(2, CharacterConversionService.normalizeAttribute(6));
        }

        @Test
        @DisplayName("maps score 8 to 3")
        void mapsEight() {
            assertEquals(3, CharacterConversionService.normalizeAttribute(8));
        }

        @Test
        @DisplayName("maps score 9 to 4")
        void mapsNine() {
            assertEquals(4, CharacterConversionService.normalizeAttribute(9));
        }

        @Test
        @DisplayName("maps score 10 to 4")
        void mapsTen() {
            assertEquals(4, CharacterConversionService.normalizeAttribute(10));
        }

        @Test
        @DisplayName("maps score 11 to 5")
        void mapsEleven() {
            assertEquals(5, CharacterConversionService.normalizeAttribute(11));
        }

        @Test
        @DisplayName("clamps below minimum")
        void clampsBelowMinimum() {
            assertEquals(0, CharacterConversionService.normalizeAttribute(1));
            assertEquals(0, CharacterConversionService.normalizeAttribute(0));
            assertEquals(0, CharacterConversionService.normalizeAttribute(-5));
        }

        @Test
        @DisplayName("clamps above maximum")
        void clampsAboveMaximum() {
            assertEquals(5, CharacterConversionService.normalizeAttribute(13));
            assertEquals(5, CharacterConversionService.normalizeAttribute(20));
        }

        @Test
        @DisplayName("all values produce 0-5 range")
        void allValuesInRange() {
            for (int i = 0; i <= 20; i++) {
                int result = CharacterConversionService.normalizeAttribute(i);
                assertTrue(result >= 0 && result <= 5, "Value " + i + " produced " + result);
            }
        }
    }

    @Nested
    @DisplayName("denormalizeAttribute")
    class DenormalizeAttribute {

        @Test
        @DisplayName("maps 0 to 2")
        void mapsZero() {
            assertEquals(2, CharacterConversionService.denormalizeAttribute(0));
        }

        @Test
        @DisplayName("maps 5 to 12")
        void mapsFive() {
            assertEquals(12, CharacterConversionService.denormalizeAttribute(5));
        }

        @Test
        @DisplayName("maps 1 to 4")
        void mapsOne() {
            assertEquals(4, CharacterConversionService.denormalizeAttribute(1));
        }

        @Test
        @DisplayName("maps 2 to 6")
        void mapsTwo() {
            assertEquals(6, CharacterConversionService.denormalizeAttribute(2));
        }

        @Test
        @DisplayName("maps 3 to 8")
        void mapsThree() {
            assertEquals(8, CharacterConversionService.denormalizeAttribute(3));
        }

        @Test
        @DisplayName("maps 4 to 10")
        void mapsFour() {
            assertEquals(10, CharacterConversionService.denormalizeAttribute(4));
        }

        @Test
        @DisplayName("clamps below minimum")
        void clampsBelowMinimum() {
            assertEquals(2, CharacterConversionService.denormalizeAttribute(-1));
            assertEquals(2, CharacterConversionService.denormalizeAttribute(-5));
        }

        @Test
        @DisplayName("clamps above maximum")
        void clampsAboveMaximum() {
            assertEquals(12, CharacterConversionService.denormalizeAttribute(6));
            assertEquals(12, CharacterConversionService.denormalizeAttribute(10));
        }

        @Test
        @DisplayName("all values produce 2-12 range")
        void allValuesInRange() {
            for (int i = -5; i <= 10; i++) {
                int result = CharacterConversionService.denormalizeAttribute(i);
                assertTrue(result >= 2 && result <= 12, "Value " + i + " produced " + result);
            }
        }

        @Test
        @DisplayName("round-trip approximate identity")
        void roundTripApproximate() {
            for (int traveller = 2; traveller <= 12; traveller++) {
                int cl = CharacterConversionService.normalizeAttribute(traveller);
                int back = CharacterConversionService.denormalizeAttribute(cl);
                // Allow ±1 difference due to rounding
                assertTrue(Math.abs(back - traveller) <= 1,
                        "Round-trip failed for " + traveller + " -> " + cl + " -> " + back);
            }
        }
    }

    @Nested
    @DisplayName("toCaptainsLog")
    class ToCaptainsLog {

        private CharacterConversionService service = new CharacterConversionService();

        private TravellerCharacter createTraveller(String name) {
            TravellerCharacter tc = new TravellerCharacter(1, name);
            tc.setAbility(AbilityScore.END, 8);
            tc.setAbility(AbilityScore.ST, 7);
            tc.setAbility(AbilityScore.INT, 9);
            tc.setAbility(AbilityScore.APP, 6);
            tc.setAbility(AbilityScore.DEX, 7);
            tc.setAbility(AbilityScore.EDU, 8);
            tc.setAbility(AbilityScore.SOC, 7);
            return tc;
        }

        @Test
        @DisplayName("throws when traveller is null")
        void throwsWhenNull() {
            assertThrows(IllegalArgumentException.class, () -> service.toCaptainsLog(null));
        }

        @Test
        @DisplayName("converts name")
        void convertsName() {
            TravellerCharacter tc = createTraveller("John Doe");
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals("John Doe", cl.getName());
        }

        @Test
        @DisplayName("converts abilities to attributes")
        void convertsAbilities() {
            TravellerCharacter tc = createTraveller("John Doe");
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            // END=8 -> STR=3, FIT=(8+7)/2=7 -> 3
            // ST=7 -> INS=3
            // INT=9 -> TEC=4, REA=4
            // APP=6 -> PRE=2
            assertEquals(3, cl.getFitness());
            assertEquals(4, cl.getTechnical());
            assertEquals(3, cl.getStrength());
            assertEquals(4, cl.getReason());
            assertEquals(3, cl.getInsight());
            assertEquals(2, cl.getPresence());
        }

        @Test
        @DisplayName("sets constitution from END")
        void setsConstitution() {
            TravellerCharacter tc = createTraveller("John Doe");
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            // END=8 -> normalizeAttribute(8)=3, max(1,3)=3
            // Note: constitution uses END directly, not FIT
            assertEquals(3, cl.getConstitution());
        }

        @Test
        @DisplayName("maps combat skills to security")
        void mapsCombatToSecurity() {
            TravellerCharacter tc = createTraveller("John Doe");
            tc.addSkill(Skill.MELEE);
            tc.addSkill(Skill.FIREARMS);
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals(2, cl.getSecurity());
        }

        @Test
        @DisplayName("maps leadership skills to command")
        void mapsLeadershipToCommand() {
            TravellerCharacter tc = createTraveller("John Doe");
            tc.addSkill(Skill.LEADERSHIP);
            tc.addSkill(Skill.TACTICS);
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals(2, cl.getCommand());
        }

        @Test
        @DisplayName("maps technical skills to science")
        void mapsTechnicalToScience() {
            TravellerCharacter tc = createTraveller("John Doe");
            tc.addSkill(Skill.COMPUTERS);
            tc.addSkill(Skill.ELECTRONICS);
            tc.addSkill(Skill.ASTRONOMY);
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals(3, cl.getScience());
        }

        @Test
        @DisplayName("maps social skills to leadership")
        void mapsSocialToLeadership() {
            TravellerCharacter tc = createTraveller("John Doe");
            tc.addSkill(Skill.LANGUAGE);
            tc.addSkill(Skill.PSYCHOLOGY);
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals(2, cl.getLeadership());
        }

        @Test
        @DisplayName("caps disciplines at 5")
        void capsDisciplines() {
            TravellerCharacter tc = createTraveller("John Doe");
            tc.addSkill(Skill.MELEE);
            tc.addSkill(Skill.FIREARMS);
            tc.addSkill(Skill.SKIRMISH);
            tc.addSkill(Skill.GUERRILLA);
            tc.addSkill(Skill.SURVIVAL);
            tc.addSkill(Skill.STEALTH);
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals(5, cl.getSecurity());
        }

        @Test
        @DisplayName("sets default rank for no tours")
        void defaultRankNoTours() {
            TravellerCharacter tc = createTraveller("John Doe");
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals(CaptainLogCharacter.Rank.ENSIGN, cl.getRank());
        }

        @Test
        @DisplayName("sets rank based on tour count")
        void rankBasedOnTours() {
            TravellerCharacter tc = createTraveller("John Doe");
            tc.addCareerTour(new TravellerCharacter.CareerTour(
                    Career.SOLDIER, 4, new java.util.ArrayList<>(), "Private", 1000, false,
                    TravellerCharacter.RetirementType.NORMAL));
            tc.addCareerTour(new TravellerCharacter.CareerTour(
                    Career.EXPLORER, 4, new java.util.ArrayList<>(), "Surveyor", 2000, true,
                    TravellerCharacter.RetirementType.HONORABLE));
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals(CaptainLogCharacter.Rank.LIEUTENANT_COMMANDER, cl.getRank());
        }

        @Test
        @DisplayName("sets assignment based on skill categories")
        void assignmentBasedOnSkills() {
            TravellerCharacter tc = createTraveller("John Doe");
            tc.addSkill(Skill.MELEE);
            tc.addSkill(Skill.FIREARMS);
            tc.addSkill(Skill.SKIRMISH);
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals(CaptainLogCharacter.Assignment.SECURITY, cl.getAssignment());
        }

        @Test
        @DisplayName("sets default assignment for no skills")
        void defaultAssignmentNoSkills() {
            TravellerCharacter tc = createTraveller("John Doe");
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals(CaptainLogCharacter.Assignment.OPERATIONS, cl.getAssignment());
        }

        @Test
        @DisplayName("sets education based on EDU+SOC")
        void setsEducation() {
            TravellerCharacter tc = createTraveller("John Doe");
            // EDU=8, SOC=7, combined=15 -> FIELD_TRAINING (>=12)
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals(CaptainLogCharacter.Education.FIELD_TRAINING, cl.getEducation());
        }

        @Test
        @DisplayName("sets environment based on SOC")
        void setsEnvironment() {
            TravellerCharacter tc = createTraveller("John Doe");
            // SOC=7 -> COLONY
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals(CaptainLogCharacter.Environment.COLONY, cl.getEnvironment());
        }

        @Test
        @DisplayName("sets outlook based on ST")
        void setsOutlook() {
            TravellerCharacter tc = createTraveller("John Doe");
            // ST=7 -> IDEALISTIC
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals(CaptainLogCharacter.Outlook.IDEALISTIC, cl.getOutlook());
        }

        @Test
        @DisplayName("maps stealth skills to security")
        void mapsStealthToSecurity() {
            TravellerCharacter tc = createTraveller("John Doe");
            tc.addSkill(Skill.STEALTH);
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals(1, cl.getSecurity());
        }

        @Test
        @DisplayName("maps survival skills to security")
        void mapsSurvivalToSecurity() {
            TravellerCharacter tc = createTraveller("John Doe");
            tc.addSkill(Skill.SURVIVAL);
            tc.addSkill(Skill.FIREARMS);
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals(2, cl.getSecurity());
        }

        @Test
        @DisplayName("maps navigation skills to science")
        void mapsNavigationToScience() {
            TravellerCharacter tc = createTraveller("John Doe");
            tc.addSkill(Skill.NAVIGATION);
            tc.addSkill(Skill.STARSHIPS);
            CaptainLogCharacter cl = service.toCaptainsLog(tc);

            assertEquals(2, cl.getScience());
        }
    }

    @Nested
    @DisplayName("toTraveller")
    class ToTraveller {

        private CharacterConversionService service = new CharacterConversionService();

        private CaptainLogCharacter createCaptainsLog(String name) {
            CaptainLogCharacter cl = new CaptainLogCharacter();
            cl.setName(name);
            cl.setFitness(2);
            cl.setTechnical(3);
            cl.setStrength(3);
            cl.setReason(3);
            cl.setInsight(2);
            cl.setPresence(2);
            cl.setConstitution(3);
            cl.setCommand(1);
            cl.setLeadership(1);
            cl.setScience(2);
            cl.setSecurity(1);
            cl.setRank(CaptainLogCharacter.Rank.LIEUTENANT);
            cl.setAssignment(CaptainLogCharacter.Assignment.SCIENCE);
            cl.setEducation(CaptainLogCharacter.Education.ACADEMIC);
            cl.setEnvironment(CaptainLogCharacter.Environment.CITY);
            cl.setOutlook(CaptainLogCharacter.Outlook.OPTIMISTIC);
            return cl;
        }

        @Test
        @DisplayName("throws when captains log is null")
        void throwsWhenNull() {
            assertThrows(IllegalArgumentException.class, () -> service.toTraveller(null));
        }

        @Test
        @DisplayName("converts name")
        void convertsName() {
            CaptainLogCharacter cl = createCaptainsLog("Jane Doe");
            TravellerCharacter tc = service.toTraveller(cl);

            assertEquals("Jane Doe", tc.getName());
        }

        @Test
        @DisplayName("converts attributes to abilities")
        void convertsAttributes() {
            CaptainLogCharacter cl = createCaptainsLog("Jane Doe");
            TravellerCharacter tc = service.toTraveller(cl);

            // STR=3 -> END=8
            // INS=2 -> ST=6
            // (REA+TEC)/2 = (3+3)/2 = 3 -> INT=8
            // PRE=2 -> APP=6
            // FIT=2 -> DEX=6
            assertEquals(8, tc.getAbility(AbilityScore.END));
            assertEquals(6, tc.getAbility(AbilityScore.ST));
            assertEquals(8, tc.getAbility(AbilityScore.INT));
            assertEquals(6, tc.getAbility(AbilityScore.APP));
            assertEquals(6, tc.getAbility(AbilityScore.DEX));
        }

        @Test
        @DisplayName("maps education to EDU score")
        void mapsEducation() {
            CaptainLogCharacter cl = createCaptainsLog("Jane Doe");
            // ACADEMIC -> 9
            TravellerCharacter tc = service.toTraveller(cl);

            assertEquals(9, tc.getAbility(AbilityScore.EDU));
        }

        @Test
        @DisplayName("maps environment to SOC score")
        void mapsEnvironment() {
            CaptainLogCharacter cl = createCaptainsLog("Jane Doe");
            // CITY -> 10
            TravellerCharacter tc = service.toTraveller(cl);

            assertEquals(10, tc.getAbility(AbilityScore.SOC));
        }

        @Test
        @DisplayName("maps disciplines to skills")
        void mapsDisciplinesToSkills() {
            CaptainLogCharacter cl = createCaptainsLog("Jane Doe");
            TravellerCharacter tc = service.toTraveller(cl);

            // Security=1 -> MELEE
            // Command=1 -> LEADERSHIP
            // Science=2 -> COMPUTERS, ELECTRONICS
            // Leadership=1 -> LANGUAGE
            assertTrue(tc.hasSkill(Skill.MELEE));
            assertTrue(tc.hasSkill(Skill.LEADERSHIP));
            assertTrue(tc.hasSkill(Skill.COMPUTERS));
            assertTrue(tc.hasSkill(Skill.ELECTRONICS));
            assertTrue(tc.hasSkill(Skill.LANGUAGE));
        }

        @Test
        @DisplayName("creates character with id from captains log")
        void createsWithId() {
            CaptainLogCharacter cl = createCaptainsLog("Jane Doe");
            cl.setId(42L);
            TravellerCharacter tc = service.toTraveller(cl);

            assertEquals(42, tc.getId());
        }

        @Test
        @DisplayName("creates character with default id when null")
        void createsWithDefaultId() {
            CaptainLogCharacter cl = createCaptainsLog("Jane Doe");
            // id is null by default
            TravellerCharacter tc = service.toTraveller(cl);

            assertEquals(1, tc.getId());
        }

        @Test
        @DisplayName("all abilities are in valid range")
        void allAbilitiesInRange() {
            CaptainLogCharacter cl = createCaptainsLog("Jane Doe");
            TravellerCharacter tc = service.toTraveller(cl);

            for (AbilityScore ability : AbilityScore.values()) {
                int value = tc.getAbility(ability);
                assertTrue(value >= 2 && value <= 12,
                        ability.name() + " = " + value + " out of range");
            }
        }
    }

    @Nested
    @DisplayName("mapSkillToDisciplineName")
    class MapSkillToDisciplineName {

        @Test
        @DisplayName("maps combat skills to Security")
        void mapsCombat() {
            assertEquals("Security", CharacterConversionService.mapSkillToDisciplineName(Skill.MELEE));
            assertEquals("Security", CharacterConversionService.mapSkillToDisciplineName(Skill.FIREARMS));
        }

        @Test
        @DisplayName("maps stealth skills to Security")
        void mapsStealth() {
            assertEquals("Security", CharacterConversionService.mapSkillToDisciplineName(Skill.STEALTH));
        }

        @Test
        @DisplayName("maps survival skills to Security")
        void mapsSurvival() {
            assertEquals("Security", CharacterConversionService.mapSkillToDisciplineName(Skill.SURVIVAL));
        }

        @Test
        @DisplayName("maps leadership skills to Command")
        void mapsLeadership() {
            assertEquals("Command", CharacterConversionService.mapSkillToDisciplineName(Skill.LEADERSHIP));
            assertEquals("Command", CharacterConversionService.mapSkillToDisciplineName(Skill.TACTICS));
        }

        @Test
        @DisplayName("maps technical skills to Science")
        void mapsTechnical() {
            assertEquals("Science", CharacterConversionService.mapSkillToDisciplineName(Skill.COMPUTERS));
            assertEquals("Science", CharacterConversionService.mapSkillToDisciplineName(Skill.ELECTRONICS));
        }

        @Test
        @DisplayName("maps navigation skills to Science")
        void mapsNavigation() {
            assertEquals("Science", CharacterConversionService.mapSkillToDisciplineName(Skill.NAVIGATION));
        }

        @Test
        @DisplayName("maps scientific skills to Science")
        void mapsScientific() {
            assertEquals("Science", CharacterConversionService.mapSkillToDisciplineName(Skill.ASTRONOMY));
        }

        @Test
        @DisplayName("maps social skills to Leadership")
        void mapsSocial() {
            assertEquals("Leadership", CharacterConversionService.mapSkillToDisciplineName(Skill.LANGUAGE));
        }
    }

    @Nested
    @DisplayName("mapDisciplinesToSkills")
    class MapDisciplinesToSkills {

        private CaptainLogCharacter createMinimalCl() {
            CaptainLogCharacter cl = new CaptainLogCharacter();
            cl.setName("Test");
            cl.setSecurity(0);
            cl.setCommand(0);
            cl.setScience(0);
            cl.setLeadership(0);
            return cl;
        }

        @Test
        @DisplayName("returns empty set when all disciplines are 0")
        void emptyWhenZero() {
            CaptainLogCharacter cl = createMinimalCl();
            Set<Skill> skills = CharacterConversionService.mapDisciplinesToSkills(cl);

            assertTrue(skills.isEmpty());
        }

        @Test
        @DisplayName("security 1 gives melee")
        void securityOne() {
            CaptainLogCharacter cl = createMinimalCl();
            cl.setSecurity(1);
            Set<Skill> skills = CharacterConversionService.mapDisciplinesToSkills(cl);

            assertTrue(skills.contains(Skill.MELEE));
            assertEquals(1, skills.size());
        }

        @Test
        @DisplayName("security 2 gives melee and firearms")
        void securityTwo() {
            CaptainLogCharacter cl = createMinimalCl();
            cl.setSecurity(2);
            Set<Skill> skills = CharacterConversionService.mapDisciplinesToSkills(cl);

            assertTrue(skills.contains(Skill.MELEE));
            assertTrue(skills.contains(Skill.FIREARMS));
            assertEquals(2, skills.size());
        }

        @Test
        @DisplayName("security 3 gives melee, firearms, survival")
        void securityThree() {
            CaptainLogCharacter cl = createMinimalCl();
            cl.setSecurity(3);
            Set<Skill> skills = CharacterConversionService.mapDisciplinesToSkills(cl);

            assertTrue(skills.contains(Skill.MELEE));
            assertTrue(skills.contains(Skill.FIREARMS));
            assertTrue(skills.contains(Skill.SURVIVAL));
            assertEquals(3, skills.size());
        }

        @Test
        @DisplayName("command 1 gives leadership")
        void commandOne() {
            CaptainLogCharacter cl = createMinimalCl();
            cl.setCommand(1);
            Set<Skill> skills = CharacterConversionService.mapDisciplinesToSkills(cl);

            assertTrue(skills.contains(Skill.LEADERSHIP));
            assertEquals(1, skills.size());
        }

        @Test
        @DisplayName("command 2 gives leadership and tactics")
        void commandTwo() {
            CaptainLogCharacter cl = createMinimalCl();
            cl.setCommand(2);
            Set<Skill> skills = CharacterConversionService.mapDisciplinesToSkills(cl);

            assertTrue(skills.contains(Skill.LEADERSHIP));
            assertTrue(skills.contains(Skill.TACTICS));
            assertEquals(2, skills.size());
        }

        @Test
        @DisplayName("science 1 gives computers")
        void scienceOne() {
            CaptainLogCharacter cl = createMinimalCl();
            cl.setScience(1);
            Set<Skill> skills = CharacterConversionService.mapDisciplinesToSkills(cl);

            assertTrue(skills.contains(Skill.COMPUTERS));
            assertEquals(1, skills.size());
        }

        @Test
        @DisplayName("science 4 gives computers, electronics, astronomy, navigation")
        void scienceFour() {
            CaptainLogCharacter cl = createMinimalCl();
            cl.setScience(4);
            Set<Skill> skills = CharacterConversionService.mapDisciplinesToSkills(cl);

            assertTrue(skills.contains(Skill.COMPUTERS));
            assertTrue(skills.contains(Skill.ELECTRONICS));
            assertTrue(skills.contains(Skill.ASTRONOMY));
            assertTrue(skills.contains(Skill.NAVIGATION));
            assertEquals(4, skills.size());
        }

        @Test
        @DisplayName("leadership 1 gives language")
        void leadershipOne() {
            CaptainLogCharacter cl = createMinimalCl();
            cl.setLeadership(1);
            Set<Skill> skills = CharacterConversionService.mapDisciplinesToSkills(cl);

            assertTrue(skills.contains(Skill.LANGUAGE));
            assertEquals(1, skills.size());
        }

        @Test
        @DisplayName("leadership 2 gives language and psychology")
        void leadershipTwo() {
            CaptainLogCharacter cl = createMinimalCl();
            cl.setLeadership(2);
            Set<Skill> skills = CharacterConversionService.mapDisciplinesToSkills(cl);

            assertTrue(skills.contains(Skill.LANGUAGE));
            assertTrue(skills.contains(Skill.PSYCHOLOGY));
            assertEquals(2, skills.size());
        }

        @Test
        @DisplayName("all disciplines max gives all skills")
        void allMax() {
            CaptainLogCharacter cl = createMinimalCl();
            cl.setSecurity(5);
            cl.setCommand(5);
            cl.setScience(5);
            cl.setLeadership(5);
            Set<Skill> skills = CharacterConversionService.mapDisciplinesToSkills(cl);

            // Security: MELEE, FIREARMS, SURVIVAL (3)
            // Command: LEADERSHIP, TACTICS (2)
            // Science: COMPUTERS, ELECTRONICS, ASTRONOMY, NAVIGATION (4)
            // Leadership: LANGUAGE, PSYCHOLOGY (2)
            assertEquals(11, skills.size());
        }
    }
}
