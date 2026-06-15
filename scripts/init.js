#!/usr/bin/env node

/**
 * 项目初始化脚本
 * 在进入项目目录时自动运行，设置环境
 */

const fs = require('fs');
const path = require('path');

// 配置
const config = {
  scriptsDir: path.join(__dirname),
  projectRoot: process.cwd()
};

// 检查是否在项目根目录
function checkInProject() {
  const requiredFiles = [
    path.join(config.projectRoot, 'README.md'),
    path.join(config.projectRoot, 'scripts', 'record-save.js')
  ];

  return requiredFiles.every(file => fs.existsSync(file));
}

// 初始化项目
function initProject() {
  console.log('🚀 检测到自动记录项目，正在初始化...');

  // 检查是否已经初始化过
  const initMarker = path.join(config.projectRoot, '.claude', 'initialized');
  if (fs.existsSync(initMarker)) {
    console.log('✅ 项目已经初始化过');
    return;
  }

  // 创建必要的目录
  const dirs = [
    path.join(config.projectRoot, 'Inbox'),
    path.join(config.projectRoot, 'Notes'),
    path.join(config.projectRoot, 'Templates'),
    path.join(config.projectRoot, '.claude')
  ];

  dirs.forEach(dir => {
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
      console.log(`✅ 创建目录: ${path.relative(config.projectRoot, dir)}`);
    }
  });

  // 初始化记录系统
  const { spawn } = require('child_process');
  const child = spawn('node', [path.join(config.scriptsDir, 'record-save.js'), 'init'], {
    stdio: 'inherit',
    cwd: config.projectRoot
  });

  child.on('close', (code) => {
    if (code === 0) {
      // 创建初始化标记
      fs.writeFileSync(initMarker, 'project initialized');
      console.log('\n🎉 项目初始化完成！');
      console.log('\n💡 接下来你可以：');
      console.log('1. 运行 "./start.sh" 使用交互式菜单');
      console.log('2. 运行 "node scripts/record-save.js status" 查看记录状态');
      console.log('3. 开始与 Claude 对话，问答将自动记录');
    } else {
      console.error('❌ 初始化失败');
    }
  });
}

// 主函数
function main() {
  if (checkInProject()) {
    initProject();
  } else {
    // 可选：自动检测并初始化新项目
    console.log('ℹ️  提示：这是一个知识记录项目');
    console.log('💡 如需初始化，请运行: node scripts/init.js');
  }
}

// 如果直接运行此脚本
if (require.main === module) {
  main();
}

module.exports = { initProject, checkInProject };