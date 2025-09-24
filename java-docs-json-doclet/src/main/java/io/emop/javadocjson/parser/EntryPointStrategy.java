package io.emop.javadocjson.parser;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.maven.plugin.logging.Log;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Strategy class for handling different entry point attempts in Javadoc crawling.
 */
public class EntryPointStrategy {

    private final Log log;
    private final String baseUrl;
    private final String userAgent;
    private final int timeout;

    private static final List<String> ALL_CLASSES_ENTRY_POINTS = Arrays.asList(
            "allclasses-frame.html",
            "allclasses.html",
            "allclasses-noframe.html"
    );

    private static final List<String> OVERVIEW_ENTRY_POINTS = Arrays.asList(
            "overview-summary.html",
            "overview-frame.html",
            "index.html"
    );

    private static final List<String> FALLBACK_ENTRY_POINTS = Arrays.asList(
            "package-list",
            "element-list"
    );

    public EntryPointStrategy(Log log, String baseUrl, String userAgent, int timeout) {
        this.log = log;
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
        this.timeout = timeout;
    }

    /**
     * Attempts to find a valid entry point by trying different strategies.
     *
     * @return EntryPointResult containing the successful entry point and document, or null if none found
     */
    public EntryPointResult findValidEntryPoint() {
        log.info("Attempting to find valid entry point for: " + baseUrl);

        // Try allclasses entry points first
        EntryPointResult result = tryEntryPointGroup("allclasses", ALL_CLASSES_ENTRY_POINTS);
        if (result != null) {
            return result;
        }

        // Try overview entry points
        result = tryEntryPointGroup("overview", OVERVIEW_ENTRY_POINTS);
        if (result != null) {
            return result;
        }

        // Try fallback entry points
        result = tryEntryPointGroup("fallback", FALLBACK_ENTRY_POINTS);
        if (result != null) {
            return result;
        }

        log.error("No valid entry point found for: " + baseUrl);
        return null;
    }

    private EntryPointResult tryEntryPointGroup(String groupName, List<String> entryPoints) {
        log.info("Trying " + groupName + " entry points...");

        for (String entryPoint : entryPoints) {
            String fullUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + entryPoint;
            log.debug("Attempting entry point: " + fullUrl);

            try {
                if (isPageAccessible(fullUrl)) {
                    Document document = fetchDocument(fullUrl);
                    if (document != null) {
                        log.info("Successfully accessed " + groupName + " entry point: " + entryPoint);
                        return new EntryPointResult(entryPoint, document, fullUrl);
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to access " + entryPoint + ": " + e.getMessage());
            }
        }

        log.warn("No accessible " + groupName + " entry points found");
        return null;
    }

    private boolean isPageAccessible(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(timeout)
                    .execute();

            int statusCode = response.statusCode();
            log.debug("Page " + url + " returned status: " + statusCode);
            return statusCode == 200;
        } catch (IOException e) {
            log.debug("Page not accessible: " + url + " - " + e.getMessage());
            return false;
        }
    }

    private Document fetchDocument(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(timeout)
                    .get();
        } catch (IOException e) {
            log.debug("Failed to fetch document from: " + url + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Result class containing information about a successful entry point.
     */
    @Data
    @RequiredArgsConstructor
    public static class EntryPointResult {
        private final String entryPoint;
        private final Document document;
        private final String fullUrl;

        public boolean isAllClassesType() {
            return ALL_CLASSES_ENTRY_POINTS.contains(entryPoint);
        }

        public boolean isOverviewType() {
            return OVERVIEW_ENTRY_POINTS.contains(entryPoint);
        }

        public boolean isFallbackType() {
            return FALLBACK_ENTRY_POINTS.contains(entryPoint);
        }
    }
}