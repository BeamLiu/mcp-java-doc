#!/usr/bin/env node

import { JavaDocMCPServer } from '../dist/index.js';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

async function testServer() {
  try {
    console.log('Testing JavaDoc MCP Server...');
    
    // Set the JavaDoc JSON path for testing
    const javadocJsonPath = join(__dirname, '..', 'tmp', 'javadoc-json');
    process.env.JAVADOC_JSON_PATH = javadocJsonPath;
    console.log(`Using JavaDoc JSON path: ${javadocJsonPath}`);
    
    // Create server instance
    const server = new JavaDocMCPServer();
    console.log('✓ Server created successfully');
    
    console.log('✓ All tests passed!');
    console.log('Server is ready to use.');
    
  } catch (error) {
    console.error('✗ Test failed:', error.message);
    console.error('Full error:', error);
    process.exit(1);
  }
}

testServer();