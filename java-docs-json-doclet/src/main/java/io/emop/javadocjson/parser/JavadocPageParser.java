package io.emop.javadocjson.parser;

import io.emop.javadocjson.config.JDK9Dialet;
import io.emop.javadocjson.model.JavadocClass;
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
 * 支持可配置的解析策略，针对不同版本的Javadoc使用不同的解析配置
 */
public class JavadocPageParser {

    private final Log log;
    private final String userAgent;
    private final int timeout;
    private final String proxyHost;
    private final int proxyPort;
    private final String proxyUsername;
    private final String proxyPassword;

    // 支持的解析配置列表
    private final List<JavadocParsingConfig> parsingConfigs;

    public JavadocPageParser(Log log, String userAgent, int timeout, String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
        this.log = log;
        this.userAgent = userAgent;
        this.timeout = timeout;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;

        // 初始化解析配置
        this.parsingConfigs = Arrays.asList(
                new JDK9Dialet()
                // 可以在这里添加更多的解析配置
        );
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

        Document doc = configureConnection(classUrl).get();
        String htmlContent = doc.html();

        // 选择合适的解析配置
        JavadocParsingConfig config = selectParsingConfig(htmlContent);

        JavadocClass javadocClass = new JavadocClass();

        // Extract basic class information
        javadocClass.setName(extractClassNameFromPage(doc));
        javadocClass.setPackageName(extractPackageNameFromPage(doc));
        javadocClass.setDescription(extractClassDescriptionFromPage(doc));
        javadocClass.setType(extractClassTypeFromPage(doc));

        // Extract methods and fields using configured parsing strategy
        if (config != null) {
            log.debug("Using parsing config: " + config.getConfigName());
            javadocClass.setSimpleMethods(parseMethodsWithConfig(doc, config));
            javadocClass.setSimpleFields(parseFieldsWithConfig(doc, config));
        } else {
            log.debug("Using fallback parsing strategy");
            javadocClass.setSimpleMethods(parseSimpleMethodsFromPage(doc));
            javadocClass.setSimpleFields(parseSimpleFieldsFromPage(doc));
        }

        log.debug("Parsed class: " + javadocClass.getName() + " with " +
                javadocClass.getSimpleMethods().size() + " simple methods and " +
                javadocClass.getSimpleFields().size() + " simple fields");

        return javadocClass;
    }

    /**
     * 选择合适的解析配置
     */
    private JavadocParsingConfig selectParsingConfig(String htmlContent) {
        for (JavadocParsingConfig config : parsingConfigs) {
            if (config.isApplicable(htmlContent)) {
                return config;
            }
        }
        return null; // 使用默认解析策略
    }

    /**
     * 使用配置化策略解析方法
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

                    // 去重检查
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
     * 使用配置化策略解析字段
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

                    // 去重检查
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

    private List<SimpleJavadocMethod> parseSimpleMethodsFromPage(Document doc) {
        List<SimpleJavadocMethod> methods = new ArrayList<>();
        Set<String> seenTexts = new HashSet<>(); // 用于去重

        try {
            // 使用更精确的选择器，专门针对方法
            String[] methodSelectors = {
                    "table.memberSummary tr td.colFirst + td.colLast", // 方法摘要表格
                    "table.methodSummary tr td.colFirst + td.colLast", // 方法摘要表格
                    ".methodSignature", // 方法签名
                    ".memberSummary .member", // 成员摘要
                    "ul.blockList li.blockList .member" // 详细方法列表
            };

            for (String selector : methodSelectors) {
                Elements elements = doc.select(selector);

                for (Element element : elements) {
                    String text = element.text().trim();

                    // 基本过滤：必须包含括号且不为空
                    if (text.contains("(") && text.contains(")") &&
                            text.length() > 5 && text.length() < 500) { // 长度合理

                        // 去重检查
                        if (!seenTexts.contains(text)) {
                            seenTexts.add(text);
                            methods.add(new SimpleJavadocMethod("", "", "", text));
                        }
                    }
                }
            }

            // 如果没有找到方法，尝试更宽泛的搜索
            if (methods.isEmpty()) {
                Elements allElements = doc.select("*");
                for (Element element : allElements) {
                    String text = element.ownText().trim();
                    if (text.contains("(") && text.contains(")") &&
                            text.length() > 10 && text.length() < 200 &&
                            !seenTexts.contains(text)) {
                        seenTexts.add(text);
                        methods.add(new SimpleJavadocMethod("", "", "", text));
                        if (methods.size() >= 50) break; // 限制数量，避免过多
                    }
                }
            }

        } catch (Exception e) {
            log.warn("Error parsing methods from page: " + e.getMessage());
        }

        log.debug("Found " + methods.size() + " unique methods");
        return methods;
    }

    private List<SimpleJavadocField> parseSimpleFieldsFromPage(Document doc) {
        List<SimpleJavadocField> fields = new ArrayList<>();
        Set<String> seenTexts = new HashSet<>(); // 用于去重

        try {
            // 使用更精确的选择器，专门针对字段
            String[] fieldSelectors = {
                    "table.memberSummary tr td.colFirst + td.colLast", // 字段摘要表格
                    "table.fieldSummary tr td.colFirst + td.colLast", // 字段摘要表格
                    ".fieldSignature", // 字段签名
                    "ul.blockList li.blockList .member" // 详细字段列表
            };

            for (String selector : fieldSelectors) {
                Elements elements = doc.select(selector);

                for (Element element : elements) {
                    String text = element.text().trim();

                    // 基本过滤：不包含括号，长度合理，看起来像字段声明
                    if (!text.contains("(") && !text.contains(")") &&
                            text.length() > 3 && text.length() < 300 &&
                            isLikelyFieldText(text)) {

                        // 去重检查
                        if (!seenTexts.contains(text)) {
                            seenTexts.add(text);
                            fields.add(new SimpleJavadocField("", "", ""));
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.warn("Error parsing fields from page: " + e.getMessage());
        }

        log.debug("Found " + fields.size() + " unique fields");
        return fields;
    }

    /**
     * 简化的字段文本判断逻辑
     */
    private boolean isLikelyFieldText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();

        // 排除明显不是字段的文本
        if (lowerText.contains("method") || lowerText.contains("constructor") ||
                lowerText.contains("class") || lowerText.contains("interface") ||
                lowerText.contains("package") || lowerText.contains("import") ||
                lowerText.startsWith("see also") || lowerText.startsWith("since") ||
                lowerText.startsWith("author") || lowerText.startsWith("version")) {
            return false;
        }

        // 检查是否包含常见的类型关键字或符合字段模式
        String[] commonPatterns = {"static", "final", "private", "public", "protected",
                "int", "string", "boolean", "long", "double", "float",
                "char", "byte", "short", "list", "map", "set", "array",
                "object", "collection"};

        for (String pattern : commonPatterns) {
            if (lowerText.contains(pattern)) {
                return true;
            }
        }

        // 基本模式检查：至少包含2个单词，不超过10个单词
        String[] words = text.trim().split("\\s+");
        return words.length >= 2 && words.length <= 10;
    }

    /**
     * 从元素中提取描述信息
     */
    private String extractDescriptionFromElement(Element element) {
        try {
            // 尝试从同一行或相邻元素中找到描述
            Elements descElements = element.select(".block, .description");
            if (!descElements.isEmpty()) {
                return descElements.first().text().trim();
            }

            // 尝试从父元素中查找描述
            Element parent = element.parent();
            if (parent != null) {
                Elements parentDesc = parent.select(".block, .description");
                if (!parentDesc.isEmpty()) {
                    return parentDesc.first().text().trim();
                }
            }

            // 尝试从下一个兄弟元素中查找描述
            Element nextSibling = element.nextElementSibling();
            if (nextSibling != null && nextSibling.hasClass("description")) {
                return nextSibling.text().trim();
            }

        } catch (Exception e) {
            log.debug("Failed to extract description: " + e.getMessage());
        }

        return "";
    }

    private String extractPackageNameFromLink(String href) {
        // Extract package name from href like "com/example/package-summary.html"
        if (href.contains("package-summary.html")) {
            String path = href.replace("/package-summary.html", "");
            return path.replace("/", ".");
        }
        return null;
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