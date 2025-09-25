package io.emop.javadocjson.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.emop.javadocjson.model.JavadocClass;
import lombok.Getter;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache management class for the Javadoc crawler.
 */
@Getter
public class CrawlerCache {

    private final Log log;
    private final boolean enableCache;
    private final String cacheDir;
    private final Set<String> cachedClasses;
    private final ObjectMapper objectMapper;

    public CrawlerCache(Log log, boolean enableCache, String cacheDir) {
        this.log = log;
        this.enableCache = enableCache;
        this.cacheDir = cacheDir != null ? cacheDir :
                System.getProperty("java.io.tmpdir") + File.separator + "javadoc-crawler-cache";
        this.cachedClasses = ConcurrentHashMap.newKeySet();

        // Initialize ObjectMapper for JSON serialization
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        if (enableCache) {
            initializeCache();
        }
    }

    /**
     * Initializes the cache directory and loads existing cache data.
     */
    private void initializeCache() {
        try {
            Path cachePath = Paths.get(cacheDir);
            if (!Files.exists(cachePath)) {
                Files.createDirectories(cachePath);
                log.info("Created cache directory: " + cacheDir);
            } else {
                log.info("Using existing cache directory: " + cacheDir);
            }

            loadExistingCache();
        } catch (IOException e) {
            log.warn("Failed to initialize cache directory: " + e.getMessage());
        }
    }

    /**
     * Loads existing cache data from the cache directory.
     */
    private void loadExistingCache() {
        try {
            Path cachePath = Paths.get(cacheDir);
            if (Files.exists(cachePath) && Files.isDirectory(cachePath)) {
                Files.walk(cachePath)
                        .filter(Files::isRegularFile)
                        .filter(path -> {
                            String fileName = path.toString();
                            return fileName.endsWith(".json");
                        })
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            cachedClasses.add(fileName.replace(".json", ""));
                        });

                if (!cachedClasses.isEmpty()) {
                    log.info("Loaded " + cachedClasses.size() + " cached classes");
                }
            }
        } catch (IOException e) {
            log.warn("Failed to load existing cache: " + e.getMessage());
        }
    }

    /**
     * Checks if a class is already cached.
     *
     * @param className The name of the class to check
     * @return true if the class is cached, false otherwise
     */
    public boolean isCached(String className) {
        if (!enableCache) {
            return false;
        }

        return cachedClasses.contains(className);
    }

    /**
     * Marks a class as cached and stores the JavadocClass object.
     *
     * @param javadocClass The JavadocClass object to cache
     */
    public void markAsCached(JavadocClass javadocClass) {
        if (!enableCache || javadocClass == null || javadocClass.getFullName() == null) {
            return;
        }

        String className = javadocClass.getFullName();
        cachedClasses.add(className);

        // Serialize and store the JavadocClass object
        try {
            Path cacheFile = Paths.get(cacheDir, className + ".json");
            if (!Files.exists(cacheFile)) {
                objectMapper.writeValue(cacheFile.toFile(), javadocClass);
                log.debug("Cached JavadocClass object for: " + className);
            }
        } catch (IOException e) {
            log.debug("Failed to cache JavadocClass for " + className + ": " + e.getMessage());
        }
    }

    /**
     * Gets a cached JavadocClass object from the cache.
     *
     * @param className The name of the class to retrieve
     * @return The cached JavadocClass object, or null if not found or error occurred
     */
    public JavadocClass getCachedClass(String className) {
        if (!enableCache || className == null || !isCached(className)) {
            return null;
        }

        try {
            Path cacheFile = Paths.get(cacheDir, className + ".json");
            if (Files.exists(cacheFile)) {
                return objectMapper.readValue(cacheFile.toFile(), JavadocClass.class);
            }
        } catch (IOException e) {
            log.debug("Failed to read cached JavadocClass for " + className + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Gets cache statistics as a formatted string.
     *
     * @return Cache statistics string
     */
    public String getCacheStats() {
        if (!enableCache) {
            return "Cache disabled";
        }

        return String.format("Cache: %d classes cached in %s",
                cachedClasses.size(), cacheDir);
    }
}