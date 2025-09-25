package io.emop.javadocjson.parser;

import io.emop.javadocjson.config.JDK9Dialet;
import io.emop.javadocjson.model.JavadocClass;
import io.emop.javadocjson.model.SimpleJavadocConstructor;
import io.emop.javadocjson.model.SimpleJavadocField;
import io.emop.javadocjson.model.SimpleJavadocMethod;
import org.apache.maven.plugin.logging.Log;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parser class for extracting Javadoc information from HTML pages.
 * Supports configurable parsing strategies, using different parsing configurations for different versions of Javadoc.
 */
public class JavadocPageParser {

    private final Log log;
    private final String userAgent;
    private final int timeout;
    private final String proxyHost;
    private final int proxyPort;
    private final String proxyUsername;
    private final String proxyPassword;

    // List of supported parsing configurations
    private final List<JavadocParsingConfig> parsingConfigs;

    public JavadocPageParser(Log log, String userAgent, int timeout, String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
        this.log = log;
        this.userAgent = userAgent;
        this.timeout = timeout;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;

        // Initialize parsing configurations
        this.parsingConfigs = Arrays.asList(
                new JDK9Dialet()
                // More parsing configurations can be added here
        );
    }

    /**
     * Parses a class page and extracts JavadocClass information.
     *
     * @param classUrl The URL of the class page to parse
     * @return JavadocClass object with extracted information
     * @throws IOException if the page cannot be accessed
     */
    public JavadocClass parseClassPage(String classUrl, String packageName, String simpleClzName) throws IOException {
        log.debug("Parsing class page: " + classUrl);

        Document doc = configureConnection(classUrl).get();
        String htmlContent = doc.html();

        // Select appropriate parsing configuration
        JavadocParsingConfig config = selectParsingConfig(htmlContent);

        JavadocClass javadocClass = new JavadocClass();

        // Extract other class information using config
        log.debug("Using parsing config: " + config.getConfigName());
        JavadocParsingConfig.ClassInfoParsingConfig classInfoConfig = config.getClassInfoParsingConfig();
        JavadocParsingConfig.InheritanceParsingConfig inheritanceConfig = config.getInheritanceParsingConfig();

        javadocClass.setName(simpleClzName);
        javadocClass.setPackageName(packageName);
        javadocClass.setDescription(classInfoConfig.extractClassDescription(doc));
        javadocClass.setType(classInfoConfig.extractClassType(doc));
        javadocClass.setModifiers(classInfoConfig.extractModifiers(doc));

        // Extract inheritance information
        javadocClass.setSuperClass(inheritanceConfig.extractSuperClass(doc));
        javadocClass.setInterfaces(inheritanceConfig.extractInterfaces(doc));

        // Extract methods, fields and constructors using configured parsing strategy
        javadocClass.setSimpleMethods(parseMethodsWithConfig(doc, config));
        javadocClass.setSimpleFields(parseFieldsWithConfig(doc, config));
        javadocClass.setSimpleConstructors(parseSimpleConstructorsWithConfig(doc, config));

        log.debug("Parsed class: " + javadocClass.getName() + " with " +
                javadocClass.getSimpleMethods().size() + " simple methods, " +
                javadocClass.getSimpleFields().size() + " simple fields, and " +
                javadocClass.getSimpleConstructors().size() + " simple constructors");

        return javadocClass;
    }

    /**
     * Select appropriate parsing configuration
     */
    private JavadocParsingConfig selectParsingConfig(String htmlContent) {
        for (JavadocParsingConfig config : parsingConfigs) {
            if (config.isApplicable(htmlContent)) {
                return config;
            }
        }
        return null; // Use default parsing strategy
    }

    /**
     * Parse methods using configurable strategy
     */
    private List<SimpleJavadocMethod> parseMethodsWithConfig(Document doc, JavadocParsingConfig config) {
        List<SimpleJavadocMethod> methods = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();

        try {
            JavadocParsingConfig.MethodParsingConfig methodConfig = config.getMethodParsingConfig();
            Elements methodElements = doc.select(methodConfig.getMethodSelector());

            for (Element element : methodElements) {
                if (methodConfig.isValidMethodElement(element)) {
                    String name = methodConfig.extractMethodName(element);
                    String modifierAndType = methodConfig.extractModifierAndType(element);
                    String description = methodConfig.extractDescription(element);
                    String detailText = methodConfig.extractDetailText(element, doc);

                    // Deduplication check
                    if (!name.isEmpty() && !seenNames.contains(name)) {
                        seenNames.add(name);
                        SimpleJavadocMethod method = new SimpleJavadocMethod(name, modifierAndType, description, detailText);
                        methods.add(method);
                    }
                }
            }

        } catch (Exception e) {
            log.warn("Error parsing methods with config: " + e.getMessage());
        }

        log.debug("Found " + methods.size() + " unique methods using config");
        return methods;
    }

    /**
     * Parse fields using configurable strategy
     */
    private List<SimpleJavadocField> parseFieldsWithConfig(Document doc, JavadocParsingConfig config) {
        List<SimpleJavadocField> fields = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();

        try {
            JavadocParsingConfig.FieldParsingConfig fieldConfig = config.getFieldParsingConfig();
            Elements fieldElements = doc.select(fieldConfig.getFieldSelector());

            for (Element element : fieldElements) {
                if (fieldConfig.isValidFieldElement(element)) {
                    String name = fieldConfig.extractFieldName(element);
                    String modifierAndType = fieldConfig.extractModifierAndType(element);
                    String description = fieldConfig.extractDescription(element);

                    // Deduplication check
                    if (!name.isEmpty() && !seenNames.contains(name)) {
                        seenNames.add(name);
                        SimpleJavadocField field = new SimpleJavadocField(name, modifierAndType, description);
                        fields.add(field);
                    }
                }
            }

        } catch (Exception e) {
            log.warn("Error parsing fields with config: " + e.getMessage());
        }

        log.debug("Found " + fields.size() + " unique fields using config");
        return fields;
    }

    /**
     * Parse constructors using configurable strategy (simplified version)
     */
    private List<SimpleJavadocConstructor> parseSimpleConstructorsWithConfig(Document doc, JavadocParsingConfig config) {
        List<SimpleJavadocConstructor> constructors = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();

        try {
            JavadocParsingConfig.ConstructorParsingConfig constructorConfig = config.getConstructorParsingConfig();
            Elements constructorElements = doc.select(constructorConfig.getConstructorSelector());

            for (Element element : constructorElements) {
                if (constructorConfig.isValidConstructorElement(element)) {
                    String name = constructorConfig.extractConstructorName(element);
                    String description = constructorConfig.extractDescription(element);
                    // Deduplication check
                    if (!name.isEmpty() && !seenNames.contains(name)) {
                        seenNames.add(name);
                        SimpleJavadocConstructor constructor = new SimpleJavadocConstructor(
                                name, description, element.text()
                        );
                        constructors.add(constructor);
                    }
                }
            }

        } catch (Exception e) {
            log.warn("Error parsing simple constructors with config: " + e.getMessage());
        }

        log.debug("Found " + constructors.size() + " unique simple constructors using config");
        return constructors;
    }


    /**
     * Extracts class URLs from a package summary page.
     *
     * @param packageUrl  The URL of the package page
     * @param baseUrl     The base URL for resolving relative links
     * @param packagePath The package path for URL construction
     * @return Set of class URLs found in the package
     * @throws IOException if the page cannot be accessed
     */
    public Set<String> extractClassUrlsFromPackagePage(String packageUrl, String baseUrl, String packagePath) throws IOException {
        log.debug("Extracting class URLs from package page: " + packageUrl);

        Document doc = configureConnection(packageUrl).get();

        Set<String> classUrls = new HashSet<>();

        // Look for class links in various table structures
        Elements classLinks = doc.select("table a[href$='.html'], .summary a[href$='.html'], .contentContainer a[href$='.html']");

        for (Element link : classLinks) {
            String href = link.attr("href");
            if (!href.contains("package-") && !href.contains("overview-")) {
                // Make sure it's a relative URL for this package
                if (!href.startsWith("http") && !href.startsWith("/")) {
                    String fullUrl = baseUrl + packagePath + "/" + href;
                    classUrls.add(fullUrl);
                }
            }
        }

        log.debug("Found " + classUrls.size() + " class URLs in package page");
        return classUrls;
    }

    /**
     * Configures a Jsoup connection with proxy settings if available.
     *
     * @param url The URL to connect to
     * @return Configured Connection object
     */
    private Connection configureConnection(String url) {
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

        return connection;
    }
}