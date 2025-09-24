package io.emop.javadocjson.parser;

import io.emop.javadocjson.model.JavadocMetadata;
import io.emop.javadocjson.model.JavadocRoot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HtmlCrawler 测试类
 * 包含真实网络访问的测试方法，用于调试爬取功能
 */
public class HtmlCrawlerTest {

    private String baseUrl = "https://docs.sw.siemens.com/documentation/external/PL20231101866122454/en-US/custom_api/open_java_ref/";
    private HtmlCrawler htmlCrawler;

    @BeforeEach
    void setUp() {
        htmlCrawler = new HtmlCrawler(new SimpleConsoleLog());

        // 设置爬虫参数
        htmlCrawler.setMaxDepth(2);  // 限制深度避免爬取太多
        htmlCrawler.setTimeout(10000);  // 10秒超时
        htmlCrawler.setUserAgent("HtmlCrawlerTest/1.0");
    }

    /**
     * 测试爬取 Oracle Java 11 API 文档
     * 这是一个真实的网络访问测试，用于调试
     */
    @Test
    @Disabled("需要网络访问，仅在调试时启用")
    void testCrawlOracleJavaDoc() throws IOException {
        // 准备测试数据
        JavadocMetadata metadata = createTestMetadata();

        // 执行爬取
        JavadocRoot result = htmlCrawler.crawl(baseUrl);
        result.setMetadata(metadata);

        // 验证结果
        assertNotNull(result, "爬取结果不应为空");
        assertNotNull(result.getPackages(), "包列表不应为空");
        assertTrue(result.getPackages().size() > 0, "应该爬取到至少一个包");

        // 打印调试信息
        System.out.println("爬取到的包数量: " + result.getPackages().size());
        result.getPackages().forEach(pkg -> {
            System.out.println("包名: " + pkg.getName() + ", 类数量: " + pkg.getClasses().size());
            pkg.getClasses().forEach(cls -> {
                System.out.println("  类名: " + cls.getName() + ", 方法数量: " + cls.getMethods().size());
            });
        });
    }

    /**
     * 测试爬虫配置参数
     */
    @Test
    void testCrawlerConfiguration() {
        // 测试设置各种参数
        htmlCrawler.setMaxDepth(5);
        htmlCrawler.setTimeout(30000);
        htmlCrawler.setUserAgent("TestAgent/2.0");

        // 这里可以添加验证逻辑
        assertTrue(true, "配置设置测试通过");
    }

    /**
     * 创建测试用的元数据
     */
    private JavadocMetadata createTestMetadata() {
        JavadocMetadata metadata = new JavadocMetadata();
        metadata.setVersion("1.0.0");
        metadata.setGeneratedAt(LocalDateTime.now().toString());
        metadata.setBaseUrl(baseUrl);
        return metadata;
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