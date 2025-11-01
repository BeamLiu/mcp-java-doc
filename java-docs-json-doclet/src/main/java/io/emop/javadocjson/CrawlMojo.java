package io.emop.javadocjson;

import io.emop.javadocjson.config.JDK9Dialet;
import io.emop.javadocjson.model.JavadocClass;
import io.emop.javadocjson.parser.HtmlCrawler;
import io.emop.javadocjson.util.JsonGenerator;
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
import java.util.List;
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
     * Output directory for individual class JSON files.
     */
    @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}/javadoc-json")
    private File outputDirectory;

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
            List<JavadocClass> classes = crawler.crawl(baseUrl);

            // Ensure output directory exists
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }

            // Generate individual JSON files for each class
            JsonGenerator generator = new JsonGenerator();
            generator.setMcpCompatible(mcpCompatible);
            
            int totalClasses = 0;
            for (JavadocClass javadocClass : classes) {
                // Create filename based on full class name
                String fileName = javadocClass.getFullName() + ".json";
                File classFile = new File(outputDirectory, fileName);
                
                // Write individual class JSON file
                generator.writeClassToFile(javadocClass, classFile);
                totalClasses++;
            }
            
            getLog().info("Javadoc crawl completed. Output written to: " + outputDirectory.getAbsolutePath());
            getLog().info("Total classes: " + totalClasses);
            getLog().info("MCP Compatible: " + mcpCompatible);

        } catch (IOException e) {
            throw new MojoExecutionException("Failed to crawl Javadoc HTML", e);
        } catch (Exception e) {
            throw new MojoExecutionException("Unexpected error during Javadoc HTML crawling", e);
        }
    }
}