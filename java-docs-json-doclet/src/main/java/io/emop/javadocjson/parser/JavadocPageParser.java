package io.emop.javadocjson.parser;

import io.emop.javadocjson.model.JavadocClass;
import io.emop.javadocjson.model.JavadocField;
import io.emop.javadocjson.model.JavadocMethod;
import org.apache.maven.plugin.logging.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parser class for extracting Javadoc information from HTML pages.
 */
public class JavadocPageParser {
    
    private final Log log;
    private final String userAgent;
    private final int timeout;
    
    public JavadocPageParser(Log log, String userAgent, int timeout) {
        this.log = log;
        this.userAgent = userAgent;
        this.timeout = timeout;
    }
    
    /**
     * Parses a class page and extracts JavadocClass information.
     * 
     * @param classUrl The URL of the class page to parse
     * @return JavadocClass object with extracted information
     * @throws IOException if the page cannot be accessed
     */
    public JavadocClass parseClassPage(String classUrl) throws IOException {
        log.debug("Parsing class page: " + classUrl);
        
        Document doc = Jsoup.connect(classUrl)
                .userAgent(userAgent)
                .timeout(timeout)
                .get();
        
        JavadocClass javadocClass = new JavadocClass();
        
        // Extract basic class information
        javadocClass.setName(extractClassNameFromPage(doc));
        javadocClass.setPackageName(extractPackageNameFromPage(doc));
        javadocClass.setDescription(extractClassDescriptionFromPage(doc));
        javadocClass.setType(extractClassTypeFromPage(doc));
        
        // Extract methods and fields
        javadocClass.setMethods(parseMethodsFromPage(doc));
        javadocClass.setFields(parseFieldsFromPage(doc));
        
        log.debug("Parsed class: " + javadocClass.getName() + " with " + 
                 javadocClass.getMethods().size() + " methods and " + 
                 javadocClass.getFields().size() + " fields");
        
        return javadocClass;
    }
    
    /**
     * Extracts class URLs from a package summary page.
     * 
     * @param packageUrl The URL of the package page
     * @param baseUrl The base URL for resolving relative links
     * @param packagePath The package path for URL construction
     * @return Set of class URLs found in the package
     * @throws IOException if the page cannot be accessed
     */
    public Set<String> extractClassUrlsFromPackagePage(String packageUrl, String baseUrl, String packagePath) throws IOException {
        log.debug("Extracting class URLs from package page: " + packageUrl);
        
        Document doc = Jsoup.connect(packageUrl)
                .userAgent(userAgent)
                .timeout(timeout)
                .get();
        
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
    
    private String extractClassNameFromPage(Document doc) {
        // Try to extract class name from title
        String title = doc.title();
        if (title != null && !title.isEmpty()) {
            // Remove common prefixes and suffixes
            title = title.replaceAll("^(Class|Interface|Enum|Annotation Type)\\s+", "");
            title = title.replaceAll("\\s+\\(.*\\)$", "");
            
            // Extract the class name (usually the first word after cleaning)
            String[] parts = title.split("\\s+");
            if (parts.length > 0) {
                return parts[0];
            }
        }
        
        // Try to extract from header elements
        Elements headerElements = doc.select("h1, .header h1, .title");
        for (Element element : headerElements) {
            String text = element.text().trim();
            if (!text.isEmpty()) {
                // Clean up the text similar to title
                text = text.replaceAll("^(Class|Interface|Enum|Annotation Type)\\s+", "");
                String[] parts = text.split("\\s+");
                if (parts.length > 0) {
                    return parts[0];
                }
            }
        }
        
        return "Unknown";
    }
    
    private String extractPackageNameFromPage(Document doc) {
        // Try to find package name in various locations
        Elements packageElements = doc.select(".header .subTitle, .subTitle");
        for (Element element : packageElements) {
            String text = element.text();
            if (text.startsWith("Package ")) {
                return text.substring(8).trim();
            }
        }
        
        // Try breadcrumb navigation
        Elements breadcrumbs = doc.select(".navBarCell1Rev, .topNav a");
        for (Element breadcrumb : breadcrumbs) {
            String href = breadcrumb.attr("href");
            if (href.contains("package-summary.html")) {
                return extractPackageNameFromLink(href);
            }
        }
        
        return "";
    }
    
    private String extractClassDescriptionFromPage(Document doc) {
        // Look for class description in various locations
        Elements descElements = doc.select(".block, .description .block, .classDescription");
        
        for (Element element : descElements) {
            String text = element.text().trim();
            if (!text.isEmpty() && text.length() > 10) { // Avoid very short descriptions
                return text;
            }
        }
        
        return "";
    }
    
    private String extractClassTypeFromPage(Document doc) {
        String title = doc.title();
        if (title != null) {
            if (title.toLowerCase().contains("interface")) {
                return "interface";
            } else if (title.toLowerCase().contains("enum")) {
                return "enum";
            } else if (title.toLowerCase().contains("annotation")) {
                return "annotation";
            }
        }
        
        // Check header elements
        Elements headerElements = doc.select("h1, .header h1, .title");
        for (Element element : headerElements) {
            String text = element.text().toLowerCase();
            if (text.contains("interface")) {
                return "interface";
            } else if (text.contains("enum")) {
                return "enum";
            } else if (text.contains("annotation")) {
                return "annotation";
            }
        }
        
        return "class"; // Default to class
    }
    
    private List<JavadocMethod> parseMethodsFromPage(Document doc) {
        List<JavadocMethod> methods = new ArrayList<>();
        
        // Look for method details sections
        Elements methodElements = doc.select(".memberSummary tr, .details .member, .methodSummary tr");
        
        for (Element element : methodElements) {
            try {
                JavadocMethod method = new JavadocMethod();
                
                // Extract method name and signature
                Elements nameElements = element.select(".colLast .member, .memberNameLink, code, .methodName");
                if (!nameElements.isEmpty()) {
                    String signature = nameElements.text();
                    if (signature.contains("(")) {
                        String methodName = signature.substring(0, signature.indexOf("("));
                        method.setName(methodName.trim());
                        method.setSignature(signature);
                    }
                }
                
                // Extract description
                Elements descElements = element.select(".block, .description");
                if (!descElements.isEmpty()) {
                    method.setDescription(descElements.first().text());
                }
                
                if (method.getName() != null && !method.getName().isEmpty()) {
                    methods.add(method);
                }
            } catch (Exception e) {
                log.debug("Failed to parse method: " + e.getMessage());
            }
        }
        
        return methods;
    }
    
    private List<JavadocField> parseFieldsFromPage(Document doc) {
        List<JavadocField> fields = new ArrayList<>();
        
        // Look for field details sections
        Elements fieldElements = doc.select(".memberSummary tr, .details .member, .fieldSummary tr");
        
        for (Element element : fieldElements) {
            try {
                JavadocField field = new JavadocField();
                
                // Extract field name and type
                Elements nameElements = element.select(".colLast .member, .memberNameLink, code, .fieldName");
                if (!nameElements.isEmpty()) {
                    String signature = nameElements.text();
                    String[] parts = signature.split("\\s+");
                    if (parts.length >= 2) {
                        field.setType(parts[0]);
                        field.setName(parts[1]);
                    } else if (parts.length == 1) {
                        field.setName(parts[0]);
                        field.setType("Unknown");
                    }
                }
                
                // Extract description
                Elements descElements = element.select(".block, .description");
                if (!descElements.isEmpty()) {
                    field.setDescription(descElements.first().text());
                }
                
                if (field.getName() != null && !field.getName().isEmpty()) {
                    fields.add(field);
                }
            } catch (Exception e) {
                log.debug("Failed to parse field: " + e.getMessage());
            }
        }
        
        return fields;
    }
    
    private String extractPackageNameFromLink(String href) {
        // Extract package name from href like "com/example/package-summary.html"
        if (href.contains("package-summary.html")) {
            String path = href.replace("/package-summary.html", "");
            return path.replace("/", ".");
        }
        return null;
    }
}