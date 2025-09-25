package io.emop.javadocjson.config;

import io.emop.javadocjson.parser.JavadocParsingConfig;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

        @Override
        public String extractDetailText(Element methodElement, Document doc) {
            String methodNameWithParameters = extractMethodNameWithParameters(methodElement);
            // 先选择所有 pre.methodSignature，然后手动过滤
            Elements methodSignatures = doc.select("pre.methodSignature");

            for (Element signature : methodSignatures) {
                if (signature.text().contains(methodNameWithParameters)) {
                    return signature.parent().text();
                }
            }
            return "";
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
            Element titleElement = doc.select("h1.title").first();
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
            // 查找继承信息，通常在 ul 列表中
            Elements inheritanceList = doc.select("ul.inheritance li");
            if (inheritanceList.size() >= 2) {
                // 倒数第二个通常是直接父类
                Element superClassElement = inheritanceList.get(inheritanceList.size() - 2);
                return superClassElement.text().trim();
            }
            return "";
        }

        @Override
        public java.util.List<String> extractInterfaces(Document doc) {
            // 查找实现的接口，通常在 dl dt dd 结构中
            java.util.List<String> interfaces = new java.util.ArrayList<>();
            Elements dlElements = doc.select("dl");
            for (Element dl : dlElements) {
                Element dt = dl.select("dt").first();
                if (dt != null && dt.text().contains("All Implemented Interfaces")) {
                    Element dd = dl.select("dd").first();
                    if (dd != null) {
                        String interfaceText = dd.text().trim();
                        // 分割多个接口（通常用逗号分隔）
                        String[] interfaceArray = interfaceText.split(",");
                        for (String iface : interfaceArray) {
                            interfaces.add(iface.trim());
                        }
                    }
                }
            }
            return interfaces;
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