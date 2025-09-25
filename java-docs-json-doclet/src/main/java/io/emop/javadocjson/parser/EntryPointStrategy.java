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

import static io.emop.javadocjson.parser.ClassUrlExtractor.validAllClassPage;

/**
 * Strategy class for handling different entry point attempts in Javadoc crawling.
 */
public class EntryPointStrategy {

    private final Log log;
    private final String baseUrl;
    private final String userAgent;
    private final int timeout;
    private final String proxyHost;
    private final int proxyPort;
    private final String proxyUsername;
    private final String proxyPassword;

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

    public EntryPointStrategy(Log log, String baseUrl, String userAgent, int timeout, String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
        this.log = log;
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
        this.timeout = timeout;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
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
                Document document = fetchDocument(fullUrl);
                if (document != null && validAllClassPage(document)) {
                    log.info("Successfully accessed " + groupName + " entry point: " + entryPoint);
                    return new EntryPointResult(entryPoint, document, fullUrl);
                }
            } catch (Exception e) {
                log.debug("Failed to access " + entryPoint + ": " + e.getMessage());
            }
        }

        log.warn("No accessible " + groupName + " entry points found");
        return null;
    }

    private Document fetchDocument(String url) {
        try {
            Connection connection = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(timeout);

            // Configure proxy if provided
            if (proxyHost != null && !proxyHost.trim().isEmpty()) {
                connection.proxy(proxyHost, proxyPort);
                if (proxyUsername != null && !proxyUsername.trim().isEmpty()) {
                    connection.header("Proxy-Authorization", "Basic " +
                            java.util.Base64.getEncoder().encodeToString((proxyUsername + ":" + proxyPassword).getBytes()));
                }
            }

            return connection.get();
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