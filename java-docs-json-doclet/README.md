# Javadoc JSON Plugin / Javadoc JSON 插件

[English](#english) | [中文](#中文)

---

## English

A Maven plugin that generates JSON documentation from Java source code and crawls external Javadoc websites to extract API documentation in JSON format.

### Features

- **Generate Javadoc Json Goal**: Generate JSON documentation from your project's source code
- **Crawl Goal**: Extract documentation from external Javadoc websites
- **Package Filtering**: Use regular expressions to filter specific packages
- **Proxy Support**: Configure proxy settings for crawling external documentation
- **Multiple Output Formats**: Support for both individual class files and consolidated JSON

### Quick Start

#### Basic Configuration
```xml
<plugin>
    <groupId>io.github.beamliu</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>0.1.1</version>
</plugin>
```

ATTENTION: add central plugin repository *ONLY* when you cannot retrieve the artifacts when using some minior sites:
```xml
<pluginRepositories>
    <pluginRepository>
        <id>central-plugins</id>
        <url>https://repo.maven.apache.org/maven2</url>
        <releases><enabled>true</enabled></releases>
        <snapshots><enabled>false</enabled></snapshots>
    </pluginRepository>
</pluginRepositories>
```

ATTENTION: if you encountered compile error due to `Lombok` plugin, please follow up [Working with Lombok](#working-with-lombok)

#### Basic Crawl Configuration
```xml
<plugin>
    <groupId>io.github.beamliu</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>0.1.1</version>
    <configuration>
        <baseUrl>https://docs.sw.siemens.com/documentation/external/PL20231101866122454/en-US/custom_api/open_java_ref/</baseUrl>
        <!-- optional to filter packages -->
        <packageFilters>
            <packageFilter>nxopen\.issue</packageFilter>
        </packageFilters>
    </configuration>
</plugin>
```

ATTENTION: Only tested with above sample javadoc site, please build from source to update `io.emop.javadocjson.config.JDK9Dialet` to meet your real javadoc site, will support the offical LTS java version doc later.

### Command Line Usage

```bash
# Generate documentation from source code
mvn javadoc-json:javadoc-json

# Crawl external Javadoc
mvn javadoc-json:crawl

# With custom parameters
mvn javadoc-json:javadoc-json \
    -DoutputDirectory=target/my-docs \
    -DincludePrivate=true \
    -DsourceDirectory=src/main/java \
    -Dencoding=GBK
```

### Configuration Parameters

#### Publish Goal Parameters
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `outputDirectory` | String | `javadoc-output` | Output directory for JSON files, default to `target/javadoc-json` |
| `sourceDirectory` | String | `${project.build.sourceDirectory}` | Source directory to process |
| `includePrivate` | boolean | `false` | Include private members in the output |
| `sourcePaths` | List<String> | (none) | Additional source paths to include |
| `encoding` | String | `UTF-8` | Source file encoding |

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
<configuration>
    <packageFilters>
        <packageFilter>java\.lang.*</packageFilter>
        <packageFilter>java\.util.*</packageFilter>
        <packageFilter>.*\.concurrent</packageFilter>
    </packageFilters>
</configuration>
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

#### Working with Lombok

When your project uses Lombok annotations, you need to use the delombok process to generate regular Java code before generating documentation. Lombok's auto-generated methods (getters, setters, constructors, etc.) won't appear in Javadoc without this step.

**Complete Lombok Configuration Example:**

```xml
<build>
    <plugins>
        <!-- Lombok Maven Plugin for delombok -->
        <plugin>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok-maven-plugin</artifactId>
            <version>1.18.20.0</version>
            <executions>
                <execution>
                    <id>delombok</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>delombok</goal>
                    </goals>
                    <configuration>
                        <sourceDirectory>src/main/java</sourceDirectory>
                        <outputDirectory>target/generated-sources/delombok</outputDirectory>
                        <addOutputDirectory>false</addOutputDirectory>
                        <encoding>UTF-8</encoding>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        
        <!-- Java Docs JSON Doclet Plugin -->
        <plugin>
            <groupId>io.github.beamliu</groupId>
            <artifactId>java-docs-json-doclet</artifactId>
            <version>0.1.1</version>
            <configuration>
                <!-- Point to delombok generated sources -->
                <sourceDirectory>target/generated-sources/delombok</sourceDirectory>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Execution Steps:**

When using Lombok, you must run the build in the correct order:

```bash
# Run delombok first, then generate docs
mvn clean compile
# target/generated-sources/delombok will conatin the delomboked java source code
mvn package
mvn javadoc-json:javadoc-json
```

**Key Points:**
- The delombok process runs in the `generate-sources` phase
- Generated sources are placed in `target/generated-sources/delombok`
- The JSON doclet plugin's `sourceDirectory` parameter points to the delombok output
- **Important**: You must compile your project first (or run `mvn compile`) before delombok can work, because delombok needs the Lombok library on the classpath
- This ensures all Lombok-generated methods appear in the documentation

**Troubleshooting:**

If you get "cannot find symbol" errors for Lombok-generated classes (like `*Builder`):
1. Make sure Lombok is in your dependencies
2. Run `mvn clean compile` first to ensure Lombok is available
3. Then run `mvn lombok:delombok` to generate the delomboked sources
4. Finally run the doclet plugin to generate JSON documentation

---

## 中文

一个 Maven 插件，可以从 Java 源代码生成 JSON 文档，并爬取外部 Javadoc 网站以提取 JSON 格式的 API 文档。

### 功能特性

- **生成 Javadoc Json 目标**: 从项目源代码生成 JSON 文档
- **爬取目标**: 从外部 Javadoc 网站提取文档
- **包过滤**: 使用正则表达式过滤特定包
- **代理支持**: 为爬取外部文档配置代理设置
- **多种输出格式**: 支持单个类文件和合并 JSON 格式

### 快速开始

#### 基本配置
```xml
<plugin>
    <groupId>io.github.beamliu</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>0.1.1</version>
</plugin>
```

注意：仅当您在某些小型站点无法检索到构件时，才添加中央插件仓库：
```xml
<pluginRepositories>
    <pluginRepository>
        <id>central-plugins</id>
        <url>https://repo.maven.apache.org/maven2</url>
        <releases><enabled>true</enabled></releases>
        <snapshots><enabled>false</enabled></snapshots>
    </pluginRepository>
</pluginRepositories>
```

注意：如果您因 `Lombok` 插件遇到编译错误，请参考[使用 Lombok](#使用-lombok)

#### 基本爬取配置
```xml
<plugin>
    <groupId>io.github.beamliu</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>0.1.1</version>
    <configuration>
        <baseUrl>https://docs.sw.siemens.com/documentation/external/PL20231101866122454/en-US/custom_api/open_java_ref/</baseUrl>
        <!-- 可选的包过滤器 -->
        <packageFilters>
            <packageFilter>nxopen\.issue</packageFilter>
        </packageFilters>
    </configuration>
</plugin>
```

注意：仅在上述示例 javadoc 站点上测试过，请从源代码构建并更新 `io.emop.javadocjson.config.JDK9Dialet` 以适配您的实际 javadoc 站点，稍后将支持官方 LTS Java 版本文档。

### 命令行使用

```bash
# 从源代码生成文档
mvn javadoc-json:javadoc-json

# 爬取外部 Javadoc
mvn javadoc-json:crawl

# 使用自定义参数
mvn javadoc-json:javadoc-json \
    -DoutputDirectory=target/my-docs \
    -DincludePrivate=true \
    -DsourceDirectory=src/main/java \
    -Dencoding=GBK
```

### 配置参数

#### 发布目标参数
| 参数 | 类型 | 默认值 | 描述 |
|-----------|------|---------|-------------|
| `outputDirectory` | String | `javadoc-output` | JSON 文件输出目录，默认为 `target/javadoc-json` |
| `sourceDirectory` | String | `${project.build.sourceDirectory}` | 要处理的源代码目录 |
| `includePrivate` | boolean | `false` | 在输出中包含私有成员 |
| `sourcePaths` | List<String> | (无) | 要包含的额外源代码路径 |
| `encoding` | String | `UTF-8` | 源文件编码 |

#### 爬取目标参数
| 参数 | 类型 | 默认值 | 描述 |
|-----------|------|---------|-------------|
| `baseUrl` | String | (必需) | Javadoc 网站的基础 URL |
| `outputDirectory` | File | `${project.build.directory}/javadocs` | JSON 文件输出目录 |
| `packageFilters` | Set<String> | (无) | 用于过滤包的正则表达式模式 |
| `userAgent` | String | `JavaDocCrawler/1.0` | HTTP User-Agent 头 |
| `timeout` | int | `30000` | 请求超时时间（毫秒） |
| `mcpCompatible` | boolean | `true` | 生成 MCP 兼容格式 |

### 高级功能

#### 使用正则表达式进行包过滤
```xml
<configuration>
    <packageFilters>
        <packageFilter>java\.lang.*</packageFilter>
        <packageFilter>java\.util.*</packageFilter>
        <packageFilter>.*\.concurrent</packageFilter>
    </packageFilters>
</configuration>
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

#### 使用 Lombok

当您的项目使用 Lombok 注解时，需要使用 delombok 过程来生成常规的 Java 代码，然后再生成文档。如果不进行这一步，Lombok 自动生成的方法（getter、setter、构造函数等）不会出现在 Javadoc 中。

**完整的 Lombok 配置示例：**

```xml
<build>
    <plugins>
        <!-- 用于 delombok 的 Lombok Maven 插件 -->
        <plugin>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok-maven-plugin</artifactId>
            <version>1.18.20.0</version>
            <executions>
                <execution>
                    <id>delombok</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>delombok</goal>
                    </goals>
                    <configuration>
                        <sourceDirectory>src/main/java</sourceDirectory>
                        <outputDirectory>target/generated-sources/delombok</outputDirectory>
                        <addOutputDirectory>false</addOutputDirectory>
                        <encoding>UTF-8</encoding>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        
        <!-- Java Docs JSON Doclet 插件 -->
        <plugin>
            <groupId>io.github.beamliu</groupId>
            <artifactId>java-docs-json-doclet</artifactId>
            <version>0.1.1</version>
            <configuration>
                <!-- 指向 delombok 生成的源代码 -->
                <sourceDirectory>target/generated-sources/delombok</sourceDirectory>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**执行步骤：**

使用 Lombok 时，必须按正确的顺序运行构建：

```bash
# 运行 delombok，然后生成文档
mvn clean compile
# 运行完应该在target/generated-sources/delombok中看到delombok处理完成后的java源代码
mvn package
mvn javadoc-json:javadoc-json
```

**关键要点：**
- delombok 过程在 `generate-sources` 阶段运行
- 生成的源代码放置在 `target/generated-sources/delombok` 目录中
- JSON doclet 插件的 `sourceDirectory` 参数指向 delombok 的输出目录
- **重要**：您必须先编译项目（或运行 `mvn compile`），然后 delombok 才能工作，因为 delombok 需要 Lombok 库在类路径上
- 这确保所有 Lombok 生成的方法都会出现在文档中

**故障排除：**

如果您遇到 Lombok 生成的类（如 `*Builder`）的"找不到符号"错误：
1. 确保 Lombok 在您的依赖项中
2. 首先运行 `mvn clean compile` 以确保 Lombok 可用
3. 然后运行 `mvn lombok:delombok` 生成 delombok 后的源代码
4. 最后运行 doclet 插件生成 JSON 文档
