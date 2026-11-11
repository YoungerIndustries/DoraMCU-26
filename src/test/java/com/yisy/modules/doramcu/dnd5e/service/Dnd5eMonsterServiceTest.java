package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonster;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonsterAbility;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eMonsterAbilityRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eMonsterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dnd5eMonsterService Tests")
class Dnd5eMonsterServiceTest {

    @Mock private Dnd5eMonsterRepository monsterRepository;
    @Mock private Dnd5eMonsterAbilityRepository abilityRepository;

    private Dnd5eMonsterService service;

    @BeforeEach
    void setUp() {
        service = new Dnd5eMonsterService(monsterRepository, abilityRepository);
    }

    private Dnd5eMonster monster(String name, double cr) {
        Dnd5eMonster m = new Dnd5eMonster();
        m.setId(1L);
        m.setName(name);
        m.setCr(cr);
        m.setXp(200);
        m.setSize("Medium");
        m.setType("Humanoid");
        m.setAlignment("Chaotic Evil");
        m.setAc(15);
        m.setHp(20);
        m.setSpeed(30);
        m.setStr(10);
        m.setDex(10);
        m.setCon(10);
        m.setIntel(10);
        m.setWis(10);
        m.setCha(10);
        m.setSenses("Darkvision 60ft");
        m.setLanguages("Common");
        m.setActive(true);
        return m;
    }

    private Dnd5eMonsterAbility ability(String monsterName, String type, String name) {
        Dnd5eMonsterAbility a = new Dnd5eMonsterAbility();
        a.setId(1L);
        a.setMonsterName(monsterName);
        a.setType(type);
        a.setName(name);
        a.setDescription("Test ability");
        a.setActive(true);
        return a;
    }

    @Nested
    @DisplayName("getMonsterByName")
    class GetMonsterByName {
        @Test
        @DisplayName("returns monster when found")
        void returnsMonsterWhenFound() {
            when(monsterRepository.findByName("Goblin")).thenReturn(Optional.of(monster("Goblin", 0.25)));
            Dnd5eMonster result = service.getMonsterByName("Goblin");
            assertEquals("Goblin", result.getName());
            assertEquals(0.25, result.getCr());
        }

        @Test
        @DisplayName("throws when not found")
        void throwsWhenNotFound() {
            when(monsterRepository.findByName("Nonexistent")).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> service.getMonsterByName("Nonexistent"));
        }
    }

    @Nested
    @DisplayName("getMonster")
    class GetMonster {
        @Test
        @DisplayName("returns monster by id")
        void returnsMonsterById() {
            when(monsterRepository.findById(1L)).thenReturn(Optional.of(monster("Goblin", 0.25)));
            Dnd5eMonster result = service.getMonster(1L);
            assertEquals("Goblin", result.getName());
        }

        @Test
        @DisplayName("throws when not found")
        void throwsWhenNotFound() {
            when(monsterRepository.findById(999L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> service.getMonster(999L));
        }
    }

    @Nested
    @DisplayName("listMonsters")
    class ListMonsters {
        @Test
        @DisplayName("returns all active monsters")
        void returnsAllActiveMonsters() {
            List<Dnd5eMonster> monsters = List.of(monster("Goblin", 0.25), monster("Ogre", 2.0));
            when(monsterRepository.findByActiveTrue()).thenReturn(monsters);
            assertEquals(2, service.listMonsters().size());
        }

        @Test
        @DisplayName("returns empty list when no monsters")
        void returnsEmptyWhenNone() {
            when(monsterRepository.findByActiveTrue()).thenReturn(Collections.emptyList());
            assertTrue(service.listMonsters().isEmpty());
        }
    }

    @Nested
    @DisplayName("listMonstersByType")
    class ListMonstersByType {
        @Test
        @DisplayName("returns monsters filtered by type")
        void returnsMonstersByType() {
            when(monsterRepository.findByTypeAndActiveTrue("Dragon")).thenReturn(List.of(monster("Red Dragon", 10)));
            List<Dnd5eMonster> result = service.listMonstersByType("Dragon");
            assertEquals(1, result.size());
            assertEquals("Red Dragon", result.get(0).getName());
        }
    }

    @Nested
    @DisplayName("listMonstersBySize")
    class ListMonstersBySize {
        @Test
        @DisplayName("returns monsters filtered by size")
        void returnsMonstersBySize() {
            when(monsterRepository.findBySizeAndActiveTrue("Gargantuan")).thenReturn(List.of(monster("Tarrasque", 30)));
            assertEquals(1, service.listMonstersBySize("Gargantuan").size());
        }
    }

    @Nested
    @DisplayName("listMonstersByCr")
    class ListMonstersByCr {
        @Test
        @DisplayName("returns monsters filtered by CR")
        void returnsMonstersByCr() {
            when(monsterRepository.findByCrAndActiveTrue(10.0)).thenReturn(List.of(monster("Black Dragon", 10)));
            assertEquals(1, service.listMonstersByCr(10.0).size());
        }
    }

    @Nested
    @DisplayName("listMonstersByCrRange")
    class ListMonstersByCrRange {
        @Test
        @DisplayName("returns monsters in CR range")
        void returnsMonstersInRange() {
            when(monsterRepository.findByCrRange(1.0, 5.0)).thenReturn(List.of(monster("Ghoul", 1), monster("Wight", 4)));
            assertEquals(2, service.listMonstersByCrRange(1.0, 5.0).size());
        }
    }

    @Nested
    @DisplayName("listMonstersByXpRange")
    class ListMonstersByXpRange {
        @Test
        @DisplayName("returns monsters in XP range")
        void returnsMonstersInXpRange() {
            when(monsterRepository.findByXpRange(200, 1000)).thenReturn(List.of(monster("Goblin", 0.25)));
            assertEquals(1, service.listMonstersByXpRange(200, 1000).size());
        }
    }

    @Nested
    @DisplayName("listMonstersByAlignment")
    class ListMonstersByAlignment {
        @Test
        @DisplayName("returns monsters filtered by alignment")
        void returnsMonstersByAlignment() {
            when(monsterRepository.findByAlignmentAndActiveTrue("Chaotic Evil")).thenReturn(List.of(monster("Ogre", 2)));
            assertEquals(1, service.listMonstersByAlignment("Chaotic Evil").size());
        }
    }

    @Nested
    @DisplayName("listMonstersWithLairActions")
    class ListMonstersWithLairActions {
        @Test
        @DisplayName("returns monsters with lair actions")
        void returnsMonstersWithLairActions() {
            when(monsterRepository.findByHasLairActionsTrueAndActiveTrue()).thenReturn(List.of(monster("Beholder", 13)));
            assertEquals(1, service.listMonstersWithLairActions().size());
        }
    }

    @Nested
    @DisplayName("listMonstersWithLegendaryActions")
    class ListMonstersWithLegendaryActions {
        @Test
        @DisplayName("returns monsters with legendary actions")
        void returnsMonstersWithLegendaryActions() {
            when(monsterRepository.findByHasLegendaryActionsTrueAndActiveTrue()).thenReturn(List.of(monster("Ancient Red Dragon", 24)));
            assertEquals(1, service.listMonstersWithLegendaryActions().size());
        }
    }

    @Nested
    @DisplayName("listMonstersWithRegionalEffects")
    class ListMonstersWithRegionalEffects {
        @Test
        @DisplayName("returns monsters with regional effects")
        void returnsMonstersWithRegionalEffects() {
            when(monsterRepository.findByHasRegionalEffectsTrueAndActiveTrue()).thenReturn(List.of(monster("Tarrasque", 30)));
            assertEquals(1, service.listMonstersWithRegionalEffects().size());
        }
    }

    @Nested
    @DisplayName("searchMonsters")
    class SearchMonsters {
        @Test
        @DisplayName("returns monsters matching keyword")
        void returnsMatchingMonsters() {
            when(monsterRepository.searchByName("dragon")).thenReturn(List.of(monster("Red Dragon", 10)));
            assertEquals(1, service.searchMonsters("dragon").size());
        }

        @Test
        @DisplayName("returns empty when no match")
        void returnsEmptyWhenNoMatch() {
            when(monsterRepository.searchByName("xyz")).thenReturn(Collections.emptyList());
            assertTrue(service.searchMonsters("xyz").isEmpty());
        }
    }

    @Nested
    @DisplayName("createMonster")
    class CreateMonster {
        @Test
        @DisplayName("creates and saves monster")
        void createsAndSavesMonster() {
            Dnd5eMonster m = monster("New Monster", 1.0);
            when(monsterRepository.save(any())).thenReturn(m);
            Dnd5eMonster result = service.createMonster(m);
            assertTrue(result.isActive());
            assertEquals("New Monster", result.getName());
            verify(monsterRepository).save(m);
        }
    }

    @Nested
    @DisplayName("updateMonster")
    class UpdateMonster {
        @Test
        @DisplayName("updates monster fields")
        void updatesMonsterFields() {
            Dnd5eMonster existing = monster("Old Name", 1.0);
            Dnd5eMonster updates = monster("New Name", 5.0);
            when(monsterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(monsterRepository.save(existing)).thenReturn(existing);
            service.updateMonster(1L, updates);
            assertEquals("New Name", existing.getName());
            assertEquals(5.0, existing.getCr());
        }
    }

    @Nested
    @DisplayName("deleteMonster")
    class DeleteMonster {
        @Test
        @DisplayName("soft deletes monster")
        void softDeletesMonster() {
            Dnd5eMonster m = monster("Goblin", 0.25);
            when(monsterRepository.findById(1L)).thenReturn(Optional.of(m));
            when(monsterRepository.save(m)).thenReturn(m);
            service.deleteMonster(1L);
            assertFalse(m.isActive());
        }
    }

    @Nested
    @DisplayName("listAbilities")
    class ListAbilities {
        @Test
        @DisplayName("returns abilities for monster")
        void returnsAbilitiesForMonster() {
            when(abilityRepository.findByMonsterNameAndActiveTrue("Goblin"))
                    .thenReturn(List.of(ability("Goblin", "Attack", "Scimitar")));
            assertEquals(1, service.listAbilities("Goblin").size());
        }
    }

    @Nested
    @DisplayName("listAbilitiesByType")
    class ListAbilitiesByType {
        @Test
        @DisplayName("returns abilities filtered by type")
        void returnsAbilitiesByType() {
            when(abilityRepository.findByMonsterNameAndTypeAndActiveTrue("Goblin", "Attack"))
                    .thenReturn(List.of(ability("Goblin", "Attack", "Scimitar")));
            assertEquals(1, service.listAbilitiesByType("Goblin", "Attack").size());
        }
    }

    @Nested
    @DisplayName("listAbilitiesBySubtype")
    class ListAbilitiesBySubtype {
        @Test
        @DisplayName("returns abilities filtered by subtype")
        void returnsAbilitiesBySubtype() {
            when(abilityRepository.findByMonsterNameAndSubtypeAndActiveTrue("Goblin", "Melee"))
                    .thenReturn(List.of(ability("Goblin", "Attack", "Scimitar")));
            assertEquals(1, service.listAbilitiesBySubtype("Goblin", "Melee").size());
        }
    }

    @Nested
    @DisplayName("searchAbilities")
    class SearchAbilities {
        @Test
        @DisplayName("returns abilities matching keyword")
        void returnsMatchingAbilities() {
            when(abilityRepository.searchByMonsterAndName("Goblin", "scimitar"))
                    .thenReturn(List.of(ability("Goblin", "Attack", "Scimitar")));
            assertEquals(1, service.searchAbilities("Goblin", "scimitar").size());
        }
    }

    @Nested
    @DisplayName("createAbility")
    class CreateAbility {
        @Test
        @DisplayName("creates and saves ability")
        void createsAndSavesAbility() {
            Dnd5eMonsterAbility a = ability("Goblin", "Attack", "Scimitar");
            when(abilityRepository.save(any())).thenReturn(a);
            Dnd5eMonsterAbility result = service.createAbility(a);
            assertTrue(result.isActive());
            verify(abilityRepository).save(a);
        }
    }

    @Nested
    @DisplayName("updateAbility")
    class UpdateAbility {
        @Test
        @DisplayName("updates ability fields")
        void updatesAbilityFields() {
            Dnd5eMonsterAbility existing = ability("Goblin", "Attack", "Old Name");
            Dnd5eMonsterAbility updates = ability("Goblin", "Attack", "New Name");
            when(abilityRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(abilityRepository.save(existing)).thenReturn(existing);
            service.updateAbility(1L, updates);
            assertEquals("New Name", existing.getName());
        }
    }

    @Nested
    @DisplayName("deleteAbility")
    class DeleteAbility {
        @Test
        @DisplayName("soft deletes ability")
        void softDeletesAbility() {
            Dnd5eMonsterAbility a = ability("Goblin", "Attack", "Scimitar");
            when(abilityRepository.findById(1L)).thenReturn(Optional.of(a));
            when(abilityRepository.save(a)).thenReturn(a);
            service.deleteAbility(1L);
            assertFalse(a.isActive());
        }
    }

    @Nested
    @DisplayName("getXpForCr")
    class GetXpForCr {
        @Test
        @DisplayName("returns XP for known CR")
        void returnsXpForKnownCr() {
            assertEquals(200, service.getXpForCr(1.0));
            assertEquals(5900, service.getXpForCr(10.0));
            assertEquals(230000, service.getXpForCr(30.0));
        }

        @Test
        @DisplayName("returns 0 for unknown CR")
        void returnsZeroForUnknownCr() {
            assertEquals(0, service.getXpForCr(99.0));
        }
    }

    @Nested
    @DisplayName("getXpTable")
    class GetXpTable {
        @Test
        @DisplayName("returns XP table")
        void returnsXpTable() {
            Map<Double, Integer> table = service.getXpTable();
            assertTrue(table.containsKey(1.0));
            assertTrue(table.containsKey(30.0));
            assertEquals(200, table.get(1.0));
        }
    }

    @Nested
    @DisplayName("listMonsterTypes")
    class ListMonsterTypes {
        @Test
        @DisplayName("returns all monster types")
        void returnsAllMonsterTypes() {
            List<String> types = service.listMonsterTypes();
            assertTrue(types.contains("Dragon"));
            assertTrue(types.contains("Undead"));
            assertTrue(types.contains("Aberration"));
            assertEquals(12, types.size());
        }
    }

    @Nested
    @DisplayName("listMonsterSizes")
    class ListMonsterSizes {
        @Test
        @DisplayName("returns all monster sizes")
        void returnsAllMonsterSizes() {
            List<String> sizes = service.listMonsterSizes();
            assertTrue(sizes.contains("Tiny"));
            assertTrue(sizes.contains("Gargantuan"));
            assertEquals(6, sizes.size());
        }
    }

    @Nested
    @DisplayName("listAlignments")
    class ListAlignments {
        @Test
        @DisplayName("returns all alignments")
        void returnsAllAlignments() {
            List<String> alignments = service.listAlignments();
            assertTrue(alignments.contains("Lawful Good"));
            assertTrue(alignments.contains("Chaotic Evil"));
            assertEquals(9, alignments.size());
        }
    }

    @Nested
    @DisplayName("listAbilityTypes")
    class ListAbilityTypes {
        @Test
        @DisplayName("returns all ability types")
        void returnsAllAbilityTypes() {
            List<String> types = service.listAbilityTypes();
            assertTrue(types.contains("Attack"));
            assertTrue(types.contains("Legendary Action"));
            assertTrue(types.contains("Lair Action"));
            assertEquals(9, types.size());
        }
    }

    @Nested
    @DisplayName("initializeMonsterDatabase")
    class InitializeMonsterDatabase {
        @Test
        @DisplayName("skips if already initialized")
        void skipsIfAlreadyInitialized() {
            when(monsterRepository.countByActiveTrue()).thenReturn(50L);
            service.initializeMonsterDatabase();
            verify(monsterRepository, never()).save(any(Dnd5eMonster.class));
        }

        @Test
        @DisplayName("populates if missing")
        void populatesIfMissing() {
            when(monsterRepository.countByActiveTrue()).thenReturn(0L);
            when(monsterRepository.findByName(anyString())).thenReturn(Optional.empty());
            when(monsterRepository.save(any(Dnd5eMonster.class))).thenAnswer(inv -> inv.getArgument(0));
            when(abilityRepository.findByMonsterNameAndTypeAndActiveTrue(anyString(), anyString())).thenReturn(Collections.emptyList());
            when(abilityRepository.save(any(Dnd5eMonsterAbility.class))).thenAnswer(inv -> inv.getArgument(0));
            when(abilityRepository.findAll()).thenReturn(Collections.emptyList());
            service.initializeMonsterDatabase();
            verify(monsterRepository, atLeastOnce()).save(any(Dnd5eMonster.class));
        }
    }
}
