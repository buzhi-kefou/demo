#!/bin/bash

# 知识整理便捷启动脚本

echo "🚀 知识整理系统"
echo "=================="
echo ""

# 检查脚本是否存在
if [ ! -f "scripts/organize-notes.js" ]; then
    echo "❌ 错误：找不到整理脚本"
    exit 1
fi

# 检查 Inbox 目录
if [ ! -d "Inbox" ]; then
    echo "⚠️  警告：Inbox 目录不存在，将自动创建"
    mkdir -p Inbox
fi

# 检查 Notes 目录
if [ ! -d "Notes" ]; then
    echo "⚠️  警告：Notes 目录不存在，将自动创建"
    mkdir -p Notes
fi

echo "📋 整理选项："
echo "1. 立即整理所有记录"
echo "2. 查看记录状态"
echo "3. 整理最近7天的记录"
echo "4. 整理最近30天的记录"
echo "5. 初始化项目（首次使用）"
echo "6. 退出"
echo ""

read -p "请选择 (1-6): " choice

case $choice in
    1)
        echo "🔄 开始整理所有记录..."
        node scripts/organize-notes.js
        ;;
    2)
        echo "📊 查看记录状态..."
        if [ -f ".claude/session-history.json" ]; then
            total_records=$(jq '. | length' .claude/session-history.json 2>/dev/null || echo "0")
            echo "总记录数: $total_records"
        else
            echo "暂无记录"
        fi
        ;;
    3)
        echo "🔄 整理最近7天的记录..."
        node scripts/organize-notes.js
        ;;
    4)
        echo "🔄 整理最近30天的记录..."
        node scripts/organize-notes.js
        ;;
    5)
        echo "🚀 初始化项目..."
        node scripts/record-save.js init
        ;;
    6)
        echo "👋 再见！"
        exit 0
        ;;
    *)
        echo "❌ 无效选择"
        exit 1
        ;;
esac

echo ""
echo "✅ 整理完成！"
echo ""
echo "📁 Notes/ 目录包含以下主题："
ls -1 Notes/ | sed 's/^/  - /' 2>/dev/null || echo "  (暂无主题笔记)"