package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMonster;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMonsterAbility;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrMonsterAbilityRepository;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrMonsterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("LotrMonsterService Tests")
class LotrMonsterServiceTest {

    private LotrMonsterRepository monsterRepository;
    private LotrMonsterAbilityRepository abilityRepository;
    private LotrMonsterService monsterService;

    @BeforeEach
    void setUp() {
        monsterRepository = mock(LotrMonsterRepository.class);
        abilityRepository = mock(LotrMonsterAbilityRepository.class);
        monsterService = new LotrMonsterService(monsterRepository, abilityRepository);
    }

    // ---- findAll ----

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("should return all monsters")
        void findAll_shouldReturnAll() {
            // Arrange
            List<LotrMonster> monsters = List.of(
                    createMonster(1L, "Orc"),
                    createMonster(2L, "Uruk-hai")
            );
            when(monsterRepository.findAll()).thenReturn(monsters);

            // Act
            List<LotrMonster> result = monsterService.findAll();

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should return empty list when no monsters exist")
        void findAll_shouldReturnEmpty() {
            // Arrange
            when(monsterRepository.findAll()).thenReturn(List.of());

            // Act
            List<LotrMonster> result = monsterService.findAll();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- findById ----

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("should return monster by id")
        void findById_shouldReturnMonster() {
            // Arrange
            LotrMonster monster = createMonster(1L, "Orc");
            when(monsterRepository.findById(1L)).thenReturn(Optional.of(monster));

            // Act
            LotrMonster result = monsterService.findById(1L);

            // Assert
            assertEquals("Orc", result.getName());
        }

        @Test
        @DisplayName("should throw when monster not found")
        void findById_shouldThrowWhenNotFound() {
            // Arrange
            when(monsterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> monsterService.findById(999L));
            assertTrue(ex.getMessage().contains("Monster not found"));
        }
    }

    // ---- findByType ----

    @Nested
    @DisplayName("findByType Tests")
    class FindByTypeTests {

        @Test
        @DisplayName("should return monsters by type")
        void findByType_shouldReturnMonsters() {
            // Arrange
            List<LotrMonster> monsters = List.of(
                    createMonster(1L, "Orc"),
                    createMonster(2L, "Uruk-hai")
            );
            when(monsterRepository.findByMonsterType("Orc")).thenReturn(monsters);

            // Act
            List<LotrMonster> result = monsterService.findByType("Orc");

            // Assert
            assertEquals(2, result.size());
        }
    }

    // ---- findBosses ----

    @Nested
    @DisplayName("findBosses Tests")
    class FindBossesTests {

        @Test
        @DisplayName("should return boss monsters")
        void findBosses_shouldReturnBosses() {
            // Arrange
            LotrMonster balrog = createMonster(1L, "Balrog");
            balrog.setBoss(true);
            when(monsterRepository.findByIsBossTrue()).thenReturn(List.of(balrog));

            // Act
            List<LotrMonster> result = monsterService.findBosses();

            // Assert
            assertEquals(1, result.size());
            assertTrue(result.get(0).isBoss());
        }
    }

    // ---- createMonster ----

    @Nested
    @DisplayName("createMonster Tests")
    class CreateMonsterTests {

        @Test
        @DisplayName("should create a monster")
        void createMonster_shouldCreateMonster() {
            // Arrange
            when(monsterRepository.save(any(LotrMonster.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrMonster monster = monsterService.createMonster(
                    "Orc", "A fierce orc warrior", "Orc",
                    1.0, 15, 19, false);

            // Assert
            assertNotNull(monster);
            assertEquals("Orc", monster.getName());
            assertEquals("Orc", monster.getMonsterType());
            assertEquals(1.0, monster.getChallengeRating());
            assertEquals(15, monster.getHitPoints());
            assertEquals(19, monster.getArmorClass());
            assertFalse(monster.isBoss());
            verify(monsterRepository).save(monster);
        }
    }

    // ---- getXpForCr ----

    @Nested
    @DisplayName("getXpForCr Tests")
    class GetXpForCrTests {

        @Test
        @DisplayName("should return 10 for CR 0")
        void getXpForCr_shouldReturn10ForCR0() {
            assertEquals(10, monsterService.getXpForCr(0));
        }

        @Test
        @DisplayName("should return 25 for CR 0.5")
        void getXpForCr_shouldReturn25ForCR0_5() {
            assertEquals(25, monsterService.getXpForCr(0.5));
        }

        @Test
        @DisplayName("should return 50 for CR 1")
        void getXpForCr_shouldReturn50ForCR1() {
            assertEquals(50, monsterService.getXpForCr(1));
        }

        @Test
        @DisplayName("should return 100 for CR 2")
        void getXpForCr_shouldReturn100ForCR2() {
            assertEquals(100, monsterService.getXpForCr(2));
        }

        @Test
        @DisplayName("should return 200 for CR 3")
        void getXpForCr_shouldReturn200ForCR3() {
            assertEquals(200, monsterService.getXpForCr(3));
        }

        @Test
        @DisplayName("should return 350 for CR 4")
        void getXpForCr_shouldReturn350ForCR4() {
            assertEquals(350, monsterService.getXpForCr(4));
        }

        @Test
        @DisplayName("should return 500 for CR 5")
        void getXpForCr_shouldReturn500ForCR5() {
            assertEquals(500, monsterService.getXpForCr(5));
        }

        @Test
        @DisplayName("should return 700 for CR 6")
        void getXpForCr_shouldReturn700ForCR6() {
            assertEquals(700, monsterService.getXpForCr(6));
        }

        @Test
        @DisplayName("should return 900 for CR 7")
        void getXpForCr_shouldReturn900ForCR7() {
            assertEquals(900, monsterService.getXpForCr(7));
        }

        @Test
        @DisplayName("should return 1100 for CR 8")
        void getXpForCr_shouldReturn1100ForCR8() {
            assertEquals(1100, monsterService.getXpForCr(8));
        }

        @Test
        @DisplayName("should return 1300 for CR 9")
        void getXpForCr_shouldReturn1300ForCR9() {
            assertEquals(1300, monsterService.getXpForCr(9));
        }

        @Test
        @DisplayName("should return 1500 for CR 10")
        void getXpForCr_shouldReturn1500ForCR10() {
            assertEquals(1500, monsterService.getXpForCr(10));
        }

        @Test
        @DisplayName("should return 4200 for CR 15")
        void getXpForCr_shouldReturn4200ForCR15() {
            assertEquals(4200, monsterService.getXpForCr(15));
        }

        @Test
        @DisplayName("should return 9500 for CR 20")
        void getXpForCr_shouldReturn9500ForCR20() {
            assertEquals(9500, monsterService.getXpForCr(20));
        }

        @Test
        @DisplayName("should return 19500 for CR 30")
        void getXpForCr_shouldReturn19500ForCR30() {
            assertEquals(19500, monsterService.getXpForCr(30));
        }
    }

    // ---- getXpTable ----

    @Nested
    @DisplayName("getXpTable Tests")
    class GetXpTableTests {

        @Test
        @DisplayName("should return XP table with all CR values")
        void getXpTable_shouldReturnTable() {
            // Act
            Map<Double, Integer> table = monsterService.getXpTable();

            // Assert
            assertNotNull(table);
            assertTrue(table.size() > 20);
            assertEquals(10, table.get(0.0));
            assertEquals(25, table.get(0.25));
            assertEquals(50, table.get(0.5));
            assertEquals(100, table.get(1.0));
            assertEquals(62000, table.get(30.0));
        }
    }

    // ---- listMonsterSizes ----

    @Nested
    @DisplayName("listMonsterSizes Tests")
    class ListMonsterSizesTests {

        @Test
        @DisplayName("should return all monster sizes")
        void listMonsterSizes_shouldReturnSizes() {
            // Act
            List<String> sizes = monsterService.listMonsterSizes();

            // Assert
            assertEquals(6, sizes.size());
            assertTrue(sizes.contains("Tiny"));
            assertTrue(sizes.contains("Small"));
            assertTrue(sizes.contains("Medium"));
            assertTrue(sizes.contains("Large"));
            assertTrue(sizes.contains("Huge"));
            assertTrue(sizes.contains("Gargantuan"));
        }
    }

    // ---- listAlignments ----

    @Nested
    @DisplayName("listAlignments Tests")
    class ListAlignmentsTests {

        @Test
        @DisplayName("should return all alignments")
        void listAlignments_shouldReturnAlignments() {
            // Act
            List<String> alignments = monsterService.listAlignments();

            // Assert
            assertEquals(9, alignments.size());
            assertTrue(alignments.contains("Lawful Good"));
            assertTrue(alignments.contains("Chaotic Evil"));
        }
    }

    // ---- delete ----

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("should delete monster by id")
        void delete_shouldDelete() {
            // Act
            monsterService.delete(1L);

            // Assert
            verify(monsterRepository).deleteById(1L);
        }
    }

    // ---- Helper methods ----

    private LotrMonster createMonster(Long id, String name) {
        LotrMonster monster = new LotrMonster();
        monster.setId(id);
        monster.setName(name);
        monster.setMonsterType("Orc");
        monster.setChallengeRating(1.0);
        monster.setHitPoints(15);
        monster.setArmorClass(19);
        monster.setStrScore(16);
        monster.setDexScore(14);
        monster.setConScore(16);
        monster.setIntScore(6);
        monster.setWisScore(10);
        monster.setChaScore(7);
        monster.setFaction("Mordor");
        monster.setBoss(false);
        return monster;
    }
}
