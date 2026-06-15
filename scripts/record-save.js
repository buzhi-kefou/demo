#!/usr/bin/env node

/**
 * 改进的自动会话记录脚本
 * 支持从环境变量或标准输入获取问答内容
 */

const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');

// 配置
const config = {
  inboxDir: path.join(process.cwd(), 'Inbox'),
  sessionHistoryFile: path.join(process.cwd(), '.claude', 'session-history.json'),
  currentSessionFile: path.join(process.cwd(), '.claude', 'current-session.json')
};

// 检查并创建必要目录
if (!fs.existsSync(config.inboxDir)) {
  fs.mkdirSync(config.inboxDir, { recursive: true });
}
if (!fs.existsSync(path.dirname(config.sessionHistoryFile))) {
  fs.mkdirSync(path.dirname(config.sessionHistoryFile), { recursive: true });
}

// 从 Claude Code 获取当前会话信息
function getCurrentSessionInfo() {
  const timestamp = new Date().toISOString();

  // 尝试从 Claude Code 获取会话信息
  const sessionInfo = {
    timestamp,
    cwd: process.cwd(),
    user: process.env.USER || process.env.LOGNAME || 'unknown',
    session: process.env.CLAUDE_SESSION_ID || 'unknown'
  };

  // 如果是钩子调用，从环境变量获取
  if (process.env.CLAUDE_QUESTION && process.env.CLAUDE_ANSWER) {
    sessionInfo.question = process.env.CLAUDE_QUESTION;
    sessionInfo.answer = process.env.CLAUDE_ANSWER;
  }

  // 保存会话信息供后续使用
  try {
    fs.writeFileSync(config.currentSessionFile, JSON.stringify(sessionInfo, null, 2));
  } catch (e) {
    console.error('无法保存会话信息:', e.message);
  }

  return sessionInfo;
}

// 获取今天的日期
function getToday() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
}

// 格式化问答内容
function formatQaContent(question, answer) {
  const timestamp = new Date().toISOString();
  const date = timestamp.split('T')[0];
  const time = timestamp.split('T')[1].split('.')[0];

  return `## 问题：${question}

**用户**：${question}

**助手**：${answer}

<time>${time}</time>

`;
}

// 保存问答内容
function saveQa(question, answer) {
  const today = getToday();
  const sessionInfo = getCurrentSessionInfo();
  const content = formatQaContent(question, answer);

  // 检查是否已有今天的记录文件
  const todayFile = path.join(config.inboxDir, `${today}.md`);

  let header = '';
  if (!fs.existsSync(todayFile)) {
    header = `# ${today} 会话记录\n\n`;
  }

  // 写入文件
  fs.appendFileSync(todayFile, header + content);

  // 记录到会话历史
  const history = {
    date: today,
    timestamp: sessionInfo.timestamp,
    cwd: sessionInfo.cwd,
    user: sessionInfo.user,
    session: sessionInfo.session,
    question: question.substring(0, 100), // 只保存前100个字符
    saved: true
  };

  let historyData = [];
  if (fs.existsSync(config.sessionHistoryFile)) {
    try {
      historyData = JSON.parse(fs.readFileSync(config.sessionHistoryFile, 'utf8'));
    } catch (e) {
      // 忽略错误
    }
  }

  historyData.push(history);
  fs.writeFileSync(config.sessionHistoryFile, JSON.stringify(historyData, null, 2));

  console.log(`📝 已保存问答记录到 ${todayFile}`);
  return todayFile;
}

// 从 Claude Code 会话历史获取问答
function extractQaFromSession() {
  try {
    // 获取 Claude Code 的会话历史
    const result = execSync('claude --session-history', { encoding: 'utf8' });
    if (result) {
      const sessions = JSON.parse(result);
      if (sessions.length > 0) {
        const lastSession = sessions[sessions.length - 1];
        return {
          question: lastSession.question || '',
          answer: lastSession.answer || ''
        };
      }
    }
  } catch (e) {
    // 如果获取失败，尝试从当前会话文件读取
    if (fs.existsSync(config.currentSessionFile)) {
      try {
        const sessionInfo = JSON.parse(fs.readFileSync(config.currentSessionFile, 'utf8'));
        if (sessionInfo.question && sessionInfo.answer) {
          return {
            question: sessionInfo.question,
            answer: sessionInfo.answer
          };
        }
      } catch (e2) {
        console.error('读取会话信息失败:', e2.message);
      }
    }
  }

  return null;
}

// 获取最近的问答
function getRecentQa(limit = 10) {
  if (!fs.existsSync(config.sessionHistoryFile)) {
    return [];
  }

  try {
    const historyData = JSON.parse(fs.readFileSync(config.sessionHistoryFile, 'utf8'));
    return historyData.slice(-limit).reverse();
  } catch (e) {
    return [];
  }
}

// 清理旧的会话历史
function cleanupOldHistory(days = 30) {
  if (!fs.existsSync(config.sessionHistoryFile)) {
    return;
  }

  try {
    const historyData = JSON.parse(fs.readFileSync(config.sessionHistoryFile, 'utf8'));
    const cutoffDate = new Date();
    cutoffDate.setDate(cutoffDate.getDate() - days);

    const filtered = historyData.filter(item =>
      new Date(item.timestamp) > cutoffDate
    );

    fs.writeFileSync(config.sessionHistoryFile, JSON.stringify(filtered, null, 2));
    console.log(`🧹 已清理 ${historyData.length - filtered.length} 条过期历史记录`);
  } catch (e) {
    console.error('清理历史记录时出错:', e.message);
  }
}

// 主函数
function main() {
  const args = process.argv.slice(2);

  // 如果是从钩子调用
  if (args[0] === 'auto-save') {
    // 尝试从环境变量获取问答
    const question = process.env.CLAUDE_QUESTION || '';
    const answer = process.env.CLAUDE_ANSWER || '';

    if (question && answer) {
      saveQa(question, answer);
    } else {
      // 如果环境变量没有，尝试从会话获取
      const qa = extractQaFromSession();
      if (qa) {
        saveQa(qa.question, qa.answer);
      } else {
        console.log('⚠️  无法获取问答内容，跳过保存');
      }
    }
    return;
  }

  if (args[0] === 'save') {
    // 手动保存模式
    const question = args[1] || '';
    const answer = args[2] || '';

    if (!question || !answer) {
      console.log('❌ 请提供问题和答案');
      console.log('用法: node scripts/record-save.js save "<问题>" "<答案>"');
      return;
    }

    saveQa(question, answer);
  } else if (args[0] === 'recent') {
    const limit = parseInt(args[1]) || 10;
    const recent = getRecentQa(limit);
    console.log(`最近 ${limit} 条问答记录：`);
    recent.forEach(item => {
      console.log(`- ${item.date}: ${item.question}`);
    });
  } else if (args[0] === 'cleanup') {
    const days = parseInt(args[1]) || 30;
    cleanupOldHistory(days);
  } else if (args[0] === 'status') {
    if (fs.existsSync(config.sessionHistoryFile)) {
      try {
        const historyData = JSON.parse(fs.readFileSync(config.sessionHistoryFile, 'utf8'));
        console.log(`共保存 ${historyData.length} 条问答记录`);

        const today = getToday();
        const todayCount = historyData.filter(item => item.date === today).length;
        console.log(`今天记录：${todayCount} 条`);

        const uniqueDays = new Set(historyData.map(item => item.date)).size;
        console.log(`共 ${uniqueDays} 天的记录`);
      } catch (e) {
        console.log('读取历史记录失败:', e.message);
      }
    } else {
      console.log('暂无问答记录');
    }
  } else if (args[0] === 'init') {
    // 初始化项目
    console.log('🚀 初始化自动记录项目...');

    // 创建必要的目录
    [config.inboxDir, path.dirname(config.sessionHistoryFile)].forEach(dir => {
      if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
        console.log(`✅ 创建目录: ${dir}`);
      }
    });

    // 创建一个示例记录
    const exampleContent = formatQaContent(
      '如何使用这个自动记录系统？',
      '这个系统会在每次 Claude 会话结束后自动记录问答内容。记录会保存到 Inbox/ 目录，然后你可以使用整理脚本按主题分类到 Notes/ 目录。'
    );

    const todayFile = path.join(config.inboxDir, `${getToday()}.md`);
    fs.writeFileSync(todayFile, `# ${getToday()} 会话记录\n\n` + exampleContent);
    console.log(`✅ 创建示例记录: ${todayFile}`);

    console.log('\n📝 项目已初始化完成！');
    console.log('💡 提示：运行 "node scripts/record-save.js status" 查看记录状态');
  } else {
    console.log('使用方法：');
    console.log('  node scripts/record-save.js init              # 初始化项目（首次使用）');
    console.log('  node scripts/record-save.js save "<问题>" "<答案>"  # 手动保存');
    console.log('  node scripts/record-save.js recent [数量]      # 查看最近记录');
    console.log('  node scripts/record-save.js cleanup [天数]    # 清理过期记录');
    console.log('  node scripts/record-save.js status            # 查看记录状态');
  }
}

// 辅助函数：同步执行命令
function execSync(command, options) {
  try {
    return require('child_process').execSync(command, options);
  } catch (e) {
    return null;
  }
}

// 运行脚本
if (require.main === module) {
  main();
}

module.exports = { saveQa, getRecentQa, cleanupOldHistory, extractQaFromSession };