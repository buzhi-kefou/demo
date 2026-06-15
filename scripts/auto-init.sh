#!/bin/bash

# 项目自动初始化脚本
# 将此脚本放在 ~/.bashrc 或 ~/.zshrc 中自动运行

# 获取当前脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 检查是否在项目目录中
if [ -f "$SCRIPT_DIR/README.md" ] && [ -f "$SCRIPT_DIR/scripts/record-save.js" ]; then
    echo "🔍 检测到知识记录项目..."

    # 检查是否已经初始化
    if [ ! -f "$SCRIPT_DIR/.claude/initialized" ]; then
        echo "🚀 正在初始化项目..."
        node "$SCRIPT_DIR/scripts/record-save.js" init
    fi

    # 显示快捷命令提示
    echo ""
    echo "💡 可用命令："
    echo "  • ./start.sh    - 启动交互式菜单"
    echo "  • node scripts/record-save.js status  - 查看记录状态"
    echo "  • node scripts/organize-notes.js       - 整理笔记"
    echo ""
fi