package io.emop.javadocjson.parser;

import org.apache.maven.plugin.logging.Log;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

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
    public Set<String> extractFromAllClassesDocument(Document document) {
        Set<String> classUrls = new HashSet<>();

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
     * Whether the given document has class URLs
     *
     * @param document The HTML document to extract URLs from
     * @return has class link
     */
    public static boolean validAllClassPage(Document document) {
        return document.select("a[href]").stream().anyMatch(link -> {
            String href = link.attr("href");
            String linkText = link.text().trim();
            return isValidClassUrl(href, linkText);
        });
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

    private static boolean isValidClassUrl(String href, String linkText) {
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

    private static Map<String, Pattern> compiledPatterns = new HashMap<>();

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
                    // Use regular expression matching
                    Pattern pattern = compiledPatterns.computeIfAbsent(filter, (exp) -> Pattern.compile(exp));
                    if (pattern.matcher(packageName).matches()) {
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
        try {
            // First, remove the base URL path from the given path
            URL baseUrlObj = new URL(baseUrl);
            String basePath = baseUrlObj.getPath();

            // Remove base path from the full path
            if (basePath != null && !basePath.equals("/") && path.startsWith(basePath)) {
                path = path.substring(basePath.length());
            }

            // Remove leading slash and trailing filename
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            int lastSlash = path.lastIndexOf('/');
            if (lastSlash > 0) {
                String packagePath = path.substring(0, lastSlash);
                return packagePath.replace('/', '.');
            }
        } catch (Exception e) {
            log.debug("Failed to extract package from path: " + path + " - " + e.getMessage());
        }

        return null;
    }
}