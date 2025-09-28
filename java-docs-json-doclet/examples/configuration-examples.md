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
                <!-- Output directory for JSON files -->
                <outputDirectory>${project.build.directory}/docs</outputDirectory>
                
                <!-- Source directory (default: src/main/java) -->
                <sourceDirectory>${project.build.sourceDirectory}</sourceDirectory>
                
                <!-- Include private members -->
                <includePrivate>true</includePrivate>
                
                <!-- Additional source paths -->
                <sourcePaths>
                    <sourcePath>src/test/java</sourcePath>
                </sourcePaths>
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
                
                <!-- Output directory for individual class JSON files -->
                <outputDirectory>${project.build.directory}/external-docs</outputDirectory>
                
                <!-- Package filters using regular expressions (only crawl matching packages) -->
                <packageFilters>
                    <packageFilter>java\.lang.*</packageFilter>
                    <packageFilter>java\.util.*</packageFilter>
                    <packageFilter>java\.io.*</packageFilter>
                </packageFilters>
                
                <!-- Custom user agent -->
                <userAgent>MyProject-DocCrawler/1.0</userAgent>
                
                <!-- Request timeout in milliseconds -->
                <timeout>60000</timeout>
                
                <!-- Proxy configuration (optional) -->
                <proxyHost>proxy.company.com</proxyHost>
                <proxyPort>8080</proxyPort>
                <proxyUsername>username</proxyUsername>
                <proxyPassword>password</proxyPassword>
                
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
                <outputDirectory>${project.build.directory}/local-javadoc</outputDirectory>
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
                <outputDirectory>${project.build.directory}/spring-javadoc</outputDirectory>
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
mvn io.emop:java-docs-json-doclet:1.0.0:crawl
```

### With Custom Parameters
```bash
mvn io.emop:java-docs-json-doclet:1.0.0:publish \
    -DoutputDirectory=target/my-docs \
    -DincludePrivate=true \
    -DsourceDirectory=src/main/java
```

### Test Commands (Using example-pom.xml)

#### Publish Goal
```bash
# Basic publish with default configuration
mvn -f examples/example-pom.xml io.emop:java-docs-json-doclet:1.0.0:publish

# Publish with simple test execution (uses test sources)
mvn -f examples/example-pom.xml io.emop:java-docs-json-doclet:1.0.0:publish@simple-test

# Publish with custom parameters
mvn -f examples/example-pom.xml io.emop:java-docs-json-doclet:1.0.0:publish -DoutputDirectory=custom-output -DsourceDirectory=src/main/java -DincludePrivate=false
```

#### Crawl Goal
```bash
# Crawl external Javadoc
mvn -f examples/example-pom.xml io.emop:java-docs-json-doclet:1.0.0:crawl@crawl-external-javadoc
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
                                <outputDirectory>${project.build.directory}/dev-docs</outputDirectory>
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
                                <outputDirectory>${project.build.directory}/public-docs</outputDirectory>
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
| `outputDirectory` | String | `javadoc-output` | Output directory for JSON files |
| `sourceDirectory` | String | `${project.build.sourceDirectory}` | Source directory to process |
| `includePrivate` | boolean | `false` | Include private members in the output |
| `sourcePaths` | List<String> | (none) | Additional source paths to include |
| `classpath` | String | (none) | Classpath for the documentation generation |

### Crawl Goal Parameters
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `baseUrl` | String | (required) | Base URL of Javadoc website |
| `outputDirectory` | File | `${project.build.directory}/javadocs` | Output directory for individual class JSON files |
| `packageFilters` | Set<String> | (none) | Set of regular expression patterns to filter packages (only matching packages will be crawled) |
| `userAgent` | String | `JavaDocCrawler/1.0` | HTTP User-Agent header |
| `timeout` | int | `30000` | Request timeout in milliseconds |
| `proxyHost` | String | (none) | Proxy server hostname or IP address |
| `proxyPort` | int | `8080` | Proxy server port number |
| `proxyUsername` | String | (none) | Proxy authentication username |
| `proxyPassword` | String | (none) | Proxy authentication password |
| `mcpCompatible` | boolean | `true` | Generate MCP-compatible format |

## Package Filtering with Regular Expressions

The `packageFilters` parameter supports regular expression patterns to provide flexible package filtering. This allows you to precisely control which packages are crawled from the Javadoc website.

### Basic Package Filtering Examples
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
                <baseUrl>https://docs.oracle.com/en/java/javase/17/docs/api/</baseUrl>
                <outputFile>${project.build.directory}/filtered-docs.json</outputFile>
                
                <!-- Match packages starting with java.lang -->
                <packageFilters>
                    <packageFilter>java\.lang.*</packageFilter>
                </packageFilters>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Advanced Regular Expression Patterns
```xml
<packageFilters>
    <!-- Match all packages starting with java.util -->
    <packageFilter>java\.util.*</packageFilter>
    
    <!-- Match packages ending with .concurrent -->
    <packageFilter>.*\.concurrent</packageFilter>
    
    <!-- Match exactly java.lang package (no subpackages) -->
    <packageFilter>java\.lang</packageFilter>
    
    <!-- Match multiple specific packages -->
    <packageFilter>java\.(lang|util|io).*</packageFilter>
    
    <!-- Match packages containing 'security' -->
    <packageFilter>.*security.*</packageFilter>
</packageFilters>
```

### Command Line Package Filtering
```bash
# Filter packages using command line
mvn io.emop:java-docs-json-doclet:1.0.0:crawl \
    -DbaseUrl=https://docs.oracle.com/en/java/javase/17/docs/api/ \
    -DpackageFilters=java\.util.*,java\.lang.*
```

### Package Filtering Notes
- Each filter is treated as a regular expression pattern that must match the **full package name**
- Use `\.` to match literal dots in package names (e.g., `java\.lang` not `java.lang`)
- Use `.*` to match any characters (e.g., `java\.util.*` matches `java.util.concurrent`)
- If no filters are specified, all packages will be crawled
- For backward compatibility, if a regex pattern is invalid, it will fall back to `startsWith` matching
- Multiple filters work as OR conditions - a package matching any filter will be included

## Proxy Configuration

When crawling Javadoc websites behind a corporate firewall or through a proxy server, you can configure proxy settings for the crawl goal.

### Basic Proxy Configuration
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
                <baseUrl>https://docs.oracle.com/en/java/javase/17/docs/api/</baseUrl>
                <outputFile>${project.build.directory}/external-docs.json</outputFile>
                
                <!-- Proxy configuration -->
                <proxyHost>proxy.company.com</proxyHost>
                <proxyPort>8080</proxyPort>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Proxy with Authentication
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
                <baseUrl>https://docs.oracle.com/en/java/javase/17/docs/api/</baseUrl>
                <outputFile>${project.build.directory}/external-docs.json</outputFile>
                
                <!-- Proxy configuration with authentication -->
                <proxyHost>proxy.company.com</proxyHost>
                <proxyPort>8080</proxyPort>
                <proxyUsername>your-username</proxyUsername>
                <proxyPassword>your-password</proxyPassword>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Command Line with Proxy
```bash
# Crawl with proxy configuration
mvn io.emop:java-docs-json-doclet:1.0.0:crawl \
    -DbaseUrl=https://docs.oracle.com/en/java/javase/17/docs/api/ \
    -DproxyHost=proxy.company.com \
    -DproxyPort=8080 \
    -DproxyUsername=username \
    -DproxyPassword=password
```

### Proxy Configuration Notes
- Only the `proxyHost` parameter is required to enable proxy support
- If `proxyPort` is not specified, it defaults to 8080
- Authentication parameters (`proxyUsername` and `proxyPassword`) are optional
- Proxy configuration only applies to the `crawl` goal, not the `publish` goal
- For security reasons, consider using environment variables or Maven settings for sensitive proxy credentials