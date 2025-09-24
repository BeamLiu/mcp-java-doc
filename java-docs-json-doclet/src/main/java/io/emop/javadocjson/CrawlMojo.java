package io.emop.javadocjson;

import io.emop.javadocjson.model.JavadocMetadata;
import io.emop.javadocjson.model.JavadocRoot;
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
     * Maximum crawling depth.
     */
    @Parameter(property = "maxDepth", defaultValue = "10")
    private int maxDepth;

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
            HtmlCrawler crawler = new HtmlCrawler(getLog());
            crawler.setMaxDepth(maxDepth);
            crawler.setUserAgent(userAgent);
            crawler.setTimeout(timeout);

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