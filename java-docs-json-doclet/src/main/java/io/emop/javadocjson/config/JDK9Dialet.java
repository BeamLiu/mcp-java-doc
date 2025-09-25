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
}