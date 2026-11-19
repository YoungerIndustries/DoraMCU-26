package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.TestDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestRegistryTest {

    @Mock
    private TestProvider provider1;

    @Mock
    private TestProvider provider2;

    private TestRegistry testRegistry;

    @BeforeEach
    void setUp() {
        testRegistry = new TestRegistry(Arrays.asList(provider1, provider2));
    }

    @Test
    @DisplayName("init should collect tests from all providers")
    void init_shouldCollectTests() {
        TestDefinition test1 = new TestDefinition("test-1", "Test 1", "API", "Description 1");
        TestDefinition test2 = new TestDefinition("test-2", "Test 2", "GAME", "Description 2");
        TestDefinition test3 = new TestDefinition("test-3", "Test 3", "AUTH", "Description 3");

        when(provider1.registerTests()).thenReturn(List.of(test1));
        when(provider2.registerTests()).thenReturn(Arrays.asList(test2, test3));

        testRegistry.init();

        List<TestDefinition> catalog = testRegistry.getTestCatalog();
        assertEquals(3, catalog.size());
    }

    @Test
    @DisplayName("getTestsByCategory should filter by category")
    void getTestsByCategory_shouldFilter() {
        TestDefinition apiTest = new TestDefinition("api-1", "API Test", "API", "API test");
        TestDefinition gameTest = new TestDefinition("game-1", "Game Test", "GAME", "Game test");

        when(provider1.registerTests()).thenReturn(List.of(apiTest));
        when(provider2.registerTests()).thenReturn(List.of(gameTest));

        testRegistry.init();

        List<TestDefinition> apiTests = testRegistry.getTestsByCategory("API");
        assertEquals(1, apiTests.size());
        assertEquals("API", apiTests.get(0).getCategory());
    }

    @Test
    @DisplayName("getTestById should return test by ID")
    void getTestById_shouldReturnTest() {
        TestDefinition test = new TestDefinition("test-1", "Test 1", "API", "Description 1");

        when(provider1.registerTests()).thenReturn(List.of(test));
        when(provider2.registerTests()).thenReturn(List.of());

        testRegistry.init();

        TestDefinition found = testRegistry.getTestById("test-1");
        assertNotNull(found);
        assertEquals("test-1", found.getId());
        assertEquals("Test 1", found.getName());
    }

    @Test
    @DisplayName("getTestById should return null for unknown ID")
    void getTestById_unknownId() {
        when(provider1.registerTests()).thenReturn(List.of());
        when(provider2.registerTests()).thenReturn(List.of());

        testRegistry.init();

        TestDefinition found = testRegistry.getTestById("unknown");
        assertNull(found);
    }

    @Test
    @DisplayName("getTestsByIds should return tests by IDs")
    void getTestsByIds_shouldReturnTests() {
        TestDefinition test1 = new TestDefinition("test-1", "Test 1", "API", "Description 1");
        TestDefinition test2 = new TestDefinition("test-2", "Test 2", "GAME", "Description 2");
        TestDefinition test3 = new TestDefinition("test-3", "Test 3", "AUTH", "Description 3");

        when(provider1.registerTests()).thenReturn(Arrays.asList(test1, test2));
        when(provider2.registerTests()).thenReturn(List.of(test3));

        testRegistry.init();

        List<TestDefinition> found = testRegistry.getTestsByIds(Arrays.asList("test-1", "test-3"));
        assertEquals(2, found.size());
    }

    @Test
    @DisplayName("executeTest should delegate to the correct provider")
    void executeTest_shouldDelegate() {
        TestDefinition test = new TestDefinition("test-1", "Test 1", "API", "Description 1");

        when(provider1.registerTests()).thenReturn(List.of(test));
        when(provider1.executeTest("test-1")).thenReturn(true);
        when(provider2.registerTests()).thenReturn(List.of());

        testRegistry.init();

        boolean result = testRegistry.executeTest("test-1");
        assertTrue(result);
    }

    @Test
    @DisplayName("executeTest should return false for unknown test")
    void executeTest_unknownTest() {
        when(provider1.registerTests()).thenReturn(List.of());
        when(provider2.registerTests()).thenReturn(List.of());

        testRegistry.init();

        boolean result = testRegistry.executeTest("unknown");
        assertFalse(result);
    }

    @Test
    @DisplayName("getCategories should return unique categories")
    void getCategories_shouldReturnUnique() {
        TestDefinition apiTest = new TestDefinition("api-1", "API Test", "API", "API test");
        TestDefinition apiTest2 = new TestDefinition("api-2", "API Test 2", "API", "API test 2");
        TestDefinition gameTest = new TestDefinition("game-1", "Game Test", "GAME", "Game test");

        when(provider1.registerTests()).thenReturn(Arrays.asList(apiTest, apiTest2));
        when(provider2.registerTests()).thenReturn(List.of(gameTest));

        testRegistry.init();

        List<String> categories = testRegistry.getCategories();
        assertEquals(2, categories.size());
        assertTrue(categories.contains("API"));
        assertTrue(categories.contains("GAME"));
    }
}
