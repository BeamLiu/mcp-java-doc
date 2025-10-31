# Javadoc JSON Plugin / Javadoc JSON 插件

[English](#english) | [中文](#中文)

---

## English

A Maven plugin that generates JSON documentation from Java source code and crawls external Javadoc websites to extract API documentation in JSON format.

### Features

- **Publish Goal**: Generate JSON documentation from your project's source code
- **Crawl Goal**: Extract documentation from external Javadoc websites
- **Package Filtering**: Use regular expressions to filter specific packages
- **Proxy Support**: Configure proxy settings for crawling external documentation
- **Multiple Output Formats**: Support for both individual class files and consolidated JSON

### Quick Start

#### Basic Publish Configuration
```xml
<plugin>
    <groupId>io.github.beamliu</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>0.1.0</version>
    <executions>
        <execution>
            <goals>
                <goal>publish</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### Basic Crawl Configuration
```xml
<plugin>
    <groupId>io.github.beamliu</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>0.1.0</version>
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
mvn io.github.beamliu:java-docs-json-doclet:0.1.0:publish

# Crawl external Javadoc
mvn io.github.beamliu:java-docs-json-doclet:0.1.0:crawl

# With custom parameters
mvn io.github.beamliu:java-docs-json-doclet:0.1.0:publish \
    -DoutputDirectory=target/my-docs \
    -DincludePrivate=true \
    -DsourceDirectory=src/main/java \
    -Dencoding=GBK
```

### Configuration Parameters

#### Publish Goal Parameters
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `outputDirectory` | String | `javadoc-output` | Output directory for JSON files |
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

#### Working with Lombok

When your project uses Lombok annotations, you need to use the delombok process to generate regular Java code before generating documentation. Lombok's auto-generated methods (getters, setters, constructors, etc.) won't appear in Javadoc without this step.

**Complete Lombok Configuration Example:**

```xml
<dependencies>
    <!-- Lombok dependency -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- Maven Compiler Plugin with Lombok support -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>11</source>
                <target>11</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.30</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
        
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
            <version>0.1.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>publish</goal>
                    </goals>
                    <phase>package</phase>
                    <configuration>
                        <!-- Point to delombok generated sources -->
                        <sourceDirectory>target/generated-sources/delombok</sourceDirectory>
                        <outputDirectory>target/javadoc-json</outputDirectory>
                        <includePrivate>true</includePrivate>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**Execution Steps:**

When using Lombok, you must run the build in the correct order:

```bash
# Option 1: Run the full build (recommended)
mvn clean package

# Option 2: Run delombok first, then generate docs
mvn clean compile
mvn lombok:delombok
mvn io.github.beamliu:java-docs-json-doclet:0.1.0:publish
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
    <groupId>io.github.beamliu</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>0.1.0</version>
    <executions>
        <execution>
            <goals>
                <goal>publish</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### 基本爬取配置
```xml
<plugin>
    <groupId>io.github.beamliu</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>0.1.0</version>
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
mvn io.github.beamliu:java-docs-json-doclet:0.1.0:publish

# 爬取外部 Javadoc
mvn io.github.beamliu:java-docs-json-doclet:0.1.0:crawl

# 使用自定义参数
mvn io.github.beamliu:java-docs-json-doclet:0.1.0:publish \
    -DoutputDirectory=target/my-docs \
    -DincludePrivate=true \
    -DsourceDirectory=src/main/java \
    -Dencoding=GBK
```

### 配置参数

#### 发布目标参数
| 参数 | 类型 | 默认值                                | 描述 |
|------|------|------------------------------------|------|
| `outputDirectory` | String | `javadoc-output`                   | JSON 文件输出目录 |
| `sourceDirectory` | String | `${project.build.sourceDirectory}` | 要处理的源代码目录 |
| `includePrivate` | boolean | `true`                             | 在输出中包含私有成员 |
| `sourcePaths` | List<String> | (无)                                | 要包含的额外源代码路径 |
| `encoding` | String | `UTF-8`                            | 源文件编码 |

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

#### 处理 Lombok

当您的项目使用 Lombok 注解时，需要使用 delombok 过程来生成常规的 Java 代码，然后再生成文档。如果不进行这一步，Lombok 自动生成的方法（getter、setter、构造函数等）不会出现在 Javadoc 中。

**完整的 Lombok 配置示例：**

```xml
<dependencies>
    <!-- Lombok 依赖 -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- 支持 Lombok 的 Maven 编译器插件 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>11</source>
                <target>11</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.30</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
        
        <!-- 用于 delombok 的 Maven Antrun 插件 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-antrun-plugin</artifactId>
            <version>3.1.0</version>
            <executions>
                <execution>
                    <id>delombok</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>run</goal>
                    </goals>
                    <configuration>
                        <target>
                            <mkdir dir="target/generated-sources/delombok"/>
                            <java classname="lombok.launch.Main" fork="true" classpathref="maven.compile.classpath">
                                <arg value="delombok"/>
                                <arg value="src/main/java"/>
                                <arg value="-d"/>
                                <arg value="target/generated-sources/delombok"/>
                                <arg value="--encoding"/>
                                <arg value="UTF-8"/>
                            </java>
                        </target>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        
        <!-- Java Docs JSON Doclet 插件 -->
        <plugin>
            <groupId>io.github.beamliu</groupId>
            <artifactId>java-docs-json-doclet</artifactId>
            <version>0.1.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>publish</goal>
                    </goals>
                    <phase>package</phase>
                    <configuration>
                        <!-- 指向 delombok 生成的源代码 -->
                        <sourceDirectory>target/generated-sources/delombok</sourceDirectory>
                        <outputDirectory>target/javadoc-json</outputDirectory>
                        <includePrivate>true</includePrivate>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**关键要点：**
- delombok 过程在 `generate-sources` 阶段运行
- 生成的源代码放置在 `target/generated-sources/delombok` 目录中
- JSON doclet 插件的 `sourceDirectory` 参数指向 delombok 的输出目录
- 这确保所有 Lombok 生成的方法都会出现在文档中

### 示例项目

查看 `examples` 目录中的示例项目，了解如何配置和使用此插件。

### 许可证

本项目采用 MIT 许可证。
