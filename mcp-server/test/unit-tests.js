#!/usr/bin/env node

import { JavaDocDataLoader } from '../dist/dataLoader.js';
import { JavaDocSearchEngine } from '../dist/searchEngine.js';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// 简单的测试框架
class TestRunner {
  constructor() {
    this.tests = [];
    this.results = { passed: 0, failed: 0, total: 0 };
  }

  test(name, testFn) {
    this.tests.push({ name, testFn });
  }

  async run() {
    console.log('🧪 单元测试开始\n');
    
    for (const { name, testFn } of this.tests) {
      console.log(`📝 ${name}`);
      try {
        await testFn();
        this.results.passed++;
        console.log('   ✅ 通过\n');
      } catch (error) {
        this.results.failed++;
        console.log(`   ❌ 失败: ${error.message}\n`);
      }
      this.results.total++;
    }

    this.printSummary();
  }

  printSummary() {
    console.log('═'.repeat(50));
    console.log('📊 测试结果:');
    console.log(`   总计: ${this.results.total}`);
    console.log(`   通过: ${this.results.passed} ✅`);
    console.log(`   失败: ${this.results.failed} ❌`);
    console.log(`   成功率: ${((this.results.passed / this.results.total) * 100).toFixed(1)}%`);
  }
}

function assertEqual(actual, expected, message) {
  if (actual !== expected) {
    throw new Error(`${message}: 期望 ${expected}, 实际 ${actual}`);
  }
}

function assertTrue(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function assertGreaterThan(actual, expected, message) {
  if (actual <= expected) {
    throw new Error(`${message}: 期望 > ${expected}, 实际 ${actual}`);
  }
}

// 创建测试运行器
const runner = new TestRunner();

// 数据加载测试
runner.test('数据加载器 - 基本功能', async () => {
  const javadocJsonPath = join(__dirname, '..', '..', 'javadoc-json');
  const dataLoader = new JavaDocDataLoader(javadocJsonPath);
  const javadocData = dataLoader.loadAllData();
  
  assertTrue(javadocData.classes.length > 0, '应该加载到类数据');
  assertTrue(javadocData.totalCount > 0, '总数应该大于0');
  assertTrue(Array.isArray(javadocData.classes), '类数据应该是数组');
});

// 搜索引擎初始化测试
runner.test('搜索引擎 - 初始化', async () => {
  const javadocJsonPath = join(__dirname, '..', '..', 'javadoc-json');
  const dataLoader = new JavaDocDataLoader(javadocJsonPath);
  const javadocData = dataLoader.loadAllData();
  const searchEngine = new JavaDocSearchEngine(javadocData);
  
  assertTrue(searchEngine !== null, '搜索引擎应该成功初始化');
  assertTrue(typeof searchEngine.searchClasses === 'function', '应该有searchClasses方法');
  assertTrue(typeof searchEngine.searchMethods === 'function', '应该有searchMethods方法');
  assertTrue(typeof searchEngine.searchFields === 'function', '应该有searchFields方法');
  assertTrue(typeof searchEngine.searchAll === 'function', '应该有searchAll方法');
});

// 模糊搜索测试
runner.test('模糊搜索 - 分数排序', async () => {
  const javadocJsonPath = join(__dirname, '..', '..', 'javadoc-json');
  const dataLoader = new JavaDocDataLoader(javadocJsonPath);
  const javadocData = dataLoader.loadAllData();
  const searchEngine = new JavaDocSearchEngine(javadocData);
  
  const results = searchEngine.searchClasses('Test', 5, 'fuzzy');
  
  if (results.length > 1) {
    for (let i = 0; i < results.length - 1; i++) {
      assertTrue(results[i].score <= results[i + 1].score, 
                '模糊搜索结果应该按分数升序排列');
    }
  }
  
  results.forEach(result => {
    assertTrue(result.score !== undefined, '每个结果都应该有分数');
    assertTrue(result.score >= 0 && result.score <= 1, '分数应该在0-1之间');
  });
});

// 关键字搜索测试
runner.test('关键字搜索 - 精确匹配', async () => {
  const javadocJsonPath = join(__dirname, '..', '..', 'javadoc-json');
  const dataLoader = new JavaDocDataLoader(javadocJsonPath);
  const javadocData = dataLoader.loadAllData();
  const searchEngine = new JavaDocSearchEngine(javadocData);
  
  const results = searchEngine.searchClasses('TestClass', 10, 'keyword');
  
  results.forEach(result => {
    assertTrue(result.name.includes('TestClass'), 
              '关键字搜索结果应该包含搜索词');
  });
});

// 正则表达式搜索测试
runner.test('正则表达式搜索 - 模式匹配', async () => {
  const javadocJsonPath = join(__dirname, '..', '..', 'javadoc-json');
  const dataLoader = new JavaDocDataLoader(javadocJsonPath);
  const javadocData = dataLoader.loadAllData();
  const searchEngine = new JavaDocSearchEngine(javadocData);
  
  const results = searchEngine.searchClasses('^Test.*', 10, 'regex');
  
  results.forEach(result => {
    assertTrue(/^Test.*/.test(result.name), 
              '正则搜索结果应该匹配模式');
  });
});

// 字段搜索测试
runner.test('字段搜索 - 类型验证', async () => {
  const javadocJsonPath = join(__dirname, '..', '..', 'javadoc-json');
  const dataLoader = new JavaDocDataLoader(javadocJsonPath);
  const javadocData = dataLoader.loadAllData();
  const searchEngine = new JavaDocSearchEngine(javadocData);
  
  const results = searchEngine.searchFields('field', undefined, 10, 'fuzzy');
  
  results.forEach(result => {
    assertEqual(result.type, 'field', '搜索结果应该是字段类型');
    assertTrue(result.parentClass !== undefined, '字段应该有父类信息');
    assertTrue(result.name !== undefined, '字段应该有名称');
  });
});

// 方法搜索测试
runner.test('方法搜索 - 类型验证', async () => {
  const javadocJsonPath = join(__dirname, '..', '..', 'javadoc-json');
  const dataLoader = new JavaDocDataLoader(javadocJsonPath);
  const javadocData = dataLoader.loadAllData();
  const searchEngine = new JavaDocSearchEngine(javadocData);
  
  const results = searchEngine.searchMethods('method', undefined, 10, 'fuzzy');
  
  results.forEach(result => {
    assertTrue(['method', 'constructor'].includes(result.type), 
              '搜索结果应该是方法或构造函数类型');
    assertTrue(result.parentClass !== undefined, '方法应该有父类信息');
    assertTrue(result.name !== undefined, '方法应该有名称');
  });
});

// 类名过滤测试
runner.test('类名过滤 - 方法搜索', async () => {
  const javadocJsonPath = join(__dirname, '..', '..', 'javadoc-json');
  const dataLoader = new JavaDocDataLoader(javadocJsonPath);
  const javadocData = dataLoader.loadAllData();
  const searchEngine = new JavaDocSearchEngine(javadocData);
  
  // 先获取所有方法
  const allMethods = searchEngine.searchMethods('get', undefined, 20, 'fuzzy');
  
  if (allMethods.length > 0) {
    const targetClass = allMethods[0].parentClass;
    const filteredMethods = searchEngine.searchMethods('get', targetClass, 10, 'fuzzy');
    
    filteredMethods.forEach(result => {
      assertEqual(result.parentClass, targetClass, 
                 '过滤后的结果应该只包含指定类的方法');
    });
  }
});

// 限制数量测试
runner.test('结果数量限制', async () => {
  const javadocJsonPath = join(__dirname, '..', '..', 'javadoc-json');
  const dataLoader = new JavaDocDataLoader(javadocJsonPath);
  const javadocData = dataLoader.loadAllData();
  const searchEngine = new JavaDocSearchEngine(javadocData);
  
  const limit = 3;
  const results = searchEngine.searchAll('Test', limit, 'fuzzy');
  
  assertTrue(results.length <= limit, `结果数量应该不超过限制 ${limit}`);
});

// 空查询测试
runner.test('空查询处理', async () => {
  const javadocJsonPath = join(__dirname, '..', '..', 'javadoc-json');
  const dataLoader = new JavaDocDataLoader(javadocJsonPath);
  const javadocData = dataLoader.loadAllData();
  const searchEngine = new JavaDocSearchEngine(javadocData);
  
  const results = searchEngine.searchClasses('', 10, 'fuzzy');
  assertEqual(results.length, 0, '空查询应该返回空结果');
});

// 不存在的查询测试
runner.test('不存在的查询', async () => {
  const javadocJsonPath = join(__dirname, '..', '..', 'javadoc-json');
  const dataLoader = new JavaDocDataLoader(javadocJsonPath);
  const javadocData = dataLoader.loadAllData();
  const searchEngine = new JavaDocSearchEngine(javadocData);
  
  const results = searchEngine.searchClasses('NonExistentClassXYZ123', 10, 'fuzzy');
  assertEqual(results.length, 0, '不存在的查询应该返回空结果');
});

// 运行所有测试
runner.run();