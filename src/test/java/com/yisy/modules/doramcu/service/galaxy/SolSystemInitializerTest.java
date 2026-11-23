package com.yisy.modules.doramcu.service.galaxy;

import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.model.galaxy.SolarSystem;
import com.yisy.modules.doramcu.model.galaxy.Star;
import com.yisy.modules.doramcu.model.misc.Planet;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import com.yisy.modules.doramcu.repository.galaxy.PlanetRepository;
import com.yisy.modules.doramcu.repository.galaxy.SolarSystemRepository;
import com.yisy.modules.doramcu.repository.galaxy.StarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolSystemInitializerTest {

    @Mock
    GalaxyRepository galaxyRepository;

    @Mock
    SolarSystemRepository solarSystemRepository;

    @Mock
    StarRepository starRepository;

    @Mock
    PlanetRepository planetRepository;

    SolSystemInitializer solSystemInitializer;

    @BeforeEach
    void setUp() {
        solSystemInitializer = new SolSystemInitializer(
                galaxyRepository, solarSystemRepository, starRepository, planetRepository);
    }

    @Test
    void initializeSolSystem_createsSolSystemWithSunAndEarth() {
        // Given a new galaxy without a Sol system
        Galaxy galaxy = new Galaxy();
        galaxy.setId(1L);
        galaxy.setName("Test Galaxy");

        when(solarSystemRepository.findByGalaxyIdAndName(1L, "Sol")).thenReturn(Optional.empty());
        when(solarSystemRepository.save(any(SolarSystem.class))).thenAnswer(invocation -> {
            SolarSystem ss = invocation.getArgument(0);
            ss.setId(100L);
            return ss;
        });
        when(starRepository.save(any(Star.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(planetRepository.save(any(Planet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        solSystemInitializer.initializeSolSystem(galaxy);

        // Then
        verify(solarSystemRepository).findByGalaxyIdAndName(1L, "Sol");
        verify(solarSystemRepository).save(any(SolarSystem.class));
        verify(starRepository).save(any(Star.class));
        verify(planetRepository).save(any(Planet.class));
    }

    @Test
    void initializeSolSystem_skipsIfSolSystemExists() {
        // Given a galaxy that already has a Sol system
        Galaxy galaxy = new Galaxy();
        galaxy.setId(1L);
        galaxy.setName("Test Galaxy");

        SolarSystem existingSol = new SolarSystem();
        existingSol.setId(100L);
        existingSol.setName("Sol");
        when(solarSystemRepository.findByGalaxyIdAndName(1L, "Sol")).thenReturn(Optional.of(existingSol));

        // When
        solSystemInitializer.initializeSolSystem(galaxy);

        // Then — nothing should be saved
        verify(solarSystemRepository, never()).save(any());
        verify(starRepository, never()).save(any());
        verify(planetRepository, never()).save(any());
    }

    @Test
    void initializeSolSystem_handlesNullGalaxy() {
        // When
        solSystemInitializer.initializeSolSystem(null);

        // Then — nothing should happen
        verifyNoInteractions(solarSystemRepository, starRepository, planetRepository);
    }

    @Test
    void hasSolSystem_returnsTrueWhenExists() {
        Galaxy galaxy = new Galaxy();
        galaxy.setId(1L);

        SolarSystem existingSol = new SolarSystem();
        when(solarSystemRepository.findByGalaxyIdAndName(1L, "Sol")).thenReturn(Optional.of(existingSol));

        assertTrue(solSystemInitializer.hasSolSystem(1L));
    }

    @Test
    void hasSolSystem_returnsFalseWhenMissing() {
        when(solarSystemRepository.findByGalaxyIdAndName(1L, "Sol")).thenReturn(Optional.empty());

        assertFalse(solSystemInitializer.hasSolSystem(1L));
    }

    @Test
    void solSystemName_constant() {
        assertEquals("Sol", SolSystemInitializer.SOL_SYSTEM_NAME);
    }

    @Test
    void sunStarName_constant() {
        assertEquals("Sun", SolSystemInitializer.SUN_STAR_NAME);
    }

    @Test
    void earthPlanetName_constant() {
        assertEquals("Earth", SolSystemInitializer.EARTH_PLANET_NAME);
    }
}
