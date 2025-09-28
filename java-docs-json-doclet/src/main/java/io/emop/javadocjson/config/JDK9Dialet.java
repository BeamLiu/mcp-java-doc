package io.emop.javadocjson.config;

import io.emop.javadocjson.parser.JavadocParsingConfig;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JDK9 Java API 的解析配置实现
 * 基于提供的 CheckScope HTML 示例进行配置
 */
public class JDK9Dialet implements JavadocParsingConfig {

    @Override
    public String getConfigName() {
        return "JDK9 API Document";
    }

    @Override
    public boolean isApplicable(String htmlContent) {
        return true;
    }

    @Override
    public JavadocParsingConfig.MethodParsingConfig getMethodParsingConfig() {
        return new MethodParsingConfig();
    }

    @Override
    public JavadocParsingConfig.FieldParsingConfig getFieldParsingConfig() {
        return new FieldParsingConfig();
    }

    @Override
    public JavadocParsingConfig.ClassInfoParsingConfig getClassInfoParsingConfig() {
        return new ClassInfoParsingConfig();
    }

    @Override
    public JavadocParsingConfig.InheritanceParsingConfig getInheritanceParsingConfig() {
        return new InheritanceParsingConfig();
    }

    @Override
    public JavadocParsingConfig.ConstructorParsingConfig getConstructorParsingConfig() {
        return new ConstructorParsingConfig();
    }

    @Override
    public String getAllClassesEntryPoint() {
        return "allclasses.html";
    }

    /**
     * JDK9 方法解析配置
     */
    private static class MethodParsingConfig implements JavadocParsingConfig.MethodParsingConfig {

        @Override
        public String getMethodSelector() {
            // 基于 HTML 结构，方法在 memberSummary 表格中
            return "#method\\.summary ~ table.memberSummary tr:has(td.colFirst):has(td.colLast)";
        }

        @Override
        public String extractMethodName(Element methodElement) {
            // 名称在第二列
            Element nameElement = methodElement.select(".colSecond").first();
            String txt = nameElement.text().trim();
            int paramStart = txt.indexOf("(");
            return paramStart > 0 ? txt.substring(0, paramStart) : txt;
        }

        @Override
        public String extractMethodNameWithParameters(Element methodElement) {
            Elements secondCol = methodElement.select(".colSecond");
            return secondCol.text().trim();
        }

        @Override
        public String extractModifierAndType(Element methodElement) {
            // 修饰符和类型在第一列
            Element modifierElement = methodElement.select(".colFirst").first();
            return modifierElement.text().trim();
        }

        @Override
        public String extractDescription(Element methodElement) {
            // 描述在第最后列
            Element descElement = methodElement.select(".colLast").first();
            return descElement.text().trim();
        }

        public String extractDetailText(Element methodElement, Document doc) {
            String methodNameWithParameters = extractMethodNameWithParameters(methodElement);
            // 先选择所有 pre.methodSignature，然后手动过滤
            Elements methodSignatures = doc.select("pre.methodSignature");

            for (Element signature : methodSignatures) {
                if (cleanInvisibleChars(signature.text()).contains(cleanInvisibleChars(methodNameWithParameters))) {
                    // 找到匹配的签名，获取其父元素
                    Element parent = signature.parent();
                    Elements children = parent.children(); // 获取所有直接子元素

                    // 使用 StringBuilder 构建结果
                    StringBuilder result = new StringBuilder();
                    // 从索引 1 开始遍历（跳过第一个子元素，通常是 <h4>）
                    for (int i = 1; i < children.size(); i++) {
                        if (result.length() > 0) {
                            result.append("\n"); // 用换行符连接
                        }
                        result.append(children.get(i).text());
                    }
                    return result.toString();
                }
            }
            return ""; // 如果没有找到匹配项，返回空字符串
        }

        /**
         * 移除常见的不可见字符，并将各种空白字符标准化为空格，
         * 然后压缩多个连续空格为单个空格。
         */
        private static String cleanInvisibleChars(String input) {
            if (input == null) return null;
            return input
                    .replaceAll("[\\p{Cf}\\p{Z}\\p{Cc}]+", "") // Cf=格式控制符（如零宽空格），Z=分隔符（各种空格），Cc=控制字符
                    .trim();
        }

        @Override
        public boolean isValidMethodElement(Element element) {
            // 验证是否为有效的方法元素
            Elements firstCol = element.select(".colFirst");
            Elements secondCol = element.select(".colSecond");
            Elements lastCol = element.select(".colLast");
            return !firstCol.isEmpty() && !secondCol.isEmpty() && !lastCol.isEmpty();
        }
    }

    /**
     * JDK9 字段解析配置
     */
    private static class FieldParsingConfig implements JavadocParsingConfig.FieldParsingConfig {

        @Override
        public String getFieldSelector() {
            // 字段也在 memberSummary 表格中，但通常在不同的部分
            return "#field\\.summary ~ table.memberSummary tr:has(td.colFirst):has(td.colLast)";
        }

        @Override
        public String extractFieldName(Element fieldElement) {
            // 名称在第二列
            Element nameElement = fieldElement.select(".colSecond").first();
            return nameElement.text().trim();
        }

        @Override
        public String extractModifierAndType(Element fieldElement) {
            // 修饰符和类型在第一列
            Element modifierElement = fieldElement.select(".colFirst").first();
            return modifierElement.text().trim();
        }

        @Override
        public String extractDescription(Element fieldElement) {
            // 描述在第二列
            Element descElement = fieldElement.select(".colLast").first();
            return descElement.text().trim();
        }

        @Override
        public String extractRawText(Element fieldElement) {
            // 返回整个行的文本作为原始文本
            return fieldElement.text().trim();
        }

        @Override
        public boolean isValidFieldElement(Element element) {
            // 验证是否为有效的字段元素
            Elements firstCol = element.select(".colFirst");
            Elements secondCol = element.select(".colSecond");
            Elements lastCol = element.select(".colLast");
            return !firstCol.isEmpty() && !secondCol.isEmpty() && !lastCol.isEmpty();
        }
    }

    /**
     * JDK9 类信息解析配置
     */
    private static class ClassInfoParsingConfig implements JavadocParsingConfig.ClassInfoParsingConfig {

        @Override
        public String extractClassDescription(Document doc) {
            // 描述通常在类声明后的第一个 div.block 中
            Element descElement = doc.select(".contentContainer .description .block").first();
            return descElement != null ? descElement.text().trim() : "";
        }

        @Override
        public String extractClassType(Document doc) {
            // 从标题中提取类型
            Element titleElement = doc.select("h1.title, h2.title").first();
            if (titleElement != null) {
                String title = titleElement.text().trim();
                if (title.startsWith("Class ")) {
                    return "class";
                } else if (title.startsWith("Interface ")) {
                    return "interface";
                } else if (title.startsWith("Enum ")) {
                    return "enum";
                }
            }
            return "class";
        }

        @Override
        public java.util.List<String> extractModifiers(Document doc) {
            // 修饰符通常在 pre 标签中的类声明里
            java.util.List<String> modifiers = new java.util.ArrayList<>();
            Element preElement = doc.select("pre").first();
            if (preElement != null) {
                String declaration = preElement.text().trim();
                // 提取 public, abstract, final 等修饰符
                String[] parts = declaration.split("\\s+");
                for (String part : parts) {
                    if (part.equals("public") || part.equals("private") || part.equals("protected") ||
                            part.equals("abstract") || part.equals("final") || part.equals("static")) {
                        modifiers.add(part);
                    }
                }
            }
            return modifiers;
        }
    }

    /**
     * JDK9 继承关系解析配置
     */
    private static class InheritanceParsingConfig implements JavadocParsingConfig.InheritanceParsingConfig {

        @Override
        public String extractSuperClass(Document doc) {
            // 首先检查是否是interface，interface没有superclass
            Element titleElement = doc.select("h1.title, h2.title").first();
            if (titleElement != null && titleElement.text().contains("Interface ")) {
                return ""; // interface没有superclass
            }
            
            List<String> inheritanceList = doc.select("ul.inheritance li")
                    .stream()
                    .map(Element::text)
                    .map(String::trim)
                    .filter(text -> !text.isEmpty())
                    .collect(Collectors.toCollection(LinkedHashSet::new))
                    .stream()
                    .collect(Collectors.toList());
            if (inheritanceList.size() >= 3) {
                // 倒数第二个通常是直接父类，同时不要java.lang.Object
                return inheritanceList.get(inheritanceList.size() - 2);
            }
            return "";
        }

        @Override
        public java.util.List<String> extractInterfaces(Document doc) {
            // 查找实现的接口或父接口，通常在 dl dt dd 结构中
            java.util.List<String> interfaces = new java.util.ArrayList<>();
            Elements dlElements = doc.select("dl");
            for (Element dl : dlElements) {
                Element dt = dl.select("dt").first();
                if (dt != null) {
                    String dtText = dt.text();
                    // 对于class，查找"All Implemented Interfaces"
                    // 对于interface，查找"All Superinterfaces"
                    if (dtText.contains("All Implemented Interfaces") || dtText.contains("All Superinterfaces")) {
                        Element dd = dl.select("dd").first();
                        if (dd != null) {
                            // 首先尝试从链接中提取完整的包名和类名
                            Elements links = dd.select("a");
                            if (!links.isEmpty()) {
                                // 有链接的情况，从链接中提取完整信息
                                 for (Element link : links) {
                                     String href = link.attr("href");
                                     String title = link.attr("title");
                                     String linkText = link.text().trim();
                                     
                                     // 优先使用title属性中的包信息
                                     String fullClassName = extractFullClassNameFromTitle(title, linkText);
                                     if (fullClassName == null || fullClassName.isEmpty()) {
                                         // 如果title无法提取，尝试从URL提取
                                         fullClassName = extractFullClassNameFromUrl(href, linkText);
                                     }
                                     
                                     if (fullClassName != null && !fullClassName.isEmpty()) {
                                         interfaces.add(fullClassName);
                                     } else {
                                         interfaces.add(linkText);
                                     }
                                 }
                                
                                // 处理没有链接的接口（如java.rmi.Remote等）
                                String fullText = dd.text().trim();
                                Elements codeElements = dd.select("code");
                                for (Element codeElement : codeElements) {
                                    String codeText = codeElement.text().trim();
                                    // 如果这个code元素没有链接，且不在已添加的接口中
                                    if (codeElement.select("a").isEmpty() && !interfaces.contains(codeText)) {
                                        interfaces.add(codeText);
                                    }
                                }
                            } else {
                                // 没有链接的情况，使用原来的文本解析方式
                                String interfaceText = dd.text().trim();
                                String[] interfaceArray = interfaceText.split(",");
                                for (String iface : interfaceArray) {
                                    interfaces.add(iface.trim());
                                }
                            }
                        }
                    }
                }
            }
            return interfaces;
        }
        
        /**
          * 从title属性中提取完整的类名（包括包名）
          * title格式通常为: "interface in nxopen.features" 或 "class in java.lang"
          */
         private String extractFullClassNameFromTitle(String title, String linkText) {
             if (title == null || title.isEmpty()) {
                 return null;
             }
             
             try {
                 // 解析title格式: "interface in nxopen.features"
                 if (title.contains(" in ")) {
                     String[] parts = title.split(" in ");
                     if (parts.length == 2) {
                         String packageName = parts[1].trim();
                         return packageName + "." + linkText;
                     }
                 }
             } catch (Exception e) {
                 // 解析失败，返回null让调用者尝试其他方法
             }
             
             return null;
         }
         
         /**
          * 从URL中提取完整的类名（包括包名）
          */
         private String extractFullClassNameFromUrl(String url, String linkText) {
            if (url == null || url.isEmpty()) {
                return linkText;
            }
            
            try {
                // 提取包名部分
                String packageName = null;
                
                // 从URL路径中提取包名
                if (url.contains("/")) {
                    String[] pathParts = url.split("/");
                    java.util.List<String> packageParts = new java.util.ArrayList<>();
                    
                    // 查找.html文件之前的路径部分
                    for (int i = 0; i < pathParts.length - 1; i++) {
                        String part = pathParts[i];
                        // 跳过协议、域名等部分，只保留包路径
                        if (!part.isEmpty() && !part.contains(":") && !part.contains(".") 
                            && !part.equals("documentation") && !part.equals("external") 
                            && !part.equals("custom_api") && !part.equals("open_java_ref")) {
                            packageParts.add(part);
                        }
                    }
                    
                    if (!packageParts.isEmpty()) {
                        packageName = String.join(".", packageParts);
                    }
                }
                
                // 如果成功提取到包名，返回完整类名
                if (packageName != null && !packageName.isEmpty()) {
                    return packageName + "." + linkText;
                } else {
                    // 如果无法提取包名，返回类名
                    return linkText;
                }
            } catch (Exception e) {
                // 如果解析失败，返回原始链接文本
                return linkText;
            }
        }
    }

    /**
     * JDK9 构造函数解析配置
     */
    private static class ConstructorParsingConfig implements JavadocParsingConfig.ConstructorParsingConfig {

        @Override
        public String getConstructorSelector() {
            // 构造函数通常在 constructor.summary 表格中
            return "#constructor\\.summary ~ table.memberSummary tr:has(td.colFirst):has(td.colLast)";
        }

        @Override
        public String extractConstructorName(Element constructorElement) {
            // 构造函数名称在第二列
            Element nameElement = constructorElement.select(".colSecond").first();
            if (nameElement != null) {
                String txt = nameElement.text().trim();
                int paramStart = txt.indexOf("(");
                return paramStart > 0 ? txt.substring(0, paramStart) : txt;
            }
            return "";
        }

        @Override
        public String extractDescription(Element constructorElement) {
            // 描述在最后一列
            Element descElement = constructorElement.select(".colLast").first();
            return descElement != null ? descElement.text().trim() : "";
        }

        @Override
        public boolean isValidConstructorElement(Element element) {
            // 验证是否为有效的构造函数元素
            Elements firstCol = element.select(".colFirst");
            Elements secondCol = element.select(".colSecond");
            Elements lastCol = element.select(".colLast");
            return !firstCol.isEmpty() && !secondCol.isEmpty() && !lastCol.isEmpty();
        }
    }
}