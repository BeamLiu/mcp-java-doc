package io.emop.javadocjson.parser;

import io.emop.javadocjson.config.JDK9Dialet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JDK9ParsingConfigTest {

    @Test
    public void testNXOpenConfigApplicability() {
        JDK9Dialet config = new JDK9Dialet();
        
        // 测试适用性检查
        String nxOpenHtml = "<html><head><title>CheckScope (NXOpen Java API Reference)</title></head></html>";
        assertTrue(config.isApplicable(nxOpenHtml));
        
        String otherHtml = "<html><head><title>Some Other API</title></head></html>";
        assertFalse(config.isApplicable(otherHtml));
    }

    @Test
    public void testNXOpenMethodParsing() {
        JDK9Dialet config = new JDK9Dialet();
        
        // 模拟NXOpen HTML结构
        String html = "<html>" +
            "<body>" +
                "<table class=\"memberSummary\">" +
                    "<tr>" +
                        "<td class=\"colFirst\">public static</td>" +
                        "<td class=\"colLast\">" +
                            "<code><span class=\"memberNameLink\"><a href=\"#checkScope-nxopen.Session-\">checkScope</a></span>" +
                            "(nxopen.Session&nbsp;session)</code>" +
                            "<div class=\"block\">Check the scope of the session.</div>" +
                        "</td>" +
                    "</tr>" +
                "</table>" +
            "</body>" +
            "</html>";
        
        Document doc = Jsoup.parse(html);
        JavadocParsingConfig.MethodParsingConfig methodConfig = config.getMethodParsingConfig();
        
        // 测试选择器
        assertNotNull(methodConfig.getMethodSelector());
        
        // 测试元素选择
        Elements elements = doc.select(methodConfig.getMethodSelector());
        assertFalse(elements.isEmpty());
        
        if (!elements.isEmpty()) {
            Element element = elements.first();
            
            // 测试名称提取
            String name = methodConfig.extractMethodName(element);
            assertNotNull(name);
            assertFalse(name.trim().isEmpty());
            
            // 测试修饰符和类型提取
            String modifierAndType = methodConfig.extractModifierAndType(element);
            assertNotNull(modifierAndType);
            
            // 测试原始文本提取
            String rawText = methodConfig.extractDetailText(element, doc);
            assertNotNull(rawText);
            assertFalse(rawText.trim().isEmpty());
        }
    }

    @Test
    public void testNXOpenFieldParsing() {
        JDK9Dialet config = new JDK9Dialet();
        
        // 模拟NXOpen字段HTML结构
        String html = "<html>" +
            "<body>" +
                "<table class=\"memberSummary\">" +
                    "<tr>" +
                        "<td class=\"colFirst\">public static final int</td>" +
                        "<td class=\"colLast\">" +
                            "<code><span class=\"memberNameLink\"><a href=\"#SCOPE_GLOBAL\">SCOPE_GLOBAL</a></span></code>" +
                            "<div class=\"block\">Global scope constant.</div>" +
                        "</td>" +
                    "</tr>" +
                "</table>" +
            "</body>" +
            "</html>";
        
        Document doc = Jsoup.parse(html);
        JavadocParsingConfig.FieldParsingConfig fieldConfig = config.getFieldParsingConfig();
        
        // 测试选择器
        assertNotNull(fieldConfig.getFieldSelector());
        
        // 测试元素选择
        Elements elements = doc.select(fieldConfig.getFieldSelector());
        assertFalse(elements.isEmpty());
        
        if (!elements.isEmpty()) {
            Element element = elements.first();
            
            // 测试名称提取
            String name = fieldConfig.extractFieldName(element);
            assertNotNull(name);
            assertFalse(name.trim().isEmpty());
            
            // 测试修饰符和类型提取
            String modifierAndType = fieldConfig.extractModifierAndType(element);
            assertNotNull(modifierAndType);
            
            // 测试原始文本提取
            String rawText = fieldConfig.extractRawText(element);
            assertNotNull(rawText);
            assertFalse(rawText.trim().isEmpty());
        }
    }
}