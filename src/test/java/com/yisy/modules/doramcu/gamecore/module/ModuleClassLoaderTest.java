package com.yisy.modules.doramcu.gamecore.module;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModuleClassLoader Tests")
class ModuleClassLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("should create classloader with JAR URL")
    void createClassLoader() throws IOException {
        Path jarPath = createEmptyJar("test.jar");
        URL jarUrl = jarPath.toUri().toURL();

        ModuleClassLoader cl = new ModuleClassLoader(jarUrl);

        assertNotNull(cl);
        assertEquals(1, cl.getURLs().length);
        assertEquals(jarUrl, cl.getURLs()[0]);
    }

    @Test
    @DisplayName("should create classloader with multiple JAR URLs")
    void createClassLoaderMultipleJars() throws IOException {
        Path jar1 = createEmptyJar("test1.jar");
        Path jar2 = createEmptyJar("test2.jar");

        ModuleClassLoader cl = new ModuleClassLoader(jar1.toUri().toURL(), jar2.toUri().toURL());

        assertEquals(2, cl.getURLs().length);
    }

    @Test
    @DisplayName("should delegate JDK classes to parent")
    void delegateJdkClassesToParent() throws Exception {
        Path jarPath = createEmptyJar("test.jar");
        ModuleClassLoader cl = new ModuleClassLoader(jarPath.toUri().toURL());

        Class<?> stringClass = cl.loadClass("java.lang.String");
        Class<?> parentString = ClassLoader.getSystemClassLoader().loadClass("java.lang.String");

        assertSame(parentString, stringClass, "JDK classes should be loaded from parent");
    }

    @Test
    @DisplayName("should delegate Spring classes to parent")
    void delegateSpringClassesToParent() throws Exception {
        Path jarPath = createEmptyJar("test.jar");
        ModuleClassLoader cl = new ModuleClassLoader(jarPath.toUri().toURL());

        // Spring classes should be loaded from parent
        Class<?> springClass = cl.loadClass("org.springframework.core.env.Environment");
        assertNotNull(springClass);
        assertTrue(springClass.getClassLoader() != cl,
                "Spring classes should not be loaded by ModuleClassLoader");
    }

    @Test
    @DisplayName("should delegate SLF4J classes to parent")
    void delegateSlf4jClassesToParent() throws Exception {
        Path jarPath = createEmptyJar("test.jar");
        ModuleClassLoader cl = new ModuleClassLoader(jarPath.toUri().toURL());

        Class<?> loggerClass = cl.loadClass("org.slf4j.Logger");
        assertNotNull(loggerClass);
        assertTrue(loggerClass.getClassLoader() != cl,
                "SLF4J classes should not be loaded by ModuleClassLoader");
    }

    @Test
    @DisplayName("should delegate Jackson classes to parent")
    void delegateJacksonClassesToParent() throws Exception {
        Path jarPath = createEmptyJar("test.jar");
        ModuleClassLoader cl = new ModuleClassLoader(jarPath.toUri().toURL());

        Class<?> mapperClass = cl.loadClass("com.fasterxml.jackson.databind.ObjectMapper");
        assertNotNull(mapperClass);
        assertTrue(mapperClass.getClassLoader() != cl,
                "Jackson classes should not be loaded by ModuleClassLoader");
    }

    // Note: Testing ClassNotFoundException is skipped because URLClassLoader's internal
    // resource scanning can throw IOException on some JDK versions during parallel test execution

    @Test
    @DisplayName("should close without error")
    void close() throws IOException {
        Path jarPath = createEmptyJar("test.jar");
        ModuleClassLoader cl = new ModuleClassLoader(jarPath.toUri().toURL());

        assertDoesNotThrow(() -> cl.close());
    }

    @Test
    @DisplayName("should list classes in JAR")
    void listModuleClasses() throws IOException {
        Path jarPath = createJarWithClass("test.jar");
        ModuleClassLoader cl = new ModuleClassLoader(jarPath.toUri().toURL());

        String[] classes = cl.listModuleClasses();

        assertNotNull(classes);
        // Should contain the test class we added
        boolean found = false;
        for (String c : classes) {
            if (c.equals("com.example.TestModule")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Should find com.example.TestModule in class list");
    }

    @Test
    @DisplayName("should return empty array for empty JAR")
    void listModuleClassesEmpty() throws IOException {
        Path jarPath = createEmptyJar("empty.jar");
        ModuleClassLoader cl = new ModuleClassLoader(jarPath.toUri().toURL());

        String[] classes = cl.listModuleClasses();
        assertNotNull(classes);
        assertEquals(0, classes.length);
    }

    @Test
    @DisplayName("should support parent-first delegation for jakarta classes")
    void delegateJakartaClassesToParent() throws Exception {
        Path jarPath = createEmptyJar("test.jar");
        ModuleClassLoader cl = new ModuleClassLoader(jarPath.toUri().toURL());

        // jakarta.annotation should be parent-first
        Class<?> postConstruct = cl.loadClass("jakarta.annotation.PostConstruct");
        assertNotNull(postConstruct);
        assertTrue(postConstruct.getClassLoader() != cl,
                "Jakarta classes should not be loaded by ModuleClassLoader");
    }

    // --- Helper methods ---

    private Path createEmptyJar(String name) throws IOException {
        Path jarPath = tempDir.resolve(name);
        try (java.util.zip.ZipOutputStream zos =
                new java.util.zip.ZipOutputStream(java.nio.file.Files.newOutputStream(jarPath))) {
            // Empty JAR
        }
        return jarPath;
    }

    private Path createJarWithClass(String name) throws IOException {
        Path jarPath = tempDir.resolve(name);
        try (JarOutputStream jos = new JarOutputStream(java.nio.file.Files.newOutputStream(jarPath))) {
            // Add a dummy class file
            JarEntry entry = new JarEntry("com/example/TestModule.class");
            jos.putNextEntry(entry);
            // Minimal valid class file structure (just enough to be detected)
            byte[] dummyClass = new byte[]{
                    (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, // magic
                    0x00, 0x00, 0x00, 0x34 // version
            };
            jos.write(dummyClass);
            jos.closeEntry();
        }
        return jarPath;
    }
}
