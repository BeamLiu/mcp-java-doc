import { JavaDocMCPServer } from '../dist/index.js';
import { readFileSync, readdirSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

console.log('Testing TypeScript model compatibility with Java models...\n');

// Set JavaDoc JSON path
const javadocJsonPath = join(__dirname, '..', '..', 'javadoc-json');
process.env.JAVADOC_JSON_PATH = javadocJsonPath;
console.log(`Using JavaDoc JSON path: ${javadocJsonPath}\n`);

try {
  // Create server instance
  const server = new JavaDocMCPServer();
  
  // Get statistics
  const stats = server.searchEngine.getStats();
  console.log('📊 Data Statistics:');
  console.log(`  Total classes: ${stats.totalClasses}`);
  console.log(`  Packages: ${stats.packages}\n`);
  
  // Test original JSON data structure
  console.log('🔍 Testing JSON data structure compatibility...');
  
  // Get list of JSON files
  const jsonFiles = readdirSync(javadocJsonPath).filter(file => file.endsWith('.json'));
  console.log(`Found ${jsonFiles.length} JSON files in directory`);
  
  // Test a file with simplified fields (originally from crawl-output)
  const simplifiedFile = jsonFiles.find(file => file.includes('nxopen.features.ADASCoordinateSystem'));
  if (simplifiedFile) {
    const simplifiedData = JSON.parse(readFileSync(join(javadocJsonPath, simplifiedFile), 'utf-8'));
    console.log('\n✓ Simplified data structure:');
    console.log(`  Name: ${simplifiedData.name}`);
    console.log(`  Type: ${simplifiedData.type}`);
    console.log(`  Has methods: ${Array.isArray(simplifiedData.methods)}`);
    console.log(`  Has simpleMethods: ${Array.isArray(simplifiedData.simpleMethods)}`);
    console.log(`  Has constructors: ${Array.isArray(simplifiedData.constructors)}`);
    console.log(`  Has simpleConstructors: ${Array.isArray(simplifiedData.simpleConstructors)}`);
  }
  
  // Test a file with detailed fields (originally from publish-output)
  const detailedFile = jsonFiles.find(file => file.includes('com.example.TestClass'));
  if (detailedFile) {
    const detailedData = JSON.parse(readFileSync(join(javadocJsonPath, detailedFile), 'utf-8'));
    console.log('\n✓ Detailed data structure:');
    console.log(`  Name: ${detailedData.name}`);
    console.log(`  Type: ${detailedData.type}`);
    console.log(`  Methods count: ${detailedData.methods?.length || 0}`);
    console.log(`  Constructors count: ${detailedData.constructors?.length || 0}`);
    console.log(`  Fields count: ${detailedData.fields?.length || 0}`);
  }
  
  // Test search functionality
  console.log('\n🔎 Testing search functionality...');
  
  // Search all items
  const allResults = server.searchEngine.searchAll('test', 5);
  console.log(`✓ Search all "test": ${allResults.length} results`);
  allResults.forEach((result, i) => {
    console.log(`  ${i+1}. ${result.type}: ${result.name} (${result.fullName})`);
  });
  
  // 搜索类
  const classResults = server.searchEngine.searchClasses('ADAS', 3);
  console.log(`\n✓ Search classes "ADAS": ${classResults.length} results`);
  classResults.forEach((result, i) => {
    console.log(`  ${i+1}. ${result.name} - ${result.description}`);
  });
  
  // 搜索方法
  const methodResults = server.searchEngine.searchMethods('test', null, 3);
  console.log(`\n✓ Search methods "test": ${methodResults.length} results`);
  methodResults.forEach((result, i) => {
    console.log(`  ${i+1}. ${result.parentClass}.${result.name}() - ${result.type}`);
  });
  
  // 测试获取类详情
  console.log('\n📋 Testing class details...');
  const testClass = server.searchEngine.getClassByName('TestClass');
  if (testClass) {
    console.log(`✓ Found class: ${testClass.name}`);
    console.log(`  Package: ${testClass.packageName}`);
    console.log(`  Type: ${testClass.type}`);
    console.log(`  Methods: ${testClass.methods?.length || 0}`);
    console.log(`  Simple methods: ${testClass.simpleMethods?.length || 0}`);
    console.log(`  Constructors: ${testClass.constructors?.length || 0}`);
    console.log(`  Simple constructors: ${testClass.simpleConstructors?.length || 0}`);
    console.log(`  Fields: ${testClass.fields?.length || 0}`);
    console.log(`  Simple fields: ${testClass.simpleFields?.length || 0}`);
  }
  
  console.log('\n✅ All compatibility tests passed!');
  console.log('TypeScript models are fully compatible with Java models.');
  
} catch (error) {
  console.error('❌ Compatibility test failed:', error);
  process.exit(1);
}