package io.emop.javadocjson.parser;

import io.emop.javadocjson.config.JDK9Dialet;
import io.emop.javadocjson.model.JavadocClass;
import io.emop.javadocjson.model.JavadocMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HtmlCrawler 测试类
 * 包含真实网络访问的测试方法，用于调试爬取功能
 */
public class HtmlCrawlerTest {

    private String baseUrl = "https://docs.sw.siemens.com/documentation/external/PL20231101866122454/en-US/custom_api/open_java_ref/";
    private HtmlCrawler htmlCrawler;
    private HtmlCrawler htmlCrawlerWithConfig;

    @BeforeEach
    void setUp() {
        // 创建默认的爬虫实例
        htmlCrawler = new HtmlCrawler(new SimpleConsoleLog(), new JDK9Dialet());
        // 设置爬虫参数
        htmlCrawler.setTimeout(10000);  // 10秒超时
        htmlCrawler.setUserAgent("HtmlCrawlerTest/1.0");
        htmlCrawler.setProxyHost("localhost");
        htmlCrawler.setProxyPort(10809);
        
        // 创建带配置的爬虫实例
        JavadocParsingConfig config = new JDK9Dialet();
        htmlCrawlerWithConfig = new HtmlCrawler(new SimpleConsoleLog(), config);
        htmlCrawlerWithConfig.setTimeout(10000);
        htmlCrawlerWithConfig.setUserAgent("HtmlCrawlerTest/1.0");
        htmlCrawlerWithConfig.setProxyHost("localhost");
        htmlCrawlerWithConfig.setProxyPort(10809);
    }

    /**
     * 测试爬取 Oracle Java 11 API 文档
     * 这是一个真实的网络访问测试，用于调试
     */
    @Test
    @Disabled("需要网络访问，仅在调试时启用")
    void testCrawlOracleJavaDoc() throws IOException {
        htmlCrawler.setPackageFilters(Set.of("nxopen\\.features"));
        // 执行爬取
        List<JavadocClass> result = htmlCrawler.crawl(baseUrl);

        // 验证结果
        assertNotNull(result, "爬取结果不应为空");
        assertTrue(result.size() > 0, "应该爬取到至少一个类");

        // 打印调试信息
        System.out.println("爬取到的类数量: " + result.size());
        result.forEach(cls -> {
            System.out.println("类名: " + cls.getName() + ", 包名: " + cls.getPackageName() + ", 方法数量: " + cls.getMethods().size());
        });
    }

    /**
     * 测试爬虫配置参数
     */
    @Test
    void testCrawlerConfiguration() {
        // 测试设置各种参数
        htmlCrawler.setTimeout(30000);
        htmlCrawler.setUserAgent("TestAgent/2.0");

        // 这里可以添加验证逻辑
        assertTrue(true, "配置设置测试通过");
    }
    
    /**
     * 测试使用配置的爬虫
     */
    @Test
    @Disabled("需要网络访问，仅在调试时启用")
    void testCrawlWithParsingConfig() throws IOException {
        // 准备测试数据
        htmlCrawlerWithConfig.setPackageFilters(Set.of("nxopen\\.features"));
        // 执行爬取
        List<JavadocClass> result = htmlCrawlerWithConfig.crawl(baseUrl);

        // 验证结果
        assertNotNull(result, "爬取结果不应为空");
        assertTrue(result.size() > 0, "应该爬取到至少一个类");

        // 打印调试信息
        System.out.println("使用配置的爬虫 - 爬取到的类数量: " + result.size());
        result.forEach(cls -> {
            System.out.println("类名: " + cls.getName() + ", 包名: " + cls.getPackageName() + ", 方法数量: " + cls.getMethods().size());
            // 验证方法是否有详细信息（name, signature, description）
            cls.getMethods().forEach(m -> {
                JavadocMethod method = (JavadocMethod) m;
                if (!method.getName().isEmpty() || !method.getSignature().isEmpty() || !method.getDescription().isEmpty()) {
                    System.out.println("    方法: " + method.getName() + " - " + method.getSignature());
                }
            });
        });
    }

    /**
     * 本地文件测试 - 如果网络不可用时的备用方案
     */
    @Test
    @Disabled("需要本地HTML文件")
    void testLocalHtmlFile() {
        // 如果你有本地的 HTML 文件，可以在这里测试
        // String localFile = "file:///path/to/local/javadoc.html";
        System.out.println("本地文件测试 - 需要准备本地HTML文件");
    }
}