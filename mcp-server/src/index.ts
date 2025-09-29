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

  constructor() {
    this.server = new Server({
      name: 'mcp-javadoc-server',
      version: '0.1.0',
    }, {
      capabilities: {
        tools: {},
      },
    });

    this.initializeData();
    this.setupHandlers();
  }

  private initializeData() {
    try {
      // Get JavaDoc JSON directory from environment variable or use default
      const javadocJsonPath = process.env.JAVADOC_JSON_PATH || join(__dirname, '..', '..', 'javadoc-json');

      console.error('Loading JavaDoc data...');
      console.error(`JavaDoc JSON path: ${javadocJsonPath}`);

      const dataLoader = new JavaDocDataLoader(javadocJsonPath);
      const javadocData = dataLoader.loadAllData();
      
      console.error(`Loaded ${javadocData.totalCount} classes from JavaDoc JSON repository`);
      
      this.searchEngine = new JavaDocSearchEngine(javadocData);
    } catch (error) {
      console.error('Failed to load JavaDoc data:', error);
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

// If this file is run directly, start the server
const isMainModule = import.meta.url === `file://${process.argv[1]}` || 
                     import.meta.url.endsWith(process.argv[1]) ||
                     process.argv[1].endsWith(__filename);

if (isMainModule) {
  try {
    console.error('Starting JavaDoc MCP Server...');
    const server = new JavaDocMCPServer();
    server.run().catch((error) => {
      console.error('Server run error:', error);
      process.exit(1);
    });
  } catch (error) {
    console.error('Server initialization error:', error);
    process.exit(1);
  }
}