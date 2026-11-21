package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.service.ModService.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ModServiceTest {

    private ModService modService;

    @BeforeEach
    void setUp() {
        modService = new ModService();
    }

    @Nested
    class RegisterMod {
        @Test
        void registerSimpleMod() {
            Optional<ModInfo> result = modService.registerMod(
                    "test-mod", "Test Mod", ModType.CONTENT_PACK, "Author", "A test mod");
            assertTrue(result.isPresent());
            assertEquals("test-mod", result.get().getId());
            assertEquals("Test Mod", result.get().getName());
            assertEquals(ModType.CONTENT_PACK, result.get().getType());
            assertEquals("Author", result.get().getAuthor());
            assertEquals("A test mod", result.get().getDescription());
            assertEquals(ModStatus.REGISTERED, result.get().getStatus());
        }

        @Test
        void registerModWithAllTypes() {
            for (ModType type : ModType.values()) {
                Optional<ModInfo> result = modService.registerMod(
                        type.name().toLowerCase() + "-mod",
                        type.getName() + " Mod",
                        type, "Author", "Description");
                assertTrue(result.isPresent(), "Should register " + type.getName() + " mod");
                assertEquals(type, result.get().getType());
            }
        }

        @Test
        void registerDuplicateModReturnsExisting() {
            Optional<ModInfo> first = modService.registerMod(
                    "dup-mod", "Duplicate Mod", ModType.CONTENT_PACK, "Author", "Desc");
            Optional<ModInfo> second = modService.registerMod(
                    "dup-mod", "Duplicate Mod 2", ModType.RULESET, "Author2", "Desc2");
            assertTrue(second.isPresent());
            assertEquals(first.get().getId(), second.get().getId());
            assertEquals("Duplicate Mod", second.get().getName());
        }

        @Test
        void registerModWithFullMetadata() {
            ModInfo mod = ModInfo.builder("meta-mod", "Metadata Mod")
                    .version("2.0.0")
                    .type(ModType.GAME_MODE)
                    .author("Meta Author")
                    .description("Full metadata mod")
                    .dependencies(Set.of("dep1", "dep2"))
                    .conflicts(Set.of("conf1"))
                    .tags(Set.of("tag1", "tag2"))
                    .build();
            Optional<ModInfo> result = modService.registerMod(mod);
            assertTrue(result.isPresent());
            assertEquals("2.0.0", result.get().getVersion());
            assertEquals(ModType.GAME_MODE, result.get().getType());
            assertEquals(2, result.get().getDependencies().size());
            assertEquals(1, result.get().getConflicts().size());
            assertEquals(2, result.get().getTags().size());
        }

        @Test
        void registrationOrderIsPreserved() {
            modService.registerMod("mod1", "Mod 1", ModType.CONTENT_PACK, "A", "D");
            modService.registerMod("mod2", "Mod 2", ModType.CONTENT_PACK, "A", "D");
            modService.registerMod("mod3", "Mod 3", ModType.CONTENT_PACK, "A", "D");
            List<ModSummary> all = modService.getAllMods();
            assertEquals(3, all.size());
            assertEquals("mod1", all.get(0).getId());
            assertEquals("mod2", all.get(1).getId());
            assertEquals("mod3", all.get(2).getId());
        }
    }

    @Nested
    class GetMod {
        @Test
        void getExistingMod() {
            modService.registerMod("get-mod", "Get Mod", ModType.CONTENT_PACK, "A", "D");
            Optional<ModInfo> result = modService.getMod("get-mod");
            assertTrue(result.isPresent());
            assertEquals("Get Mod", result.get().getName());
        }

        @Test
        void getNonExistentMod() {
            Optional<ModInfo> result = modService.getMod("non-existent");
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class GetAllMods {
        @Test
        void getAllModsIsEmptyInitially() {
            List<ModSummary> all = modService.getAllMods();
            assertTrue(all.isEmpty());
        }

        @Test
        void getAllModsReturnsRegisteredMods() {
            modService.registerMod("a", "A", ModType.CONTENT_PACK, "X", "D");
            modService.registerMod("b", "B", ModType.RULESET, "X", "D");
            List<ModSummary> all = modService.getAllMods();
            assertEquals(2, all.size());
        }
    }

    @Nested
    class GetModsByType {
        @Test
        void getModsByType() {
            modService.registerMod("cp1", "CP1", ModType.CONTENT_PACK, "A", "D");
            modService.registerMod("cp2", "CP2", ModType.CONTENT_PACK, "A", "D");
            modService.registerMod("gm1", "GM1", ModType.GAME_MODE, "A", "D");
            List<ModSummary> contentPacks = modService.getModsByType(ModType.CONTENT_PACK);
            assertEquals(2, contentPacks.size());
            List<ModSummary> gameModes = modService.getModsByType(ModType.GAME_MODE);
            assertEquals(1, gameModes.size());
        }

        @Test
        void getModsByTypeReturnsEmptyForNoMatches() {
            modService.registerMod("cp1", "CP1", ModType.CONTENT_PACK, "A", "D");
            List<ModSummary> gameModes = modService.getModsByType(ModType.GAME_MODE);
            assertTrue(gameModes.isEmpty());
        }
    }

    @Nested
    class GetModsByStatus {
        @Test
        void getModsByStatus() {
            modService.registerMod("r1", "R1", ModType.CONTENT_PACK, "A", "D");
            modService.registerMod("r2", "R2", ModType.CONTENT_PACK, "A", "D");
            modService.enableMod("r1");
            List<ModSummary> enabled = modService.getModsByStatus(ModStatus.ENABLED);
            assertEquals(1, enabled.size());
            assertEquals("r1", enabled.get(0).getId());
        }
    }

    @Nested
    class SearchMods {
        @Test
        void searchModsByTag() {
            ModInfo mod1 = ModInfo.builder("tag1", "Tag1").tag("sci-fi").build();
            ModInfo mod2 = ModInfo.builder("tag2", "Tag2").tag("fantasy").build();
            ModInfo mod3 = ModInfo.builder("tag3", "Tag3").tag("sci-fi").build();
            modService.registerMod(mod1);
            modService.registerMod(mod2);
            modService.registerMod(mod3);
            List<ModSummary> sciFi = modService.searchModsByTag("sci-fi");
            assertEquals(2, sciFi.size());
        }

        @Test
        void searchModsByName() {
            modService.registerMod("alpha", "Alpha Mod", ModType.CONTENT_PACK, "A", "D");
            modService.registerMod("beta", "Beta Mod", ModType.CONTENT_PACK, "A", "D");
            modService.registerMod("gamma", "Gamma Mod", ModType.CONTENT_PACK, "A", "D");
            List<ModSummary> alpha = modService.searchModsByName("alpha");
            assertEquals(1, alpha.size());
            List<ModSummary> mod = modService.searchModsByName("mod");
            assertEquals(3, mod.size());
        }

        @Test
        void searchModsByNameIsCaseInsensitive() {
            modService.registerMod("test", "Test Mod", ModType.CONTENT_PACK, "A", "D");
            List<ModSummary> upper = modService.searchModsByName("TEST");
            assertEquals(1, upper.size());
            List<ModSummary> lower = modService.searchModsByName("test");
            assertEquals(1, lower.size());
        }
    }

    @Nested
    class ValidateMod {
        @Test
        void validateModWithNoDependencies() {
            modService.registerMod("valid", "Valid", ModType.CONTENT_PACK, "A", "D");
            ValidationResult result = modService.validateMod("valid");
            assertTrue(result.isValid());
            assertTrue(result.getErrors().isEmpty());
        }

        @Test
        void validateNonExistentMod() {
            ValidationResult result = modService.validateMod("non-existent");
            assertFalse(result.isValid());
            assertEquals(1, result.getErrorCount());
        }

        @Test
        void validateModWithMissingDependencyWarning() {
            ModInfo mod = ModInfo.builder("dep-mod", "Dep Mod")
                    .dependency("missing-dep")
                    .build();
            modService.registerMod(mod);
            ValidationResult result = modService.validateMod("dep-mod");
            assertTrue(result.isValid());
            assertEquals(1, result.getWarningCount());
        }

        @Test
        void validateModWithDependencyNotEnabled() {
            modService.registerMod("dep", "Dep", ModType.CONTENT_PACK, "A", "D");
            ModInfo mod = ModInfo.builder("main", "Main").dependency("dep").build();
            modService.registerMod(mod);
            ValidationResult result = modService.validateMod("main");
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Dependency not enabled")));
        }

        @Test
        void validateModWithConflictWithEnabledMod() {
            modService.registerMod("conflict", "Conflict", ModType.CONTENT_PACK, "A", "D");
            modService.enableMod("conflict");
            ModInfo mod = ModInfo.builder("main", "Main").conflict("conflict").build();
            modService.registerMod(mod);
            ValidationResult result = modService.validateMod("main");
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Conflicts with enabled mod")));
        }

        @Test
        void validateModWithCircularDependency() {
            ModInfo modA = ModInfo.builder("a", "A").dependency("b").build();
            ModInfo modB = ModInfo.builder("b", "B").dependency("a").build();
            modService.registerMod(modA);
            modService.registerMod(modB);
            ValidationResult result = modService.validateMod("a");
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Circular dependency")));
        }

        @Test
        void validationSetsStatusToValidated() {
            modService.registerMod("valid", "Valid", ModType.CONTENT_PACK, "A", "D");
            modService.validateMod("valid");
            Optional<ModInfo> mod = modService.getMod("valid");
            assertTrue(mod.isPresent());
            assertEquals(ModStatus.VALIDATED, mod.get().getStatus());
        }

        @Test
        void validationSetsStatusToErrorOnFailure() {
            modService.registerMod("dep", "Dep", ModType.CONTENT_PACK, "A", "D");
            ModInfo mod = ModInfo.builder("err", "Err").dependency("dep").build();
            modService.registerMod(mod);
            modService.validateMod("err");
            Optional<ModInfo> found = modService.getMod("err");
            assertTrue(found.isPresent());
            assertEquals(ModStatus.ERROR, found.get().getStatus());
        }
    }

    @Nested
    class EnableMod {
        @Test
        void enableValidMod() {
            modService.registerMod("enable", "Enable", ModType.CONTENT_PACK, "A", "D");
            assertTrue(modService.enableMod("enable"));
            Optional<ModInfo> mod = modService.getMod("enable");
            assertTrue(mod.isPresent());
            assertEquals(ModStatus.ENABLED, mod.get().getStatus());
            assertNotNull(mod.get().getEnabledAt());
        }

        @Test
        void enableNonExistentMod() {
            assertFalse(modService.enableMod("non-existent"));
        }

        @Test
        void enableModWithUnmetDependencies() {
            modService.registerMod("dep", "Dep", ModType.CONTENT_PACK, "A", "D");
            ModInfo mod = ModInfo.builder("main", "Main").dependency("dep").build();
            modService.registerMod(mod);
            assertFalse(modService.enableMod("main"));
        }

        @Test
        void enableModWithEnabledDependencies() {
            modService.registerMod("dep", "Dep", ModType.CONTENT_PACK, "A", "D");
            modService.enableMod("dep");
            ModInfo mod = ModInfo.builder("main", "Main").dependency("dep").build();
            modService.registerMod(mod);
            assertTrue(modService.enableMod("main"));
        }

        @Test
        void enabledModsAreReturned() {
            modService.registerMod("e1", "E1", ModType.CONTENT_PACK, "A", "D");
            modService.registerMod("e2", "E2", ModType.CONTENT_PACK, "A", "D");
            modService.enableMod("e1");
            List<ModSummary> enabled = modService.getEnabledMods();
            assertEquals(1, enabled.size());
            assertEquals("e1", enabled.get(0).getId());
        }
    }

    @Nested
    class DisableMod {
        @Test
        void disableEnabledMod() {
            modService.registerMod("disable", "Disable", ModType.CONTENT_PACK, "A", "D");
            modService.enableMod("disable");
            assertTrue(modService.disableMod("disable"));
            Optional<ModInfo> mod = modService.getMod("disable");
            assertTrue(mod.isPresent());
            assertEquals(ModStatus.DISABLED, mod.get().getStatus());
        }

        @Test
        void disableNonExistentMod() {
            assertFalse(modService.disableMod("non-existent"));
        }

        @Test
        void cannotDisableModWithDependents() {
            modService.registerMod("dep", "Dep", ModType.CONTENT_PACK, "A", "D");
            modService.enableMod("dep");
            ModInfo mod = ModInfo.builder("main", "Main").dependency("dep").build();
            modService.registerMod(mod);
            modService.enableMod("main");
            assertFalse(modService.disableMod("dep"));
        }
    }

    @Nested
    class ModConfig {
        @Test
        void setAndGetConfig() {
            modService.registerMod("cfg", "Cfg", ModType.CONTENT_PACK, "A", "D");
            modService.setModConfig("cfg", "key1", "value1");
            Optional<Object> value = modService.getModConfig("cfg", "key1");
            assertTrue(value.isPresent());
            assertEquals("value1", value.get());
        }

        @Test
        void getNonExistentConfig() {
            Optional<Object> value = modService.getModConfig("cfg", "key1");
            assertTrue(value.isEmpty());
        }

        @Test
        void getAllConfigForMod() {
            modService.registerMod("cfg", "Cfg", ModType.CONTENT_PACK, "A", "D");
            modService.setModConfig("cfg", "key1", "value1");
            modService.setModConfig("cfg", "key2", 42);
            Map<String, Object> config = modService.getModConfig("cfg");
            assertEquals(2, config.size());
            assertEquals("value1", config.get("key1"));
            assertEquals(42, config.get("key2"));
        }

        @Test
        void getConfigForNonExistentMod() {
            Map<String, Object> config = modService.getModConfig("non-existent");
            assertTrue(config.isEmpty());
        }
    }

    @Nested
    class Statistics {
        @Test
        void getModStatistics() {
            modService.registerMod("stats", "Stats", ModType.CONTENT_PACK, "A", "D");
            Map<String, Object> stats = modService.getModStatistics("stats");
            assertEquals("stats", stats.get("modId"));
            assertEquals("Stats", stats.get("name"));
            assertEquals("Content Pack", stats.get("type"));
            assertEquals("Registered", stats.get("status"));
        }

        @Test
        void getStatisticsForNonExistentMod() {
            Map<String, Object> stats = modService.getModStatistics("non-existent");
            assertEquals(false, stats.get("exists"));
        }

        @Test
        void getGlobalStatistics() {
            modService.registerMod("s1", "S1", ModType.CONTENT_PACK, "A", "D");
            modService.registerMod("s2", "S2", ModType.GAME_MODE, "A", "D");
            modService.enableMod("s1");
            Map<String, Object> stats = modService.getStatistics();
            assertEquals(2, stats.get("totalMods"));
            assertEquals(1, stats.get("enabledMods"));
        }

        @Test
        void getGlobalStatisticsIsEmptyInitially() {
            Map<String, Object> stats = modService.getStatistics();
            assertEquals(0, stats.get("totalMods"));
        }
    }

    @Nested
    class ModTypesAndStatuses {
        @Test
        void getAllModTypes() {
            List<Map<String, String>> types = modService.getAllModTypes();
            assertEquals(ModType.values().length, types.size());
        }

        @Test
        void getAllModStatuses() {
            List<Map<String, String>> statuses = modService.getAllModStatuses();
            assertEquals(ModStatus.values().length, statuses.size());
        }
    }

    @Nested
    class RemoveMod {
        @Test
        void removeNonEnabledMod() {
            modService.registerMod("remove", "Remove", ModType.CONTENT_PACK, "A", "D");
            assertTrue(modService.removeMod("remove"));
            assertTrue(modService.getMod("remove").isEmpty());
        }

        @Test
        void cannotRemoveEnabledMod() {
            modService.registerMod("enabled", "Enabled", ModType.CONTENT_PACK, "A", "D");
            modService.enableMod("enabled");
            assertFalse(modService.removeMod("enabled"));
        }

        @Test
        void cannotRemoveModWithDependents() {
            modService.registerMod("dep", "Dep", ModType.CONTENT_PACK, "A", "D");
            ModInfo mod = ModInfo.builder("main", "Main").dependency("dep").build();
            modService.registerMod(mod);
            assertFalse(modService.removeMod("dep"));
        }

        @Test
        void removeNonExistentMod() {
            assertFalse(modService.removeMod("non-existent"));
        }
    }

    @Nested
    class ClearAll {
        @Test
        void clearAllMods() {
            modService.registerMod("c1", "C1", ModType.CONTENT_PACK, "A", "D");
            modService.registerMod("c2", "C2", ModType.CONTENT_PACK, "A", "D");
            modService.clearAll();
            assertTrue(modService.getAllMods().isEmpty());
        }
    }

    @Nested
    class ModInfoBuilder {
        @Test
        void builderWithDefaults() {
            ModInfo mod = ModInfo.builder("default", "Default").build();
            assertEquals("default", mod.getId());
            assertEquals("Default", mod.getName());
            assertEquals("1.0.0", mod.getVersion());
            assertEquals(ModType.CONTENT_PACK, mod.getType());
            assertEquals("Unknown", mod.getAuthor());
            assertEquals("", mod.getDescription());
            assertEquals(ModStatus.REGISTERED, mod.getStatus());
        }

        @Test
        void builderWithAllFields() {
            ModInfo mod = ModInfo.builder("full", "Full")
                    .version("2.0.0")
                    .type(ModType.GAME_MODE)
                    .author("Author")
                    .description("Description")
                    .dependencies(Set.of("dep1", "dep2"))
                    .conflicts(Set.of("conf1"))
                    .tags(Set.of("tag1"))
                    .status(ModStatus.VALIDATED)
                    .build();
            assertEquals("2.0.0", mod.getVersion());
            assertEquals(ModType.GAME_MODE, mod.getType());
            assertEquals("Author", mod.getAuthor());
            assertEquals("Description", mod.getDescription());
            assertEquals(2, mod.getDependencies().size());
            assertEquals(1, mod.getConflicts().size());
            assertEquals(1, mod.getTags().size());
            assertEquals(ModStatus.VALIDATED, mod.getStatus());
        }

        @Test
        void dependsOnMethod() {
            ModInfo mod = ModInfo.builder("dep", "Dep").dependency("target").build();
            assertTrue(mod.dependsOn("target"));
            assertFalse(mod.dependsOn("other"));
        }

        @Test
        void conflictsWithMethod() {
            ModInfo mod = ModInfo.builder("conf", "Conf").conflict("target").build();
            assertTrue(mod.conflictsWith("target"));
            assertFalse(mod.conflictsWith("other"));
        }

        @Test
        void hasTagMethod() {
            ModInfo mod = ModInfo.builder("tag", "Tag").tag("sci-fi").build();
            assertTrue(mod.hasTag("sci-fi"));
            assertFalse(mod.hasTag("fantasy"));
        }

        @Test
        void setStatusChangesStatus() {
            ModInfo mod = ModInfo.builder("status", "Status").build();
            assertEquals(ModStatus.REGISTERED, mod.getStatus());
            mod.setStatus(ModStatus.VALIDATED);
            assertEquals(ModStatus.VALIDATED, mod.getStatus());
            mod.setStatus(ModStatus.ENABLED);
            assertEquals(ModStatus.ENABLED, mod.getStatus());
        }

        @Test
        void setMetadata() {
            ModInfo mod = ModInfo.builder("meta", "Meta").build();
            mod.setMetadata(Map.of("key", "value"));
            assertEquals("value", mod.getMetadata().get("key"));
        }
    }

    @Nested
    class ValidationResultBuilder {
        @Test
        void validResult() {
            ValidationResult result = ValidationResult.valid();
            assertTrue(result.isValid());
            assertTrue(result.getErrors().isEmpty());
            assertTrue(result.getWarnings().isEmpty());
        }

        @Test
        void invalidResult() {
            ValidationResult result = ValidationResult.invalid("Error message");
            assertFalse(result.isValid());
            assertEquals(1, result.getErrorCount());
            assertEquals("Error message", result.getErrors().get(0));
        }

        @Test
        void builderWithErrorsAndWarnings() {
            ValidationResult result = ValidationResult.builder()
                    .addError("Error 1")
                    .addError("Error 2")
                    .addWarning("Warning 1")
                    .build();
            assertFalse(result.isValid());
            assertEquals(2, result.getErrorCount());
            assertEquals(1, result.getWarningCount());
        }

        @Test
        void requireMethod() {
            ValidationResult result = ValidationResult.builder()
                    .require(true, "Should not add")
                    .require(false, "Should add")
                    .build();
            assertFalse(result.isValid());
            assertEquals(1, result.getErrorCount());
        }
    }

    @Nested
    class ModSummaryTest {
        @Test
        void summaryFromModInfo() {
            ModInfo mod = ModInfo.builder("sum", "Sum")
                    .version("1.5.0")
                    .type(ModType.RULESET)
                    .author("Sum Author")
                    .dependencies(Set.of("d1", "d2"))
                    .conflicts(Set.of("c1"))
                    .build();
            ModSummary summary = new ModSummary(mod);
            assertEquals("sum", summary.getId());
            assertEquals("Sum", summary.getName());
            assertEquals("1.5.0", summary.getVersion());
            assertEquals(ModType.RULESET, summary.getType());
            assertEquals("Sum Author", summary.getAuthor());
            assertEquals(2, summary.getDependencyCount());
            assertEquals(1, summary.getConflictCount());
        }
    }

    @Nested
    class IntegrationScenarios {
        @Test
        void fullModLifecycle() {
            Optional<ModInfo> result = modService.registerMod(
                    "lifecycle", "Lifecycle", ModType.GAME_MODE, "Author", "Full lifecycle test");
            assertTrue(result.isPresent());
            assertEquals(ModStatus.REGISTERED, result.get().getStatus());

            ValidationResult validation = modService.validateMod("lifecycle");
            assertTrue(validation.isValid());

            assertTrue(modService.enableMod("lifecycle"));
            Optional<ModInfo> enabled = modService.getMod("lifecycle");
            assertTrue(enabled.isPresent());
            assertEquals(ModStatus.ENABLED, enabled.get().getStatus());

            modService.setModConfig("lifecycle", "setting", "value");
            assertEquals("value", modService.getModConfig("lifecycle", "setting").orElse(null));

            assertTrue(modService.disableMod("lifecycle"));
            Optional<ModInfo> disabled = modService.getMod("lifecycle");
            assertTrue(disabled.isPresent());
            assertEquals(ModStatus.DISABLED, disabled.get().getStatus());

            assertTrue(modService.removeMod("lifecycle"));
            assertTrue(modService.getMod("lifecycle").isEmpty());
        }

        @Test
        void dependencyChain() {
            modService.registerMod("base", "Base", ModType.GAME_MODE, "A", "D");
            modService.enableMod("base");

            ModInfo dep = ModInfo.builder("dep", "Dep").dependency("base").build();
            modService.registerMod(dep);
            modService.enableMod("dep");

            ModInfo dep2 = ModInfo.builder("dep2", "Dep2").dependency("dep").build();
            modService.registerMod(dep2);
            modService.enableMod("dep2");

            List<ModSummary> enabled = modService.getEnabledMods();
            assertEquals(3, enabled.size());

            assertFalse(modService.disableMod("base"));
            assertTrue(modService.disableMod("dep2"));
            assertTrue(modService.disableMod("dep"));
            assertTrue(modService.disableMod("base"));
        }

        @Test
        void conflictDetection() {
            modService.registerMod("mod-a", "Mod A", ModType.RULESET, "A", "D");
            modService.registerMod("mod-b", "Mod B", ModType.RULESET, "A", "D");
            modService.enableMod("mod-a");

            ModInfo modC = ModInfo.builder("mod-c", "Mod C").conflict("mod-a").build();
            modService.registerMod(modC);

            assertFalse(modService.enableMod("mod-c"));
            modService.disableMod("mod-a");
            assertTrue(modService.enableMod("mod-c"));
        }

        @Test
        void searchAndFilterWorkflow() {
            modService.registerMod("sci-fi-1", "Sci-Fi 1", ModType.CONTENT_PACK, "A", "D");
            ModInfo mod2 = ModInfo.builder("sci-fi-2", "Sci-Fi 2")
                    .type(ModType.CONTENT_PACK)
                    .tag("sci-fi")
                    .build();
            modService.registerMod(mod2);
            ModInfo mod3 = ModInfo.builder("fantasy-1", "Fantasy 1")
                    .type(ModType.CONTENT_PACK)
                    .tag("fantasy")
                    .build();
            modService.registerMod(mod3);

            List<ModSummary> sciFi = modService.searchModsByTag("sci-fi");
            assertEquals(1, sciFi.size());

            List<ModSummary> sci = modService.searchModsByName("sci");
            assertEquals(2, sci.size());

            List<ModSummary> contentPacks = modService.getModsByType(ModType.CONTENT_PACK);
            assertEquals(3, contentPacks.size());
        }
    }
}
