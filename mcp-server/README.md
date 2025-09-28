# JavaDoc MCP Server / JavaDoc MCP 服务器

[English](#english) | [中文](#中文)

---

## English

A simple MCP (Model Context Protocol) server for searching Java documentation JSON files.

### Features

- Search classes, methods, constructors, and fields
- Automatic data deduplication and merging
- Fuzzy search powered by Fuse.js
- Multiple search modes (global search, class search, method search)
- Support for both detailed and simplified JavaDoc data structures

### Installation and Setup

1. Install dependencies:
```bash
npm install
```

2. Build the project:
```bash
npm run build
```

3. Run the server:
```bash
npm start
```

Or run in development mode:
```bash
npm run dev
```

### Configuration

The server uses the `JAVADOC_JSON_PATH` environment variable to locate JavaDoc JSON files. If not set, it defaults to `../tmp/javadoc-json/`.

You can set this environment variable in your system or in the Claude Desktop configuration.

### Available Tools

#### 1. search_all
Search all types of items (classes, methods, constructors, fields)

Parameters:
- `query` (string): Search query
- `limit` (number, optional): Maximum number of results, default 10

#### 2. search_classes
Search specifically for classes and interfaces

Parameters:
- `query` (string): Class name or description
- `limit` (number, optional): Maximum number of results, default 10

#### 3. search_methods
Search specifically for methods and constructors

Parameters:
- `query` (string): Method name or signature
- `className` (string, optional): Filter by class name
- `limit` (number, optional): Maximum number of results, default 10

#### 4. get_class_details
Get detailed information about a specific class

Parameters:
- `className` (string): Full class name

#### 5. get_stats
Get statistics about loaded JavaDoc data

### Data Source

The server loads JavaDoc JSON files from a single directory specified by the `JAVADOC_JSON_PATH` environment variable. If multiple files contain the same class, the server automatically deduplicates them, keeping the version with more detailed information.

### Testing

Run the test script to verify the server is working correctly:

```bash
node test-server.js
```

### Claude Desktop Configuration

1. Locate your Claude Desktop configuration file:
   - Windows: `%APPDATA%\Claude\claude_desktop_config.json`
   - macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
   - Linux: `~/.config/Claude/claude_desktop_config.json`

2. Add the following to your configuration file:

```json
{
  "mcpServers": {
    "javadoc-search": {
      "command": "node",
      "args": ["d:\\workspace\\mcp-java-doc\\mcp-server\\dist\\index.js"],
      "env": {
        "JAVADOC_JSON_PATH": "d:\\workspace\\mcp-java-doc\\tmp\\javadoc-json"
      }
    }
  }
}
```

3. Restart Claude Desktop

### Usage Examples

After configuration, you can use the following commands in Claude Desktop:

- Search all content: `search_all("TestClass")`
- Search classes: `search_classes("ADAS")`
- Search methods: `search_methods("getName")`
- Get class details: `get_class_details("com.example.TestClass")`
- Get statistics: `get_stats()`

### Troubleshooting

If you encounter issues:

1. Ensure Node.js version >= 18
2. Check that the JavaDoc JSON directory exists and contains JSON files
3. Run `node test-server.js` to check server status
4. Check Claude Desktop log files
5. Verify the `JAVADOC_JSON_PATH` environment variable is set correctly

---

## 中文

一个简单的 MCP (Model Context Protocol) 服务器，用于搜索 Java 文档 JSON 文件。

### 功能特性

- 支持搜索类、方法、构造函数和字段
- 自动数据去重和合并
- 使用 Fuse.js 提供模糊搜索功能
- 支持多种搜索模式（全局搜索、类搜索、方法搜索）
- 支持详细和简化的 JavaDoc 数据结构

### 安装和设置

1. 安装依赖：
```bash
npm install
```

2. 构建项目：
```bash
npm run build
```

3. 运行服务器：
```bash
npm start
```

或者直接运行开发模式：
```bash
npm run dev
```

### 配置

服务器使用 `JAVADOC_JSON_PATH` 环境变量来定位 JavaDoc JSON 文件。如果未设置，默认为 `../tmp/javadoc-json/`。

您可以在系统中设置此环境变量，或在 Claude Desktop 配置中设置。

### 可用工具

#### 1. search_all
搜索所有类型的项目（类、方法、构造函数、字段）

参数：
- `query` (string): 搜索查询
- `limit` (number, 可选): 最大结果数量，默认 10

#### 2. search_classes
专门搜索类和接口

参数：
- `query` (string): 类名或描述
- `limit` (number, 可选): 最大结果数量，默认 10

#### 3. search_methods
专门搜索方法和构造函数

参数：
- `query` (string): 方法名或签名
- `className` (string, 可选): 按类名过滤
- `limit` (number, 可选): 最大结果数量，默认 10

#### 4. get_class_details
获取特定类的详细信息

参数：
- `className` (string): 完整类名

#### 5. get_stats
获取加载的 JavaDoc 数据统计信息

### 数据源

服务器从 `JAVADOC_JSON_PATH` 环境变量指定的单个目录加载 JavaDoc JSON 文件。如果多个文件包含相同的类，服务器会自动去重，保留信息更详细的版本。

### 测试服务器

运行测试脚本来验证服务器是否正常工作：

```bash
node test-server.js
```

### 配置 Claude Desktop

1. 找到 Claude Desktop 的配置文件：
   - Windows: `%APPDATA%\Claude\claude_desktop_config.json`
   - macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
   - Linux: `~/.config/Claude/claude_desktop_config.json`

2. 在配置文件中添加：

```json
{
  "mcpServers": {
    "javadoc-search": {
      "command": "node",
      "args": ["d:\\workspace\\mcp-java-doc\\mcp-server\\dist\\index.js"],
      "env": {
        "JAVADOC_JSON_PATH": "d:\\workspace\\mcp-java-doc\\tmp\\javadoc-json"
      }
    }
  }
}
```

3. 重启 Claude Desktop

### 使用示例

配置完成后，你可以在 Claude Desktop 中使用以下命令：

- 搜索所有内容：`search_all("TestClass")`
- 搜索类：`search_classes("ADAS")`
- 搜索方法：`search_methods("getName")`
- 获取类详情：`get_class_details("com.example.TestClass")`
- 获取统计信息：`get_stats()`

### 故障排除

如果遇到问题：

1. 确保 Node.js 版本 >= 18
2. 检查 JavaDoc JSON 目录是否存在并包含 JSON 文件
3. 运行 `node test-server.js` 检查服务器状态
4. 查看 Claude Desktop 的日志文件
5. 验证 `JAVADOC_JSON_PATH` 环境变量是否正确设置