package io.emop.javadocjson.parser;

import org.apache.maven.plugin.logging.Log;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility class for extracting class URLs from Javadoc HTML documents.
 */
public class ClassUrlExtractor {
    
    private final Log log;
    private final String baseUrl;
    private final Set<String> packageFilters;
    
    public ClassUrlExtractor(Log log, String baseUrl, Set<String> packageFilters) {
        this.log = log;
        this.baseUrl = baseUrl;
        this.packageFilters = packageFilters;
    }
    
    /**
     * Extracts class URLs from an allclasses-type document.
     * 
     * @param document The HTML document to extract URLs from
     * @return List of absolute class URLs
     */
    public List<String> extractFromAllClassesDocument(Document document) {
        List<String> classUrls = new ArrayList<>();
        
        // Try different selectors for different Javadoc versions
        Elements links = document.select("a[href]");
        
        for (Element link : links) {
            String href = link.attr("href");
            String linkText = link.text().trim();
            
            if (isValidClassUrl(href, linkText)) {
                String absoluteUrl = convertToAbsoluteUrl(href);
                if (absoluteUrl != null && passesPackageFilter(absoluteUrl)) {
                    classUrls.add(absoluteUrl);
                }
            }
        }
        
        log.info("Extracted " + classUrls.size() + " class URLs from allclasses document");
        return classUrls;
    }
    
    /**
     * Extracts class URLs from an overview-type document.
     * 
     * @param document The HTML document to extract URLs from
     * @return List of absolute class URLs
     */
    public List<String> extractFromOverviewDocument(Document document) {
        List<String> classUrls = new ArrayList<>();
        
        // Look for package links and class links in overview pages
        Elements packageLinks = document.select("a[href*='package-summary.html'], a[href*='package-frame.html']");
        Elements classLinks = document.select("a[href$='.html']:not([href*='package-'])");
        
        // Add package links for further crawling
        for (Element link : packageLinks) {
            String href = link.attr("href");
            String absoluteUrl = convertToAbsoluteUrl(href);
            if (absoluteUrl != null && passesPackageFilter(absoluteUrl)) {
                classUrls.add(absoluteUrl);
            }
        }
        
        // Add direct class links
        for (Element link : classLinks) {
            String href = link.attr("href");
            String linkText = link.text().trim();
            
            if (isValidClassUrl(href, linkText)) {
                String absoluteUrl = convertToAbsoluteUrl(href);
                if (absoluteUrl != null && passesPackageFilter(absoluteUrl)) {
                    classUrls.add(absoluteUrl);
                }
            }
        }
        
        log.info("Extracted " + classUrls.size() + " URLs from overview document");
        return classUrls;
    }
    
    /**
     * Extracts class URLs from a package summary document.
     * 
     * @param document The HTML document to extract URLs from
     * @return List of absolute class URLs
     */
    public List<String> extractFromPackageDocument(Document document) {
        List<String> classUrls = new ArrayList<>();
        
        // Look for class links in package summary pages
        Elements classLinks = document.select("a[href$='.html']:not([href*='package-'])");
        
        for (Element link : classLinks) {
            String href = link.attr("href");
            String linkText = link.text().trim();
            
            if (isValidClassUrl(href, linkText)) {
                String absoluteUrl = convertToAbsoluteUrl(href);
                if (absoluteUrl != null && passesPackageFilter(absoluteUrl)) {
                    classUrls.add(absoluteUrl);
                }
            }
        }
        
        log.debug("Extracted " + classUrls.size() + " class URLs from package document");
        return classUrls;
    }
    
    /**
     * Alternative extraction method for different Javadoc formats.
     * 
     * @param document The HTML document to extract URLs from
     * @return List of absolute class URLs
     */
    public List<String> extractAlternative(Document document) {
        List<String> classUrls = new ArrayList<>();
        
        // Try more generic selectors
        Elements allLinks = document.select("a[href]");
        
        for (Element link : allLinks) {
            String href = link.attr("href");
            String linkText = link.text().trim();
            
            // More lenient validation for alternative extraction
            if (href.endsWith(".html") && 
                !href.contains("package-") && 
                !href.contains("overview") && 
                !href.contains("index") &&
                !linkText.isEmpty()) {
                
                String absoluteUrl = convertToAbsoluteUrl(href);
                if (absoluteUrl != null && passesPackageFilter(absoluteUrl)) {
                    classUrls.add(absoluteUrl);
                }
            }
        }
        
        log.info("Alternative extraction found " + classUrls.size() + " potential class URLs");
        return classUrls;
    }
    
    private boolean isValidClassUrl(String href, String linkText) {
        if (href == null || href.isEmpty() || linkText == null || linkText.isEmpty()) {
            return false;
        }
        
        // Must be an HTML file
        if (!href.endsWith(".html")) {
            return false;
        }
        
        // Exclude non-class pages
        if (href.contains("package-") || 
            href.contains("overview") || 
            href.contains("index") ||
            href.contains("help-") ||
            href.contains("constant-values") ||
            href.contains("serialized-form") ||
            href.contains("deprecated-list") ||
            href.contains("allclasses")) {
            return false;
        }
        
        // Link text should look like a class name (starts with uppercase)
        return Character.isUpperCase(linkText.charAt(0));
    }
    
    private String convertToAbsoluteUrl(String href) {
        try {
            if (href.startsWith("http://") || href.startsWith("https://")) {
                return href;
            }
            
            URL base = new URL(baseUrl);
            URL absolute = new URL(base, href);
            return absolute.toString();
        } catch (Exception e) {
            log.debug("Failed to convert URL: " + href + " - " + e.getMessage());
            return null;
        }
    }
    
    private boolean passesPackageFilter(String url) {
        if (packageFilters == null || packageFilters.isEmpty()) {
            return true;
        }
        
        // Extract package name from URL path
        try {
            URL urlObj = new URL(url);
            String path = urlObj.getPath();
            
            // Convert path to package name
            String packageName = extractPackageFromPath(path);
            
            if (packageName != null) {
                for (String filter : packageFilters) {
                    if (packageName.startsWith(filter)) {
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            log.debug("Failed to check package filter for URL: " + url + " - " + e.getMessage());
        }
        
        return true; // Default to include if we can't determine package
    }
    
    private String extractPackageFromPath(String path) {
        // Remove leading slash and trailing filename
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String packagePath = path.substring(0, lastSlash);
            return packagePath.replace('/', '.');
        }
        
        return null;
    }
}