package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrClass;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrClassRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("LotrClassService Tests")
class LotrClassServiceTest {

    private LotrClassRepository classRepository;
    private LotrClassService classService;

    @BeforeEach
    void setUp() {
        classRepository = mock(LotrClassRepository.class);
        classService = new LotrClassService(classRepository);
    }

    // ---- findAll ----

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("should return all classes")
        void findAll_shouldReturnAllClasses() {
            // Arrange
            LotrClass cls = new LotrClass();
            cls.setId(1L);
            cls.setName("Scout");
            when(classRepository.findAll()).thenReturn(List.of(cls));

            // Act
            List<LotrClass> result = classService.findAll();

            // Assert
            assertEquals(1, result.size());
            assertEquals("Scout", result.get(0).getName());
        }

        @Test
        @DisplayName("should return empty list when no classes")
        void findAll_shouldReturnEmptyList() {
            // Arrange
            when(classRepository.findAll()).thenReturn(List.of());

            // Act
            List<LotrClass> result = classService.findAll();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- findActiveClasses ----

    @Nested
    @DisplayName("findActiveClasses Tests")
    class FindActiveClassesTests {

        @Test
        @DisplayName("should return only active classes")
        void findActiveClasses_shouldReturnOnlyActive() {
            // Arrange
            LotrClass active = new LotrClass();
            active.setId(1L);
            active.setName("Scout");
            active.setActive(true);

            LotrClass inactive = new LotrClass();
            inactive.setId(2L);
            inactive.setName("OldClass");
            inactive.setActive(false);

            when(classRepository.findAll()).thenReturn(List.of(active, inactive));

            // Act
            List<LotrClass> result = classService.findActiveClasses();

            // Assert
            assertEquals(1, result.size());
            assertEquals("Scout", result.get(0).getName());
        }
    }

    // ---- findById ----

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("should return class when found")
        void findById_shouldReturnClass() {
            // Arrange
            LotrClass cls = new LotrClass();
            cls.setId(1L);
            cls.setName("Warrior");
            when(classRepository.findById(1L)).thenReturn(Optional.of(cls));

            // Act
            Optional<LotrClass> result = classService.findById(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Warrior", result.get().getName());
        }

        @Test
        @DisplayName("should return empty when not found")
        void findById_shouldReturnEmpty() {
            // Arrange
            when(classRepository.findById(99L)).thenReturn(Optional.empty());

            // Act
            Optional<LotrClass> result = classService.findById(99L);

            // Assert
            assertFalse(result.isPresent());
        }
    }

    // ---- findByName ----

    @Nested
    @DisplayName("findByName Tests")
    class FindByNameTests {

        @Test
        @DisplayName("should find class by partial name")
        void findByName_shouldFindPartialMatch() {
            // Arrange
            LotrClass cls = new LotrClass();
            cls.setId(1L);
            cls.setName("Scout");
            when(classRepository.findAll()).thenReturn(List.of(cls));

            // Act
            List<LotrClass> result = classService.findByName("scout");

            // Assert
            assertEquals(1, result.size());
            assertEquals("Scout", result.get(0).getName());
        }

        @Test
        @DisplayName("should be case insensitive")
        void findByName_shouldBeCaseInsensitive() {
            // Arrange
            LotrClass cls = new LotrClass();
            cls.setId(1L);
            cls.setName("Ranger");
            when(classRepository.findAll()).thenReturn(List.of(cls));

            // Act
            List<LotrClass> result = classService.findByName("RAN");

            // Assert
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should return empty when no match")
        void findByName_shouldReturnEmpty() {
            // Arrange
            when(classRepository.findAll()).thenReturn(List.of());

            // Act
            List<LotrClass> result = classService.findByName("Nonexistent");

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- findCasterClasses ----

    @Nested
    @DisplayName("findCasterClasses Tests")
    class FindCasterClassesTests {

        @Test
        @DisplayName("should return only caster classes")
        void findCasterClasses_shouldReturnOnlyCasters() {
            // Arrange
            LotrClass caster = new LotrClass();
            caster.setId(1L);
            caster.setName("Mage");
            caster.setCaster(true);

            LotrClass nonCaster = new LotrClass();
            nonCaster.setId(2L);
            nonCaster.setName("Warrior");
            nonCaster.setCaster(false);

            when(classRepository.findAll()).thenReturn(List.of(caster, nonCaster));

            // Act
            List<LotrClass> result = classService.findCasterClasses();

            // Assert
            assertEquals(1, result.size());
            assertEquals("Mage", result.get(0).getName());
        }
    }

    // ---- createClass ----

    @Nested
    @DisplayName("createClass Tests")
    class CreateClassTests {

        @Test
        @DisplayName("should create a new class")
        void createClass_shouldCreateClass() {
            // Arrange
            when(classRepository.save(any(LotrClass.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrClass result = classService.createClass("Scout", "Swift explorer", "d6", "Dex",
                    "Light armor", "Simple weapons", false, null);

            // Assert
            assertNotNull(result);
            assertEquals("Scout", result.getName());
            assertEquals("Swift explorer", result.getDescription());
            assertEquals("d6", result.getHitDice());
            assertEquals("Dex", result.getPrimaryAbility());
            assertFalse(result.isCaster());
            assertTrue(result.isActive());
            verify(classRepository).save(result);
        }

        @Test
        @DisplayName("should create a caster class")
        void createClass_shouldCreateCasterClass() {
            // Arrange
            when(classRepository.save(any(LotrClass.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrClass result = classService.createClass("Mage", "Arcane spellcaster", "d6", "Int",
                    "None", "Wands and staves", true, "Int");

            // Assert
            assertTrue(result.isCaster());
            assertEquals("Int", result.getCasterAbility());
        }
    }

    // ---- updateClass ----

    @Nested
    @DisplayName("updateClass Tests")
    class UpdateClassTests {

        @Test
        @DisplayName("should update an existing class")
        void updateClass_shouldUpdateClass() {
            // Arrange
            LotrClass existing = new LotrClass();
            existing.setId(1L);
            existing.setName("Scout");
            when(classRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(classRepository.save(any(LotrClass.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrClass result = classService.updateClass(1L, "Updated Scout", "Better explorer", "d8",
                    "Str", "Heavy armor", "All weapons", false, null);

            // Assert
            assertEquals("Updated Scout", result.getName());
            assertEquals("Better explorer", result.getDescription());
            assertEquals("d8", result.getHitDice());
            verify(classRepository).save(result);
        }

        @Test
        @DisplayName("should throw when class not found")
        void updateClass_shouldThrowWhenNotFound() {
            // Arrange
            when(classRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    classService.updateClass(99L, "Name", "Desc", "d6", "Str", "p1", "p2", false, null));
        }
    }

    // ---- deleteClass ----

    @Nested
    @DisplayName("deleteClass Tests")
    class DeleteClassTests {

        @Test
        @DisplayName("should delete an existing class")
        void deleteClass_shouldDeleteClass() {
            // Arrange
            when(classRepository.existsById(1L)).thenReturn(true);

            // Act
            classService.deleteClass(1L);

            // Assert
            verify(classRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when class not found")
        void deleteClass_shouldThrowWhenNotFound() {
            // Arrange
            when(classRepository.existsById(99L)).thenReturn(false);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> classService.deleteClass(99L));
        }
    }

    // ---- initializeClassDatabase ----

    @Nested
    @DisplayName("initializeClassDatabase Tests")
    class InitializeClassDatabaseTests {

        @Test
        @DisplayName("should skip if database already has classes")
        void initializeClassDatabase_shouldSkipIfNotEmpty() {
            // Arrange
            LotrClass cls = new LotrClass();
            cls.setId(1L);
            when(classRepository.findAll()).thenReturn(List.of(cls));

            // Act
            classService.initializeClassDatabase();

            // Assert
            verify(classRepository, never()).save(any());
        }

        @Test
        @DisplayName("should create 6 LOTR classes when empty")
        void initializeClassDatabase_shouldCreateClasses() {
            // Arrange
            when(classRepository.findAll()).thenReturn(List.of());
            when(classRepository.save(any(LotrClass.class))).thenAnswer(inv -> {
                LotrClass cls = inv.getArgument(0);
                cls.setId((long) (classRepository.findAll().size() + 1));
                return cls;
            });
            // After first save, findAll will return non-empty for the count check
            // But since initializeClassDatabase calls findAll() once at the start, it will proceed

            // Act
            classService.initializeClassDatabase();

            // Assert
            // 6 classes should be created: Scout, Warrior, Ranger, Bard, Mage, Healer
            verify(classRepository, times(6)).save(any(LotrClass.class));
        }
    }
}
