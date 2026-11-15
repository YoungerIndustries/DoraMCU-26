package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Family;
import com.yisy.modules.doramcu.earth2026.model.Earth2026FamilyMember;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026FamilyMemberRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026FamilyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Earth2026FamilyServiceTest {

    @Mock
    private Earth2026FamilyRepository familyRepository;

    @Mock
    private Earth2026FamilyMemberRepository memberRepository;

    @InjectMocks
    private Earth2026FamilyService service;

    private Earth2026Family createFamily() {
        Earth2026Family family = new Earth2026Family();
        family.setId(1L);
        family.setName("Smith Family");
        family.setDescription("A test family");
        family.setHomeStructureId(50L);
        family.setFamilyWealth(60);
        family.setFamilyReputation(70);
        family.setFamilyTraits("[\"Military Family\"]");
        family.setActive(true);
        return family;
    }

    private Earth2026FamilyMember createMember(Earth2026Family family) {
        Earth2026FamilyMember member = new Earth2026FamilyMember();
        member.setId(1L);
        member.setFamily(family);
        member.setCharacterId(100L);
        member.setNpcId(null);
        member.setRelationshipType(Earth2026FamilyMember.RelationshipType.SPOUSE);
        member.setBondScore(50);
        member.setActive(true);
        return member;
    }

    @Nested
    @DisplayName("Family CRUD Operations")
    class FamilyCrudOperations {

        @Test
        @DisplayName("findById returns family when found")
        void findById_returnsFamily_whenFound() {
            Earth2026Family family = createFamily();
            when(familyRepository.findById(1L)).thenReturn(Optional.of(family));

            Optional<Earth2026Family> result = service.findById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
            assertEquals("Smith Family", result.get().getName());
            verify(familyRepository).findById(1L);
        }

        @Test
        @DisplayName("findById returns empty when not found")
        void findById_returnsEmpty_whenNotFound() {
            when(familyRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Family> result = service.findById(999L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("findAll returns all families")
        void findAll_returnsAllFamilies() {
            List<Earth2026Family> families = Arrays.asList(createFamily(), createFamily());
            families.get(1).setId(2L);
            when(familyRepository.findAll()).thenReturn(families);

            List<Earth2026Family> result = service.findAll();

            assertEquals(2, result.size());
            verify(familyRepository).findAll();
        }

        @Test
        @DisplayName("findActiveFamilies returns only active families")
        void findActiveFamilies_returnsActiveFamilies() {
            Earth2026Family family = createFamily();
            when(familyRepository.findByActiveTrue()).thenReturn(List.of(family));

            List<Earth2026Family> result = service.findActiveFamilies();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findByName finds families case-insensitively")
        void findByName_findsFamiliesCaseInsensitively() {
            Earth2026Family family = createFamily();
            when(familyRepository.findByNameContainingIgnoreCase("smith"))
                    .thenReturn(List.of(family));

            List<Earth2026Family> result = service.findByName("smith");

            assertEquals(1, result.size());
            assertEquals("Smith Family", result.get(0).getName());
        }

        @Test
        @DisplayName("findByHomeStructureId returns families at location")
        void findByHomeStructureId_returnsFamilies() {
            Earth2026Family family = createFamily();
            when(familyRepository.findByHomeStructureId(50L)).thenReturn(List.of(family));

            List<Earth2026Family> result = service.findByHomeStructureId(50L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("saveFamily persists family")
        void saveFamily_persistsFamily() {
            Earth2026Family family = createFamily();
            when(familyRepository.save(family)).thenReturn(family);

            Earth2026Family result = service.saveFamily(family);

            assertEquals(family, result);
            verify(familyRepository).save(family);
        }

        @Test
        @DisplayName("deleteFamily deletes family")
        void deleteFamily_deletesFamily() {
            service.deleteFamily(1L);
            verify(familyRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Family defaults are correct")
        void familyDefaults_areCorrect() {
            Earth2026Family family = new Earth2026Family();
            assertEquals(50, family.getFamilyWealth());
            assertEquals(50, family.getFamilyReputation());
            assertEquals("[]", family.getFamilyTraits());
            assertTrue(family.isActive());
        }
    }

    @Nested
    @DisplayName("Member Operations")
    class MemberOperations {

        @Test
        @DisplayName("findMembersByFamilyId returns members")
        void findMembersByFamilyId_returnsMembers() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember member = createMember(family);
            when(memberRepository.findByFamilyId(1L)).thenReturn(List.of(member));

            List<Earth2026FamilyMember> result = service.findMembersByFamilyId(1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findActiveMembersByFamilyId returns active members")
        void findActiveMembersByFamilyId_returnsActiveMembers() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember member = createMember(family);
            when(memberRepository.findByFamilyIdAndActiveTrue(1L)).thenReturn(List.of(member));

            List<Earth2026FamilyMember> result = service.findActiveMembersByFamilyId(1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findMembersByCharacterId returns memberships for character")
        void findMembersByCharacterId_returnsMemberships() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember member = createMember(family);
            when(memberRepository.findByCharacterId(100L)).thenReturn(List.of(member));

            List<Earth2026FamilyMember> result = service.findMembersByCharacterId(100L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findActiveMembersByCharacterId returns active memberships")
        void findActiveMembersByCharacterId_returnsActiveMemberships() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember member = createMember(family);
            when(memberRepository.findByCharacterIdAndActiveTrue(100L)).thenReturn(List.of(member));

            List<Earth2026FamilyMember> result = service.findActiveMembersByCharacterId(100L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findMembersByNpcId returns memberships for NPC")
        void findMembersByNpcId_returnsMemberships() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember member = createMember(family);
            member.setNpcId(200L);
            when(memberRepository.findByNpcId(200L)).thenReturn(List.of(member));

            List<Earth2026FamilyMember> result = service.findMembersByNpcId(200L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("addMember adds character member to family")
        void addMember_addsCharacterMember() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember savedMember = createMember(family);
            when(familyRepository.findById(1L)).thenReturn(Optional.of(family));
            when(memberRepository.save(any())).thenReturn(savedMember);

            Optional<Earth2026FamilyMember> result = service.addMember(
                    1L, 100L, null, Earth2026FamilyMember.RelationshipType.SPOUSE);

            assertTrue(result.isPresent());
            assertEquals(100L, result.get().getCharacterId());
            assertNull(result.get().getNpcId());
            assertEquals(Earth2026FamilyMember.RelationshipType.SPOUSE, result.get().getRelationshipType());
            assertEquals(50, result.get().getBondScore());
            assertTrue(result.get().isActive());
            verify(memberRepository).save(any());
        }

        @Test
        @DisplayName("addMember adds NPC-only member to family")
        void addMember_addsNpcOnlyMember() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember savedMember = createMember(family);
            savedMember.setCharacterId(null);
            savedMember.setNpcId(200L);
            when(familyRepository.findById(1L)).thenReturn(Optional.of(family));
            when(memberRepository.save(any())).thenReturn(savedMember);

            Optional<Earth2026FamilyMember> result = service.addMember(
                    1L, null, 200L, Earth2026FamilyMember.RelationshipType.FRIEND);

            assertTrue(result.isPresent());
            assertNull(result.get().getCharacterId());
            assertEquals(200L, result.get().getNpcId());
        }

        @Test
        @DisplayName("addMember returns empty when family not found")
        void addMember_returnsEmpty_whenFamilyNotFound() {
            when(familyRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026FamilyMember> result = service.addMember(
                    999L, 100L, null, Earth2026FamilyMember.RelationshipType.SPOUSE);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("removeMember deactivates member")
        void removeMember_deactivatesMember() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember member = createMember(family);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.save(member)).thenReturn(member);

            boolean result = service.removeMember(1L);

            assertTrue(result);
            assertFalse(member.isActive());
            verify(memberRepository).save(member);
        }

        @Test
        @DisplayName("removeMember returns false when not found")
        void removeMember_returnsFalse_whenNotFound() {
            when(memberRepository.findById(999L)).thenReturn(Optional.empty());

            boolean result = service.removeMember(999L);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Bond Management")
    class BondManagement {

        @Test
        @DisplayName("increaseBond increases bond score")
        void increaseBond_increasesBondScore() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember member = createMember(family);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.save(member)).thenReturn(member);

            Optional<Earth2026FamilyMember> result = service.increaseBond(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(70, result.get().getBondScore());
            verify(memberRepository).save(member);
        }

        @Test
        @DisplayName("increaseBond clamps to 100")
        void increaseBond_clampsTo100() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember member = createMember(family);
            member.setBondScore(90);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.save(member)).thenReturn(member);

            Optional<Earth2026FamilyMember> result = service.increaseBond(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(100, result.get().getBondScore());
        }

        @Test
        @DisplayName("decreaseBond decreases bond score")
        void decreaseBond_decreasesBondScore() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember member = createMember(family);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.save(member)).thenReturn(member);

            Optional<Earth2026FamilyMember> result = service.decreaseBond(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(30, result.get().getBondScore());
        }

        @Test
        @DisplayName("decreaseBond clamps to 0")
        void decreaseBond_clampsTo0() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember member = createMember(family);
            member.setBondScore(10);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.save(member)).thenReturn(member);

            Optional<Earth2026FamilyMember> result = service.decreaseBond(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(0, result.get().getBondScore());
        }

        @Test
        @DisplayName("increaseBond returns empty when not found")
        void increaseBond_returnsEmpty_whenNotFound() {
            when(memberRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026FamilyMember> result = service.increaseBond(999L, 10);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("decreaseBond returns empty when not found")
        void decreaseBond_returnsEmpty_whenNotFound() {
            when(memberRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026FamilyMember> result = service.decreaseBond(999L, 10);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Family Events")
    class FamilyEvents {

        @Test
        @DisplayName("familyGathering increases all member bonds")
        void familyGathering_increasesMemberBonds() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember member1 = createMember(family);
            Earth2026FamilyMember member2 = createMember(family);
            member2.setId(2L);
            member2.setBondScore(40);

            when(familyRepository.findById(1L)).thenReturn(Optional.of(family));
            when(memberRepository.findByFamilyIdAndActiveTrue(1L))
                    .thenReturn(List.of(member1, member2));
            when(memberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(familyRepository.save(family)).thenReturn(family);

            Optional<Earth2026Family> result = service.familyGathering(1L, 10);

            assertTrue(result.isPresent());
            assertEquals(60, member1.getBondScore());
            assertEquals(50, member2.getBondScore());
            assertEquals(72, family.getFamilyReputation());
            verify(memberRepository, times(2)).save(any());
        }

        @Test
        @DisplayName("familyGathering clamps reputation to 100")
        void familyGathering_clampsReputation() {
            Earth2026Family family = createFamily();
            family.setFamilyReputation(100);
            when(familyRepository.findById(1L)).thenReturn(Optional.of(family));
            when(memberRepository.findByFamilyIdAndActiveTrue(1L)).thenReturn(List.of());
            when(familyRepository.save(family)).thenReturn(family);

            Optional<Earth2026Family> result = service.familyGathering(1L, 10);

            assertTrue(result.isPresent());
            assertEquals(100, result.get().getFamilyReputation());
        }

        @Test
        @DisplayName("familyGathering returns empty when not found")
        void familyGathering_returnsEmpty_whenNotFound() {
            when(familyRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Family> result = service.familyGathering(999L, 10);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("familyGathering only affects active members")
        void familyGathering_onlyAffectsActiveMembers() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember activeMember = createMember(family);
            Earth2026FamilyMember inactiveMember = createMember(family);
            inactiveMember.setId(2L);
            inactiveMember.setActive(false);

            when(familyRepository.findById(1L)).thenReturn(Optional.of(family));
            when(memberRepository.findByFamilyIdAndActiveTrue(1L))
                    .thenReturn(List.of(activeMember));
            when(memberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(familyRepository.save(family)).thenReturn(family);

            Optional<Earth2026Family> result = service.familyGathering(1L, 10);

            assertTrue(result.isPresent());
            assertEquals(60, activeMember.getBondScore());
            assertEquals(50, inactiveMember.getBondScore());
        }

        @Test
        @DisplayName("familyGathering clamps bond scores to 100")
        void familyGathering_clampsBondScores() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember member = createMember(family);
            member.setBondScore(95);

            when(familyRepository.findById(1L)).thenReturn(Optional.of(family));
            when(memberRepository.findByFamilyIdAndActiveTrue(1L))
                    .thenReturn(List.of(member));
            when(memberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(familyRepository.save(family)).thenReturn(family);

            Optional<Earth2026Family> result = service.familyGathering(1L, 10);

            assertTrue(result.isPresent());
            assertEquals(100, member.getBondScore());
        }
    }

    @Nested
    @DisplayName("Entity Methods")
    class EntityMethods {

        @Test
        @DisplayName("Family constructor sets name")
        void familyConstructor_setsName() {
            Earth2026Family family = new Earth2026Family("Test Family");

            assertEquals("Test Family", family.getName());
        }

        @Test
        @DisplayName("setFamilyWealth clamps to 1-100")
        void setFamilyWealth_clampsToRange() {
            Earth2026Family family = new Earth2026Family();
            family.setFamilyWealth(0);
            assertEquals(1, family.getFamilyWealth());
            family.setFamilyWealth(150);
            assertEquals(100, family.getFamilyWealth());
        }

        @Test
        @DisplayName("setFamilyReputation clamps to 1-100")
        void setFamilyReputation_clampsToRange() {
            Earth2026Family family = new Earth2026Family();
            family.setFamilyReputation(0);
            assertEquals(1, family.getFamilyReputation());
            family.setFamilyReputation(150);
            assertEquals(100, family.getFamilyReputation());
        }

        @Test
        @DisplayName("FamilyMember defaults are correct")
        void familyMemberDefaults_areCorrect() {
            Earth2026FamilyMember member = new Earth2026FamilyMember();
            assertEquals(0, member.getBondScore());
            assertTrue(member.isActive());
        }

        @Test
        @DisplayName("FamilyMember constructor sets fields")
        void familyMemberConstructor_setsFields() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember member = new Earth2026FamilyMember(
                    family, Earth2026FamilyMember.RelationshipType.SPOUSE);

            assertEquals(family, member.getFamily());
            assertEquals(Earth2026FamilyMember.RelationshipType.SPOUSE, member.getRelationshipType());
        }

        @Test
        @DisplayName("setBondScore clamps to 0-100")
        void setBondScore_clampsToRange() {
            Earth2026FamilyMember member = new Earth2026FamilyMember();
            member.setBondScore(-10);
            assertEquals(0, member.getBondScore());
            member.setBondScore(150);
            assertEquals(100, member.getBondScore());
        }

        @Test
        @DisplayName("increaseBond clamps to 100")
        void increaseBond_clampsTo100() {
            Earth2026FamilyMember member = new Earth2026FamilyMember();
            member.setBondScore(90);
            member.increaseBond(20);
            assertEquals(100, member.getBondScore());
        }

        @Test
        @DisplayName("decreaseBond clamps to 0")
        void decreaseBond_clampsTo0() {
            Earth2026FamilyMember member = new Earth2026FamilyMember();
            member.setBondScore(10);
            member.decreaseBond(20);
            assertEquals(0, member.getBondScore());
        }

        @Test
        @DisplayName("RelationshipType enum has all expected values")
        void relationshipTypeEnum_hasAllValues() {
            Earth2026FamilyMember.RelationshipType[] types = Earth2026FamilyMember.RelationshipType.values();
            assertTrue(types.length >= 10);
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("SPOUSE")).findFirst().orElse(null));
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("PARENT")).findFirst().orElse(null));
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("CHILD")).findFirst().orElse(null));
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("SIBLING")).findFirst().orElse(null));
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("EXTENDED")).findFirst().orElse(null));
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("FRIEND")).findFirst().orElse(null));
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("MENTOR")).findFirst().orElse(null));
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("RIVAL")).findFirst().orElse(null));
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("ALLY")).findFirst().orElse(null));
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("ENEMY")).findFirst().orElse(null));
        }

        @Test
        @DisplayName("toString returns meaningful string")
        void toString_returnsMeaningfulString() {
            Earth2026Family family = createFamily();
            String str = family.toString();
            assertTrue(str.contains("Earth2026Family"));
            assertTrue(str.contains("Smith Family"));
        }

        @Test
        @DisplayName("FamilyMember toString returns meaningful string")
        void memberToString_returnsMeaningfulString() {
            Earth2026Family family = createFamily();
            Earth2026FamilyMember member = createMember(family);
            String str = member.toString();
            assertTrue(str.contains("Earth2026FamilyMember"));
            assertTrue(str.contains("SPOUSE"));
        }
    }
}
