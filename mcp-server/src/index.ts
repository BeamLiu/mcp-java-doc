#!/usr/bin/env node
import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import {
  CallToolRequestSchema,
  ErrorCode,
  ListToolsRequestSchema,
  McpError,
} from '@modelcontextprotocol/sdk/types.js';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';
import { JavaDocDataLoader } from './dataLoader.js';
import { JavaDocSearchEngine, SearchMode } from './searchEngine.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

export class JavaDocMCPServer {
  private server: Server;
  private searchEngine!: JavaDocSearchEngine;

  constructor(javadocJsonPaths?: string | string[]) {
    this.server = new Server({
      name: 'mcp-javadoc-server',
      version: '0.1.6',
    }, {
      capabilities: {
        tools: {},
      },
    });

    this.initializeData(javadocJsonPaths);
    this.setupHandlers();
  }

  private initializeData(javadocJsonPaths?: string | string[]) {
    try {
      // Priority: 1. Constructor parameter, 2. Environment variable, 3. Default path
      let dataPaths: string[];

      if (javadocJsonPaths) {
        dataPaths = Array.isArray(javadocJsonPaths) ? javadocJsonPaths : [javadocJsonPaths];
      } else if (process.env.JAVADOC_JSON_PATH) {
        // Support multiple paths separated by path delimiter (: on Unix, ; on Windows)
        const delimiter = process.platform === 'win32' ? ';' : ':';
        dataPaths = process.env.JAVADOC_JSON_PATH.split(delimiter).map(p => p.trim()).filter(p => p);
      } else {
        dataPaths = [join(__dirname, '..', '..', 'javadoc-json')];
      }

      console.error('Loading JavaDoc data...');
      console.error(`JavaDoc JSON paths (${dataPaths.length}):`);
      dataPaths.forEach((path, index) => {
        console.error(`  ${index + 1}. ${path}`);
      });

      const dataLoader = new JavaDocDataLoader(dataPaths);
      const javadocData = dataLoader.loadAllData();

      if (javadocData.totalCount === 0) {
        console.error('WARNING: No JavaDoc classes loaded. Please check your paths.');
        console.error('The server will start but searches will return no results.');
      } else {
        console.error(`Loaded ${javadocData.totalCount} classes from ${dataPaths.length} JavaDoc JSON ${dataPaths.length === 1 ? 'directory' : 'directories'}`);
      }

      this.searchEngine = new JavaDocSearchEngine(javadocData);
    } catch (error) {
      console.error('Failed to load JavaDoc data:', error);
      console.error('Error details:', error instanceof Error ? error.message : String(error));
      throw new Error('Could not initialize JavaDoc data');
    }
  }

  private setupHandlers() {
    this.server.setRequestHandler(ListToolsRequestSchema, async () => ({
      tools: [
        {
          name: 'search_all',
          description: 'Search for classes, methods, constructors, and fields in the JavaDocs',
          inputSchema: {
            type: 'object',
            properties: {
              query: {
                type: 'string',
                description: 'Search query (e.g., "TestClass", "getName", "nxopen.features")',
              },
              limit: {
                type: 'number',
                description: 'Maximum number of results to return (default: 10)',
                default: 10,
              },
              mode: {
                type: 'string',
                enum: ['fuzzy', 'keyword', 'regex'],
                description: 'Search mode: fuzzy (default), keyword (contains), or regex (regular expression)',
                default: 'fuzzy',
              },
            },
            required: ['query'],
          },
        },
        {
          name: 'search_classes',
          description: 'Search specifically for classes and interfaces',
          inputSchema: {
            type: 'object',
            properties: {
              query: {
                type: 'string',
                description: 'Class name or description to search for',
              },
              limit: {
                type: 'number',
                description: 'Maximum number of results (default: 10)',
                default: 10,
              },
              mode: {
                type: 'string',
                enum: ['fuzzy', 'keyword', 'regex'],
                description: 'Search mode: fuzzy (default), keyword (contains), or regex (regular expression)',
                default: 'fuzzy',
              },
            },
            required: ['query'],
          },
        },
        {
          name: 'search_methods',
          description: 'Search specifically for methods and constructors',
          inputSchema: {
            type: 'object',
            properties: {
              query: {
                type: 'string',
                description: 'Method name or signature to search for',
              },
              className: {
                type: 'string',
                description: 'Optional: filter by class name',
              },
              limit: {
                type: 'number',
                description: 'Maximum number of results (default: 10)',
                default: 10,
              },
              mode: {
                type: 'string',
                enum: ['fuzzy', 'keyword', 'regex'],
                description: 'Search mode: fuzzy (default), keyword (contains), or regex (regular expression)',
                default: 'fuzzy',
              },
            },
            required: ['query'],
          },
        },
        {
          name: 'search_fields',
          description: 'Search specifically for fields/properties',
          inputSchema: {
            type: 'object',
            properties: {
              query: {
                type: 'string',
                description: 'Field name or type to search for',
              },
              className: {
                type: 'string',
                description: 'Optional: filter by class name',
              },
              limit: {
                type: 'number',
                description: 'Maximum number of results (default: 10)',
                default: 10,
              },
              mode: {
                type: 'string',
                enum: ['fuzzy', 'keyword', 'regex'],
                description: 'Search mode: fuzzy (default), keyword (contains), or regex (regular expression)',
                default: 'fuzzy',
              },
            },
            required: ['query'],
          },
        },
        {
          name: 'get_class_details',
          description: 'Get detailed information about a specific class',
          inputSchema: {
            type: 'object',
            properties: {
              className: {
                type: 'string',
                description: 'Full class name (e.g., "com.example.TestClass" or "TestClass")',
              },
            },
            required: ['className'],
          },
        },
        {
          name: 'get_stats',
          description: 'Get statistics about the loaded JavaDoc data',
          inputSchema: {
            type: 'object',
            properties: {},
          },
        },
      ],
    }));

    this.server.setRequestHandler(CallToolRequestSchema, async (request) => {
      const { name, arguments: args } = request.params;

      try {
        switch (name) {
          case 'search_all': {
            const { query, limit = 10, mode = 'fuzzy' } = args as {
              query: string;
              limit?: number;
              mode?: SearchMode
            };
            const results = this.searchEngine.searchAll(query, limit, mode);
            return {
              content: [
                {
                  type: 'text',
                  text: JSON.stringify({
                    query,
                    mode,
                    results,
                    total: results.length,
                  }, null, 2),
                },
              ],
            };
          }

          case 'search_classes': {
            const { query, limit = 10, mode = 'fuzzy' } = args as {
              query: string;
              limit?: number;
              mode?: SearchMode
            };
            const results = this.searchEngine.searchClasses(query, limit, mode);
            return {
              content: [
                {
                  type: 'text',
                  text: JSON.stringify({
                    query,
                    mode,
                    results,
                    total: results.length,
                  }, null, 2),
                },
              ],
            };
          }

          case 'search_methods': {
            const { query, className, limit = 10, mode = 'fuzzy' } = args as {
              query: string;
              className?: string;
              limit?: number;
              mode?: SearchMode
            };
            const results = this.searchEngine.searchMethods(query, className, limit, mode);
            return {
              content: [
                {
                  type: 'text',
                  text: JSON.stringify({
                    query,
                    className,
                    mode,
                    results,
                    total: results.length,
                  }, null, 2),
                },
              ],
            };
          }

          case 'search_fields': {
            const { query, className, limit = 10, mode = 'fuzzy' } = args as {
              query: string;
              className?: string;
              limit?: number;
              mode?: SearchMode
            };
            const results = this.searchEngine.searchFields(query, className, limit, mode);
            return {
              content: [
                {
                  type: 'text',
                  text: JSON.stringify({
                    query,
                    className,
                    mode,
                    results,
                    total: results.length,
                  }, null, 2),
                },
              ],
            };
          }

          case 'get_class_details': {
            const { className } = args as { className: string };
            const classDetails = this.searchEngine.getClassByName(className);

            if (!classDetails) {
              throw new McpError(ErrorCode.InvalidRequest, `Class not found: ${className}`);
            }

            return {
              content: [
                {
                  type: 'text',
                  text: JSON.stringify(classDetails, null, 2),
                },
              ],
            };
          }

          case 'get_stats': {
            const stats = this.searchEngine.getStats();
            return {
              content: [
                {
                  type: 'text',
                  text: JSON.stringify(stats, null, 2),
                },
              ],
            };
          }

          default:
            throw new McpError(ErrorCode.MethodNotFound, `Unknown tool: ${name}`);
        }
      } catch (error) {
        if (error instanceof McpError) {
          throw error;
        }
        throw new McpError(ErrorCode.InternalError, `Tool execution failed: ${error}`);
      }
    });
  }

  async run() {
    const transport = new StdioServerTransport();
    await this.server.connect(transport);
    console.error('JavaDoc MCP Server running on stdio');
  }
}

// Parse command line arguments
function parseArgs(): { javadocPaths?: string[]; help?: boolean } {
  const args = process.argv.slice(2);
  const result: { javadocPaths?: string[]; help?: boolean } = {};

  for (let i = 0; i < args.length; i++) {
    const arg = args[i];
    if (arg === '--javadoc-path' || arg === '-p') {
      if (!result.javadocPaths) {
        result.javadocPaths = [];
      }
      result.javadocPaths.push(args[++i]);
    } else if (arg === '--help' || arg === '-h') {
      result.help = true;
    }
  }

  return result;
}

function printHelp() {
  console.error(`
JavaDoc MCP Server

Usage: node dist/index.js [options]

Options:
  -p, --javadoc-path <path>   Path to JavaDoc JSON directory (can be specified multiple times)
  -h, --help                  Show this help message

Configuration Priority:
  1. Command line arguments (--javadoc-path, can specify multiple)
  2. Environment variable (JAVADOC_JSON_PATH, use : or ; to separate multiple paths)
  3. Default path (../../javadoc-json)

Examples:
  # Single path
  node dist/index.js --javadoc-path /path/to/javadoc-json
  
  # Multiple paths
  node dist/index.js --javadoc-path /path/to/json1 --javadoc-path /path/to/json2
  
  # Environment variable (Unix/Linux/macOS)
  JAVADOC_JSON_PATH=/path/to/json1:/path/to/json2 node dist/index.js
  
  # Environment variable (Windows)
  set JAVADOC_JSON_PATH=C:\\path\\to\\json1;C:\\path\\to\\json2
  node dist/index.js
`);
}

// If this file is run directly, start the server
// Check if this module is the main entry point
// This works for: node dist/index.js, npx package, or via bin script
const isMainModule = process.argv[1] && (
  import.meta.url === `file://${process.argv[1]}` ||
  import.meta.url.endsWith(process.argv[1]) ||
  process.argv[1].endsWith(__filename) ||
  process.argv[1].includes('mcp-javadoc-server') // Handles npx and bin script execution
);

if (isMainModule) {
  // Handle uncaught exceptions
  process.on('uncaughtException', (error) => {
    console.error('Uncaught exception:', error);
    process.exit(1);
  });

  process.on('unhandledRejection', (reason, promise) => {
    console.error('Unhandled rejection at:', promise, 'reason:', reason);
    process.exit(1);
  });

  try {
    const args = parseArgs();

    if (args.help) {
      printHelp();
      process.exit(0);
    }

    console.error('Starting JavaDoc MCP Server...');
    const server = new JavaDocMCPServer(args.javadocPaths);
    server.run().catch((error) => {
      console.error('Server run error:', error);
      console.error('Stack trace:', error instanceof Error ? error.stack : 'No stack trace available');
      process.exit(1);
    });
  } catch (error) {
    console.error('Server initialization error:', error);
    console.error('Stack trace:', error instanceof Error ? error.stack : 'No stack trace available');
    process.exit(1);
  }
}