package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaignTemplate;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCampaignTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Dnd5eCampaignTemplateServiceTest {

    @Mock
    private Dnd5eCampaignTemplateRepository templateRepository;

    private Dnd5eCampaignTemplateService templateService;

    private Dnd5eCampaignTemplate horrorTemplate;
    private Dnd5eCampaignTemplate sandboxTemplate;

    @BeforeEach
    void setUp() {
        templateService = new Dnd5eCampaignTemplateService(templateRepository);

        horrorTemplate = new Dnd5eCampaignTemplate();
        horrorTemplate.setId(1L);
        horrorTemplate.setName("Ravenloft Horror");
        horrorTemplate.setTagline("Descend into madness in the Domains of Dread");
        horrorTemplate.setDescription("A horror-themed campaign set in a Domain of Dread.");
        horrorTemplate.setTheme("Horror");
        horrorTemplate.setMinLevel(1);
        horrorTemplate.setMaxLevel(10);
        horrorTemplate.setRecommendedPartySize(4);
        horrorTemplate.setEstimatedSessions(12);
        horrorTemplate.setRecommendedSubsystems("[\"horror\", \"tension_pools\", \"dark_gifts\"]");
        horrorTemplate.setSetupInstructions("1. Create a Domain of Dread\n2. Set up a Darklord");
        horrorTemplate.setPacingGuide("Sessions 1-2: Arrival\nSessions 3-4: Discovery");
        horrorTemplate.setExampleHook("The party stumbles upon a fog-shrouded village.");
        horrorTemplate.setExampleVillain("Count Varnek the Undying — A lich who rules Barovia.");
        horrorTemplate.setExampleNpcs("[\"Father Wilhelm\", \"Anya the Innkeeper\"]");
        horrorTemplate.setExampleEncounters("[\"Wandering zombies\", \"Haunted mirror\"]");
        horrorTemplate.setDifficultyScaling("For smaller parties: Reduce monster HP by 25%.");
        horrorTemplate.setDmTips("- Use fear checks sparingly\n- Let dread accumulate naturally");
        horrorTemplate.setActive(true);

        sandboxTemplate = new Dnd5eCampaignTemplate();
        sandboxTemplate.setId(2L);
        sandboxTemplate.setName("Kingdoms at War");
        sandboxTemplate.setTagline("Shape the fate of kingdoms in an open-world epic");
        sandboxTemplate.setDescription("An open-world sandbox campaign.");
        sandboxTemplate.setTheme("Sandbox");
        sandboxTemplate.setMinLevel(3);
        sandboxTemplate.setMaxLevel(15);
        sandboxTemplate.setRecommendedPartySize(5);
        sandboxTemplate.setEstimatedSessions(20);
        sandboxTemplate.setRecommendedSubsystems("[\"bastions\", \"npcs\", \"tension_pools\"]");
        sandboxTemplate.setSetupInstructions("1. Create multiple kingdoms\n2. Set up bastions");
        sandboxTemplate.setPacingGuide("Sessions 1-4: Introduction\nSessions 5-8: Rising Tensions");
        sandboxTemplate.setExampleHook("The party is caught between two warring kingdoms.");
        sandboxTemplate.setExampleVillain("General Voss — A warlord who seeks to unite the kingdoms.");
        sandboxTemplate.setExampleNpcs("[\"King Aldric\", \"Queen Seraphina\"]");
        sandboxTemplate.setExampleEncounters("[\"Ambush on the trade road\", \"Diplomatic negotiation\"]");
        sandboxTemplate.setDifficultyScaling("For smaller parties: Focus on diplomacy.");
        sandboxTemplate.setDmTips("- Let player choices affect the war's outcome");
        sandboxTemplate.setActive(true);
    }

    @Nested
    @DisplayName("createTemplate")
    class CreateTemplate {
        @Test
        @DisplayName("should create and save a template")
        void shouldCreateAndSaveTemplate() {
            when(templateRepository.save(any())).thenReturn(horrorTemplate);
            Dnd5eCampaignTemplate created = templateService.createTemplate(horrorTemplate);
            assertThat(created).isNotNull();
            assertThat(created.getName()).isEqualTo("Ravenloft Horror");
            verify(templateRepository).save(horrorTemplate);
        }
    }

    @Nested
    @DisplayName("getTemplate")
    class GetTemplate {
        @Test
        @DisplayName("should return template when found")
        void shouldReturnTemplateWhenFound() {
            when(templateRepository.findById(1L)).thenReturn(Optional.of(horrorTemplate));
            Optional<Dnd5eCampaignTemplate> result = templateService.getTemplate(1L);
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Ravenloft Horror");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(templateRepository.findById(99L)).thenReturn(Optional.empty());
            Optional<Dnd5eCampaignTemplate> result = templateService.getTemplate(99L);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTemplateByName")
    class GetTemplateByName {
        @Test
        @DisplayName("should return template when found by name")
        void shouldReturnTemplateWhenFoundByName() {
            when(templateRepository.findByName("Ravenloft Horror")).thenReturn(Optional.of(horrorTemplate));
            Optional<Dnd5eCampaignTemplate> result = templateService.getTemplateByName("Ravenloft Horror");
            assertThat(result).isPresent();
            assertThat(result.get().getTheme()).isEqualTo("Horror");
        }

        @Test
        @DisplayName("should return empty when not found by name")
        void shouldReturnEmptyWhenNotFoundByName() {
            when(templateRepository.findByName("Nonexistent")).thenReturn(Optional.empty());
            Optional<Dnd5eCampaignTemplate> result = templateService.getTemplateByName("Nonexistent");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listTemplates")
    class ListTemplates {
        @Test
        @DisplayName("should return all templates")
        void shouldReturnAllTemplates() {
            when(templateRepository.findAll()).thenReturn(Arrays.asList(horrorTemplate, sandboxTemplate));
            List<Dnd5eCampaignTemplate> result = templateService.listTemplates();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should return empty list when no templates")
        void shouldReturnEmptyListWhenNoTemplates() {
            when(templateRepository.findAll()).thenReturn(Collections.emptyList());
            List<Dnd5eCampaignTemplate> result = templateService.listTemplates();
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listActiveTemplates")
    class ListActiveTemplates {
        @Test
        @DisplayName("should return only active templates")
        void shouldReturnOnlyActiveTemplates() {
            when(templateRepository.findByActiveTrue()).thenReturn(Collections.singletonList(horrorTemplate));
            List<Dnd5eCampaignTemplate> result = templateService.listActiveTemplates();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("listTemplatesByTheme")
    class ListTemplatesByTheme {
        @Test
        @DisplayName("should return templates filtered by theme")
        void shouldReturnTemplatesFilteredByTheme() {
            when(templateRepository.findByThemeAndActiveTrue("Horror")).thenReturn(Collections.singletonList(horrorTemplate));
            List<Dnd5eCampaignTemplate> result = templateService.listTemplatesByTheme("Horror");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTheme()).isEqualTo("Horror");
        }

        @Test
        @DisplayName("should return empty list for unknown theme")
        void shouldReturnEmptyListForUnknownTheme() {
            when(templateRepository.findByThemeAndActiveTrue("Unknown")).thenReturn(Collections.emptyList());
            List<Dnd5eCampaignTemplate> result = templateService.listTemplatesByTheme("Unknown");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listTemplatesByPartyLevel")
    class ListTemplatesByPartyLevel {
        @Test
        @DisplayName("should return templates appropriate for party level")
        void shouldReturnTemplatesAppropriateForPartyLevel() {
            when(templateRepository.findByPartyLevel(5)).thenReturn(Arrays.asList(horrorTemplate, sandboxTemplate));
            List<Dnd5eCampaignTemplate> result = templateService.listTemplatesByPartyLevel(5);
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("listTemplatesByLevelRange")
    class ListTemplatesByLevelRange {
        @Test
        @DisplayName("should return templates within level range")
        void shouldReturnTemplatesWithinLevelRange() {
            when(templateRepository.findByLevelRange(1, 10)).thenReturn(Collections.singletonList(horrorTemplate));
            List<Dnd5eCampaignTemplate> result = templateService.listTemplatesByLevelRange(1, 10);
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("searchTemplates")
    class SearchTemplates {
        @Test
        @DisplayName("should return templates matching keyword")
        void shouldReturnTemplatesMatchingKeyword() {
            when(templateRepository.searchByKeyword("Horror")).thenReturn(Collections.singletonList(horrorTemplate));
            List<Dnd5eCampaignTemplate> result = templateService.searchTemplates("Horror");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Ravenloft Horror");
        }

        @Test
        @DisplayName("should return empty list when no match")
        void shouldReturnEmptyListWhenNoMatch() {
            when(templateRepository.searchByKeyword("Nonexistent")).thenReturn(Collections.emptyList());
            List<Dnd5eCampaignTemplate> result = templateService.searchTemplates("Nonexistent");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listAllThemes")
    class ListAllThemes {
        @Test
        @DisplayName("should return all distinct themes")
        void shouldReturnAllDistinctThemes() {
            when(templateRepository.findAllThemes()).thenReturn(Arrays.asList("Horror", "Sandbox", "High Magic"));
            List<String> result = templateService.listAllThemes();
            assertThat(result).containsExactlyInAnyOrder("Horror", "Sandbox", "High Magic");
        }
    }

    @Nested
    @DisplayName("updateTemplate")
    class UpdateTemplate {
        @Test
        @DisplayName("should update template fields")
        void shouldUpdateTemplateFields() {
            when(templateRepository.findById(1L)).thenReturn(Optional.of(horrorTemplate));
            when(templateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Dnd5eCampaignTemplate updated = new Dnd5eCampaignTemplate();
            updated.setName("Updated Horror");
            updated.setTagline("Updated tagline");
            updated.setDescription("Updated description");
            updated.setTheme("Horror");
            updated.setMinLevel(1);
            updated.setMaxLevel(12);
            updated.setRecommendedPartySize(5);
            updated.setEstimatedSessions(15);
            updated.setActive(true);

            Dnd5eCampaignTemplate result = templateService.updateTemplate(1L, updated);
            assertThat(result.getName()).isEqualTo("Updated Horror");
            assertThat(result.getTagline()).isEqualTo("Updated tagline");
            assertThat(result.getMaxLevel()).isEqualTo(12);
            verify(templateRepository).save(horrorTemplate);
        }

        @Test
        @DisplayName("should throw when template not found")
        void shouldThrowWhenTemplateNotFound() {
            when(templateRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> templateService.updateTemplate(99L, new Dnd5eCampaignTemplate()))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("deleteTemplate")
    class DeleteTemplate {
        @Test
        @DisplayName("should delete template by id")
        void shouldDeleteTemplateById() {
            doNothing().when(templateRepository).deleteById(1L);
            templateService.deleteTemplate(1L);
            verify(templateRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("toggleTemplateActive")
    class ToggleTemplateActive {
        @Test
        @DisplayName("should toggle active state from true to false")
        void shouldToggleActiveStateFromTrueToFalse() {
            when(templateRepository.findById(1L)).thenReturn(Optional.of(horrorTemplate));
            when(templateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            templateService.toggleTemplateActive(1L);
            assertThat(horrorTemplate.isActive()).isFalse();
            verify(templateRepository).save(horrorTemplate);
        }

        @Test
        @DisplayName("should toggle active state from false to true")
        void shouldToggleActiveStateFromFalseToTrue() {
            horrorTemplate.setActive(false);
            when(templateRepository.findById(1L)).thenReturn(Optional.of(horrorTemplate));
            when(templateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            templateService.toggleTemplateActive(1L);
            assertThat(horrorTemplate.isActive()).isTrue();
        }

        @Test
        @DisplayName("should throw when template not found")
        void shouldThrowWhenTemplateNotFound() {
            when(templateRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> templateService.toggleTemplateActive(99L))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("getTemplateSummary")
    class GetTemplateSummary {
        @Test
        @DisplayName("should return complete summary map")
        void shouldReturnCompleteSummaryMap() {
            when(templateRepository.findById(1L)).thenReturn(Optional.of(horrorTemplate));
            Map<String, Object> summary = templateService.getTemplateSummary(1L);

            assertThat(summary).isNotNull();
            assertThat(summary.get("id")).isEqualTo(1L);
            assertThat(summary.get("name")).isEqualTo("Ravenloft Horror");
            assertThat(summary.get("tagline")).isEqualTo("Descend into madness in the Domains of Dread");
            assertThat(summary.get("theme")).isEqualTo("Horror");
            assertThat(summary.get("levelRange")).isEqualTo("1-10");
            assertThat(summary.get("recommendedPartySize")).isEqualTo(4);
            assertThat(summary.get("estimatedSessions")).isEqualTo(12);
            assertThat(summary.get("setupInstructions")).isEqualTo("1. Create a Domain of Dread\n2. Set up a Darklord");
            assertThat(summary.get("pacingGuide")).isEqualTo("Sessions 1-2: Arrival\nSessions 3-4: Discovery");
            assertThat(summary.get("exampleHook")).isEqualTo("The party stumbles upon a fog-shrouded village.");
            assertThat(summary.get("exampleVillain")).isEqualTo("Count Varnek the Undying — A lich who rules Barovia.");
            assertThat(summary.get("difficultyScaling")).isEqualTo("For smaller parties: Reduce monster HP by 25%.");
            assertThat(summary.get("dmTips")).isEqualTo("- Use fear checks sparingly\n- Let dread accumulate naturally");
        }

        @Test
        @DisplayName("should parse recommended subsystems JSON")
        void shouldParseRecommendedSubsystemsJson() {
            when(templateRepository.findById(1L)).thenReturn(Optional.of(horrorTemplate));
            Map<String, Object> summary = templateService.getTemplateSummary(1L);
            @SuppressWarnings("unchecked")
            List<String> subsystems = (List<String>) summary.get("recommendedSubsystems");
            assertThat(subsystems).contains("horror", "tension_pools", "dark_gifts");
        }

        @Test
        @DisplayName("should throw when template not found")
        void shouldThrowWhenTemplateNotFound() {
            when(templateRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> templateService.getTemplateSummary(99L))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("populateCampaignTemplates")
    class PopulateCampaignTemplates {
        @Test
        @DisplayName("should populate 6 templates when empty")
        void shouldPopulate6TemplatesWhenEmpty() {
            when(templateRepository.count()).thenReturn(0L);
            when(templateRepository.save(any())).thenAnswer(invocation -> {
                Dnd5eCampaignTemplate t = invocation.getArgument(0);
                t.setId(System.currentTimeMillis());
                return t;
            });

            templateService.populateCampaignTemplates();
            verify(templateRepository, times(6)).save(any(Dnd5eCampaignTemplate.class));
        }

        @Test
        @DisplayName("should skip population when templates exist")
        void shouldSkipPopulationWhenTemplatesExist() {
            when(templateRepository.count()).thenReturn(6L);
            templateService.populateCampaignTemplates();
            verify(templateRepository, never()).save(any(Dnd5eCampaignTemplate.class));
        }
    }

    @Nested
    @DisplayName("initializeCampaignTemplates")
    class InitializeCampaignTemplates {
        @Test
        @DisplayName("should initialize when Ravenloft Horror does not exist")
        void shouldInitializeWhenRavenloftHorrorDoesNotExist() {
            when(templateRepository.existsByName("Ravenloft Horror")).thenReturn(false);
            when(templateRepository.count()).thenReturn(0L);
            when(templateRepository.save(any())).thenAnswer(invocation -> {
                Dnd5eCampaignTemplate t = invocation.getArgument(0);
                t.setId(System.currentTimeMillis());
                return t;
            });

            templateService.initializeCampaignTemplates();
            verify(templateRepository, times(6)).save(any(Dnd5eCampaignTemplate.class));
        }

        @Test
        @DisplayName("should skip initialization when Ravenloft Horror exists")
        void shouldSkipInitializationWhenRavenloftHorrorExists() {
            when(templateRepository.existsByName("Ravenloft Horror")).thenReturn(true);
            templateService.initializeCampaignTemplates();
            verify(templateRepository, never()).save(any(Dnd5eCampaignTemplate.class));
        }
    }

    @Nested
    @DisplayName("Model validation")
    class ModelValidation {
        @Test
        @DisplayName("should clamp minLevel to minimum 1")
        void shouldClampMinLevelToMinimum1() {
            horrorTemplate.setMinLevel(0);
            assertThat(horrorTemplate.getMinLevel()).isEqualTo(1);
        }

        @Test
        @DisplayName("should clamp maxLevel to at least minLevel")
        void shouldClampMaxLevelToAtLeastMinLevel() {
            horrorTemplate.setMinLevel(10);
            horrorTemplate.setMaxLevel(5);
            assertThat(horrorTemplate.getMaxLevel()).isEqualTo(10);
        }

        @Test
        @DisplayName("should clamp recommendedPartySize between 1 and 8")
        void shouldClampRecommendedPartySizeBetween1And8() {
            horrorTemplate.setRecommendedPartySize(0);
            assertThat(horrorTemplate.getRecommendedPartySize()).isEqualTo(1);

            horrorTemplate.setRecommendedPartySize(10);
            assertThat(horrorTemplate.getRecommendedPartySize()).isEqualTo(8);
        }

        @Test
        @DisplayName("should clamp estimatedSessions to minimum 1")
        void shouldClampEstimatedSessionsToMinimum1() {
            horrorTemplate.setEstimatedSessions(0);
            assertThat(horrorTemplate.getEstimatedSessions()).isEqualTo(1);
        }
    }
}
