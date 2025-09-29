#!/usr/bin/env node

import { JavaDocDataLoader } from '../dist/dataLoader.js';
import { JavaDocSearchEngine } from '../dist/searchEngine.js';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

async function testSearchFeatures() {
  try {
    console.log('🔍 测试新的搜索功能...\n');
    
    // 加载数据
    const javadocJsonPath = join(__dirname, '..', 'javadoc-json');
    const dataLoader = new JavaDocDataLoader(javadocJsonPath);
    const javadocData = dataLoader.loadAllData();
    
    console.log(`📚 已加载 ${javadocData.totalCount} 个类\n`);
    
    // 创建搜索引擎
    const searchEngine = new JavaDocSearchEngine(javadocData);
    
    // 测试不同的搜索模式
    const testQuery = 'TestClass';
    
    console.log(`🔍 搜索查询: "${testQuery}"\n`);
    
    // 1. Fuzzy 搜索
    console.log('1️⃣ Fuzzy 搜索 (默认):');
    const fuzzyResults = searchEngine.searchClasses(testQuery, 5, 'fuzzy');
    fuzzyResults.forEach((result, index) => {
      console.log(`   ${index + 1}. ${result.name} (score: ${result.score?.toFixed(3)})`);
    });
    console.log();
    
    // 2. 关键字搜索
    console.log('2️⃣ 关键字搜索:');
    const keywordResults = searchEngine.searchClasses(testQuery, 5, 'keyword');
    keywordResults.forEach((result, index) => {
      console.log(`   ${index + 1}. ${result.name}`);
    });
    console.log();
    
    // 3. 正则表达式搜索
    console.log('3️⃣ 正则表达式搜索 (.*Test.*):');
    const regexResults = searchEngine.searchClasses('.*Test.*', 5, 'regex');
    regexResults.forEach((result, index) => {
      console.log(`   ${index + 1}. ${result.name}`);
    });
    console.log();
    
    // 4. 测试字段搜索
    console.log('4️⃣ 字段搜索 (createdAt):');
    const fieldResults = searchEngine.searchFields('createdAt', undefined, 5, 'fuzzy');
    fieldResults.forEach((result, index) => {
      console.log(`   ${index + 1}. ${result.parentClass}.${result.name} (score: ${result.score?.toFixed(3)})`);
    });
    console.log();
    
    // 5. 测试方法搜索 (带score排序)
    console.log('5️⃣ 方法搜索 (get):');
    const methodResults = searchEngine.searchMethods('get', undefined, 5, 'fuzzy');
    methodResults.forEach((result, index) => {
      console.log(`   ${index + 1}. ${result.parentClass}.${result.name} (score: ${result.score?.toFixed(3)})`);
    });
    console.log();
    
    console.log('✅ 所有测试完成！');
    
  } catch (error) {
    console.error('❌ 测试失败:', error);
  }
}

testSearchFeatures();