package io.emop.javadocjson.parser;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.maven.plugin.logging.Log;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

import static io.emop.javadocjson.parser.ClassUrlExtractor.validAllClassPage;

/**
 * Strategy class for handling different entry point attempts in Javadoc crawling.
 */
@RequiredArgsConstructor
public class EntryPointStrategy {

    private final Log log;
    private final String baseUrl;
    private final String userAgent;
    private final int timeout;
    private final String proxyHost;
    private final int proxyPort;
    private final String proxyUsername;
    private final String proxyPassword;
    private final String allClassEntryPoint;


    /**
     * Attempts to find a valid entry point by trying different strategies.
     *
     * @return EntryPointResult containing the successful entry point and document, or null if none found
     */
    public EntryPointResult findValidEntryPoint() {
        log.info("Attempting to find valid entry point for: " + baseUrl);

        log.info("Trying " + allClassEntryPoint + " entry point...");

        String fullUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + allClassEntryPoint;
        log.debug("Attempting entry point: " + fullUrl);

        try {
            Document document = fetchDocument(fullUrl);
            if (document != null && validAllClassPage(document)) {
                log.info("Successfully accessed  entry point: " + allClassEntryPoint);
                return new EntryPointResult(allClassEntryPoint, document, fullUrl);
            }
        } catch (Exception e) {
            log.debug("Failed to access " + allClassEntryPoint + ": " + e.getMessage());
        }
        log.warn("No accessible entry points found: " + allClassEntryPoint);
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
    }
}