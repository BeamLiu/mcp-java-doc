package io.emop.javadocjson.parser;

import io.emop.javadocjson.config.JDK9Dialet;
import io.emop.javadocjson.model.*;
import lombok.Setter;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * HTML Crawler for extracting Javadoc information from HTML documentation websites.
 * Refactored to use specialized components for better maintainability.
 */
@Setter
public class HtmlCrawler {
    
    private final Log log;
    private String userAgent = "JavaDocCrawler/1.0";
    private int timeout = 30000;
    private int threadPoolSize = 5;
    
    // Proxy configuration
    private String proxyHost;
    private int proxyPort = 8080;
    private String proxyUsername;
    private String proxyPassword;
    
    // Package filtering
    private Set<String> packageFilters = new HashSet<>();
    
    // Cache support
    private boolean enableCache = true;
    private String cacheDir = System.getProperty("java.io.tmpdir") + java.io.File.separator + "javadoc-crawler-cache";
    
    // Component instances
    private CrawlerCache cache;
    private ProgressTracker progressTracker;
    private EntryPointStrategy entryPointStrategy;
    private ClassUrlExtractor classUrlExtractor;
    private JavadocPageParser pageParser;
    
    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    private final Map<String, JavadocPackage> packageMap = new ConcurrentHashMap<>();
    
    private final JavadocParsingConfig parsingConfig;

    public HtmlCrawler(Log log, JavadocParsingConfig parsingConfig) {
        this.log = log;
        this.parsingConfig = parsingConfig;
        initializeComponents();
    }
    
    /**
     * Initializes all component instances.
     */
    private void initializeComponents() {
        this.cache = new CrawlerCache(log, enableCache, cacheDir);
        this.progressTracker = new ProgressTracker(log);
        // Other components will be initialized when baseUrl is available
    }
    
    /**
     * Crawls the Javadoc website and extracts documentation information.
     * 
     * @param baseUrl The base URL of the Javadoc website
     * @return JavadocRoot containing all extracted documentation
     */
    public JavadocRoot crawl(String baseUrl) {
        log.info("Starting crawl of Javadoc website: " + baseUrl);
        
        // Initialize URL-dependent components
        initializeUrlDependentComponents(baseUrl);
        
        progressTracker.start();
        
        try {
            // Find valid entry point
            EntryPointStrategy.EntryPointResult entryPoint = entryPointStrategy.findValidEntryPoint();
            if (entryPoint == null) {
                log.error("No valid entry point found for: " + baseUrl);
                return createEmptyResult();
            }
            
            // Extract class URLs based on entry point type
            Set<String> classUrls = extractClassUrls(entryPoint);
            
            if (classUrls.isEmpty()) {
                log.warn("No class URLs found");
                tryDirectPackageDiscovery(baseUrl);
            } else {
                progressTracker.setTotalClasses(classUrls.size());
                crawlClassesConcurrently(classUrls);
            }
            
            progressTracker.logFinalSummary();
            
            return buildResult();
            
        } catch (Exception e) {
            log.error("Error during crawling: " + e.getMessage(), e);
            return createEmptyResult();
        }
    }
    
    /**
     * Initializes components that depend on the base URL.
     */
    private void initializeUrlDependentComponents(String baseUrl) {
        // Use provided config or create default NXOpen config
        JavadocParsingConfig config = this.parsingConfig != null ? this.parsingConfig : new JDK9Dialet();
        
        this.entryPointStrategy = new EntryPointStrategy(log, baseUrl, userAgent, timeout, proxyHost, proxyPort, proxyUsername, proxyPassword, config.getAllClassesEntryPoint());
        this.classUrlExtractor = new ClassUrlExtractor(log, baseUrl, packageFilters);
        this.pageParser = new JavadocPageParser(log, userAgent, timeout, proxyHost, proxyPort, proxyUsername, proxyPassword);
        
        // Update cache with current settings
        this.cache = new CrawlerCache(log, enableCache, cacheDir);
    }
    
    /**
     * Extracts class URLs from the entry point document.
     */
    private Set<String> extractClassUrls(EntryPointStrategy.EntryPointResult entryPoint) {
        Set<String> classUrls = classUrlExtractor.extractFromAllClassesDocument(entryPoint.getDocument());
        log.info("Extracted " + classUrls.size() + " class URLs from entry point: " + entryPoint.getEntryPoint());
        return classUrls;
    }
    
    /**
     * Crawls classes concurrently using thread pool.
     */
    private void crawlClassesConcurrently(Set<String> classUrls) {
        if (classUrls.isEmpty()) {
            return;
        }
        
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<JavadocClass>> futures = new ArrayList<>();
        
        log.info("Starting concurrent crawling of " + classUrls.size() + " classes with " + threadPoolSize + " threads");
        
        for (String classUrl : classUrls) {
            Future<JavadocClass> future = executor.submit(() -> {
                try {
                    return crawlClass(classUrl);
                } catch (Exception e) {
                    log.warn("Failed to crawl class: " + classUrl + " - " + e.getMessage());
                    progressTracker.incrementFailed();
                    return null;
                }
            });
            futures.add(future);
        }
        
        // Collect results
        for (Future<JavadocClass> future : futures) {
            try {
                JavadocClass javadocClass = future.get();
                if (javadocClass != null) {
                    addClassToPackage(javadocClass);
                    progressTracker.incrementProcessed();
                } else {
                    progressTracker.incrementFailed();
                }
            } catch (Exception e) {
                log.warn("Failed to get crawl result: " + e.getMessage());
                progressTracker.incrementFailed();
            }
        }
        
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Crawls a single class page.
     */
    private JavadocClass crawlClass(String classUrl) throws IOException {
        if (visitedUrls.contains(classUrl)) {
            progressTracker.incrementSkipped();
            return null;
        }
        
        // Check if we have a cached JavadocClass object using the full class name from URL
        String fullClassName = extractClassNameFromUrl(classUrl);
        if (fullClassName != null && cache.isCached(fullClassName)) {
            JavadocClass cachedClass = cache.getCachedClass(fullClassName);
            if (cachedClass != null) {
                log.debug("Using cached JavadocClass for: " + fullClassName);
                progressTracker.incrementSkipped();
                return cachedClass;
            }
        }
        
        visitedUrls.add(classUrl);
        
        try {
            JavadocClass javadocClass = pageParser.parseClassPage(classUrl);
            
            // Cache the parsed JavadocClass object
            if (javadocClass != null) {
                cache.markAsCached(javadocClass);
            }
            
            return javadocClass;
        } catch (IOException e) {
            log.debug("Failed to parse class page: " + classUrl + " - " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Extracts full class name (including package) from a URL.
     * 
     * @param url The URL to extract class name from
     * @return The full class name, or null if it cannot be extracted
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
            if (fileName.isEmpty() || !Character.isUpperCase(fileName.charAt(0))) {
                return null;
            }
            
            // Extract package name from URL path
            String packageName = extractPackageNameFromUrl(url);
            
            if (packageName != null && !packageName.isEmpty()) {
                return packageName + "." + fileName;
            } else {
                return fileName;
            }
        } catch (Exception e) {
            log.debug("Failed to extract class name from URL: " + url + " - " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extracts package name from a URL path.
     * 
     * @param url The URL to extract package name from
     * @return The package name, or null if it cannot be extracted
     */
    private String extractPackageNameFromUrl(String url) {
        try {
            // Find the base documentation path and extract the package path after it
            // Look for common patterns in Javadoc URLs
            String[] patterns = {
                "/api/", "/javadoc/", "/docs/", "/reference/", "/open_java_ref/"
            };
            
            for (String pattern : patterns) {
                int patternIndex = url.indexOf(pattern);
                if (patternIndex != -1) {
                    // Extract the path after the pattern
                    String pathAfterPattern = url.substring(patternIndex + pattern.length());
                    
                    // Remove the class file name from the end
                    int lastSlash = pathAfterPattern.lastIndexOf('/');
                    if (lastSlash > 0) {
                        String packagePath = pathAfterPattern.substring(0, lastSlash);
                        // Convert path separators to dots
                        return packagePath.replace('/', '.');
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract package name from URL: " + url + " - " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Adds a class to its corresponding package.
     */
    private void addClassToPackage(JavadocClass javadocClass) {
        String packageName = javadocClass.getPackageName();
        if (packageName == null || packageName.isEmpty()) {
            packageName = "default";
        }
        
        JavadocPackage javadocPackage = packageMap.computeIfAbsent(packageName, name -> {
            JavadocPackage pkg = new JavadocPackage();
            pkg.setName(name);
            pkg.setClasses(new ArrayList<>());
            return pkg;
        });
        
        javadocPackage.getClasses().add(javadocClass);
    }
    
    /**
     * Attempts direct package discovery when standard entry points fail.
     */
    private void tryDirectPackageDiscovery(String baseUrl) {
        log.info("Attempting direct package discovery...");
        
        if (!packageFilters.isEmpty()) {
            for (String packageFilter : packageFilters) {
                String packagePath = packageFilter.replace(".", "/");
                String packageUrl = baseUrl + "/" + packagePath + "/package-summary.html";
                
                try {
                    log.info("Trying direct package access: " + packageUrl);
                    Set<String> classUrls = pageParser.extractClassUrlsFromPackagePage(packageUrl, baseUrl, packagePath);
                    
                    if (!classUrls.isEmpty()) {
                        log.info("Found " + classUrls.size() + " classes in package " + packageFilter);
                        progressTracker.addToTotalClasses(classUrls.size());
                        crawlClassesConcurrently(classUrls);
                    }
                    
                } catch (IOException e) {
                    log.debug("Failed to access package directly: " + packageUrl + " - " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Builds the final result from collected data.
     */
    private JavadocRoot buildResult() {
        JavadocRoot root = new JavadocRoot();
        root.setPackages(new ArrayList<>(packageMap.values()));
        
        // Sort packages by name
        root.getPackages().sort(Comparator.comparing(JavadocPackage::getName));
        
        // Sort classes within each package
        for (JavadocPackage pkg : root.getPackages()) {
            if (pkg.getClasses() != null) {
                pkg.getClasses().sort(Comparator.comparing(JavadocClass::getName));
            }
        }
        
        log.info("Crawling completed. Found " + packageMap.size() + " packages with " + 
                progressTracker.getProcessedCount() + " classes");
        
        if (cache.isEnableCache()) {
            log.info(cache.getCacheStats());
        }
        
        return root;
    }
    
    /**
     * Creates an empty result when crawling fails.
     */
    private JavadocRoot createEmptyResult() {
        JavadocRoot root = new JavadocRoot();
        root.setPackages(new ArrayList<>());
        return root;
    }
    
}