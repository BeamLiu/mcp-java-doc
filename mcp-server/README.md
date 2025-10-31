# JavaDoc MCP Server / JavaDoc MCP 服务器

[English](#english) | [中文](#中文)

---

## English

A Model Context Protocol (MCP) server for searching Java documentation. This server enables AI assistants to search and retrieve Java API documentation from JSON files.

### Prerequisites

Before using this MCP server, you need to generate JavaDoc JSON files using the [java-docs-json-doclet](../java-docs-json-doclet) Maven plugin.

**Quick setup with Maven:**

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
            <configuration>
                <outputDirectory>target/javadoc-json</outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Then run: `mvn clean package`

See the [java-docs-json-doclet README](../java-docs-json-doclet/README.md) for detailed configuration options, including Lombok support.

### Features

- Search classes, methods, constructors, and fields
- Multiple search modes: fuzzy, keyword, and regex
- Automatic data deduplication and merging
- Support for multiple JavaDoc JSON directories

### Installation

```bash
# Using npx (recommended, no installation needed)
npx @io.emop/mcp-javadoc-server --javadoc-path /path/to/javadoc-json

# Or install globally
npm install -g @io.emop/mcp-javadoc-server
mcp-javadoc-server --javadoc-path /path/to/javadoc-json
```

### MCP Client Configuration

Add to your MCP client configuration (e.g., Claude Desktop, Cline):

**Single JavaDoc directory:**
```json
{
  "mcpServers": {
    "javadoc-search": {
      "command": "npx",
      "args": [
        "-y",
        "@io.emop/mcp-javadoc-server",
        "--javadoc-path",
        "/absolute/path/to/javadoc-json"
      ]
    }
  }
}
```

**Multiple JavaDoc directories:**
```json
{
  "mcpServers": {
    "javadoc-search": {
      "command": "npx",
      "args": [
        "-y",
        "@io.emop/mcp-javadoc-server",
        "--javadoc-path",
        "/path/to/project1/javadoc-json",
        "--javadoc-path",
        "/path/to/project2/javadoc-json"
      ]
    }
  }
}
```

**Claude Desktop config locations:**
- Windows: `%APPDATA%\Claude\claude_desktop_config.json`
- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
- Linux: `~/.config/Claude/claude_desktop_config.json`

After editing, restart your MCP client.

### Usage Examples

Once configured, your AI assistant can search your Java documentation:

- `search_all("TestClass")` - Search everything
- `search_classes("UserService")` - Find classes
- `search_methods("getName")` - Find methods
- `search_fields("userId")` - Find fields
- `get_class_details("com.example.User")` - Get full class info
- `get_stats()` - View loaded documentation stats

### Testing with MCP Inspector

You can test the server locally using the MCP Inspector:

```bash
# Test published version (note the -- separator before server arguments)
npx @modelcontextprotocol/inspector npx -y @io.emop/mcp-javadoc-server -- --javadoc-path /path/to/javadoc-json

# Or test local development version
npx @modelcontextprotocol/inspector node dist/index.js -- --javadoc-path /path/to/javadoc-json
```

The inspector will open a web interface (usually http://localhost:6274) where you can:
- View all available tools
- Test tool calls with different parameters
- See real-time request/response data
- Debug any issues

**Important:** The `--` separator is required to pass arguments to your server instead of to npx.

### Troubleshooting

- Ensure Node.js >= 18 is installed
- Verify JavaDoc JSON files exist in the specified path
- Check MCP client logs for connection errors
- Restart your MCP client after configuration changes

---

## 中文

一个用于搜索 Java 文档的模型上下文协议（MCP）服务器。该服务器使 AI 助手能够从 JSON 文件中搜索和检索 Java API 文档。

### 前置要求

使用此 MCP 服务器之前，需要使用 [java-docs-json-doclet](../java-docs-json-doclet) Maven 插件生成 JavaDoc JSON 文件。

**Maven 快速配置：**

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
            <configuration>
                <outputDirectory>target/javadoc-json</outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```

然后运行：`mvn clean package`

详细配置选项（包括 Lombok 支持）请参阅 [java-docs-json-doclet README](../java-docs-json-doclet/README.md)。

### 功能特性

- 搜索类、方法、构造函数和字段
- 多种搜索模式：模糊、关键字和正则表达式
- 自动数据去重和合并
- 支持多个 JavaDoc JSON 目录

### 安装

```bash
# 使用 npx（推荐，无需安装）
npx @io.emop/mcp-javadoc-server --javadoc-path /path/to/javadoc-json

# 或全局安装
npm install -g @io.emop/mcp-javadoc-server
mcp-javadoc-server --javadoc-path /path/to/javadoc-json
```

### MCP 客户端配置

在 MCP 客户端配置中添加（如 Claude Desktop、Cline）：

**单个 JavaDoc 目录：**
```json
{
  "mcpServers": {
    "javadoc-search": {
      "command": "npx",
      "args": [
        "-y",
        "@io.emop/mcp-javadoc-server",
        "--javadoc-path",
        "/绝对路径/到/javadoc-json"
      ]
    }
  }
}
```

**多个 JavaDoc 目录：**
```json
{
  "mcpServers": {
    "javadoc-search": {
      "command": "npx",
      "args": [
        "-y",
        "@io.emop/mcp-javadoc-server",
        "--javadoc-path",
        "/路径/到/project1/javadoc-json",
        "--javadoc-path",
        "/路径/到/project2/javadoc-json"
      ]
    }
  }
}
```

**Claude Desktop 配置文件位置：**
- Windows: `%APPDATA%\Claude\claude_desktop_config.json`
- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
- Linux: `~/.config/Claude/claude_desktop_config.json`

编辑后重启 MCP 客户端。

### 可用工具

#### 1. search_all
搜索所有类型的项目（类、方法、构造函数、字段）

参数：
- `query` (string): 搜索查询
- `limit` (number, 可选): 最大结果数量，默认 10
- `mode` (string, 可选): 搜索模式 - 'fuzzy'（默认）、'keyword' 或 'regex'

#### 2. search_classes
专门搜索类和接口

参数：
- `query` (string): 类名或描述
- `limit` (number, 可选): 最大结果数量，默认 10
- `mode` (string, 可选): 搜索模式 - 'fuzzy'（默认）、'keyword' 或 'regex'

#### 3. search_methods
专门搜索方法和构造函数

参数：
- `query` (string): 方法名或签名
- `className` (string, 可选): 按类名过滤
- `limit` (number, 可选): 最大结果数量，默认 10
- `mode` (string, 可选): 搜索模式 - 'fuzzy'（默认）、'keyword' 或 'regex'

#### 4. search_fields
专门搜索字段和属性

参数：
- `query` (string): 字段名或类型
- `className` (string, 可选): 按类名过滤
- `limit` (number, 可选): 最大结果数量，默认 10
- `mode` (string, 可选): 搜索模式 - 'fuzzy'（默认）、'keyword' 或 'regex'

#### 5. get_class_details
获取特定类的详细信息

参数：
- `className` (string): 完整类名

#### 6. get_stats
获取加载的 JavaDoc 数据统计信息

### 搜索模式

- **模糊**（默认）：智能匹配，支持拼写容错
- **关键字**：精确子字符串匹配
- **正则表达式**：使用正则表达式进行模式匹配

### 使用示例

配置完成后，AI 助手可以搜索你的 Java 文档：

- `search_all("TestClass")` - 搜索所有内容
- `search_classes("UserService")` - 查找类
- `search_methods("getName")` - 查找方法
- `search_fields("userId")` - 查找字段
- `get_class_details("com.example.User")` - 获取完整类信息
- `get_stats()` - 查看已加载的文档统计

### 使用 MCP Inspector 测试

可以使用 MCP Inspector 在本地测试服务器：

```bash
# 测试已发布版本（注意服务器参数前的 -- 分隔符）
npx @modelcontextprotocol/inspector npx -y @io.emop/mcp-javadoc-server -- --javadoc-path /path/to/javadoc-json

# 或测试本地开发版本
npx @modelcontextprotocol/inspector node dist/index.js -- --javadoc-path /path/to/javadoc-json
```

Inspector 会打开一个 Web 界面（通常是 http://localhost:6274），你可以：
- 查看所有可用工具
- 使用不同参数测试工具调用
- 查看实时请求/响应数据
- 调试任何问题

**重要提示：** 必须使用 `--` 分隔符来将参数传递给你的服务器，而不是传递给 npx。

### 故障排除

- 确保已安装 Node.js >= 18
- 验证指定路径中存在 JavaDoc JSON 文件
- 检查 MCP 客户端日志以查看连接错误
- 配置更改后重启 MCP 客户端