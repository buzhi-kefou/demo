#!/bin/bash

# 项目快捷命令
# 使用方法：在项目根目录运行 ./qa.sh [command]

# 如果脚本被直接调用，确保在项目根目录
if [ "$(basename "$0")" = "qa.sh" ] && [ -f "scripts/record-save.js" ]; then
    # 已经在项目目录中
    PROJECT_DIR="$(pwd)"
else
    # 从其他地方调用，查找项目目录
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
    cd "$PROJECT_DIR"
fi

case "${1:-help}" in
    "init")
        echo "🚀 初始化项目..."
        node scripts/record-save.js init
        ;;
    "save")
        echo "📝 手动保存问答..."
        node scripts/record-save.js save "$2" "$3"
        ;;
    "status")
        echo "📊 查看记录状态..."
        node scripts/record-save.js status
        ;;
    "organize")
        echo "🔄 整理笔记..."
        node scripts/organize-notes.js
        ;;
    "recent")
        echo "📋 最近记录..."
        node scripts/record-save.js recent "${2:-5}"
        ;;
    "help"|*)
        echo "知识记录系统快捷命令"
        echo ""
        echo "用法：./qa.sh [command]"
        echo ""
        echo "命令："
        echo "  init     - 初始化项目"
        echo "  save     - 手动保存问答 (./qa.sh save \"问题\" \"答案\")"
        echo "  status   - 查看记录状态"
        echo "  organize - 整理笔记"
        echo "  recent   - 查看最近记录 (默认5条)"
        echo "  help     - 显示此帮助"
        ;;
esac