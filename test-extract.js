const fs = require('fs');

// 直接定义主题映射
const questionTopicMap = {
  'docker常用命令': 'Docker',
  'docker常用命令有哪些': 'Docker',
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
  '后端': '后端',
  '命令': '工具',
  '技术': '系统'
};

// 读取文件
const content = fs.readFileSync('Inbox/_archive/2026-06-15.md', 'utf8');

// 使用简单的字符串匹配
const questionMatch = content.match(/\*\*用户\*\*：(.+?)\n\*\*助手\*\*/);
if (questionMatch) {
  const question = questionMatch[1].trim();
  console.log('Question:', question);

  // 检测主题
  let detectedTopic = '其他';
  for (const [keyword, topic] of Object.entries(questionTopicMap)) {
    if (question.includes(keyword)) {
      detectedTopic = topic;
      break;
    }
  }

  console.log('Detected topic:', detectedTopic);

  // 写入到对应的主题文件
  const topicFile = `Notes/${detectedTopic}.md`;
  const topicContent = `\n\n## 问题：${question}\n\n整理了 Docker 常用命令详解，分为以下几大类：\n\n1. **镜像管理**\n   - 拉取镜像：\`docker pull\`\n   - 查看镜像：\`docker images\`\n   - 删除镜像：\`docker rmi\`\n   - 构建镜像：\`docker build\`\n\n2. **容器管理**\n   - 运行容器：\`docker run\`\n   - 查看容器：\`docker ps\`\n   - 停止/启动/重启容器：\`docker stop/start/restart\`\n   - 删除容器：\`docker rm\`\n   - 进入容器：\`docker exec\`\n\n3. **数据管理**\n   - 卷管理：\`docker volume\`\n   - 备份和恢复：\`docker save/load\`\n\n4. **网络管理**\n   - 网络操作：\`docker network\`\n   - 端口映射：\`docker port\`\n\n5. **系统管理**\n   - 系统信息：\`docker version/info\`\n   - 资源清理：\`docker system prune\`\n\n6. **Docker Compose**\n   - 服务管理：\`docker-compose up/down\`\n   - 查看日志：\`docker-compose logs\`\n\n7. **安全相关**\n   - 安全扫描：\`docker scan\`\n   - 查看镜像历史：\`docker history\`\n\n8. **调试和故障排除**\n   - 故障排查命令\n   - 性能分析工具\n\n9. **高级操作**\n   - 多阶段构建\n   - 资源限制\n   - 健康检查\n\n还提供了具体的使用示例和命令详解。\n\n[[提问记录]]\n\n#Docker #技术笔记 #命令大全`;

  // 检查文件是否已存在
  let existingContent = '';
  if (fs.existsSync(topicFile)) {
    existingContent = fs.readFileSync(topicFile, 'utf8');
  }

  // 添加今天的日期标记
  const today = new Date().toISOString().split('T')[0];
  const dateHeader = `# ${today}`;

  if (!existingContent.includes(dateHeader)) {
    if (existingContent && !existingContent.endsWith('\n\n')) {
      existingContent += '\n\n';
    }
    if (!existingContent.includes(dateHeader)) {
      existingContent += dateHeader + topicContent;
    }

    fs.writeFileSync(topicFile, existingContent);
    console.log(`✅ 已添加到 ${topicFile}`);
  } else {
    console.log('⚠️  今天的内容已存在');
  }
} else {
  console.log('❌ 未找到问答内容');
}