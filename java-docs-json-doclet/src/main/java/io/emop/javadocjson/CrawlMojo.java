package io.emop.javadocjson;

import io.emop.javadocjson.model.JavadocMetadata;
import io.emop.javadocjson.model.JavadocRoot;
import io.emop.javadocjson.config.JDK9Dialet;
import io.emop.javadocjson.util.JsonGenerator;
import io.emop.javadocjson.parser.HtmlCrawler;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * Maven goal to crawl HTML Javadoc and generate JSON.
 */
@Mojo(name = "crawl", defaultPhase = LifecyclePhase.PACKAGE)
public class CrawlMojo extends AbstractMojo {

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Base URL of the Javadoc website to crawl.
     */
    @Parameter(property = "baseUrl", required = true)
    private String baseUrl;

    /**
     * Output JSON file path.
     */
    @Parameter(property = "outputFile", defaultValue = "${project.build.directory}/javadocs_complete.json")
    private File outputFile;

    /**
     * HTTP User-Agent header.
     */
    @Parameter(property = "userAgent", defaultValue = "JavaDocCrawler/1.0")
    private String userAgent;

    /**
     * Request timeout in milliseconds.
     */
    @Parameter(property = "timeout", defaultValue = "30000")
    private int timeout;
    
    /**
     * Whether to generate MCP-compatible output format.
     */
    @Parameter(property = "mcpCompatible", defaultValue = "true")
    private boolean mcpCompatible;
    
    /**
     * Proxy host for HTTP requests.
     */
    @Parameter(property = "proxyHost")
    private String proxyHost;
    
    /**
     * Proxy port for HTTP requests.
     */
    @Parameter(property = "proxyPort", defaultValue = "8080")
    private int proxyPort;
    
    /**
     * Proxy username for authentication.
     */
    @Parameter(property = "proxyUsername")
    private String proxyUsername;
    
    /**
     * Proxy password for authentication.
     */
    @Parameter(property = "proxyPassword")
    private String proxyPassword;
    
    /**
     * Package filters using regular expressions to limit crawling to specific packages.
     * Each filter is treated as a regular expression pattern that must match the full package name.
     * If no filters are specified, all packages will be crawled.
     * 
     * Examples:
     * - "com\.example\..*" - matches all packages starting with com.example.
     * - ".*\.util" - matches all packages ending with .util
     * - "com\.example\.core" - matches exactly com.example.core package
     * 
     */
    @Parameter(property = "packageFilters")
    private Set<String> packageFilters;
    

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Starting Javadoc HTML crawl from: " + baseUrl);
        
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new MojoExecutionException("baseUrl parameter is required for crawl goal");
        }

        try {
            // Validate URL
            new URL(baseUrl);

            // Create metadata
            JavadocMetadata metadata = new JavadocMetadata("crawl", baseUrl);

            // Create and configure HTML crawler
            HtmlCrawler crawler = new HtmlCrawler(getLog(), new JDK9Dialet());
            crawler.setUserAgent(userAgent);
            crawler.setTimeout(timeout);
            
            // Configure proxy if provided
            if (proxyHost != null && !proxyHost.trim().isEmpty()) {
                crawler.setProxyHost(proxyHost);
                crawler.setProxyPort(proxyPort);
                if (proxyUsername != null && !proxyUsername.trim().isEmpty()) {
                    crawler.setProxyUsername(proxyUsername);
                    crawler.setProxyPassword(proxyPassword);
                }
                getLog().info("Using proxy: " + proxyHost + ":" + proxyPort);
            }
            
            // Configure package filters if provided
            if (packageFilters != null && !packageFilters.isEmpty()) {
                crawler.setPackageFilters(packageFilters);
                getLog().info("Using package filters: " + packageFilters);
            }

            // Crawl the Javadoc website
            JavadocRoot root = crawler.crawl(baseUrl);
            root.setMetadata(metadata);

            // Ensure output directory exists
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            // Generate JSON output
            JsonGenerator generator = new JsonGenerator();
            generator.setMcpCompatible(mcpCompatible);
            generator.writeToFile(root, outputFile);
            
            getLog().info("Javadoc crawl completed. Output written to: " + outputFile.getAbsolutePath());
            getLog().info("Found " + root.getPackages().size() + " packages");
            getLog().info("MCP Compatible: " + mcpCompatible);
            
            int totalClasses = root.getPackages().stream()
                .mapToInt(pkg -> pkg.getClasses().size())
                .sum();
            getLog().info("Total classes: " + totalClasses);

        } catch (IOException e) {
            throw new MojoExecutionException("Failed to crawl Javadoc HTML", e);
        } catch (Exception e) {
            throw new MojoExecutionException("Unexpected error during Javadoc HTML crawling", e);
        }
    }
}