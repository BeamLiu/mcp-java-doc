# Configuration Examples

This document provides various configuration examples for the Javadoc JSON Plugin.

## Basic Configuration

### Minimal Publish Goal
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
        <!-- Jackson databind includes core and annotations transitively -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.2</version>
        </dependency>
        <!-- JSR310 support for LocalDateTime, etc. -->
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>2.15.2</version>
        </dependency>
        <!-- HTML parsing -->
        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>1.16.1</version>
        </dependency>
    </dependencies>
</plugin>
```

### Minimal Crawl Goal
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
    <!-- Required dependencies -->
    <dependencies>
        <!-- Jackson databind includes core and annotations transitively -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.2</version>
        </dependency>
        <!-- JSR310 support for LocalDateTime, etc. -->
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>2.15.2</version>
        </dependency>
        <!-- HTML parsing -->
        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>1.16.1</version>
        </dependency>
    </dependencies>
</plugin>
```

## Advanced Configurations

### Complete Publish Configuration
```xml
<plugin>
    <groupId>io.emop</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <id>generate-javadoc-json</id>
            <phase>package</phase>
            <goals>
                <goal>publish</goal>
            </goals>
            <configuration>
                <!-- Output file path -->
                <outputFile>${project.build.directory}/docs/javadoc.json</outputFile>
                
                <!-- Source directory (default: src/main/java) -->
                <sourceDirectory>${project.build.sourceDirectory}</sourceDirectory>
                
                <!-- Include private members -->
                <includePrivate>true</includePrivate>
                
                <!-- Source encoding -->
                <encoding>UTF-8</encoding>
                
                <!-- MCP compatibility mode -->
                <mcpCompatible>true</mcpCompatible>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Complete Crawl Configuration
```xml
<plugin>
    <groupId>io.emop</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <id>crawl-external-docs</id>
            <phase>generate-resources</phase>
            <goals>
                <goal>crawl</goal>
            </goals>
            <configuration>
                <!-- Base URL of the Javadoc website -->
                <baseUrl>https://docs.oracle.com/en/java/javase/17/docs/api/</baseUrl>
                
                <!-- Output file path -->
                <outputFile>${project.build.directory}/external-docs.json</outputFile>
                
                <!-- Maximum crawling depth -->
                <maxDepth>3</maxDepth>
                
                <!-- Package filters (only crawl specific packages) -->
                <packageFilters>
                    <packageFilter>java.lang</packageFilter>
                    <packageFilter>java.util</packageFilter>
                    <packageFilter>java.io</packageFilter>
                </packageFilters>
                
                <!-- Enable caching for improved performance -->
                <enableCache>true</enableCache>
                
                <!-- Thread pool size for concurrent crawling -->
                <threadPoolSize>4</threadPoolSize>
                
                <!-- Custom user agent -->
                <userAgent>MyProject-DocCrawler/1.0</userAgent>
                
                <!-- Request timeout in milliseconds -->
                <timeout>60000</timeout>
                
                <!-- MCP compatibility mode -->
                <mcpCompatible>true</mcpCompatible>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Multiple Executions

### Generate Both Local and External Documentation
```xml
<plugin>
    <groupId>io.emop</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>1.0.0</version>
    <executions>
        <!-- Local project documentation -->
        <execution>
            <id>local-docs</id>
            <phase>package</phase>
            <goals>
                <goal>publish</goal>
            </goals>
            <configuration>
                <outputFile>${project.build.directory}/local-javadoc.json</outputFile>
                <includePrivate>true</includePrivate>
            </configuration>
        </execution>
        
        <!-- External library documentation -->
        <execution>
            <id>spring-docs</id>
            <phase>package</phase>
            <goals>
                <goal>crawl</goal>
            </goals>
            <configuration>
                <baseUrl>https://docs.spring.io/spring-framework/docs/current/javadoc-api/</baseUrl>
                <outputFile>${project.build.directory}/spring-javadoc.json</outputFile>
                <maxDepth>2</maxDepth>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Command Line Usage

### Run Publish Goal
```bash
mvn io.emop:java-docs-json-doclet:1.0.0:publish
```

### Run Crawl Goal
```bash
mvn io.emop:java-docs-json-doclet:1.0.0:crawl -DbaseUrl=https://docs.sw.siemens.com/documentation/external/PL20231101866122454/en-US/custom_api/open_java_ref/
```

### With Custom Parameters
```bash
mvn io.emop:java-docs-json-doclet:1.0.0:publish \
    -DoutputFile=target/my-docs.json \
    -DincludePrivate=true \
    -DmcpCompatible=false
```

### Test Commands (Using example-pom.xml)

#### Publish Goal
```bash
# Basic publish with default configuration
mvn -f examples/example-pom.xml io.emop:java-docs-json-doclet:1.0.0:publish

# Publish with simple test execution (uses test sources)
mvn -f examples/example-pom.xml io.emop:java-docs-json-doclet:1.0.0:publish@simple-test

# Publish with custom parameters
mvn -f examples/example-pom.xml io.emop:java-docs-json-doclet:1.0.0:publish -DoutputFile=custom-output.json -DsourceDir=src/main/java -DincludePrivate=false
```

#### Crawl Goal
```bash
# Crawl external Javadoc
mvn -f examples/example-pom.xml io.emop:java-docs-json-doclet:1.0.0:crawl -DbaseUrl='https://docs.sw.siemens.com/documentation/external/PL20231101866122454/en-US/custom_api/open_java_ref/' '-DoutputFile=external-docs.json'
```

## Profile-Based Configuration

### Development Profile
```xml
<profiles>
    <profile>
        <id>dev</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>io.emop</groupId>
                    <artifactId>java-docs-json-doclet</artifactId>
                    <version>1.0.0</version>
                    <executions>
                        <execution>
                            <goals>
                                <goal>publish</goal>
                            </goals>
                            <configuration>
                                <includePrivate>true</includePrivate>
                                <outputFile>${project.build.directory}/dev-docs.json</outputFile>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

### Production Profile
```xml
<profiles>
    <profile>
        <id>prod</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>io.emop</groupId>
                    <artifactId>java-docs-json-doclet</artifactId>
                    <version>1.0.0</version>
                    <executions>
                        <execution>
                            <goals>
                                <goal>publish</goal>
                            </goals>
                            <configuration>
                                <includePrivate>false</includePrivate>
                                <outputFile>${project.build.directory}/public-docs.json</outputFile>
                                <mcpCompatible>true</mcpCompatible>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

## Integration with Other Plugins

### With Maven Javadoc Plugin
```xml
<plugins>
    <!-- Standard Javadoc Plugin -->
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-javadoc-plugin</artifactId>
        <version>3.4.1</version>
        <executions>
            <execution>
                <goals>
                    <goal>javadoc</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
    
    <!-- Javadoc JSON Plugin -->
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
    </plugin>
</plugins>
```

## Important Notes

### Plugin Dependencies
**IMPORTANT**: When using the plugin, you must include the required dependencies in the plugin configuration to avoid `ClassNotFoundException`. Add the following dependencies to your plugin configuration:

```xml
<plugin>
    <groupId>io.emop</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>1.0.0</version>
    <configuration>
        <!-- your configuration -->
    </configuration>
    <dependencies>
        <!-- Jackson databind includes core and annotations transitively -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.2</version>
        </dependency>
        <!-- JSR310 support for LocalDateTime, etc. -->
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>2.15.2</version>
        </dependency>
        <!-- HTML parsing -->
        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>1.16.1</version>
        </dependency>
    </dependencies>
</plugin>
```

## Parameter Reference

### Publish Goal Parameters
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `outputFile` | File | `${project.build.directory}/javadoc.json` | Output JSON file path |
| `sourceDirectory` | File | `${project.build.sourceDirectory}` | Source directory to scan |
| `includePrivate` | boolean | `false` | Include private members |
| `encoding` | String | `UTF-8` | Source file encoding |
| `mcpCompatible` | boolean | `true` | Generate MCP-compatible format |

### Crawl Goal Parameters
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `baseUrl` | String | (required) | Base URL of Javadoc website |
| `outputFile` | File | `${project.build.directory}/crawled-javadoc.json` | Output JSON file path |
| `maxDepth` | int | `5` | Maximum crawling depth |
| `packageFilters` | List<String> | (none) | List of package names to crawl (filters out other packages) |
| `enableCache` | boolean | `false` | Enable temporary caching for improved performance |
| `threadPoolSize` | int | `2` | Number of threads for concurrent crawling |
| `userAgent` | String | `Javadoc-JSON-Plugin/1.0` | HTTP User-Agent header |
| `timeout` | int | `30000` | Request timeout in milliseconds |
| `mcpCompatible` | boolean | `true` | Generate MCP-compatible format |