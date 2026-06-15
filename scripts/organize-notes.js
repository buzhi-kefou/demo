#!/usr/bin/env node

/**
 * 会话记录整理脚本
 * 自动将 Inbox/ 目录中的原始记录按主题整理到 Notes/ 目录
 */

const fs = require('fs');
const path = require('path');

// 配置
const config = {
  inboxDir: path.join(process.cwd(), 'Inbox'),
  notesDir: path.join(process.cwd(), 'Notes'),
  templatesDir: path.join(process.cwd(), 'Templates')
};

// 检查并创建必要目录
if (!fs.existsSync(config.notesDir)) {
  fs.mkdirSync(config.notesDir, { recursive: true });
}
if (!fs.existsSync(config.templatesDir)) {
  fs.mkdirSync(config.templatesDir, { recursive: true });
}

// 主题关键词映射
const topicKeywords = {
  'GitHub': ['github', 'git', '仓库', '上传', '提交', '推送', '克隆', '远程', 'repository', 'commit'],
  'Docker': ['docker', '镜像', '容器', '部署', '离线', '启动', '停止', '导入', 'image', 'container', 'deploy'],
  'Claude': ['claude', 'assistant', 'ai', 'chatgpt', '对话', '问答', 'claude-code', 'wiki', 'ingest'],
  '项目': ['项目', '工程', '应用', '功能', '开发', '实现', 'project', 'feature', 'application'],
  '系统': ['系统', '配置', '环境', '安装', '设置', '权限', 'system', 'config', 'install', 'setup'],
  '网络': ['网络', '代理', '端口', '连接', '访问', '服务', 'network', 'proxy', 'port', 'connection'],
  '数据库': ['数据库', 'sql', 'mysql', 'postgres', '存储', '查询', 'database', 'sql', 'storage', 'query'],
  '前端': ['前端', 'html', 'css', 'js', 'vue', 'react', '界面', 'frontend', 'html', 'css', 'javascript'],
  '后端': ['后端', 'api', '服务', '接口', '服务器', '架构', 'backend', 'api', 'server', 'service'],
  '工具': ['工具', '软件', '插件', '脚本', '命令', '自动化', 'tool', 'software', 'plugin', 'script', 'command']
};

// 获取主题关键词
function getTopicKeywords(content) {
  const lowerContent = content.toLowerCase();
  const topics = {};

  Object.entries(topicKeywords).forEach(([topic, keywords]) => {
    const score = keywords.reduce((acc, keyword) => {
      const count = (lowerContent.match(new RegExp(keyword, 'g')) || []).length;
      return acc + count;
    }, 0);

    if (score > 0) {
      topics[topic] = score;
    }
  });

  // 返回按分数排序的主题列表
  const sortedTopics = Object.entries(topics)
    .sort((a, b) => b[1] - a[1])
    .map(([topic]) => topic);

  // 如果没有匹配的主题，返回 '其他'
  return sortedTopics.length > 0 ? sortedTopics : ['其他'];
}

// 提取问题和答案
function extractQaPairs(content) {
  const qaPairs = [];
  const lines = content.split('\n');
  let currentQa = null;

  lines.forEach(line => {
    // 匹配问题标题（支持两种格式）
    if (line.includes('问题：')) {
      if (currentQa) {
        qaPairs.push(currentQa);
      }
      const questionMatch = line.match(/问题：(.*)$/);
      if (questionMatch) {
        currentQa = {
          question: questionMatch[1].trim(),
          answer: ''
        };
      }
    }
    // 匹配用户
    else if (line.startsWith('**用户**：') && currentQa) {
      const userQuestion = line.replace('**用户**：', '').trim();
      // 如果问题和用户内容相同，跳过重复
      if (currentQa.question !== userQuestion) {
        currentQa.question = userQuestion;
      }
    }
    // 匹配助手回答
    else if (line.startsWith('**助手**：') && currentQa) {
      currentQa.answer = line.replace('**助手**：', '').trim();
    }
    // 追加助手回答的后续内容
    else if (currentQa && currentQa.answer && line.trim() && !line.startsWith('##')) {
      // 如果当前行不是新的问题标题，追加到回答
      currentQa.answer += '\n' + line.trim();
    }
  });

  if (currentQa) {
    qaPairs.push(currentQa);
  }

  return qaPairs;
}

// 获取最近修改的文件
function getRecentFiles(days = 7) {
  const files = fs.readdirSync(config.inboxDir);
  const now = new Date();

  return files
    .filter(file => file.endsWith('.md'))
    .map(file => {
      const stats = fs.statSync(path.join(config.inboxDir, file));
      return {
        file,
        path: path.join(config.inboxDir, file),
        stats,
        date: new Date(stats.mtime)
      };
    })
    .filter(item => (now - item.date) / (1000 * 60 * 60 * 24) <= days)
    .sort((a, b) => b.date - a.date);
}

// 整理文件
function organizeFile(item) {
  const content = fs.readFileSync(item.path, 'utf8');
  const topics = getTopicKeywords(content);
  const qaPairs = extractQaPairs(content);

  const timestamp = item.date.toISOString().split('T')[0];
  const results = [];

  // 为每个主题创建或更新笔记
  topics.forEach(topic => {
    const topicFile = path.join(config.notesDir, `${topic}.md`);

    let topicContent = '';
    if (fs.existsSync(topicFile)) {
      topicContent = fs.readFileSync(topicFile, 'utf8');
    }

    // 检查是否已包含该日期的记录
    const dateHeader = `# ${timestamp}`;
    if (!topicContent.includes(dateHeader)) {
      if (topicContent && !topicContent.trim().endsWith('---\n\n')) {
        topicContent += '\n\n---\n\n';
      }
      topicContent += dateHeader + '\n';

      // 只添加与当前主题相关的问题
      qaPairs.forEach((qa, index) => {
        // 检查问题内容是否包含主题关键词
        const lowerQuestion = qa.question.toLowerCase();
        const lowerAnswer = qa.answer.toLowerCase();

        // 检查关键词映射
        const topicKeywordsList = topicKeywords[topic] || [];
        const hasKeyword = topicKeywordsList.some(keyword =>
          lowerQuestion.includes(keyword) || lowerAnswer.includes(keyword)
        );

        if (hasKeyword) {
          topicContent += `\n## 问题 ${index + 1}：${qa.question}\n`;
          topicContent += `\n${qa.answer}\n`;
        }
      });

      // 只有添加了相关内容才保存
      if (topicContent.includes('## 问题')) {
        topicContent += `\n\n[[提问记录]]\n\n`;

        // 写入文件
        fs.writeFileSync(topicFile, topicContent);
        console.log(`✅ 已整理：${item.file} → ${topic}.md`);
        results.push(topic);
      }
    }
  });

  return results.length > 0 ? {
    file: item.file,
    topics: results,
    date: timestamp,
    questions: qaPairs.length
  } : null;
}

// 主函数
function main() {
  console.log('🔍 开始整理会话记录...');

  const recentFiles = getRecentFiles();
  console.log(`📁 找到 ${recentFiles.length} 个最近文件需要整理`);

  const organized = [];
  const topics = {};

  recentFiles.forEach(item => {
    const result = organizeFile(item);
    if (result) {
      organized.push(result);
      result.topics.forEach(topic => {
        topics[topic] = (topics[topic] || 0) + 1;
      });
    }
  });

  console.log('\n📊 整理完成：');
  console.log(`- 整理了 ${organized.length} 条记录`);
  console.log(`- 涉及 ${Object.keys(topics).length} 个主题`);

  Object.entries(topics).forEach(([topic, count]) => {
    console.log(`  - ${topic}：${count} 条`);
  });

  // 移动已整理的文件到归档目录
  const archiveDir = path.join(config.inboxDir, '_archive');
  if (!fs.existsSync(archiveDir)) {
    fs.mkdirSync(archiveDir, { recursive: true });
  }

  recentFiles.forEach(item => {
    const dest = path.join(archiveDir, item.file);
    fs.renameSync(item.path, dest);
  });

  console.log(`\n📂 已将整理后的文件移动到 ${archiveDir}`);
}

// 运行脚本
if (require.main === module) {
  main();
}

module.exports = { organizeFile, getTopicKeywords, extractQaPairs };