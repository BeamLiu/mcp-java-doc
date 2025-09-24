package io.emop.javadocjson.parser;

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
public class CrawlerCache {
    
    private final Log log;
    private final boolean enableCache;
    private final String cacheDir;
    private final Set<String> cachedClasses;
    
    public CrawlerCache(Log log, boolean enableCache, String cacheDir) {
        this.log = log;
        this.enableCache = enableCache;
        this.cacheDir = cacheDir != null ? cacheDir : 
            System.getProperty("java.io.tmpdir") + File.separator + "javadoc-crawler-cache";
        this.cachedClasses = ConcurrentHashMap.newKeySet();
        
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
                    .filter(path -> path.toString().endsWith(".cache"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String className = fileName.replace(".cache", "");
                        cachedClasses.add(className);
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
     * Marks a class as cached.
     * 
     * @param className The name of the class to cache
     */
    public void markAsCached(String className) {
        if (!enableCache) {
            return;
        }
        
        cachedClasses.add(className);
        
        // Create a cache file for persistence
        try {
            Path cacheFile = Paths.get(cacheDir, className + ".cache");
            if (!Files.exists(cacheFile)) {
                Files.createFile(cacheFile);
                log.debug("Created cache file for class: " + className);
            }
        } catch (IOException e) {
            log.debug("Failed to create cache file for class " + className + ": " + e.getMessage());
        }
    }
    
    /**
     * Checks if a URL has been visited (cached).
     * 
     * @param url The URL to check
     * @return true if the URL has been visited, false otherwise
     */
    public boolean isUrlVisited(String url) {
        if (!enableCache) {
            return false;
        }
        
        // Extract class name from URL for caching
        String className = extractClassNameFromUrl(url);
        return className != null && isCached(className);
    }
    
    /**
     * Marks a URL as visited by caching the associated class.
     * 
     * @param url The URL to mark as visited
     */
    public void markUrlAsVisited(String url) {
        if (!enableCache) {
            return;
        }
        
        String className = extractClassNameFromUrl(url);
        if (className != null) {
            markAsCached(className);
        }
    }
    
    /**
     * Clears the cache.
     */
    public void clearCache() {
        if (!enableCache) {
            return;
        }
        
        cachedClasses.clear();
        
        try {
            Path cachePath = Paths.get(cacheDir);
            if (Files.exists(cachePath)) {
                Files.walk(cachePath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".cache"))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.debug("Failed to delete cache file: " + path + " - " + e.getMessage());
                        }
                    });
                
                log.info("Cache cleared");
            }
        } catch (IOException e) {
            log.warn("Failed to clear cache: " + e.getMessage());
        }
    }
    
    /**
     * Gets the number of cached classes.
     * 
     * @return The number of cached classes
     */
    public int getCacheSize() {
        return cachedClasses.size();
    }
    
    /**
     * Checks if caching is enabled.
     * 
     * @return true if caching is enabled, false otherwise
     */
    public boolean isCacheEnabled() {
        return enableCache;
    }
    
    /**
     * Gets the cache directory path.
     * 
     * @return The cache directory path
     */
    public String getCacheDirectory() {
        return cacheDir;
    }
    
    /**
     * Extracts class name from a URL.
     * 
     * @param url The URL to extract class name from
     * @return The class name, or null if it cannot be extracted
     */
    private String extractClassNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        try {
            // Extract the file name from the URL
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            
            // Remove .html extension
            if (fileName.endsWith(".html")) {
                fileName = fileName.substring(0, fileName.length() - 5);
            }
            
            // Basic validation - class names should start with uppercase
            if (!fileName.isEmpty() && Character.isUpperCase(fileName.charAt(0))) {
                return fileName;
            }
        } catch (Exception e) {
            log.debug("Failed to extract class name from URL: " + url + " - " + e.getMessage());
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