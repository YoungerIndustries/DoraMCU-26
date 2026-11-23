package com.yisy.modules.doramcu.gamecore.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * Custom {@link URLClassLoader} for isolated module loading.
 * <p>
 * Uses parent-first delegation for shared classes (Spring, JDK, core services)
 * and isolated loading for module-specific classes. This enables true class
 * isolation between modules and proper garbage collection of unloaded modules.
 * </p>
 * <p>
 * Each module JAR loads in its own instance of this classloader, so a crashing
 * module cannot affect other modules or the main server.
 * </p>
 */
public class ModuleClassLoader extends URLClassLoader {

    private static final Logger log = LoggerFactory.getLogger(ModuleClassLoader.class);

    /**
     * Create a new module classloader for the given JAR file.
     *
     * @param jarUrl the URL of the module JAR
     */
    public ModuleClassLoader(URL jarUrl) {
        super(new URL[]{jarUrl}, ModuleClassLoader.class.getClassLoader());
    }

    /**
     * Create a new module classloader for the given JAR files.
     *
     * @param jarUrls the URLs of the module JARs
     */
    public ModuleClassLoader(URL... jarUrls) {
        super(jarUrls, ModuleClassLoader.class.getClassLoader());
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // Check if already loaded in this classloader
            Class<?> c = findLoadedClass(name);

            if (c == null) {
                // Parent-first delegation for core classes
                if (isParentFirstClass(name)) {
                    try {
                        c = getParent().loadClass(name);
                        if (resolve) {
                            resolveClass(c);
                        }
                        return c;
                    } catch (ClassNotFoundException ignored) {
                        // Fall through to load from module JAR
                    }
                }
            }

            if (c == null) {
                // Try to find and load from module JAR
                try {
                    c = findClass(name);
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                } catch (ClassNotFoundException ignored) {
                    // Fall through to parent
                }
            }

            if (c == null) {
                // Final fallback to parent
                c = getParent().loadClass(name);
                if (resolve) {
                    resolveClass(c);
                }
            }

            return c;
        }
    }

    /**
     * Determine if a class should be loaded from the parent classloader first.
     * Core Java, Spring, and shared service classes are always parent-first.
     */
    private boolean isParentFirstClass(String className) {
        // JDK core classes
        if (className.startsWith("java.")) return true;
        if (className.startsWith("javax.")) return true;
        if (className.startsWith("jdk.")) return true;
        if (className.startsWith("sun.")) return true;

        // Spring framework classes
        if (className.startsWith("org.springframework.")) return true;
        if (className.startsWith("org.apache.")) return true;
        if (className.startsWith("jakarta.")) return true;

        // Logging
        if (className.startsWith("org.slf4j.")) return true;
        if (className.startsWith("ch.qos.logback.")) return true;

        // Jackson
        if (className.startsWith("com.fasterxml.")) return true;

        // Hibernate/JPA
        if (className.startsWith("org.hibernate.")) return true;
        if (className.startsWith("org.eclipse.")) return true;

        return false;
    }

    @Override
    public void close() throws IOException {
        try {
            log.debug("Closing ModuleClassLoader with {} URLs", getURLs().length);
            for (URL url : getURLs()) {
                String decoded = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);
                log.debug("  - {}", decoded);
            }
        } finally {
            super.close();
        }
    }

    /**
     * List all classes in the module JAR for debugging.
     */
    public String[] listModuleClasses() {
        try {
            java.util.List<String> classes = new java.util.ArrayList<>();
            for (URL url : getURLs()) {
                String path = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);
                try (java.util.jar.JarFile jar = new java.util.jar.JarFile(path)) {
                    Enumeration<java.util.jar.JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        String name = entries.nextElement().getName();
                        if (name.endsWith(".class") && !name.contains("$")) {
                            String className = name.replace('/', '.').replace(".class", "");
                            classes.add(className);
                        }
                    }
                }
            }
            return classes.toArray(new String[0]);
        } catch (Exception e) {
            log.warn("Failed to list module classes: {}", e.getMessage());
            return new String[0];
        }
    }
}
