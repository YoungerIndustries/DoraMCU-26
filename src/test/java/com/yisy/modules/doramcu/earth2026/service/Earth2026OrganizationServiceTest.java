package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Connection;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Organization;
import com.yisy.modules.doramcu.earth2026.model.Earth2026OrgMember;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026OrgMemberRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026OrganizationRepository;
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
class Earth2026OrganizationServiceTest {

    @Mock
    private Earth2026OrganizationRepository organizationRepository;

    @Mock
    private Earth2026OrgMemberRepository memberRepository;

    @InjectMocks
    private Earth2026OrganizationService service;

    private Earth2026Organization createOrganization() {
        Earth2026Organization org = new Earth2026Organization();
        org.setId(1L);
        org.setName("Test Organization");
        org.setDescription("A test org");
        org.setOrgType(Earth2026Organization.OrganizationType.ESPIONAGE_RING);
        org.setLeaderType(Earth2026Connection.EntityType.NPC);
        org.setLeaderId(100L);
        org.setHierarchyLevel(3);
        org.setReputation(75);
        org.setWealth(60);
        org.setSecrecyLevel(80);
        org.setHomeStructureId(50L);
        org.setActive(true);
        return org;
    }

    private Earth2026OrgMember createMember(Earth2026Organization org) {
        Earth2026OrgMember member = new Earth2026OrgMember();
        member.setId(1L);
        member.setOrganization(org);
        member.setMemberType(Earth2026Connection.EntityType.CHARACTER);
        member.setMemberId(100L);
        member.setRole("Field Agent");
        member.setRank(3);
        member.setLoyalty(70);
        member.setActive(true);
        return member;
    }

    @Nested
    @DisplayName("Organization CRUD Operations")
    class OrganizationCrudOperations {

        @Test
        @DisplayName("findById returns organization when found")
        void findById_returnsOrganization_whenFound() {
            Earth2026Organization org = createOrganization();
            when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));

            Optional<Earth2026Organization> result = service.findById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
            assertEquals("Test Organization", result.get().getName());
            verify(organizationRepository).findById(1L);
        }

        @Test
        @DisplayName("findById returns empty when not found")
        void findById_returnsEmpty_whenNotFound() {
            when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Organization> result = service.findById(999L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("findAll returns all organizations")
        void findAll_returnsAllOrganizations() {
            List<Earth2026Organization> orgs = Arrays.asList(createOrganization(), createOrganization());
            orgs.get(1).setId(2L);
            when(organizationRepository.findAll()).thenReturn(orgs);

            List<Earth2026Organization> result = service.findAll();

            assertEquals(2, result.size());
            verify(organizationRepository).findAll();
        }

        @Test
        @DisplayName("findActiveOrganizations returns only active organizations")
        void findActiveOrganizations_returnsActiveOrganizations() {
            Earth2026Organization org = createOrganization();
            when(organizationRepository.findByActiveTrue()).thenReturn(List.of(org));

            List<Earth2026Organization> result = service.findActiveOrganizations();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findByName finds organizations case-insensitively")
        void findByName_findsOrganizationsCaseInsensitively() {
            Earth2026Organization org = createOrganization();
            when(organizationRepository.findByNameContainingIgnoreCase("test"))
                    .thenReturn(List.of(org));

            List<Earth2026Organization> result = service.findByName("test");

            assertEquals(1, result.size());
            assertEquals("Test Organization", result.get(0).getName());
        }

        @Test
        @DisplayName("findByType returns organizations of type")
        void findByType_returnsOrganizations() {
            Earth2026Organization org = createOrganization();
            when(organizationRepository.findByOrgType(Earth2026Organization.OrganizationType.ESPIONAGE_RING))
                    .thenReturn(List.of(org));

            List<Earth2026Organization> result = service.findByType(Earth2026Organization.OrganizationType.ESPIONAGE_RING);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findByHomeStructureId returns organizations at location")
        void findByHomeStructureId_returnsOrganizations() {
            Earth2026Organization org = createOrganization();
            when(organizationRepository.findByHomeStructureId(50L)).thenReturn(List.of(org));

            List<Earth2026Organization> result = service.findByHomeStructureId(50L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("saveOrganization persists organization")
        void saveOrganization_persistsOrganization() {
            Earth2026Organization org = createOrganization();
            when(organizationRepository.save(org)).thenReturn(org);

            Earth2026Organization result = service.saveOrganization(org);

            assertEquals(org, result);
            verify(organizationRepository).save(org);
        }

        @Test
        @DisplayName("deleteOrganization deletes organization")
        void deleteOrganization_deletesOrganization() {
            service.deleteOrganization(1L);
            verify(organizationRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("Member Operations")
    class MemberOperations {

        @Test
        @DisplayName("findMembersByOrganizationId returns members")
        void findMembersByOrganizationId_returnsMembers() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member = createMember(org);
            when(memberRepository.findByOrganizationId(1L)).thenReturn(List.of(member));

            List<Earth2026OrgMember> result = service.findMembersByOrganizationId(1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findActiveMembersByOrganizationId returns active members")
        void findActiveMembersByOrganizationId_returnsActiveMembers() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member = createMember(org);
            when(memberRepository.findByOrganizationIdAndActiveTrue(1L)).thenReturn(List.of(member));

            List<Earth2026OrgMember> result = service.findActiveMembersByOrganizationId(1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findMembersByEntity returns memberships for entity")
        void findMembersByEntity_returnsMemberships() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member = createMember(org);
            when(memberRepository.findByMemberTypeAndMemberId(
                    Earth2026Connection.EntityType.CHARACTER, 100L))
                    .thenReturn(List.of(member));

            List<Earth2026OrgMember> result = service.findMembersByEntity(
                    Earth2026Connection.EntityType.CHARACTER, 100L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("addMember adds member to organization")
        void addMember_addsMember() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember savedMember = new Earth2026OrgMember();
            savedMember.setId(1L);
            savedMember.setOrganization(org);
            savedMember.setMemberType(Earth2026Connection.EntityType.CHARACTER);
            savedMember.setMemberId(100L);
            savedMember.setRole("Field Agent");
            savedMember.setRank(1);
            savedMember.setLoyalty(50);
            savedMember.setActive(true);
            when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
            when(memberRepository.save(any())).thenReturn(savedMember);

            Optional<Earth2026OrgMember> result = service.addMember(
                    1L, Earth2026Connection.EntityType.CHARACTER, 100L, "Field Agent");

            assertTrue(result.isPresent());
            assertEquals("Field Agent", result.get().getRole());
            assertEquals(1, result.get().getRank());
            assertEquals(50, result.get().getLoyalty());
            assertTrue(result.get().isActive());
            verify(memberRepository).save(any());
        }

        @Test
        @DisplayName("addMember returns empty when organization not found")
        void addMember_returnsEmpty_whenOrganizationNotFound() {
            when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026OrgMember> result = service.addMember(
                    999L, Earth2026Connection.EntityType.CHARACTER, 100L, "Agent");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("removeMember deactivates member")
        void removeMember_deactivatesMember() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member = createMember(org);
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

        @Test
        @DisplayName("isMember returns true when entity is member")
        void isMember_returnsTrue_whenMember() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member = createMember(org);
            when(memberRepository.findByOrganizationIdAndActiveTrue(1L)).thenReturn(List.of(member));

            boolean result = service.isMember(1L, Earth2026Connection.EntityType.CHARACTER, 100L);

            assertTrue(result);
        }

        @Test
        @DisplayName("isMember returns false when entity is not member")
        void isMember_returnsFalse_whenNotMember() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member = createMember(org);
            member.setMemberId(200L);
            when(memberRepository.findByOrganizationIdAndActiveTrue(1L)).thenReturn(List.of(member));

            boolean result = service.isMember(1L, Earth2026Connection.EntityType.CHARACTER, 100L);

            assertFalse(result);
        }

        @Test
        @DisplayName("getActiveMemberCount returns count")
        void getActiveMemberCount_returnsCount() {
            when(memberRepository.findByOrganizationIdAndActiveTrue(1L))
                    .thenReturn(List.of(createMember(createOrganization()), createMember(createOrganization())));

            int count = service.getActiveMemberCount(1L);

            assertEquals(2, count);
        }
    }

    @Nested
    @DisplayName("Loyalty Management")
    class LoyaltyManagement {

        @Test
        @DisplayName("increaseLoyalty increases loyalty")
        void increaseLoyalty_increasesLoyalty() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member = createMember(org);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.save(member)).thenReturn(member);

            Optional<Earth2026OrgMember> result = service.increaseLoyalty(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(90, result.get().getLoyalty());
            verify(memberRepository).save(member);
        }

        @Test
        @DisplayName("increaseLoyalty clamps to 100")
        void increaseLoyalty_clampsTo100() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member = createMember(org);
            member.setLoyalty(90);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.save(member)).thenReturn(member);

            Optional<Earth2026OrgMember> result = service.increaseLoyalty(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(100, result.get().getLoyalty());
        }

        @Test
        @DisplayName("decreaseLoyalty decreases loyalty")
        void decreaseLoyalty_decreasesLoyalty() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member = createMember(org);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.save(member)).thenReturn(member);

            Optional<Earth2026OrgMember> result = service.decreaseLoyalty(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(50, result.get().getLoyalty());
        }

        @Test
        @DisplayName("decreaseLoyalty clamps to 0")
        void decreaseLoyalty_clampsTo0() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member = createMember(org);
            member.setLoyalty(10);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.save(member)).thenReturn(member);

            Optional<Earth2026OrgMember> result = service.decreaseLoyalty(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(0, result.get().getLoyalty());
        }

        @Test
        @DisplayName("increaseLoyalty returns empty when not found")
        void increaseLoyalty_returnsEmpty_whenNotFound() {
            when(memberRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026OrgMember> result = service.increaseLoyalty(999L, 10);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Hierarchy Operations")
    class HierarchyOperations {

        @Test
        @DisplayName("promoteMember increases rank")
        void promoteMember_increasesRank() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member = createMember(org);
            member.setRank(3);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.save(member)).thenReturn(member);

            Optional<Earth2026OrgMember> result = service.promoteMember(1L, 2);

            assertTrue(result.isPresent());
            assertEquals(5, result.get().getRank());
        }

        @Test
        @DisplayName("promoteMember clamps to 10")
        void promoteMember_clampsTo10() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member = createMember(org);
            member.setRank(9);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.save(member)).thenReturn(member);

            Optional<Earth2026OrgMember> result = service.promoteMember(1L, 2);

            assertTrue(result.isPresent());
            assertEquals(10, result.get().getRank());
        }

        @Test
        @DisplayName("demoteMember decreases rank")
        void demoteMember_decreasesRank() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member = createMember(org);
            member.setRank(5);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.save(member)).thenReturn(member);

            Optional<Earth2026OrgMember> result = service.demoteMember(1L, 2);

            assertTrue(result.isPresent());
            assertEquals(3, result.get().getRank());
        }

        @Test
        @DisplayName("demoteMember clamps to 1")
        void demoteMember_clampsTo1() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member = createMember(org);
            member.setRank(2);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.save(member)).thenReturn(member);

            Optional<Earth2026OrgMember> result = service.demoteMember(1L, 2);

            assertTrue(result.isPresent());
            assertEquals(1, result.get().getRank());
        }
    }

    @Nested
    @DisplayName("Organization Events")
    class OrganizationEvents {

        @Test
        @DisplayName("organizationMeeting increases all member loyalty")
        void organizationMeeting_increasesMemberLoyalty() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member1 = createMember(org);
            Earth2026OrgMember member2 = createMember(org);
            member2.setId(2L);
            member2.setLoyalty(60);

            when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
            when(memberRepository.findByOrganizationIdAndActiveTrue(1L))
                    .thenReturn(List.of(member1, member2));
            when(memberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(organizationRepository.save(org)).thenReturn(org);

            Optional<Earth2026Organization> result = service.organizationMeeting(1L, 10);

            assertTrue(result.isPresent());
            assertEquals(80, member1.getLoyalty());
            assertEquals(70, member2.getLoyalty());
            assertEquals(76, org.getReputation());
            verify(memberRepository, times(2)).save(any());
        }

        @Test
        @DisplayName("organizationMeeting clamps reputation to 100")
        void organizationMeeting_clampsReputation() {
            Earth2026Organization org = createOrganization();
            org.setReputation(100);
            when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
            when(memberRepository.findByOrganizationIdAndActiveTrue(1L)).thenReturn(List.of());
            when(organizationRepository.save(org)).thenReturn(org);

            Optional<Earth2026Organization> result = service.organizationMeeting(1L, 10);

            assertTrue(result.isPresent());
            assertEquals(100, result.get().getReputation());
        }

        @Test
        @DisplayName("organizationMeeting returns empty when not found")
        void organizationMeeting_returnsEmpty_whenNotFound() {
            when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Organization> result = service.organizationMeeting(999L, 10);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Entity Methods")
    class EntityMethods {

        @Test
        @DisplayName("Organization defaults are correct")
        void organizationDefaults_areCorrect() {
            Earth2026Organization org = new Earth2026Organization();
            assertEquals(1, org.getHierarchyLevel());
            assertEquals(50, org.getReputation());
            assertEquals(50, org.getWealth());
            assertEquals(0, org.getSecrecyLevel());
            assertTrue(org.isActive());
        }

        @Test
        @DisplayName("Organization constructor sets fields")
        void organizationConstructor_setsFields() {
            Earth2026Organization org = new Earth2026Organization(
                    "Test Org", Earth2026Organization.OrganizationType.FAMILY);

            assertEquals("Test Org", org.getName());
            assertEquals(Earth2026Organization.OrganizationType.FAMILY, org.getOrgType());
        }

        @Test
        @DisplayName("setReputation clamps to 1-100")
        void setReputation_clampsToRange() {
            Earth2026Organization org = new Earth2026Organization();
            org.setReputation(0);
            assertEquals(1, org.getReputation());
            org.setReputation(150);
            assertEquals(100, org.getReputation());
        }

        @Test
        @DisplayName("setWealth clamps to 1-100")
        void setWealth_clampsToRange() {
            Earth2026Organization org = new Earth2026Organization();
            org.setWealth(0);
            assertEquals(1, org.getWealth());
            org.setWealth(150);
            assertEquals(100, org.getWealth());
        }

        @Test
        @DisplayName("setSecrecyLevel clamps to 0-100")
        void setSecrecyLevel_clampsToRange() {
            Earth2026Organization org = new Earth2026Organization();
            org.setSecrecyLevel(-10);
            assertEquals(0, org.getSecrecyLevel());
            org.setSecrecyLevel(150);
            assertEquals(100, org.getSecrecyLevel());
        }

        @Test
        @DisplayName("OrgMember defaults are correct")
        void orgMemberDefaults_areCorrect() {
            Earth2026Organization org = createOrganization();
            Earth2026OrgMember member = new Earth2026OrgMember();
            assertEquals(1, member.getRank());
            assertEquals(50, member.getLoyalty());
            assertTrue(member.isActive());
        }

        @Test
        @DisplayName("setRank clamps to 1-10")
        void setRank_clampsToRange() {
            Earth2026OrgMember member = new Earth2026OrgMember();
            member.setRank(0);
            assertEquals(1, member.getRank());
            member.setRank(15);
            assertEquals(10, member.getRank());
        }

        @Test
        @DisplayName("setLoyalty clamps to 0-100")
        void setLoyalty_clampsToRange() {
            Earth2026OrgMember member = new Earth2026OrgMember();
            member.setLoyalty(-10);
            assertEquals(0, member.getLoyalty());
            member.setLoyalty(150);
            assertEquals(100, member.getLoyalty());
        }

        @Test
        @DisplayName("promote clamps to 10")
        void promote_clampsTo10() {
            Earth2026OrgMember member = new Earth2026OrgMember();
            member.setRank(9);
            member.promote(2);
            assertEquals(10, member.getRank());
        }

        @Test
        @DisplayName("demote clamps to 1")
        void demote_clampsTo1() {
            Earth2026OrgMember member = new Earth2026OrgMember();
            member.setRank(2);
            member.demote(2);
            assertEquals(1, member.getRank());
        }

        @Test
        @DisplayName("toString returns meaningful string")
        void toString_returnsMeaningfulString() {
            Earth2026Organization org = createOrganization();
            String str = org.toString();
            assertTrue(str.contains("Earth2026Organization"));
            assertTrue(str.contains("Test Organization"));
        }

        @Test
        @DisplayName("OrganizationType enum has all expected values")
        void organizationTypeEnum_hasAllValues() {
            Earth2026Organization.OrganizationType[] types = Earth2026Organization.OrganizationType.values();
            assertTrue(types.length >= 10);
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("ESPIONAGE_RING")).findFirst().orElse(null));
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("MERCHANT_GUILD")).findFirst().orElse(null));
            assertNotNull(Arrays.stream(types).filter(t -> t.name().equals("CRIMINAL_SYNDICATE")).findFirst().orElse(null));
        }
    }
}
