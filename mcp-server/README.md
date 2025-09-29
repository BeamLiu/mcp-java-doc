# JavaDoc MCP Server / JavaDoc MCP 服务器

[English](#english) | [中文](#中文)

---

## English

A simple MCP (Model Context Protocol) server for searching Java documentation JSON files.

### Features

- Search classes, methods, constructors, and fields
- Automatic data deduplication and merging
- **Multiple search modes**: fuzzy, keyword, and regex search
- **Advanced search capabilities**: dedicated field search with filtering
- Fuzzy search powered by Fuse.js
- Multiple search types (global search, class search, method search, field search)
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
- `mode` (string, optional): Search mode - 'fuzzy' (default), 'keyword', or 'regex'

#### 2. search_classes
Search specifically for classes and interfaces

Parameters:
- `query` (string): Class name or description
- `limit` (number, optional): Maximum number of results, default 10
- `mode` (string, optional): Search mode - 'fuzzy' (default), 'keyword', or 'regex'

#### 3. search_methods
Search specifically for methods and constructors

Parameters:
- `query` (string): Method name or signature
- `className` (string, optional): Filter by class name
- `limit` (number, optional): Maximum number of results, default 10
- `mode` (string, optional): Search mode - 'fuzzy' (default), 'keyword', or 'regex'

#### 4. search_fields
Search specifically for fields and properties

Parameters:
- `query` (string): Field name or type
- `className` (string, optional): Filter by class name
- `limit` (number, optional): Maximum number of results, default 10
- `mode` (string, optional): Search mode - 'fuzzy' (default), 'keyword', or 'regex'

#### 5. get_class_details
Get detailed information about a specific class

Parameters:
- `className` (string): Full class name

#### 6. get_stats
Get statistics about loaded JavaDoc data

### Search Modes

The server supports three different search modes:

- **Fuzzy Search** (`mode: 'fuzzy'`): Uses Fuse.js for intelligent matching with typo tolerance and relevance scoring
- **Keyword Search** (`mode: 'keyword'`): Exact substring matching for precise results
- **Regex Search** (`mode: 'regex'`): Pattern matching using regular expressions for advanced queries

### Data Source

The server loads JavaDoc JSON files from a single directory specified by the `JAVADOC_JSON_PATH` environment variable. If multiple files contain the same class, the server automatically deduplicates them, keeping the version with more detailed information.

### Testing

The project includes comprehensive test suites to verify all functionality:

#### Quick Test
Run the basic search functionality test:
```bash
node test/basic-search-test.js
```

#### Comprehensive Testing
Run all test suites:
```bash
node test/run-all-tests.js
```

#### Individual Test Suites
- **Basic Search Test**: `node test/basic-search-test.js` - Tests all search modes with sample queries
- **Unit Tests**: `node test/unit-tests.js` - Comprehensive unit tests for all components
- **Comprehensive Test**: `node test/comprehensive-search-test.js` - Advanced test scenarios with performance testing

#### Test Coverage
The test suites cover:
- All search modes (fuzzy, keyword, regex)
- All search types (classes, methods, fields, global)
- Result filtering and sorting
- Edge cases and error handling
- Performance benchmarks

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

#### Basic Search
- Search all content: `search_all("TestClass")`
- Search classes: `search_classes("ADAS")`
- Search methods: `search_methods("getName")`
- Search fields: `search_fields("createdAt")`

#### Advanced Search with Modes
- Fuzzy search (default): `search_classes("TestClass", 10, "fuzzy")`
- Keyword search: `search_classes("TestClass", 10, "keyword")`
- Regex search: `search_classes(".*Test.*", 10, "regex")`

#### Filtered Search
- Methods in specific class: `search_methods("get", "TestClass")`
- Fields in specific class: `search_fields("field", "MyClass")`

#### Other Tools
- Get class details: `get_class_details("com.example.TestClass")`
- Get statistics: `get_stats()`

### Troubleshooting

If you encounter issues:

1. Ensure Node.js version >= 18
2. Check that the JavaDoc JSON directory exists and contains JSON files
3. Run `node test/basic-search-test.js` to verify search functionality
4. Run `node test/run-all-tests.js` for comprehensive testing
5. Check Claude Desktop log files
6. Verify the `JAVADOC_JSON_PATH` environment variable is set correctly
7. Ensure the project is built with `npm run build` before testing

---

## 中文

一个简单的 MCP (Model Context Protocol) 服务器，用于搜索 Java 文档 JSON 文件。

### 功能特性

- 支持搜索类、方法、构造函数和字段
- 自动数据去重和合并
- **多种搜索模式**：模糊搜索、关键字搜索和正则表达式搜索
- **高级搜索功能**：专门的字段搜索和过滤功能
- **智能结果排序**：模糊搜索结果按相关性分数排序
- 使用 Fuse.js 提供模糊搜索功能
- 支持多种搜索类型（全局搜索、类搜索、方法搜索、字段搜索）
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

服务器支持三种不同的搜索模式：

- **模糊搜索** (`mode: 'fuzzy'`): 使用 Fuse.js 进行智能匹配，支持拼写容错和相关性评分
- **关键字搜索** (`mode: 'keyword'`): 精确子字符串匹配，获得准确结果
- **正则表达式搜索** (`mode: 'regex'`): 使用正则表达式进行模式匹配，支持高级查询

### 数据源

服务器从 `JAVADOC_JSON_PATH` 环境变量指定的单个目录加载 JavaDoc JSON 文件。如果多个文件包含相同的类，服务器会自动去重，保留信息更详细的版本。

### 测试服务器

项目包含全面的测试套件来验证所有功能：

#### 快速测试
运行基本搜索功能测试：
```bash
node test/basic-search-test.js
```

#### 综合测试
运行所有测试套件：
```bash
node test/run-all-tests.js
```

#### 单独测试套件
- **基本搜索测试**: `node test/basic-search-test.js` - 使用示例查询测试所有搜索模式
- **单元测试**: `node test/unit-tests.js` - 所有组件的综合单元测试
- **综合测试**: `node test/comprehensive-search-test.js` - 高级测试场景和性能测试

#### 测试覆盖范围
测试套件涵盖：
- 所有搜索模式（模糊、关键字、正则表达式）
- 所有搜索类型（类、方法、字段、全局）
- 结果过滤和排序
- 边界条件和错误处理
- 性能基准测试

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

#### 基本搜索
- 搜索所有内容：`search_all("TestClass")`
- 搜索类：`search_classes("ADAS")`
- 搜索方法：`search_methods("getName")`
- 搜索字段：`search_fields("createdAt")`

#### 高级搜索模式
- 模糊搜索（默认）：`search_classes("TestClass", 10, "fuzzy")`
- 关键字搜索：`search_classes("TestClass", 10, "keyword")`
- 正则表达式搜索：`search_classes(".*Test.*", 10, "regex")`

#### 过滤搜索
- 特定类中的方法：`search_methods("get", "TestClass")`
- 特定类中的字段：`search_fields("field", "MyClass")`

#### 其他工具
- 获取类详情：`get_class_details("com.example.TestClass")`
- 获取统计信息：`get_stats()`

### 故障排除

如果遇到问题：

1. 确保 Node.js 版本 >= 18
2. 检查 JavaDoc JSON 目录是否存在并包含 JSON 文件
3. 运行 `node test/basic-search-test.js` 验证搜索功能
4. 运行 `node test/run-all-tests.js` 进行综合测试
5. 查看 Claude Desktop 的日志文件
6. 验证 `JAVADOC_JSON_PATH` 环境变量是否正确设置
7. 确保在测试前使用 `npm run build` 构建项目