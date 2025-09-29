# Javadoc JSON Plugin / Javadoc JSON 插件

[English](#english) | [中文](#中文)

---

## English

A Maven plugin that generates JSON documentation from Java source code and crawls external Javadoc websites to extract API documentation in JSON format.

### Features

- **Publish Goal**: Generate JSON documentation from your project's source code
- **Crawl Goal**: Extract documentation from external Javadoc websites
- **MCP Compatible**: Generate documentation in MCP (Model Context Protocol) compatible format
- **Package Filtering**: Use regular expressions to filter specific packages
- **Proxy Support**: Configure proxy settings for crawling external documentation
- **Multiple Output Formats**: Support for both individual class files and consolidated JSON

### Quick Start

#### Basic Publish Configuration
```xml
<plugin>
    <groupId>io.emop</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <goals>
                <goal>publish</goal>
            </goals>
        </execution>
    </executions>
    <!-- Required dependencies -->
    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.2</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>2.15.2</version>
        </dependency>
        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>1.16.1</version>
        </dependency>
    </dependencies>
</plugin>
```

#### Basic Crawl Configuration
```xml
<plugin>
    <groupId>io.emop</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <goals>
                <goal>crawl</goal>
            </goals>
            <configuration>
                <baseUrl>https://docs.oracle.com/en/java/javase/11/docs/api/</baseUrl>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Command Line Usage

```bash
# Generate documentation from source code
mvn io.emop:java-docs-json-doclet:1.0.0:publish

# Crawl external Javadoc
mvn io.emop:java-docs-json-doclet:1.0.0:crawl

# With custom parameters
mvn io.emop:java-docs-json-doclet:1.0.0:publish \
    -DoutputDirectory=target/my-docs \
    -DincludePrivate=true \
    -DsourceDirectory=src/main/java
```

### Configuration Parameters

#### Publish Goal Parameters
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `outputDirectory` | String | `javadoc-output` | Output directory for JSON files |
| `sourceDirectory` | String | `${project.build.sourceDirectory}` | Source directory to process |
| `includePrivate` | boolean | `false` | Include private members in the output |
| `sourcePaths` | List<String> | (none) | Additional source paths to include |

#### Crawl Goal Parameters
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `baseUrl` | String | (required) | Base URL of Javadoc website |
| `outputDirectory` | File | `${project.build.directory}/javadocs` | Output directory for JSON files |
| `packageFilters` | Set<String> | (none) | Regular expression patterns to filter packages |
| `userAgent` | String | `JavaDocCrawler/1.0` | HTTP User-Agent header |
| `timeout` | int | `30000` | Request timeout in milliseconds |
| `mcpCompatible` | boolean | `true` | Generate MCP-compatible format |

### Advanced Features

#### Package Filtering with Regular Expressions
```xml
<packageFilters>
    <packageFilter>java\.lang.*</packageFilter>
    <packageFilter>java\.util.*</packageFilter>
    <packageFilter>.*\.concurrent</packageFilter>
</packageFilters>
```

#### Proxy Configuration
```xml
<configuration>
    <proxyHost>proxy.company.com</proxyHost>
    <proxyPort>8080</proxyPort>
    <proxyUsername>username</proxyUsername>
    <proxyPassword>password</proxyPassword>
</configuration>
```

---

## 中文

一个 Maven 插件，可以从 Java 源代码生成 JSON 文档，并爬取外部 Javadoc 网站以提取 JSON 格式的 API 文档。

### 功能特性

- **发布目标**: 从项目源代码生成 JSON 文档
- **爬取目标**: 从外部 Javadoc 网站提取文档
- **MCP 兼容**: 生成 MCP（模型上下文协议）兼容格式的文档
- **包过滤**: 使用正则表达式过滤特定包
- **代理支持**: 为爬取外部文档配置代理设置
- **多种输出格式**: 支持单个类文件和合并 JSON 格式

### 快速开始

#### 基本发布配置
```xml
<plugin>
    <groupId>io.emop</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <goals>
                <goal>publish</goal>
            </goals>
        </execution>
    </executions>
    <!-- 必需的依赖 -->
    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.2</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>2.15.2</version>
        </dependency>
        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>1.16.1</version>
        </dependency>
    </dependencies>
</plugin>
```

#### 基本爬取配置
```xml
<plugin>
    <groupId>io.emop</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <goals>
                <goal>crawl</goal>
            </goals>
            <configuration>
                <baseUrl>https://docs.oracle.com/en/java/javase/11/docs/api/</baseUrl>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 命令行使用

```bash
# 从源代码生成文档
mvn io.emop:java-docs-json-doclet:1.0.0:publish

# 爬取外部 Javadoc
mvn io.emop:java-docs-json-doclet:1.0.0:crawl

# 使用自定义参数
mvn io.emop:java-docs-json-doclet:1.0.0:publish \
    -DoutputDirectory=target/my-docs \
    -DincludePrivate=true \
    -DsourceDirectory=src/main/java
```

### 配置参数

#### 发布目标参数
| 参数 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `outputDirectory` | String | `javadoc-output` | JSON 文件输出目录 |
| `sourceDirectory` | String | `${project.build.sourceDirectory}` | 要处理的源代码目录 |
| `includePrivate` | boolean | `false` | 在输出中包含私有成员 |
| `sourcePaths` | List<String> | (无) | 要包含的额外源代码路径 |

#### 爬取目标参数
| 参数 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `baseUrl` | String | (必需) | Javadoc 网站的基础 URL |
| `outputDirectory` | File | `${project.build.directory}/javadocs` | JSON 文件输出目录 |
| `packageFilters` | Set<String> | (无) | 用于过滤包的正则表达式模式 |
| `userAgent` | String | `JavaDocCrawler/1.0` | HTTP User-Agent 头 |
| `timeout` | int | `30000` | 请求超时时间（毫秒） |
| `mcpCompatible` | boolean | `true` | 生成 MCP 兼容格式 |

### 高级功能

#### 使用正则表达式进行包过滤
```xml
<packageFilters>
    <packageFilter>java\.lang.*</packageFilter>
    <packageFilter>java\.util.*</packageFilter>
    <packageFilter>.*\.concurrent</packageFilter>
</packageFilters>
```

#### 代理配置
```xml
<configuration>
    <proxyHost>proxy.company.com</proxyHost>
    <proxyPort>8080</proxyPort>
    <proxyUsername>username</proxyUsername>
    <proxyPassword>password</proxyPassword>
</configuration>
```

### 重要说明

**重要**: 使用插件时，必须在插件配置中包含必需的依赖项以避免 `ClassNotFoundException`。请将以下依赖项添加到您的插件配置中：

- Jackson Databind (包含 core 和 annotations)
- Jackson JSR310 (支持 LocalDateTime 等)
- JSoup (HTML 解析)

### 示例项目

查看 `examples` 目录中的示例项目，了解如何配置和使用此插件。

### 许可证

本项目采用 MIT 许可证。

### 贡献

欢迎提交 Issue 和 Pull Request！