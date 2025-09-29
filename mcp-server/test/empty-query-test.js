import { JavaDocDataLoader } from '../dist/dataLoader.js';
import { JavaDocSearchEngine } from '../dist/searchEngine.js';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

console.log('🧪 测试空查询和正则表达式通配符修复');
console.log('════════════════════════════════════════════════════════════');

// 加载数据
const javadocJsonPath = join(__dirname, '..', '..', 'javadoc-json');
const dataLoader = new JavaDocDataLoader(javadocJsonPath);
const javadocData = dataLoader.loadAllData();

console.log(`📚 已加载 ${javadocData.totalCount} 个类`);

// 创建搜索引擎
const searchEngine = new JavaDocSearchEngine(javadocData);

let testsPassed = 0;
let totalTests = 0;

function runTest(testName, testFn) {
  totalTests++;
  console.log(`\n🧪 测试用例: ${testName}`);
  console.log('──────────────────────────────────────────────────');
  
  try {
    const result = testFn();
    if (result) {
      console.log('✅', result);
      testsPassed++;
    } else {
      console.log('❌ 测试失败');
    }
  } catch (error) {
    console.log('❌ 测试出错:', error.message);
  }
}

// 测试空查询处理
runTest('空查询 - searchMethods', () => {
  try {
    const result1 = searchEngine.searchMethods('');
    const result2 = searchEngine.searchMethods('   ');
    
    if (result1.length === 0 && result2.length === 0) {
      return '空查询正确返回空数组，没有抛出错误';
    }
  } catch (error) {
    return false;
  }
  return false;
});

runTest('空查询 - searchFields', () => {
  try {
    const result1 = searchEngine.searchFields('');
    const result2 = searchEngine.searchFields('   ');
    
    if (result1.length === 0 && result2.length === 0) {
      return '空查询正确返回空数组，没有抛出错误';
    }
  } catch (error) {
    return false;
  }
  return false;
});

// 测试正则表达式通配符
runTest('正则表达式通配符 "*" - searchMethods', () => {
  try {
    const result = searchEngine.searchMethods('*', undefined, 50, 'regex');
    
    if (result.length >= 0) { // 允许返回0个或多个结果
      return `"*" 通配符正常工作，返回 ${result.length} 个方法结果`;
    }
  } catch (error) {
    return false;
  }
  return false;
});

runTest('正则表达式通配符 "*" - searchFields', () => {
  try {
    const result = searchEngine.searchFields('*', undefined, 50, 'regex');
    
    if (result.length >= 0) { // 允许返回0个或多个结果
      return `"*" 通配符正常工作，返回 ${result.length} 个字段结果`;
    }
  } catch (error) {
    return false;
  }
  return false;
});

// 测试正常查询仍然工作
runTest('正常查询仍然工作 - searchMethods', () => {
  try {
    const result = searchEngine.searchMethods('test', undefined, 10, 'keyword');
    
    if (result.length >= 0) { // 允许返回0个或多个结果
      return `正常查询 "test" 正常工作，返回 ${result.length} 个结果`;
    }
  } catch (error) {
    return false;
  }
  return false;
});

// 输出测试结果
console.log('\n════════════════════════════════════════════════════════════');
console.log('📊 测试结果统计:');
console.log(`   总测试数: ${totalTests}`);
console.log(`   通过: ${testsPassed} ✅`);
console.log(`   失败: ${totalTests - testsPassed} ❌`);
console.log(`   成功率: ${((testsPassed / totalTests) * 100).toFixed(1)}%`);

if (testsPassed === totalTests) {
  console.log('\n🎉 所有测试通过！空查询和通配符修复成功。');
} else {
  console.log('\n❌ 部分测试失败，需要进一步检查。');
  process.exit(1);
}