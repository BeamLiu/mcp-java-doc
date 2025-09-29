package io.emop.javadocjson;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import javax.tools.DocumentationTool;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Maven goal to generate JSON documentation using JDK Doclet API.
 * This replaces the manual parsing approach with the official Doclet implementation.
 */
@Mojo(name = "publish", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class PublishMojo extends AbstractMojo {

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * Output directory for JSON files.
     */
    @Parameter(property = "outputDirectory", defaultValue = "javadoc-output")
    private String outputDirectory;

    /**
     * Source directory to process.
     */
    @Parameter(property = "sourceDirectory", defaultValue = "${project.build.sourceDirectory}")
    private String sourceDirectory;

    /**
     * Include private members in the output.
     */
    @Parameter(property = "includePrivate", defaultValue = "false")
    private boolean includePrivate;

    /**
     * Additional source paths to include.
     */
    @Parameter(property = "sourcePaths")
    private List<String> sourcePaths;

    /**
     * Classpath for the documentation generation.
     */
    @Parameter(property = "classpath")
    private String classpath;

    /**
     * Source file encoding.
     */
    @Parameter(property = "encoding", defaultValue = "UTF-8")
    private String encoding;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Generating JSON documentation using JDK Doclet API...");
            getLog().info("Source directory: " + sourceDirectory);
            getLog().info("Output directory: " + outputDirectory);

            // Get the documentation tool
            DocumentationTool docTool = ToolProvider.getSystemDocumentationTool();
            if (docTool == null) {
                throw new MojoExecutionException("Documentation tool not available. Make sure you're running with JDK (not JRE).");
            }

            // Get file manager
            StandardJavaFileManager fileManager = docTool.getStandardFileManager(null, null, null);

            try {
                // Set up classpath for the file manager
                setupClasspath(fileManager);
                
                // Collect all Java source files
                List<File> javaFiles = collectJavaFiles();
                if (javaFiles.isEmpty()) {
                    getLog().warn("No Java source files found in: " + sourceDirectory);
                    return;
                }

                getLog().info("Found " + javaFiles.size() + " Java source files");

                // Convert to JavaFileObject
                Iterable<? extends JavaFileObject> compilationUnits = 
                    fileManager.getJavaFileObjectsFromFiles(javaFiles);

                // Prepare doclet arguments
                List<String> options = prepareDocletOptions();

                // Create and run the documentation task
                DocumentationTool.DocumentationTask task = docTool.getTask(
                    null,           // Writer for additional output
                    fileManager,    // File manager
                    null,           // Diagnostic listener
                    io.emop.javadocjson.doclet.JsonDoclet.class, // Doclet class
                    options,        // Options
                    compilationUnits // Source files
                );

                // Execute the task
                Boolean result = task.call();
                if (result == null || !result) {
                    throw new MojoExecutionException("Doclet execution failed");
                }

                getLog().info("JSON documentation generated successfully in: " + outputDirectory);

            } finally {
                fileManager.close();
            }

        } catch (IOException e) {
            throw new MojoExecutionException("Error during documentation generation", e);
        }
    }

    private List<File> collectJavaFiles() throws IOException {
        List<File> javaFiles = new ArrayList<>();

        // Add main source directory
        if (sourceDirectory != null && !sourceDirectory.isEmpty()) {
            Path sourcePath = Paths.get(sourceDirectory);
            if (Files.exists(sourcePath)) {
                javaFiles.addAll(findJavaFiles(sourcePath));
            }
        }

        // Add additional source paths
        if (sourcePaths != null) {
            for (String path : sourcePaths) {
                Path sourcePath = Paths.get(path);
                if (Files.exists(sourcePath)) {
                    javaFiles.addAll(findJavaFiles(sourcePath));
                }
            }
        }

        return javaFiles;
    }

    private List<File> findJavaFiles(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .map(Path::toFile)
                .collect(Collectors.toList());
        }
    }

    private void setupClasspath(StandardJavaFileManager fileManager) throws MojoExecutionException {
        try {
            if (project != null) {
                // Get both compile and runtime classpath elements
                List<String> classpathElements = new ArrayList<>();
                
                // Add compile dependencies
                List<String> compileClasspath = project.getCompileClasspathElements();
                getLog().info("Compile classpath elements: " + compileClasspath.size());
                for (String element : compileClasspath) {
                    getLog().debug("Compile classpath: " + element);
                }
                classpathElements.addAll(compileClasspath);
                
                // Add runtime dependencies (this includes Apache Commons, etc.)
                List<String> runtimeClasspath = project.getRuntimeClasspathElements();
                getLog().info("Runtime classpath elements: " + runtimeClasspath.size());
                for (String element : runtimeClasspath) {
                    getLog().debug("Runtime classpath: " + element);
                }
                for (String runtimeElement : runtimeClasspath) {
                    if (!classpathElements.contains(runtimeElement)) {
                        classpathElements.add(runtimeElement);
                    }
                }
                
                if (!classpathElements.isEmpty()) {
                    // Convert string paths to File objects
                    List<File> classpathFiles = classpathElements.stream()
                        .map(File::new)
                        .filter(File::exists)
                        .collect(Collectors.toList());
                    
                    // Set the classpath in the file manager
                    fileManager.setLocation(StandardLocation.CLASS_PATH, classpathFiles);
                    
                    getLog().info("Set classpath with " + classpathFiles.size() + " entries");
                    getLog().debug("Classpath entries: " + classpathFiles);
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to setup classpath: " + e.getMessage(), e);
        }
    }

    private List<String> prepareDocletOptions() {
        List<String> options = new ArrayList<>();

        // Add custom doclet options
        options.add("-outputDirectory");
        options.add(outputDirectory);

        if (includePrivate) {
            options.add("-includePrivate");
        }

        // Add encoding option to handle UTF-8 source files
        options.add("-encoding");
        options.add(encoding);

        // Add classpath - include both compile and runtime dependencies
        if (classpath != null && !classpath.isEmpty()) {
            options.add("-classpath");
            options.add(classpath);
        } else if (project != null) {
            try {
                // Get both compile and runtime classpath elements
                List<String> classpathElements = new ArrayList<>();

                // Add compile dependencies
                classpathElements.addAll(project.getCompileClasspathElements());
                
                // Add runtime dependencies (this includes Apache Commons, etc.)
                List<String> runtimeClasspath = project.getRuntimeClasspathElements();
                for (String runtimeElement : runtimeClasspath) {
                    if (!classpathElements.contains(runtimeElement)) {
                        classpathElements.add(runtimeElement);
                    }
                }
                
                if (!classpathElements.isEmpty()) {
                    options.add("-classpath");
                    options.add(String.join(File.pathSeparator, classpathElements));
                    getLog().debug("Using classpath: " + String.join(File.pathSeparator, classpathElements));
                }
            } catch (Exception e) {
                getLog().warn("Could not determine project classpath: " + e.getMessage());
            }
        }

        // Add standard javadoc options
        options.add("-quiet");
        
        // Add source path for better resolution
        if (sourceDirectory != null && !sourceDirectory.isEmpty()) {
            options.add("-sourcepath");
            options.add(sourceDirectory);
        }

        getLog().debug("Doclet options: " + String.join(" ", options));
        return options;
    }

}