#!/usr/bin/env node

/**
 * 精确整理脚本 - 按问题主题精确分类
 */

const fs = require('fs');
const path = require('path');

const config = {
  inboxDir: path.join(process.cwd(), 'Inbox'),
  notesDir: path.join(process.cwd(), 'Notes'),
  templatesDir: path.join(process.cwd(), 'Templates')
};

// 创建问题到主题的映射
const questionTopicMap = {
  '本地项目怎么上传github，给出完整步骤与说明': 'GitHub',
  'docker项目怎么离线部署，给出详细步骤与解释说明': 'Docker',
  '搜索 AgriciDaniel/claude-obsidian 项目，详细记录该如何使用该项目': 'Claude',
  '第二大脑': 'Claude',
  'wiki': 'Claude',
  'ingest': 'Claude',
  'git': 'GitHub',
  'repository': 'GitHub',
  'docker': 'Docker',
  'container': 'Docker',
  'image': 'Docker',
  '部署': 'Docker',
  '离线': 'Docker',
  '项目': '项目',
  '系统': '系统',
  '网络': '网络',
  '工具': '工具',
  '数据库': '数据库',
  '前端': '前端',
  '后端': '后端'
};

// 获取问题主题
function getQuestionTopic(question) {
  for (const [keyword, topic] of Object.entries(questionTopicMap)) {
    if (question.includes(keyword)) {
      return topic;
    }
  }
  return '其他';
}

// 提取问题和答案
function extractQaPairs(content) {
  const qaPairs = [];
  const lines = content.split('\n');
  let currentQa = null;

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];

    // 匹配问题标题（## 问题 X：格式）
    if (line.includes('问题') && line.includes('：')) {
      if (currentQa) {
        qaPairs.push(currentQa);
      }
      const questionMatch = line.match(/问题.*：(.*$)/);
      if (questionMatch) {
        currentQa = {
          question: questionMatch[1].trim(),
          answer: ''
        };
      }
    }
    // 匹配用户行
    else if (line.startsWith('**用户**：') && currentQa) {
      const userQuestion = line.replace('**用户**：', '').trim();
      if (currentQa.question !== userQuestion) {
        currentQa.question = userQuestion;
      }
    }
    // 匹配助手回答
    else if (line.startsWith('**助手**：') && currentQa) {
      currentQa.answer = line.replace('**助手**：', '').trim();
      // 追加后续行直到遇到新的标题
      for (let j = i + 1; j < lines.length; j++) {
        const nextLine = lines[j];
        if (nextLine.trim().startsWith('**') ||
            nextLine.includes('## 问题') ||
            nextLine.includes('---') ||
            nextLine.includes('<time>') ||
            nextLine.includes('##')) {
          break;
        }
        if (nextLine.trim()) {
          currentQa.answer += '\n' + nextLine.trim();
        }
      }
      // 跳过已处理的行
      i = i + currentQa.answer.split('\n').length;
    }
  }

  if (currentQa) {
    qaPairs.push(currentQa);
  }

  return qaPairs;
}

// 按主题分组问题
function groupQuestionsByTopic(qaPairs) {
  const topics = {};

  qaPairs.forEach(qa => {
    const topic = getQuestionTopic(qa.question);
    if (!topics[topic]) {
      topics[topic] = [];
    }
    topics[topic].push(qa);
  });

  return topics;
}

// 获取最近修改的文件
function getRecentFiles(days = 7) {
  // 检查 Inbox 目录
  if (!fs.existsSync(config.inboxDir)) {
    return [];
  }

  const files = fs.readdirSync(config.inboxDir);
  const now = new Date();

  return files
    .filter(file => file.endsWith('.md'))
    .map(file => {
      const filePath = path.join(config.inboxDir, file);
      const stats = fs.statSync(filePath);
      return {
        file,
        path: filePath,
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
  const qaPairs = extractQaPairs(content);
  const topics = groupQuestionsByTopic(qaPairs);

  const timestamp = item.date.toISOString().split('T')[0];
  const results = [];

  Object.entries(topics).forEach(([topic, questions]) => {
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

      questions.forEach((qa, index) => {
        topicContent += `\n## 问题 ${index + 1}：${qa.question}\n`;
        topicContent += `\n${qa.answer}\n`;
      });

      topicContent += `\n\n[[提问记录]]\n\n`;

      // 写入文件
      fs.writeFileSync(topicFile, topicContent);
      console.log(`✅ 已整理：${item.file} → ${topic}.md (${questions.length} 个问题)`);
      results.push(topic);
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
  console.log('🔍 开始精确整理会话记录...');

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

module.exports = { organizeFile, extractQaPairs, getQuestionTopic };