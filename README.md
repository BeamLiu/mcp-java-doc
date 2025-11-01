# MCP Java Documentation / MCP Java 文档

[English](#english) | [中文](#中文)

---

## English

A comprehensive solution for generating structured JSON documentation from Java source code and providing intelligent search capabilities through MCP (Model Context Protocol).

### 🚀 Overview

This project consists of three main components that work together to create a complete Java documentation ecosystem:

1. **Maven Plugin** (`java-docs-json-doclet`) - Generates structured JSON documentation from Java source code
2. **MCP Server** (`mcp-server`) - Provides intelligent search and query capabilities for the generated documentation
3. **Sample Data** (`javadoc-json`) - Example JSON documentation files for testing and reference

### 🏗️ Architecture

```
┌─────────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐
│   Java Source Code  │    │   HTML Javadoc      │    │   Lombok Projects   │
│                     │    │   (JDK 9)          │    │                     │
└──────────┬──────────┘    └──────────┬──────────┘    └──────────┬──────────┘
           │                          │                          │
           ▼                          ▼                          ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    Maven Plugin (java-docs-json-doclet)                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐  │
│  │  Publish Goal   │  │   Crawl Goal    │  │      Lombok Support         │  │
│  │  (Source Code)  │  │  (HTML Docs)    │  │     (Delombok Process)      │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────────────────┘  │
└─────────────────────────────┬───────────────────────────────────────────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │   JSON Documentation│
                    │   (Structured Data) │
                    └──────────┬──────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           MCP Server                                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐  │
│  │  Search Classes │  │  Search Methods │  │      Get Statistics         │  │
│  │                 │  │                 │  │                             │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────────────────┘  │
└─────────────────────────────┬───────────────────────────────────────────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │   Claude Desktop    │
                    │   (AI Assistant)    │
                    └─────────────────────┘
```

### 📦 Components

#### 1. Java Docs JSON Doclet (`java-docs-json-doclet/`)

A Maven plugin that converts Java documentation into structured JSON format.

**Supported Java Versions:**

| Goal | Supported Versions | Notes                                  |
|------|-------------------|----------------------------------------|
| **javadoc-json** | Java 8+ | Supports all Java 8 and above versions |
| **Crawl** | Java 9 | To be supported for other versions in the future, currently only support https://docs.sw.siemens.com/documentation/external/PL20231101866122454/en-US/custom_api/open_java_ref/nxopen/issue/package-summary.html , you could update `io.emop.javadocjson.doclet.JsonDoclet` to build your own html to json parser     |

*Future Java versions will be supported in upcoming releases*

**Features:**
- **Generate Javadoc Json Goal**: Generate JSON from Java source code
- **Crawl Goal**: Extract documentation from HTML Javadoc websites (JDK 9+ supported)
- **Lombok Support**: Handle Lombok annotations with delombok process
- **MCP Compatible**: Generate documentation in MCP-compatible format
- **Package Filtering**: Use regex patterns to filter specific packages
- **Proxy Support**: Configure proxy settings for crawling external documentation

**Quick Start:**
```xml
<plugin>
    <groupId>io.github.beamliu</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>0.1.1</version>
</plugin>
```
Pay attention, you may encounter compile error if you are using a lombok, please jump to [Lombok Projects](#lombok-projects) section.

More detail goes to [README](./java-docs-json-doclet/README.md)

#### 2. MCP Server (`mcp-server/`)

A Node.js-based MCP server that provides intelligent search capabilities for Java documentation.

**Features:**
- **Fuzzy Search**: Powered by Fuse.js for intelligent matching
- **Multiple Search Types**: Classes, methods, constructors, and fields
- **Data Deduplication**: Automatic merging of duplicate entries
- **Statistics**: Get overview of documentation coverage
- **Claude Integration**: Seamless integration with Claude Desktop

**Quick Start:**
```bash
npx @io.emop/mcp-javadoc-server --javadoc-path /path/to/javadoc-json
```

#### 3. Sample Data (`javadoc-json/`)

Contains example JSON documentation files generated from various Java projects for testing and reference purposes.

### 🚀 Getting Started

#### Step 1: Generate JSON Documentation

1. **From Source Code:**

add maven dependency
```xml
<plugin>
    <groupId>io.github.beamliu</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>0.1.1</version>
</plugin>
```
and then
```bash
mvn javadoc-json:javadoc-json
```
Sample maven config could be found at [./java-docs-json-doclet/examples/](./java-docs-json-doclet/examples/)

2. **From HTML Javadoc:**

please refer to [README](./java-docs-json-doclet/README.md)


#### Step 2: Start MCP Server

```bash
# Using npx (recommended, no installation needed)
npx @io.emop/mcp-javadoc-server --javadoc-path /path/to/javadoc-json
```

使用`mcp inspector`测试
```bash
npx @modelcontextprotocol/inspector npx -y @io.emop/mcp-javadoc-server -- --javadoc-path /path/to/javadoc-json
```
please refer to [README](./mcp-server/README.md)

### 📖 Usage Examples

Once configured, you can use these commands in Claude Desktop:

- **Search all items**: `search_all("ArrayList")`
- **Search classes**: `search_classes("HashMap")`
- **Search methods**: `search_methods("toString")`
- **Get class details**: `get_class_details("java.util.List")`
- **Get statistics**: `get_stats()`

### 🔧 Advanced Configuration

#### Lombok Projects

For projects using Lombok, configure the delombok process:

please refer to [README](./java-docs-json-doclet/README.md#working-with-lombok)

### 📄 License

This project is licensed under the MIT License.

---

## 中文

一个完整的解决方案，用于从 Java 源代码生成结构化 JSON 文档，并通过 MCP（模型上下文协议）提供智能搜索功能。

### 🚀 概述

本项目由三个主要组件组成，它们协同工作创建完整的 Java 文档生态系统：

1. **Maven 插件** (`java-docs-json-doclet`) - 从 Java 源代码生成结构化 JSON 文档
2. **MCP 服务器** (`mcp-server`) - 为生成的文档提供智能搜索和查询功能
3. **样例数据** (`javadoc-json`) - 用于测试和参考的示例 JSON 文档文件

### 🏗️ 架构

```
┌─────────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐
│     Java 源代码      │    │   HTML Javadoc      │    │   Lombok 项目       │
│                     │    │   (JDK 9)          │    │                     │
└──────────┬──────────┘    └──────────┬──────────┘    └──────────┬──────────┘
           │                          │                          │
           ▼                          ▼                          ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    Maven 插件 (java-docs-json-doclet)                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐  │
│  │   发布目标      │  │   爬取目标      │  │      Lombok 支持            │  │
│  │  (源代码)       │  │  (HTML 文档)    │  │     (Delombok 过程)         │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────────────────┘  │
└─────────────────────────────┬───────────────────────────────────────────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │   JSON 文档         │
                    │   (结构化数据)      │
                    └──────────┬──────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           MCP 服务器                                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐  │
│  │   搜索类        │  │   搜索方法      │  │      获取统计信息           │  │
│  │                 │  │                 │  │                             │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────────────────┘  │
└─────────────────────────────┬───────────────────────────────────────────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │   Claude Desktop    │
                    │   (AI 助手)         │
                    └─────────────────────┘
```

### 📦 组件

#### 1. Java Docs JSON Doclet (`java-docs-json-doclet/`)

一个将 Java 文档转换为结构化 JSON 格式的 Maven 插件。

**支持的 Java 版本：**

| 目标 | 支持版本 | 说明                 |
|------|--------|--------------------|
| **javadoc-json** | Java 8+ | 支持所有 Java 8 及以上版本 |
| **爬取 (Crawl)** | Java 9 | 未来将支持其他版本，目前仅支持 https://docs.sw.siemens.com/documentation/external/PL20231101866122454/en-US/custom_api/open_java_ref/nxopen/issue/package-summary.html ，您可以更新 `io.emop.javadocjson.doclet.JsonDoclet` 来构建自己的 HTML 到 JSON 解析器     |

*后续 Java 版本将在未来发布中支持*

**功能特性：**
- **生成 Javadoc Json 目标**：从 Java 源代码生成 JSON
- **爬取目标**：从 HTML Javadoc 网站提取文档（支持 JDK 9+）
- **Lombok 支持**：通过 delombok 过程处理 Lombok 注解
- **MCP 兼容**：生成 MCP 兼容格式的文档
- **包过滤**：使用正则表达式模式过滤特定包
- **代理支持**：为爬取外部文档配置代理设置

**快速开始：**
```xml
<plugin>
    <groupId>io.github.beamliu</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>0.1.1</version>
</plugin>
```
注意，如果您正在使用 Lombok，可能会遇到编译错误，请跳转到 [Lombok 项目](#lombok-项目) 章节。

更多详情请参考 [README](./java-docs-json-doclet/README.md)

#### 2. MCP 服务器 (`mcp-server/`)

基于 Node.js 的 MCP 服务器，为 Java 文档提供智能搜索功能。

**功能特性：**
- **模糊搜索**：由 Fuse.js 提供智能匹配
- **多种搜索类型**：类、方法、构造函数和字段
- **数据去重**：自动合并重复条目
- **统计信息**：获取文档覆盖率概览
- **Claude 集成**：与 Claude Desktop 无缝集成

**快速开始：**
```bash
npx @io.emop/mcp-javadoc-server --javadoc-path /path/to/javadoc-json
```

#### 3. 样例数据 (`javadoc-json/`)

包含从各种 Java 项目生成的示例 JSON 文档文件，用于测试和参考。

### 🚀 入门指南

#### 步骤 1：生成 JSON 文档

1. **从源代码：**

添加 Maven 依赖
```xml
<plugin>
    <groupId>io.github.beamliu</groupId>
    <artifactId>java-docs-json-doclet</artifactId>
    <version>0.1.1</version>
</plugin>
```
然后执行
```bash
mvn javadoc-json:javadoc-json
```
Maven 配置示例可以在 [./java-docs-json-doclet/examples/](./java-docs-json-doclet/examples/) 找到

2. **从 HTML Javadoc：**

请参考 [README](./java-docs-json-doclet/README.md)

#### 步骤 2：启动 MCP 服务器

```bash
# 使用 npx（推荐，无需安装）
npx @io.emop/mcp-javadoc-server --javadoc-path /path/to/javadoc-json
```

使用 `mcp inspector` 测试
```bash
npx @modelcontextprotocol/inspector npx -y @io.emop/mcp-javadoc-server -- --javadoc-path /path/to/javadoc-json
```
请参考 [README](./mcp-server/README.md)

### 📖 使用示例

配置完成后，您可以在 Claude Desktop 中使用这些命令：

- **搜索所有项目**：`search_all("ArrayList")`
- **搜索类**：`search_classes("HashMap")`
- **搜索方法**：`search_methods("toString")`
- **获取类详情**：`get_class_details("java.util.List")`
- **获取统计信息**：`get_stats()`

### 🔧 高级配置

#### Lombok 项目

对于使用 Lombok 的项目，配置 delombok 过程：

请参考 [README](./java-docs-json-doclet/README.md#使用-lombok)

### 📄 许可证

本项目采用 MIT 许可证。