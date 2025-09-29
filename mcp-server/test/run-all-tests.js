#!/usr/bin/env node

import { spawn } from 'child_process';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';
import { readdir } from 'fs/promises';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

async function runAllTests() {
  console.log('🚀 运行所有测试...\n');
  console.log('═'.repeat(60));
  
  try {
    // 获取测试目录中的所有测试文件
    const testFiles = await readdir(__dirname);
    const testScripts = testFiles.filter(file => 
      file.endsWith('-test.js') && file !== 'run-all-tests.js'
    );
    
    if (testScripts.length === 0) {
      console.log('❌ 没有找到测试文件');
      return;
    }
    
    console.log(`📋 找到 ${testScripts.length} 个测试文件:`);
    testScripts.forEach(file => console.log(`   - ${file}`));
    console.log();
    
    let totalPassed = 0;
    let totalFailed = 0;
    
    // 逐个运行测试文件
    for (const testFile of testScripts) {
      console.log(`🧪 运行测试: ${testFile}`);
      console.log('─'.repeat(50));
      
      const testPath = join(__dirname, testFile);
      
      try {
        await runTest(testPath);
        console.log(`✅ ${testFile} 完成\n`);
      } catch (error) {
        console.log(`❌ ${testFile} 失败: ${error.message}\n`);
        totalFailed++;
      }
    }
    
    // 输出总结
    console.log('═'.repeat(60));
    console.log('📊 所有测试完成');
    console.log(`   测试文件: ${testScripts.length}`);
    console.log(`   成功: ${testScripts.length - totalFailed} ✅`);
    console.log(`   失败: ${totalFailed} ❌`);
    
    if (totalFailed === 0) {
      console.log('\n🎉 所有测试通过！');
    } else {
      console.log('\n⚠️  部分测试失败，请检查上面的错误信息。');
      process.exit(1);
    }
    
  } catch (error) {
    console.error('❌ 运行测试时发生错误:', error);
    process.exit(1);
  }
}

function runTest(testPath) {
  return new Promise((resolve, reject) => {
    const child = spawn('node', [testPath], {
      stdio: 'inherit',
      cwd: dirname(testPath)
    });
    
    child.on('close', (code) => {
      if (code === 0) {
        resolve();
      } else {
        reject(new Error(`测试退出码: ${code}`));
      }
    });
    
    child.on('error', (error) => {
      reject(error);
    });
  });
}

// 运行所有测试
runAllTests();