#!/usr/bin/env node

import { JavaDocDataLoader } from '../dist/dataLoader.js';
import { JavaDocSearchEngine } from '../dist/searchEngine.js';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// 测试结果统计
let testResults = {
  passed: 0,
  failed: 0,
  total: 0
};

// 测试断言函数
function assert(condition, message) {
  testResults.total++;
  if (condition) {
    testResults.passed++;
    console.log(`✅ ${message}`);
  } else {
    testResults.failed++;
    console.log(`❌ ${message}`);
  }
}

// 测试用例函数
function testCase(name, testFn) {
  console.log(`\n🧪 测试用例: ${name}`);
  console.log('─'.repeat(50));
  try {
    testFn();
  } catch (error) {
    console.error(`❌ 测试失败: ${error.message}`);
    testResults.failed++;
    testResults.total++;
  }
}

async function runComprehensiveTests() {
  try {
    console.log('🔍 JavaDoc 搜索引擎综合测试\n');
    console.log('═'.repeat(60));
    
    // 加载数据
    const javadocJsonPath = join(__dirname, '..', '..', 'javadoc-json');
    const dataLoader = new JavaDocDataLoader(javadocJsonPath);
    const javadocData = dataLoader.loadAllData();
    
    console.log(`📚 已加载 ${javadocData.totalCount} 个类`);
    console.log(`📊 统计信息:`);
    console.log(`   - 类: ${javadocData.classes.length}`);
    console.log(`   - 方法: ${javadocData.classes.reduce((sum, cls) => sum + cls.methods.length, 0)}`);
    console.log(`   - 字段: ${javadocData.classes.reduce((sum, cls) => sum + cls.fields.length, 0)}`);
    
    // 创建搜索引擎
    const searchEngine = new JavaDocSearchEngine(javadocData);
    
    // 测试 1: 模糊搜索功能
    testCase('模糊搜索 - 类搜索', () => {
      const results = searchEngine.searchClasses('TestClass', 5, 'fuzzy');
      assert(results.length > 0, '模糊搜索应该返回结果');
      assert(results.every(r => r.score !== undefined), '模糊搜索结果应该包含分数');
      assert(results[0].score <= results[results.length - 1].score, '模糊搜索结果应该按分数升序排列');
      console.log(`   找到 ${results.length} 个类，最佳匹配: ${results[0].name} (score: ${results[0].score?.toFixed(3)})`);
    });
    
    // 测试 2: 关键字搜索功能
    testCase('关键字搜索 - 类搜索', () => {
      const results = searchEngine.searchClasses('TestClass', 10, 'keyword');
      assert(results.length > 0, '关键字搜索应该返回结果');
      assert(results.every(r => r.name.includes('TestClass')), '关键字搜索结果应该包含搜索词');
      console.log(`   找到 ${results.length} 个包含 'TestClass' 的类`);
    });
    
    // 测试 3: 正则表达式搜索功能
    testCase('正则表达式搜索 - 类搜索', () => {
      const results = searchEngine.searchClasses('.*Test.*', 10, 'regex');
      assert(results.length > 0, '正则表达式搜索应该返回结果');
      assert(results.every(r => /.*Test.*/.test(r.name)), '正则表达式搜索结果应该匹配模式');
      console.log(`   找到 ${results.length} 个匹配 '.*Test.*' 的类`);
    });
    
    // 测试 4: 字段搜索功能
    testCase('字段搜索 - 模糊搜索', () => {
      const results = searchEngine.searchFields('createdAt', undefined, 5, 'fuzzy');
      assert(results.length > 0, '字段搜索应该返回结果');
      assert(results.every(r => r.type === 'field'), '字段搜索结果应该都是字段类型');
      assert(results.every(r => r.parentClass), '字段搜索结果应该包含父类信息');
      console.log(`   找到 ${results.length} 个字段，最佳匹配: ${results[0].parentClass}.${results[0].name}`);
    });
    
    // 测试 5: 方法搜索功能
    testCase('方法搜索 - 模糊搜索', () => {
      const results = searchEngine.searchMethods('get', undefined, 5, 'fuzzy');
      assert(results.length > 0, '方法搜索应该返回结果');
      assert(results.every(r => ['method', 'constructor'].includes(r.type)), '方法搜索结果应该是方法或构造函数');
      assert(results.every(r => r.parentClass), '方法搜索结果应该包含父类信息');
      console.log(`   找到 ${results.length} 个方法，最佳匹配: ${results[0].parentClass}.${results[0].name}`);
    });
    
    // 测试 6: 全局搜索功能
    testCase('全局搜索 - 混合结果', () => {
      const results = searchEngine.searchAll('Test', 10, 'fuzzy');
      assert(results.length > 0, '全局搜索应该返回结果');
      const types = [...new Set(results.map(r => r.type))];
      assert(types.length > 1, '全局搜索应该返回多种类型的结果');
      console.log(`   找到 ${results.length} 个结果，包含类型: ${types.join(', ')}`);
    });
    
    // 测试 7: 类名过滤功能
    testCase('类名过滤 - 方法搜索', () => {
      const allResults = searchEngine.searchMethods('get', undefined, 20, 'fuzzy');
      if (allResults.length > 0) {
        const className = allResults[0].parentClass;
        const filteredResults = searchEngine.searchMethods('get', className, 10, 'fuzzy');
        assert(filteredResults.every(r => r.parentClass.toLowerCase().includes(className.toLowerCase())), '过滤后的结果应该包含指定类名的方法');
        console.log(`   在包含 "${className}" 的类中找到 ${filteredResults.length} 个方法`);
      }
    });
    
    // 测试 8: 边界条件测试
    testCase('边界条件测试', () => {
      // 空查询
      const emptyResults = searchEngine.searchClasses('', 5, 'fuzzy');
      assert(emptyResults.length === 0, '空查询应该返回空结果');
      
      // 不存在的查询
      const noResults = searchEngine.searchClasses('NonExistentClassXYZ123', 5, 'fuzzy');
      assert(noResults.length === 0, '不存在的查询应该返回空结果');
      
      // 限制数量测试
      const limitedResults = searchEngine.searchClasses('Test', 2, 'fuzzy');
      assert(limitedResults.length <= 2, '结果数量应该受限制参数控制');
      
      console.log('   边界条件测试通过');
    });
    
    // 测试 9: 性能测试
    testCase('性能测试', () => {
      const startTime = Date.now();
      
      // 执行多次搜索
      for (let i = 0; i < 10; i++) {
        searchEngine.searchAll('Test', 5, 'fuzzy');
        searchEngine.searchClasses('Class', 5, 'keyword');
        searchEngine.searchMethods('get', undefined, 5, 'regex');
        searchEngine.searchFields('field', undefined, 5, 'fuzzy');
      }
      
      const endTime = Date.now();
      const duration = endTime - startTime;
      
      assert(duration < 5000, '40次搜索操作应该在5秒内完成');
      console.log(`   40次搜索操作耗时: ${duration}ms`);
    });
    
    // 测试 10: 搜索模式对比
    testCase('搜索模式对比', () => {
      const query = 'Test';
      const fuzzyResults = searchEngine.searchClasses(query, 10, 'fuzzy');
      const keywordResults = searchEngine.searchClasses(query, 10, 'keyword');
      const regexResults = searchEngine.searchClasses(`.*${query}.*`, 10, 'regex');
      
      console.log(`   模糊搜索: ${fuzzyResults.length} 个结果`);
      console.log(`   关键字搜索: ${keywordResults.length} 个结果`);
      console.log(`   正则搜索: ${regexResults.length} 个结果`);
      
      assert(fuzzyResults.length > 0 || keywordResults.length > 0 || regexResults.length > 0, 
             '至少一种搜索模式应该返回结果');
    });
    
    // 输出测试结果统计
    console.log('\n' + '═'.repeat(60));
    console.log('📊 测试结果统计:');
    console.log(`   总测试数: ${testResults.total}`);
    console.log(`   通过: ${testResults.passed} ✅`);
    console.log(`   失败: ${testResults.failed} ❌`);
    console.log(`   成功率: ${((testResults.passed / testResults.total) * 100).toFixed(1)}%`);
    
    if (testResults.failed === 0) {
      console.log('\n🎉 所有测试通过！搜索引擎工作正常。');
    } else {
      console.log('\n⚠️  部分测试失败，请检查搜索引擎实现。');
    }
    
  } catch (error) {
    console.error('❌ 测试执行失败:', error);
    process.exit(1);
  }
}

// 运行测试
runComprehensiveTests();