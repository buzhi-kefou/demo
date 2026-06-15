#!/usr/bin/env node

/**
 * 演示自动保存功能
 * 模拟 Claude 会话结束时的自动记录
 */

const { saveQa } = require('./record-save.js');

// 模拟问答内容
const demoQaPairs = [
  {
    question: "如何使用这个自动记录系统？",
    answer: "这个系统会在每次 Claude 会话结束后自动记录问答内容。记录会保存到 Inbox/ 目录，然后你可以使用整理脚本按主题分类到 Notes/ 目录。"
  },
  {
    question: "如何整理笔记？",
    answer: "使用 './start.sh' 或 'node scripts/organize-notes.js' 整理笔记。脚本会分析内容主题并创建相应的主题笔记。"
  },
  {
    question: "支持哪些主题？",
    answer: "系统支持 GitHub、Docker、Claude、项目、系统、网络、数据库、前端、后端、工具等多个主题。会根据内容关键词自动分类。"
  }
];

// 演示保存功能
console.log('🎬 演示自动保存功能...\n');

demoQaPairs.forEach((qa, index) => {
  console.log(`💬 会话 ${index + 1}:`);
  console.log(`问题: ${qa.question}`);
  console.log(`答案: ${qa.answer}`);

  // 模拟环境变量（实际使用时 Claude Code 会自动设置）
  process.env.CLAUDE_QUESTION = qa.question;
  process.env.CLAUDE_ANSWER = qa.answer;

  // 调用保存函数
  saveQa(qa.question, qa.answer);

  console.log('✅ 已保存\n');
});

console.log('🎉 演示完成！现在你可以：');
console.log('1. 运行 "./qa.sh status" 查看记录');
console.log('2. 运行 "./qa.sh organize" 整理笔记');
console.log('3. 查看 Notes/ 目录下的主题笔记');