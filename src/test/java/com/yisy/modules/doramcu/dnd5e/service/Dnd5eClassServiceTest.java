package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.*;
import com.yisy.modules.doramcu.dnd5e.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dnd5eClassService Tests")
class Dnd5eClassServiceTest {

    @Mock Dnd5eClassRepository classRepository;
    @Mock Dnd5eClassFeatureRepository classFeatureRepository;
    @Mock Dnd5eSubclassRepository subclassRepository;
    @Mock Dnd5eCharacterClassRepository characterClassRepository;
    @Mock Dnd5eCharacterRepository characterRepository;

    @InjectMocks Dnd5eClassService service;

    private Dnd5eClass makeClass(String name) {
        Dnd5eClass cls = new Dnd5eClass();
        cls.setId(1L);
        cls.setName(name);
        cls.setDescription("Test class");
        cls.setHitDie("d10");
        cls.setPrimaryAbility("STR");
        cls.setWeaponProficiencies("Simple weapons");
        cls.setSavingThrowProficiencies("Strength, Constitution");
        cls.setActive(true);
        return cls;
    }

    private Dnd5eCharacter makeCharacter() {
        Dnd5eCharacter c = new Dnd5eCharacter();
        c.setId(1L);
        c.setName("TestChar");
        c.setCharacterClass("Fighter");
        c.setLevel(5);
        return c;
    }

    // ==================== Class CRUD ====================

    @Nested
    @DisplayName("getClassByName")
    class GetClassByName {
        @Test
        @DisplayName("should get class by name")
        void shouldGetClassByName() {
            Dnd5eClass cls = makeClass("Fighter");
            when(classRepository.findByName("Fighter")).thenReturn(Optional.of(cls));
            assertEquals(cls, service.getClassByName("Fighter"));
        }

        @Test
        @DisplayName("should throw if not found")
        void shouldThrowIfNotFound() {
            when(classRepository.findByName("Nonexistent")).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> service.getClassByName("Nonexistent"));
        }
    }

    @Nested
    @DisplayName("getClass")
    class GetClass {
        @Test
        @DisplayName("should get class by ID")
        void shouldGetClassById() {
            Dnd5eClass cls = makeClass("Fighter");
            when(classRepository.findById(1L)).thenReturn(Optional.of(cls));
            assertEquals(cls, service.getClass(1L));
        }

        @Test
        @DisplayName("should throw if not found")
        void shouldThrowIfNotFound() {
            when(classRepository.findById(999L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> service.getClass(999L));
        }
    }

    @Nested
    @DisplayName("listClasses")
    class ListClasses {
        @Test
        @DisplayName("should list all active classes")
        void shouldListAllActiveClasses() {
            List<Dnd5eClass> classes = Arrays.asList(makeClass("Fighter"), makeClass("Wizard"));
            when(classRepository.findByActiveTrue()).thenReturn(classes);
            assertEquals(2, service.listClasses().size());
        }

        @Test
        @DisplayName("should return empty list when no classes")
        void shouldReturnEmptyList() {
            when(classRepository.findByActiveTrue()).thenReturn(Collections.emptyList());
            assertTrue(service.listClasses().isEmpty());
        }
    }

    @Nested
    @DisplayName("listClassesByHitDie")
    class ListClassesByHitDie {
        @Test
        @DisplayName("should filter by hit die")
        void shouldFilterByHitDie() {
            Dnd5eClass cls = makeClass("Fighter");
            when(classRepository.findByHitDieAndActiveTrue("d10")).thenReturn(Collections.singletonList(cls));
            assertEquals(1, service.listClassesByHitDie("d10").size());
        }
    }

    @Nested
    @DisplayName("listClassesByPrimaryAbility")
    class ListClassesByPrimaryAbility {
        @Test
        @DisplayName("should filter by primary ability")
        void shouldFilterByPrimaryAbility() {
            Dnd5eClass cls = makeClass("Fighter");
            when(classRepository.findByPrimaryAbilityAndActiveTrue("STR")).thenReturn(Collections.singletonList(cls));
            assertEquals(1, service.listClassesByPrimaryAbility("STR").size());
        }
    }

    @Nested
    @DisplayName("listSpellcastingClasses")
    class ListSpellcastingClasses {
        @Test
        @DisplayName("should list spellcasting classes")
        void shouldListSpellcastingClasses() {
            Dnd5eClass cls = makeClass("Wizard");
            cls.setHasSpellcasting(true);
            when(classRepository.findByHasSpellcastingTrueAndActiveTrue()).thenReturn(Collections.singletonList(cls));
            assertEquals(1, service.listSpellcastingClasses().size());
        }
    }

    @Nested
    @DisplayName("listClassesBySpellcastingType")
    class ListClassesBySpellcastingType {
        @Test
        @DisplayName("should filter by spellcasting type")
        void shouldFilterBySpellcastingType() {
            Dnd5eClass cls = makeClass("Wizard");
            cls.setSpellcastingType("Full");
            when(classRepository.findBySpellcastingTypeAndActiveTrue("Full")).thenReturn(Collections.singletonList(cls));
            assertEquals(1, service.listClassesBySpellcastingType("Full").size());
        }
    }

    @Nested
    @DisplayName("searchClasses")
    class SearchClasses {
        @Test
        @DisplayName("should search by keyword")
        void shouldSearchByKeyword() {
            Dnd5eClass cls = makeClass("Fighter");
            when(classRepository.searchByKeyword("fight")).thenReturn(Collections.singletonList(cls));
            assertEquals(1, service.searchClasses("fight").size());
        }
    }

    @Nested
    @DisplayName("createClass")
    class CreateClass {
        @Test
        @DisplayName("should create a new class")
        void shouldCreateNewClass() {
            Dnd5eClass cls = makeClass("NewClass");
            cls.setId(null);
            when(classRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            Dnd5eClass created = service.createClass(cls);
            assertNotNull(created);
            assertEquals("NewClass", created.getName());
        }
    }

    @Nested
    @DisplayName("updateClass")
    class UpdateClass {
        @Test
        @DisplayName("should update a class")
        void shouldUpdateClass() {
            Dnd5eClass existing = makeClass("Fighter");
            Dnd5eClass updates = makeClass("Fighter");
            updates.setHitDie("d12");
            when(classRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(classRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            Dnd5eClass updated = service.updateClass(1L, updates);
            assertEquals("d12", updated.getHitDie());
        }
    }

    @Nested
    @DisplayName("deleteClass")
    class DeleteClass {
        @Test
        @DisplayName("should deactivate a class")
        void shouldDeactivateClass() {
            Dnd5eClass cls = makeClass("Fighter");
            when(classRepository.findById(1L)).thenReturn(Optional.of(cls));
            when(classRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            service.deleteClass(1L);
            assertFalse(cls.isActive());
        }
    }

    // ==================== Subclass CRUD ====================

    @Nested
    @DisplayName("getSubclassByName")
    class GetSubclassByName {
        @Test
        @DisplayName("should get subclass by name")
        void shouldGetSubclassByName() {
            Dnd5eSubclass sub = new Dnd5eSubclass();
            sub.setName("Champion");
            sub.setClassName("Fighter");
            when(subclassRepository.findByName("Champion")).thenReturn(Optional.of(sub));
            assertEquals(sub, service.getSubclassByName("Champion"));
        }

        @Test
        @DisplayName("should throw if not found")
        void shouldThrowIfNotFound() {
            when(subclassRepository.findByName("Nonexistent")).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> service.getSubclassByName("Nonexistent"));
        }
    }

    @Nested
    @DisplayName("listSubclasses")
    class ListSubclasses {
        @Test
        @DisplayName("should list all subclasses")
        void shouldListAllSubclasses() {
            Dnd5eSubclass sub = new Dnd5eSubclass();
            sub.setName("Champion");
            when(subclassRepository.findByActiveTrue()).thenReturn(Collections.singletonList(sub));
            assertEquals(1, service.listSubclasses().size());
        }
    }

    @Nested
    @DisplayName("listSubclassesByClass")
    class ListSubclassesByClass {
        @Test
        @DisplayName("should filter by class")
        void shouldFilterByClass() {
            Dnd5eSubclass sub = new Dnd5eSubclass();
            sub.setName("Champion");
            sub.setClassName("Fighter");
            when(subclassRepository.findByClassNameAndActiveTrue("Fighter")).thenReturn(Collections.singletonList(sub));
            List<Dnd5eSubclass> result = service.listSubclassesByClass("Fighter");
            assertEquals(1, result.size());
            assertEquals("Champion", result.get(0).getName());
        }
    }

    @Nested
    @DisplayName("searchSubclasses")
    class SearchSubclasses {
        @Test
        @DisplayName("should search subclasses")
        void shouldSearchSubclasses() {
            Dnd5eSubclass sub = new Dnd5eSubclass();
            sub.setName("Champion");
            when(subclassRepository.searchByKeyword("champ")).thenReturn(Collections.singletonList(sub));
            assertEquals(1, service.searchSubclasses("champ").size());
        }
    }

    @Nested
    @DisplayName("createSubclass")
    class CreateSubclass {
        @Test
        @DisplayName("should create a subclass")
        void shouldCreateSubclass() {
            Dnd5eSubclass sub = new Dnd5eSubclass();
            sub.setName("NewSub");
            sub.setClassName("Fighter");
            when(subclassRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            Dnd5eSubclass created = service.createSubclass(sub);
            assertEquals("NewSub", created.getName());
        }
    }

    // ==================== Class Features ====================

    @Nested
    @DisplayName("listFeaturesByClass")
    class ListFeaturesByClass {
        @Test
        @DisplayName("should list features for a class")
        void shouldListFeaturesForClass() {
            Dnd5eClassFeature f = new Dnd5eClassFeature();
            f.setClassName("Fighter");
            f.setLevel(1);
            f.setName("Second Wind");
            when(classFeatureRepository.findByClassNameAndActiveTrue("Fighter")).thenReturn(Collections.singletonList(f));
            assertEquals(1, service.listFeaturesByClass("Fighter").size());
        }
    }

    @Nested
    @DisplayName("listFeaturesByClassAndLevel")
    class ListFeaturesByClassAndLevel {
        @Test
        @DisplayName("should list features at a specific level")
        void shouldListFeaturesAtLevel() {
            Dnd5eClassFeature f = new Dnd5eClassFeature();
            f.setClassName("Fighter");
            f.setLevel(5);
            f.setName("Extra Attack");
            when(classFeatureRepository.findByClassNameAndLevelAndActiveTrue("Fighter", 5)).thenReturn(Collections.singletonList(f));
            List<Dnd5eClassFeature> result = service.listFeaturesByClassAndLevel("Fighter", 5);
            assertEquals(1, result.size());
            assertEquals("Extra Attack", result.get(0).getName());
        }
    }

    @Nested
    @DisplayName("listFeaturesUpToLevel")
    class ListFeaturesUpToLevel {
        @Test
        @DisplayName("should list features up to a level")
        void shouldListFeaturesUpToLevel() {
            Dnd5eClassFeature f1 = new Dnd5eClassFeature();
            f1.setClassName("Fighter");
            f1.setLevel(1);
            Dnd5eClassFeature f2 = new Dnd5eClassFeature();
            f2.setClassName("Fighter");
            f2.setLevel(5);
            when(classFeatureRepository.findFeaturesUpToLevel("Fighter", 5)).thenReturn(Arrays.asList(f1, f2));
            assertEquals(2, service.listFeaturesUpToLevel("Fighter", 5).size());
        }
    }

    @Nested
    @DisplayName("searchFeatures")
    class SearchFeatures {
        @Test
        @DisplayName("should search features by name")
        void shouldSearchFeaturesByName() {
            Dnd5eClassFeature f = new Dnd5eClassFeature();
            f.setName("Second Wind");
            when(classFeatureRepository.searchByName("wind")).thenReturn(Collections.singletonList(f));
            assertEquals(1, service.searchFeatures("wind").size());
        }
    }

    @Nested
    @DisplayName("createFeature")
    class CreateFeature {
        @Test
        @DisplayName("should create a feature")
        void shouldCreateFeature() {
            Dnd5eClassFeature f = new Dnd5eClassFeature();
            f.setClassName("Fighter");
            f.setLevel(1);
            f.setName("Test Feature");
            when(classFeatureRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            Dnd5eClassFeature created = service.createFeature(f);
            assertEquals("Test Feature", created.getName());
        }
    }

    // ==================== Character Class Management ====================

    @Nested
    @DisplayName("addClassToCharacter")
    class AddClassToCharacter {
        @Test
        @DisplayName("should add class to character")
        void shouldAddClassToCharacter() {
            Dnd5eCharacter char1 = makeCharacter();
            when(characterRepository.findById(1L)).thenReturn(Optional.of(char1));
            when(classRepository.findByName("Fighter")).thenReturn(Optional.of(makeClass("Fighter")));
            when(characterClassRepository.existsByCharacterIdAndClassNameAndActiveTrue(1L, "Fighter")).thenReturn(false);
            when(characterClassRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(Collections.emptyList());
            when(characterClassRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(characterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eCharacterClass result = service.addClassToCharacter(1L, "Fighter");
            assertNotNull(result);
            assertEquals("Fighter", result.getClassName());
            assertEquals(1, result.getLevel());
        }

        @Test
        @DisplayName("should throw if character already has class")
        void shouldThrowIfAlreadyHasClass() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(makeCharacter()));
            when(characterClassRepository.existsByCharacterIdAndClassNameAndActiveTrue(1L, "Fighter")).thenReturn(true);
            assertThrows(IllegalArgumentException.class, () -> service.addClassToCharacter(1L, "Fighter"));
        }

        @Test
        @DisplayName("should throw if character not found")
        void shouldThrowIfCharacterNotFound() {
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> service.addClassToCharacter(999L, "Fighter"));
        }

        @Test
        @DisplayName("should throw if class not found")
        void shouldThrowIfClassNotFound() {
            Dnd5eCharacter char1 = makeCharacter();
            when(characterRepository.findById(1L)).thenReturn(Optional.of(char1));
            when(characterClassRepository.existsByCharacterIdAndClassNameAndActiveTrue(1L, "Nonexistent")).thenReturn(false);
            when(classRepository.findByName("Nonexistent")).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> service.addClassToCharacter(1L, "Nonexistent"));
        }
    }

    @Nested
    @DisplayName("levelUpClass")
    class LevelUpClass {
        @Test
        @DisplayName("should level up a class")
        void shouldLevelUpClass() {
            Dnd5eCharacterClass cc = new Dnd5eCharacterClass();
            cc.setCharacterId(1L);
            cc.setClassName("Fighter");
            cc.setLevel(5);
            Dnd5eCharacter char1 = makeCharacter();
            when(characterClassRepository.findByCharacterIdAndClassNameAndActiveTrue(1L, "Fighter")).thenReturn(Optional.of(cc));
            when(characterRepository.findById(1L)).thenReturn(Optional.of(char1));
            when(characterClassRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(Collections.singletonList(cc));
            when(characterClassRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(characterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.levelUpClass(1L, "Fighter", 10);
            assertEquals(10, cc.getLevel());
        }

        @Test
        @DisplayName("should clamp level to 1-20")
        void shouldClampLevel() {
            Dnd5eCharacterClass cc = new Dnd5eCharacterClass();
            cc.setCharacterId(1L);
            cc.setClassName("Fighter");
            cc.setLevel(5);
            Dnd5eCharacter char1 = makeCharacter();
            when(characterClassRepository.findByCharacterIdAndClassNameAndActiveTrue(1L, "Fighter")).thenReturn(Optional.of(cc));
            when(characterRepository.findById(1L)).thenReturn(Optional.of(char1));
            when(characterClassRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(Collections.singletonList(cc));
            when(characterClassRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(characterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.levelUpClass(1L, "Fighter", 50);
            assertEquals(20, cc.getLevel());
        }

        @Test
        @DisplayName("should throw if class not found")
        void shouldThrowIfNotFound() {
            when(characterClassRepository.findByCharacterIdAndClassNameAndActiveTrue(1L, "Fighter")).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> service.levelUpClass(1L, "Fighter", 10));
        }
    }

    @Nested
    @DisplayName("setSubclass")
    class SetSubclass {
        @Test
        @DisplayName("should set subclass")
        void shouldSetSubclass() {
            Dnd5eCharacterClass cc = new Dnd5eCharacterClass();
            cc.setCharacterId(1L);
            cc.setClassName("Fighter");
            Dnd5eSubclass sub = new Dnd5eSubclass();
            sub.setName("Champion");
            sub.setClassName("Fighter");
            when(characterClassRepository.findByCharacterIdAndClassNameAndActiveTrue(1L, "Fighter")).thenReturn(Optional.of(cc));
            when(subclassRepository.findByName("Champion")).thenReturn(Optional.of(sub));
            when(characterClassRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.setSubclass(1L, "Fighter", "Champion");
            assertEquals("Champion", cc.getSubclass());
        }

        @Test
        @DisplayName("should throw if subclass not for class")
        void shouldThrowIfSubclassNotForClass() {
            Dnd5eCharacterClass cc = new Dnd5eCharacterClass();
            cc.setClassName("Fighter");
            Dnd5eSubclass sub = new Dnd5eSubclass();
            sub.setName("School of Evocation");
            sub.setClassName("Wizard");
            when(characterClassRepository.findByCharacterIdAndClassNameAndActiveTrue(1L, "Fighter")).thenReturn(Optional.of(cc));
            when(subclassRepository.findByName("School of Evocation")).thenReturn(Optional.of(sub));
            assertThrows(IllegalArgumentException.class, () -> service.setSubclass(1L, "Fighter", "School of Evocation"));
        }
    }

    @Nested
    @DisplayName("listCharacterClasses")
    class ListCharacterClasses {
        @Test
        @DisplayName("should list character classes")
        void shouldListCharacterClasses() {
            Dnd5eCharacterClass cc = new Dnd5eCharacterClass();
            cc.setClassName("Fighter");
            when(characterClassRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(Collections.singletonList(cc));
            assertEquals(1, service.listCharacterClasses(1L).size());
        }
    }

    @Nested
    @DisplayName("removeClassFromCharacter")
    class RemoveClassFromCharacter {
        @Test
        @DisplayName("should remove class from character")
        void shouldRemoveClass() {
            Dnd5eCharacterClass cc = new Dnd5eCharacterClass();
            cc.setClassName("Fighter");
            Dnd5eCharacter char1 = makeCharacter();
            when(characterClassRepository.findByCharacterIdAndClassNameAndActiveTrue(1L, "Fighter")).thenReturn(Optional.of(cc));
            when(characterClassRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(characterRepository.findById(1L)).thenReturn(Optional.of(char1));
            when(characterClassRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(Collections.emptyList());
            when(characterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.removeClassFromCharacter(1L, "Fighter");
            assertFalse(cc.isActive());
        }
    }

    // ==================== Spellcaster Level ====================

    @Nested
    @DisplayName("calculateSpellcasterLevel")
    class CalculateSpellcasterLevel {
        @Test
        @DisplayName("should calculate full caster level")
        void shouldCalculateFullCasterLevel() {
            Dnd5eCharacterClass cc = new Dnd5eCharacterClass();
            cc.setClassName("Wizard");
            cc.setLevel(10);
            Dnd5eClass cls = makeClass("Wizard");
            cls.setSpellcastingType("Full");
            when(characterClassRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(Collections.singletonList(cc));
            when(classRepository.findByName("Wizard")).thenReturn(Optional.of(cls));
            assertEquals(10, service.calculateSpellcasterLevel(1L));
        }

        @Test
        @DisplayName("should calculate half caster level")
        void shouldCalculateHalfCasterLevel() {
            Dnd5eCharacterClass cc = new Dnd5eCharacterClass();
            cc.setClassName("Paladin");
            cc.setLevel(10);
            Dnd5eClass cls = makeClass("Paladin");
            cls.setSpellcastingType("Half");
            when(characterClassRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(Collections.singletonList(cc));
            when(classRepository.findByName("Paladin")).thenReturn(Optional.of(cls));
            assertEquals(5, service.calculateSpellcasterLevel(1L));
        }

        @Test
        @DisplayName("should calculate multiclass spellcaster level")
        void shouldCalculateMulticlassSpellcasterLevel() {
            Dnd5eCharacterClass cc1 = new Dnd5eCharacterClass();
            cc1.setClassName("Wizard");
            cc1.setLevel(6);
            Dnd5eCharacterClass cc2 = new Dnd5eCharacterClass();
            cc2.setClassName("Paladin");
            cc2.setLevel(6);
            Dnd5eClass wizard = makeClass("Wizard");
            wizard.setSpellcastingType("Full");
            Dnd5eClass paladin = makeClass("Paladin");
            paladin.setSpellcastingType("Half");
            when(characterClassRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(Arrays.asList(cc1, cc2));
            when(classRepository.findByName("Wizard")).thenReturn(Optional.of(wizard));
            when(classRepository.findByName("Paladin")).thenReturn(Optional.of(paladin));
            // 6 full + 6/2 half = 6 + 3 = 9
            assertEquals(9, service.calculateSpellcasterLevel(1L));
        }

        @Test
        @DisplayName("should handle artificer as third caster")
        void shouldHandleArtificer() {
            Dnd5eCharacterClass cc = new Dnd5eCharacterClass();
            cc.setClassName("Artificer");
            cc.setLevel(9);
            Dnd5eClass cls = makeClass("Artificer");
            cls.setSpellcastingType("Eldritch Burst");
            when(characterClassRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(Collections.singletonList(cc));
            when(classRepository.findByName("Artificer")).thenReturn(Optional.of(cls));
            // 9/3 = 3
            assertEquals(3, service.calculateSpellcasterLevel(1L));
        }

        @Test
        @DisplayName("should skip warlock pact magic")
        void shouldSkipWarlock() {
            Dnd5eCharacterClass cc = new Dnd5eCharacterClass();
            cc.setClassName("Warlock");
            cc.setLevel(5);
            Dnd5eClass cls = makeClass("Warlock");
            cls.setSpellcastingType("Pact");
            when(characterClassRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(Collections.singletonList(cc));
            when(classRepository.findByName("Warlock")).thenReturn(Optional.of(cls));
            assertEquals(0, service.calculateSpellcasterLevel(1L));
        }
    }

    // ==================== Initialization ====================

    @Nested
    @DisplayName("initializeClassDatabase")
    class InitializeClassDatabase {
        @Test
        @DisplayName("should skip if already initialized")
        void shouldSkipIfAlreadyInitialized() {
            when(classRepository.count()).thenReturn(20L);
            service.initializeClassDatabase();
            verify(classRepository, never()).save(any());
        }

        @Test
        @DisplayName("should populate if missing")
        void shouldPopulateIfMissing() {
            when(classRepository.count()).thenReturn(0L);
            when(classRepository.existsByName(anyString())).thenReturn(false);
            when(classRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(subclassRepository.existsByName(anyString())).thenReturn(false);
            when(subclassRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(classFeatureRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            service.initializeClassDatabase();
            verify(classRepository, atLeastOnce()).save(any());
        }
    }
}
