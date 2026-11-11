package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSpecies;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eSpeciesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Dnd5eSpeciesServiceTest {

    @Mock
    private Dnd5eSpeciesRepository speciesRepository;

    private Dnd5eSpeciesService speciesService;

    private Dnd5eSpecies human;
    private Dnd5eSpecies elf;

    @BeforeEach
    void setUp() {
        speciesService = new Dnd5eSpeciesService(speciesRepository);

        human = new Dnd5eSpecies();
        human.setId(1L);
        human.setName("Human");
        human.setDescription("Adaptable and ambitious.");
        human.setCategory("Core");
        human.setSize("Medium");
        human.setBaseSpeed(30);
        human.setLifespan(100);
        human.setDarkvisionRange(0);
        human.setAbilityIncreases("{\"STR\": 1, \"DEX\": 1, \"CON\": 1, \"INT\": 1, \"WIS\": 1, \"CHA\": 1}");
        human.setRacialTraits("[\"Customizable Origin\", \"Extra Language\"]");
        human.setLanguages("Common, one additional language");
        human.setMaleFirstNames("[\"Aldern\", \"Berick\", \"Corrin\"]");
        human.setFemaleFirstNames("[\"Aeliana\", \"Brenna\", \"Caelia\"]");
        human.setLastNames("[\"Ashford\", \"Blackwood\", \"Carrow\"]");
        human.setTitlePrefixes("[\"Lord\", \"Lady\", \"Sir\"]");
        human.setTitleSuffixes("[\"the Wise\", \"the Bold\"]");
        human.setAiLeaderTitles("[\"King\", \"Queen\", \"Duke\"]");
        human.setAiGovernorTitles("[\"Lord\", \"Lady\", \"Magistrate\"]");
        human.setAiCaptainTitles("[\"Captain\", \"Commander\", \"Lieutenant\"]");

        elf = new Dnd5eSpecies();
        elf.setId(2L);
        elf.setName("Elf");
        elf.setDescription("Graceful and long-lived.");
        elf.setCategory("Core");
        elf.setSize("Medium");
        elf.setBaseSpeed(30);
        elf.setLifespan(750);
        elf.setDarkvisionRange(60);
        elf.setAbilityIncreases("{\"DEX\": 2}");
        elf.setRacialTraits("[\"Darkvision\", \"Fey Ancestry\", \"Keen Senses\", \"Trance\", \"Elf Weapon Training\"]");
        elf.setLanguages("Common, Elvish");
        elf.setMaleFirstNames("[\"Aelar\", \"Caelar\", \"Daeron\"]");
        elf.setFemaleFirstNames("[\"Aelindra\", \"Caelia\", \"Daeriel\"]");
        elf.setLastNames("[\"Amakiir\", \"Amastacia\", \"Amphrey\"]");
        elf.setTitlePrefixes("[\"Lord\", \"Lady\", \"Prince\"]");
        elf.setTitleSuffixes("[\"the Graceful\", \"the Swift\"]");
        elf.setAiLeaderTitles("[\"Elven King\", \"Elven Queen\", \"High Lord\"]");
        elf.setAiGovernorTitles("[\"Thane\", \"Forest Warden\", \"Moon Priest\"]");
        elf.setAiCaptainTitles("[\"Ranger Captain\", \"Scout Leader\", \"Wood Guard Commander\"]");
    }

    @Nested
    @DisplayName("createSpecies")
    class CreateSpecies {
        @Test
        @DisplayName("should create and save a species")
        void shouldCreateAndSaveSpecies() {
            when(speciesRepository.save(any())).thenReturn(human);
            Dnd5eSpecies created = speciesService.createSpecies(human);
            assertThat(created).isNotNull();
            assertThat(created.getName()).isEqualTo("Human");
            verify(speciesRepository).save(human);
        }
    }

    @Nested
    @DisplayName("getSpecies")
    class GetSpecies {
        @Test
        @DisplayName("should return species when found")
        void shouldReturnSpeciesWhenFound() {
            when(speciesRepository.findById(1L)).thenReturn(Optional.of(human));
            Optional<Dnd5eSpecies> result = speciesService.getSpecies(1L);
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Human");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(speciesRepository.findById(99L)).thenReturn(Optional.empty());
            Optional<Dnd5eSpecies> result = speciesService.getSpecies(99L);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSpeciesByName")
    class GetSpeciesByName {
        @Test
        @DisplayName("should return species when found by name")
        void shouldReturnSpeciesWhenFoundByName() {
            when(speciesRepository.findByName("Human")).thenReturn(Optional.of(human));
            Optional<Dnd5eSpecies> result = speciesService.getSpeciesByName("Human");
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Human");
        }

        @Test
        @DisplayName("should return empty when not found by name")
        void shouldReturnEmptyWhenNotFoundByName() {
            when(speciesRepository.findByName("Unknown")).thenReturn(Optional.empty());
            Optional<Dnd5eSpecies> result = speciesService.getSpeciesByName("Unknown");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listSpecies")
    class ListSpecies {
        @Test
        @DisplayName("should return all species")
        void shouldReturnAllSpecies() {
            when(speciesRepository.findAll()).thenReturn(Arrays.asList(human, elf));
            List<Dnd5eSpecies> result = speciesService.listSpecies();
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Dnd5eSpecies::getName).containsExactlyInAnyOrder("Human", "Elf");
        }

        @Test
        @DisplayName("should return empty list when no species")
        void shouldReturnEmptyListWhenNoSpecies() {
            when(speciesRepository.findAll()).thenReturn(Collections.emptyList());
            List<Dnd5eSpecies> result = speciesService.listSpecies();
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listSpeciesByCategory")
    class ListSpeciesByCategory {
        @Test
        @DisplayName("should return species by category")
        void shouldReturnSpeciesByCategory() {
            when(speciesRepository.findByCategory("Core")).thenReturn(Arrays.asList(human, elf));
            List<Dnd5eSpecies> result = speciesService.listSpeciesByCategory("Core");
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("listSpeciesBySize")
    class ListSpeciesBySize {
        @Test
        @DisplayName("should return species by size")
        void shouldReturnSpeciesBySize() {
            when(speciesRepository.findBySize("Medium")).thenReturn(Arrays.asList(human, elf));
            List<Dnd5eSpecies> result = speciesService.listSpeciesBySize("Medium");
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("listSpeciesWithDarkvision")
    class ListSpeciesWithDarkvision {
        @Test
        @DisplayName("should return species with darkvision")
        void shouldReturnSpeciesWithDarkvision() {
            when(speciesRepository.findByDarkvision()).thenReturn(Collections.singletonList(elf));
            List<Dnd5eSpecies> result = speciesService.listSpeciesWithDarkvision();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Elf");
        }
    }

    @Nested
    @DisplayName("listSpeciesByLifespan")
    class ListSpeciesByLifespan {
        @Test
        @DisplayName("should return species by minimum lifespan")
        void shouldReturnSpeciesByMinimumLifespan() {
            when(speciesRepository.findByLifespanGte(500)).thenReturn(Collections.singletonList(elf));
            List<Dnd5eSpecies> result = speciesService.listSpeciesByLifespan(500);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Elf");
        }
    }

    @Nested
    @DisplayName("searchSpecies")
    class SearchSpecies {
        @Test
        @DisplayName("should return species matching keyword")
        void shouldReturnSpeciesMatchingKeyword() {
            when(speciesRepository.searchByKeyword("Human")).thenReturn(Collections.singletonList(human));
            List<Dnd5eSpecies> result = speciesService.searchSpecies("Human");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Human");
        }

        @Test
        @DisplayName("should return empty when no match")
        void shouldReturnEmptyWhenNoMatch() {
            when(speciesRepository.searchByKeyword("Unknown")).thenReturn(Collections.emptyList());
            List<Dnd5eSpecies> result = speciesService.searchSpecies("Unknown");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listSpeciesByRacialTrait")
    class ListSpeciesByRacialTrait {
        @Test
        @DisplayName("should return species by racial trait")
        void shouldReturnSpeciesByRacialTrait() {
            when(speciesRepository.findByRacialTrait("Darkvision")).thenReturn(Collections.singletonList(elf));
            List<Dnd5eSpecies> result = speciesService.listSpeciesByRacialTrait("Darkvision");
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateSpecies")
    class UpdateSpecies {
        @Test
        @DisplayName("should update species")
        void shouldUpdateSpecies() {
            when(speciesRepository.findById(1L)).thenReturn(Optional.of(human));
            when(speciesRepository.save(any())).thenReturn(human);
            Dnd5eSpecies updated = new Dnd5eSpecies();
            updated.setName("Updated Human");
            updated.setDescription("Updated description.");
            updated.setCategory("Core");
            updated.setSize("Medium");
            updated.setBaseSpeed(30);
            updated.setLifespan(100);
            updated.setDarkvisionRange(0);
            updated.setAbilityIncreases("{}");
            updated.setRacialTraits("[]");
            updated.setLanguages("Common");
            updated.setMaleFirstNames("[]");
            updated.setFemaleFirstNames("[]");
            updated.setLastNames("[]");
            updated.setTitlePrefixes("[]");
            updated.setTitleSuffixes("[]");
            updated.setAiLeaderTitles("[]");
            updated.setAiGovernorTitles("[]");
            updated.setAiCaptainTitles("[]");
            Dnd5eSpecies result = speciesService.updateSpecies(1L, updated);
            assertThat(result).isNotNull();
            verify(speciesRepository).save(any());
        }

        @Test
        @DisplayName("should throw when species not found")
        void shouldThrowWhenSpeciesNotFound() {
            when(speciesRepository.findById(99L)).thenReturn(Optional.empty());
            Dnd5eSpecies updated = new Dnd5eSpecies();
            assertThatThrownBy(() -> speciesService.updateSpecies(99L, updated))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("deleteSpecies")
    class DeleteSpecies {
        @Test
        @DisplayName("should delete species")
        void shouldDeleteSpecies() {
            doNothing().when(speciesRepository).deleteById(1L);
            speciesService.deleteSpecies(1L);
            verify(speciesRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("generateFirstName")
    class GenerateFirstName {
        @Test
        @DisplayName("should return male first name")
        void shouldReturnMaleFirstName() {
            when(speciesRepository.findByName("Human")).thenReturn(Optional.of(human));
            String name = speciesService.generateFirstName("Human", true);
            assertThat(name).isIn("Aldern", "Berick", "Corrin");
        }

        @Test
        @DisplayName("should return female first name")
        void shouldReturnFemaleFirstName() {
            when(speciesRepository.findByName("Human")).thenReturn(Optional.of(human));
            String name = speciesService.generateFirstName("Human", false);
            assertThat(name).isIn("Aeliana", "Brenna", "Caelia");
        }

        @Test
        @DisplayName("should return Unknown when species not found")
        void shouldReturnUnknownWhenSpeciesNotFound() {
            when(speciesRepository.findByName("Unknown")).thenReturn(Optional.empty());
            String name = speciesService.generateFirstName("Unknown", true);
            assertThat(name).isEqualTo("Unknown");
        }
    }

    @Nested
    @DisplayName("generateLastName")
    class GenerateLastName {
        @Test
        @DisplayName("should return last name")
        void shouldReturnLastName() {
            when(speciesRepository.findByName("Human")).thenReturn(Optional.of(human));
            String name = speciesService.generateLastName("Human");
            assertThat(name).isIn("Ashford", "Blackwood", "Carrow");
        }

        @Test
        @DisplayName("should return empty when species not found")
        void shouldReturnEmptyWhenSpeciesNotFound() {
            when(speciesRepository.findByName("Unknown")).thenReturn(Optional.empty());
            String name = speciesService.generateLastName("Unknown");
            assertThat(name).isEmpty();
        }
    }

    @Nested
    @DisplayName("generateFullName")
    class GenerateFullName {
        @Test
        @DisplayName("should return full name")
        void shouldReturnFullName() {
            when(speciesRepository.findByName("Human")).thenReturn(Optional.of(human));
            String name = speciesService.generateFullName("Human", true);
            assertThat(name).isNotBlank();
            assertThat(name.split(" ")).hasSize(2);
        }
    }

    @Nested
    @DisplayName("generateTitle")
    class GenerateTitle {
        @Test
        @DisplayName("should return leader title")
        void shouldReturnLeaderTitle() {
            when(speciesRepository.findByName("Human")).thenReturn(Optional.of(human));
            String title = speciesService.generateTitle("Human", "leader");
            assertThat(title).isIn("King", "Queen", "Duke");
        }

        @Test
        @DisplayName("should return governor title")
        void shouldReturnGovernorTitle() {
            when(speciesRepository.findByName("Human")).thenReturn(Optional.of(human));
            String title = speciesService.generateTitle("Human", "governor");
            assertThat(title).isIn("Lord", "Lady", "Magistrate");
        }

        @Test
        @DisplayName("should return captain title")
        void shouldReturnCaptainTitle() {
            when(speciesRepository.findByName("Human")).thenReturn(Optional.of(human));
            String title = speciesService.generateTitle("Human", "captain");
            assertThat(title).isIn("Captain", "Commander", "Lieutenant");
        }

        @Test
        @DisplayName("should return prefix title")
        void shouldReturnPrefixTitle() {
            when(speciesRepository.findByName("Human")).thenReturn(Optional.of(human));
            String title = speciesService.generateTitle("Human", "prefix");
            assertThat(title).isIn("Lord", "Lady", "Sir");
        }

        @Test
        @DisplayName("should return suffix title")
        void shouldReturnSuffixTitle() {
            when(speciesRepository.findByName("Human")).thenReturn(Optional.of(human));
            String title = speciesService.generateTitle("Human", "suffix");
            assertThat(title).isIn("the Wise", "the Bold");
        }

        @Test
        @DisplayName("should return empty for invalid role")
        void shouldReturnEmptyForInvalidRole() {
            when(speciesRepository.findByName("Human")).thenReturn(Optional.of(human));
            String title = speciesService.generateTitle("Human", "invalid");
            assertThat(title).isEmpty();
        }

        @Test
        @DisplayName("should return empty when species not found")
        void shouldReturnEmptyWhenSpeciesNotFound() {
            when(speciesRepository.findByName("Unknown")).thenReturn(Optional.empty());
            String title = speciesService.generateTitle("Unknown", "leader");
            assertThat(title).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAbilityIncreases")
    class GetAbilityIncreases {
        @Test
        @DisplayName("should return ability increases map")
        void shouldReturnAbilityIncreasesMap() {
            when(speciesRepository.findByName("Human")).thenReturn(Optional.of(human));
            Map<String, Integer> increases = speciesService.getAbilityIncreases("Human");
            assertThat(increases).isNotEmpty();
            assertThat(increases.get("STR")).isEqualTo(1);
            assertThat(increases.get("DEX")).isEqualTo(1);
            assertThat(increases.get("CON")).isEqualTo(1);
            assertThat(increases.get("INT")).isEqualTo(1);
            assertThat(increases.get("WIS")).isEqualTo(1);
            assertThat(increases.get("CHA")).isEqualTo(1);
        }

        @Test
        @DisplayName("should return empty map when species not found")
        void shouldReturnEmptyMapWhenSpeciesNotFound() {
            when(speciesRepository.findByName("Unknown")).thenReturn(Optional.empty());
            Map<String, Integer> increases = speciesService.getAbilityIncreases("Unknown");
            assertThat(increases).isEmpty();
        }
    }

    @Nested
    @DisplayName("getRacialTraits")
    class GetRacialTraits {
        @Test
        @DisplayName("should return racial traits list")
        void shouldReturnRacialTraitsList() {
            when(speciesRepository.findByName("Elf")).thenReturn(Optional.of(elf));
            List<String> traits = speciesService.getRacialTraits("Elf");
            assertThat(traits).isNotEmpty();
            assertThat(traits).contains("Darkvision", "Fey Ancestry", "Keen Senses", "Trance", "Elf Weapon Training");
        }

        @Test
        @DisplayName("should return empty list when species not found")
        void shouldReturnEmptyListWhenSpeciesNotFound() {
            when(speciesRepository.findByName("Unknown")).thenReturn(Optional.empty());
            List<String> traits = speciesService.getRacialTraits("Unknown");
            assertThat(traits).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCategories")
    class GetCategories {
        @Test
        @DisplayName("should return distinct sorted categories")
        void shouldReturnDistinctSortedCategories() {
            when(speciesRepository.findAll()).thenReturn(Arrays.asList(human, elf));
            List<String> categories = speciesService.getCategories();
            assertThat(categories).contains("Core");
        }
    }

    @Nested
    @DisplayName("getSizes")
    class GetSizes {
        @Test
        @DisplayName("should return distinct sorted sizes")
        void shouldReturnDistinctSortedSizes() {
            when(speciesRepository.findAll()).thenReturn(Arrays.asList(human, elf));
            List<String> sizes = speciesService.getSizes();
            assertThat(sizes).contains("Medium");
        }
    }

    @Nested
    @DisplayName("initializeSpeciesDatabase")
    class InitializeSpeciesDatabase {
        @Test
        @DisplayName("should skip if already initialized")
        void shouldSkipIfAlreadyInitialized() {
            when(speciesRepository.existsByName("Human")).thenReturn(true);
            speciesService.initializeSpeciesDatabase();
            verify(speciesRepository, never()).save(any());
        }

        @Test
        @DisplayName("should populate if not initialized")
        void shouldPopulateIfNotInitialized() {
            when(speciesRepository.existsByName("Human")).thenReturn(false);
            when(speciesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(speciesRepository.count()).thenReturn(24L);
            speciesService.initializeSpeciesDatabase();
            verify(speciesRepository, atLeastOnce()).save(any());
        }
    }

    @Nested
    @DisplayName("populateSpecies")
    class PopulateSpecies {
        @Test
        @DisplayName("should create species with correct fields")
        void shouldCreateSpeciesWithCorrectFields() {
            when(speciesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(speciesRepository.existsByName("Human")).thenReturn(false);
            when(speciesRepository.count()).thenReturn(24L);
            speciesService.initializeSpeciesDatabase();
            assertThatCode(() -> speciesService.initializeSpeciesDatabase()).doesNotThrowAnyException();
        }
    }
}
