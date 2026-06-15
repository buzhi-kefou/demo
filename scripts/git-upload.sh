#!/bin/bash

# GitHub 上传自动化脚本
# 使用方法：./git-upload.sh [repository-name]

set -e

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
GITHUB_USER="your-username"  # 修改为你的 GitHub 用户名
EMAIL="your-email@example.com"  # 修改为你的邮箱

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 检查 Git 是否已安装
check_git() {
    if ! command -v git &> /dev/null; then
        log_error "Git 未安装，请先安装 Git"
        exit 1
    fi
}

# 检查是否在 Git 仓库中
check_git_repo() {
    if ! git rev-parse --is-inside-work-tree > /dev/null 2>&1; then
        log_warn "当前目录不是 Git 仓库，将初始化..."
        git init
    fi
}

# 检查是否有未提交的更改
check_unstaged_changes() {
    if ! git diff --quiet; then
        log_warn "有未暂存的更改，请先提交"
        echo "未暂存的文件："
        git diff --name-only
        echo ""
        read -p "是否提交这些更改？(y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            git add .
            git commit -m "提交未暂存的更改"
        else
            log_info "跳过未暂存的更改"
        fi
    fi
}

# 创建 .gitignore 文件
create_gitignore() {
    if [ ! -f ".gitignore" ]; then
        log_step "创建 .gitignore 文件..."
        cat > .gitignore << EOF
# 依赖
node_modules/
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*

# 环境变量
.env
.env.local
.env.development.local
.env.test.local
.env.production.local

# 系统文件
.DS_Store
Thumbs.db

# IDE 文件
.vscode/
.idea/
*.swp
*.swo

# 构建产物
dist/
build/
*.tar.gz
*.zip

# 缓存
.cache/
.temp/

# 临时文件
*.tmp
*.bak
EOF
        git add .gitignore
        git commit -m "feat: 添加 .gitignore 文件"
    fi
}

# 初始化 Git 配置
init_git_config() {
    log_step "检查 Git 配置..."

    # 检查用户名
    if ! git config user.name > /dev/null 2>&1; then
        read -p "请输入 Git 用户名 [$GITHUB_USER]: " username
        username=${username:-$GITHUB_USER}
        git config user.name "$username"
    fi

    # 检查邮箱
    if ! git config user.email > /dev/null 2>&1; then
        read -p "请输入 Git 邮箱 [$EMAIL]: " email
        email=${email:-$EMAIL}
        git config user.email "$email"
    fi

    log_info "Git 配置完成"
    log_info "用户名: $(git config user.name)"
    log_info "邮箱: $(git config user.email)"
}

# 创建 GitHub 仓库
create_github_repo() {
    local repo_name=$1

    if [ -z "$repo_name" ]; then
        repo_name=$(basename $(pwd))
    fi

    log_step "检查是否已连接到 GitHub CLI..."

    if command -v gh &> /dev/null; then
        # 使用 GitHub CLI 创建仓库
        log_info "使用 GitHub CLI 创建仓库..."
        gh repo create "$repo_name" --public --source=. --push --remote=origin
        log_info "仓库创建成功！"
    else
        log_warn "GitHub CLI 未安装，请手动创建仓库："
        echo "1. 访问 https://github.com/new"
        echo "2. Repository name: $repo_name"
        echo "3. 选择 Public/Private"
        echo "4. 勾选 'Add a README file'"
        echo "5. 点击 'Create repository'"
        echo ""
        read -p "创建完成后按回车继续..."

        # 手动添加远程仓库
        read -p "请输入 GitHub 仓库地址 (格式: git@github.com:$GITHUB_USER/$repo_name.git): " repo_url

        if [ -z "$repo_url" ]; then
            repo_url="git@github.com:$GITHUB_USER/$repo_name.git"
        fi

        git remote add origin "$repo_url"
    fi
}

# 上传项目
upload_project() {
    local repo_name=$1

    log_step "开始上传项目..."

    # 添加所有文件
    git add .

    # 检查是否有文件需要提交
    if git diff --cached --quiet; then
        log_info "没有需要提交的文件"
        return
    fi

    # 提交
    read -p "请输入提交信息: " commit_msg
    commit_msg=${commit_msg:-"feat: 初始化项目"}

    git commit -m "$commit_msg"

    # 推送到 GitHub
    git push -u origin main

    log_info "项目上传成功！"
    log_info "访问地址: https://github.com/$GITHUB_USER/$repo_name"
}

# 显示仓库信息
show_repo_info() {
    local repo_name=$1

    if [ -z "$repo_name" ]; then
        repo_name=$(basename $(pwd))
    fi

    echo ""
    echo "=========================================="
    echo "            仓库信息"
    echo "=========================================="
    echo ""
    echo "仓库名: $repo_name"
    echo "用户名: $GITHUB_USER"
    echo "远程地址: $(git remote get-url origin 2>/dev/null || echo '未设置')"
    echo "当前分支: $(git branch --show-current 2>/dev/null || echo '未知')"
    echo ""
    echo "常用命令："
    echo "  git status          # 查看状态"
    echo "  git add .           # 添加所有文件"
    echo "  git commit -m \"msg\" # 提交"
    echo "  git push            # 推送到 GitHub"
    echo "  git pull            # 从 GitHub 拉取"
    echo ""
}

# 主菜单
show_menu() {
    echo ""
    echo "=========================================="
    echo "      GitHub 上传工具"
    echo "=========================================="
    echo ""
    echo "请选择操作："
    echo "1. 初始化项目并上传"
    echo "2. 仅初始化 Git 仓库"
    echo "3. 仅上传已初始化的项目"
    echo "4. 查看仓库信息"
    echo "5. 创建新的分支"
    echo "6. 创建标签并推送"
    echo "7. 退出"
    echo ""
    read -p "请输入选择 (1-7): " choice
}

# 创建分支
create_branch() {
    read -p "请输入新分支名: " branch_name
    if [ -z "$branch_name" ]; then
        log_error "分支名不能为空"
        return
    fi

    # 创建并切换到新分支
    git checkout -b "$branch_name"
    log_info "已创建并切换到分支: $branch_name"

    # 推送远程分支
    git push -u origin "$branch_name"
    log_info "分支已推送到远程"
}

# 创建标签
create_tag() {
    read -p "请输入标签名 (如: v1.0.0): " tag_name
    if [ -z "$tag_name" ]; then
        log_error "标签名不能为空"
        return
    fi

    # 创建标签
    git tag -a "$tag_name" -m "Release $tag_name"

    # 推送标签
    git push origin "$tag_name"
    log_info "标签 $tag_name 已创建并推送到远程"
}

# 主函数
main() {
    case "${1:-menu}" in
        "init")
            # 初始化模式
            check_git
            check_git_repo
            init_git_config
            create_gitignore

            repo_name=$2
            if [ -z "$repo_name" ]; then
                read -p "请输入仓库名 (默认为当前目录名): " repo_name
                repo_name=${repo_name:-$(basename $(pwd))}
            fi

            create_github_repo "$repo_name"
            upload_project "$repo_name"
            show_repo_info "$repo_name"
            ;;
        "upload")
            # 仅上传模式
            check_git
            check_git_repo
            check_unstaged_changes
            repo_name=$(basename $(pwd))
            upload_project "$repo_name"
            ;;
        "menu")
            # 菜单模式
            show_menu

            case $choice in
                1)
                    check_git
                    check_git_repo
                    init_git_config
                    create_gitignore

                    read -p "请输入仓库名 (默认为当前目录名): " repo_name
                    repo_name=${repo_name:-$(basename $(pwd))}

                    create_github_repo "$repo_name"
                    upload_project "$repo_name"
                    show_repo_info "$repo_name"
                    ;;
                2)
                    check_git
                    check_git_repo
                    init_git_config
                    create_gitignore
                    log_info "Git 仓库初始化完成"
                    ;;
                3)
                    check_git
                    check_git_repo
                    check_unstaged_changes
                    repo_name=$(basename $(pwd))
                    upload_project "$repo_name"
                    ;;
                4)
                    check_git
                    repo_name=$(basename $(pwd))
                    show_repo_info "$repo_name"
                    ;;
                5)
                    check_git
                    create_branch
                    ;;
                6)
                    check_git
                    create_tag
                    ;;
                7)
                    exit 0
                    ;;
                *)
                    log_error "无效选择"
                    exit 1
                    ;;
            esac
            ;;
        *)
            log_error "使用方法: $0 [init|upload|menu] [仓库名]"
            echo ""
            echo "参数说明："
            echo "  init     - 初始化并上传项目"
            echo "  upload   - 仅上传已初始化的项目"
            echo "  menu     - 显示交互式菜单"
            exit 1
            ;;
    esac
}

# 运行主函数
main "$@"