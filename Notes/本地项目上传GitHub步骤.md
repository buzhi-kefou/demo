# 本地项目上传 GitHub 完整指南

## 目录

1. [准备工作](#准备工作)
2. [创建 GitHub 仓库](#创建-github-仓库)
3. [本地 Git 配置](#本地-git-配置)
4. [初始化 Git 仓库](#初始化-git-仓库)
5. [添加文件到仓库](#添加文件到仓库)
6. [提交和推送](#提交和推送)
7. [管理分支](#管理分支)
8. [常见问题](#常见问题)
9. [最佳实践](#最佳实践)

---

## 准备工作

### 1. 安装必要软件

```bash
# 检查是否安装了 Git
git --version

# 如果未安装，macOS 使用 Homebrew
brew install git

# Linux (Ubuntu/Debian)
sudo apt update
sudo apt install git

# Windows
# 从 https://git-scm.com/download/win 下载并安装
```

### 2. 注册 GitHub 账号

1. 访问 [GitHub 官网](https://github.com/)
2. 点击 "Sign up" 注册新账号
3. 验证邮箱地址
4. 完善个人信息（可选）

### 3. 配置 SSH 密钥（推荐）

```bash
# 生成 SSH 密钥
ssh-keygen -t ed25519 -C "your_email@example.com"

# 一路回车使用默认设置
# 密钥会生成在 ~/.ssh/ 目录下
```

### 4. 复制 SSH 密钥

```bash
# 查看公钥内容
cat ~/.ssh/id_ed25519.pub

# 复制输出的公钥内容（从 ssh-ed25519 开始）
```

### 5. 在 GitHub 添加 SSH 密钥

1. 登录 GitHub
2. 点击右上角头像 → Settings
3. 左侧菜单找到 "SSH and GPG keys"
4. 点击 "New SSH key"
5. 粘贴刚才复制的公钥内容
6. 输入密钥标题（如 "My MacBook Pro"）
7. 点击 "Add SSH key"

### 6. 测试 SSH 连接

```bash
ssh -T git@github.com
# 如果看到 Hi username! You've successfully authenticated...，说明连接成功
```

---

## 创建 GitHub 仓库

### 方法一：网页创建

1. 登录 GitHub
2. 点击右上角 "+" → "New repository"
3. 填写仓库信息：
   - **Repository name**: 仓库名称（必填）
   - **Description**: 项目描述（可选）
   - **Public/Private**: 选择公开或私有
   - **Initialize with README**: 勾选（推荐）
   - **Add .gitignore**: 选择适合项目的忽略文件
   - **Add a license**: 选择开源许可证（可选）
4. 点击 "Create repository"

### 方法二：使用 GitHub CLI（推荐）

```bash
# 安装 GitHub CLI
# macOS
brew install gh

# Linux (Ubuntu/Debian)
sudo apt install gh

# 登录 GitHub
gh auth login

# 创建新仓库
gh repo create my-project \
  --description "我的项目描述" \
  --public \
  --source=. \
  --push \
  --remote=origin
```

---

## 本地 Git 配置

### 1. 配置用户信息

```bash
# 设置全局用户名和邮箱（一次即可）
git config --global user.name "Your Name"
git config --global user.email "your_email@example.com"

# 查看配置
git config --list

# 只查看某项配置
git config user.name
```

### 2. 配置编辑器

```bash
# 设置默认编辑器
git config --global core.editor "vim"
# 或其他编辑器如：nano, code (VS Code)
```

### 3. 配置别名（提高效率）

```bash
# 常用别名
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.st status
git config --global alias.ci commit
git config --global alias.unstage 'reset HEAD --'
git config --global alias.last 'log -1 HEAD'
git config --global alias.visual '!gitk'

# 颜色配置
git config --global color.ui true
```

### 4. 忽略文件配置

创建 `.gitignore` 文件：
```bash
# 创建 .gitignore 文件
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
EOF
```

---

## 初始化 Git 仓库

### 1. 现有项目添加 Git

```bash
# 进入项目目录
cd /path/to/your/project

# 初始化 Git 仓库
git init

# 添加远程仓库（替换为你的仓库地址）
git remote add origin git@github.com:username/repository-name.git

# 查看远程仓库
git remote -v
```

### 2. 克隆已有仓库

```bash
# 克隆到本地
git clone git@github.com:username/repository-name.git

# 克隆到指定目录
git clone git@github.com:username/repository-name.git my-folder

# 克隆时包含子模块
git clone --recurse-submodules git@github.com:username/repo.git
```

---

## 添加文件到仓库

### 1. 查看状态

```bash
# 查看仓库状态
git status

# 查看变更的详细信息
git diff

# 查看还未暂存的变更
git diff HEAD

# 查看已暂存但未提交的变更
git diff --cached
```

### 2. 添加文件

```bash
# 添加单个文件
git add filename.txt

# 添加特定目录下的所有文件
git add path/to/directory/

# 添加所有文件（包括新文件和修改的文件）
git add .

# 添加所有修改的文件，但不包括新文件
git add -u

# 添加所有文件，但忽略 .gitignore 中的文件
git add . -f  # 强制添加被忽略的文件

# 交互式添加（可以按空格选择，按 i 进入选择模式）
git add -i
```

### 3. 移除文件

```bash
# 从暂存区移除
git reset HEAD filename.txt

# 从仓库中删除（同时删除文件）
git rm filename.txt

# 从仓库中删除但保留文件
git rm --cached filename.txt

# 删除整个目录
git rm -r directory-name/
```

### 4. 移动文件

```bash
# 移动文件
git mv old-name.txt new-name.txt

# 批量移动
git mv old-dir/* new-dir/
```

---

## 提交和推送

### 1. 提交更改

```bash
# 提交并添加说明
git commit -m "feat: 添加用户登录功能"

# 提交并查看详细信息
git commit -v

# 提交时跳过验证（紧急情况）
git commit --no-verify -m "紧急修复"

# 修改上次提交（不修改提交ID）
git commit --amend -m "更新提交信息"

# 修改上次提交并包含暂存区的更改
git commit --amend --no-edit

# 添加修改并重新提交
git add forgotten-file
git commit --amend
```

### 2. 推送到 GitHub

```bash
# 首次推送（-u 设置上游分支）
git push -u origin main

# 后续推送
git push origin main

# 推送所有分支
git push --all origin

# 强制推送（谨慎使用）
git push --force-with-lease origin main

# 推送时创建远程分支
git push -u origin feature/login
```

### 3. 拉取更新

```bash
# 拉取远程更新
git pull origin main

# 拉取并重新基（更干净的合并）
git pull --rebase origin main

# 查看远程仓库地址
git remote -v

# 添加新的远程仓库
git remote add upstream git@github.com:original-owner/original-repo.git
```

---

## 管理分支

### 1. 查看分支

```bash
# 查看所有分支
git branch

# 查看远程分支
git branch -r

# 查看所有分支（包括远程）
git branch -a

# 查看分支最后提交
git branch -v
```

### 2. 创建分支

```bash
# 创建新分支
git branch feature/new-feature

# 创建并切换到新分支
git checkout -b feature/new-feature

# 基于当前分支创建新分支
git checkout -b feature/new-feature

# 基于远程分支创建
git checkout -b feature/new-feature origin/feature/base
```

### 3. 切换分支

```bash
# 切换到指定分支
git checkout main

# 切换到上一个分支
git checkout -

# 快速切换分支（git switch 新版命令）
git switch main
git switch -c feature/new-feature
```

### 4. 合并分支

```bash
# 切换到目标分支
git checkout main

# 合并其他分支
git merge feature/new-feature

# 快进合并（不会创建新的提交）
git merge --ff-only feature/new-feature

# 递归合并策略（适用于二进制文件）
git merge -s recursive -X rename-threshold=50% feature/new-feature

# 查看合并日志
git log --graph --oneline --decorate
```

### 5. 删除分支

```bash
# 删除本地分支
git branch -d feature/old-feature

# 强制删除未合并的分支
git branch -D feature/old-feature

# 删除远程分支
git push origin --delete feature/old-feature

# 或使用推送方式删除
git push origin :feature/old-feature
```

### 6. 标签管理

```bash
# 创建标签（轻量标签）
git tag v1.0.0

# 创建带注释的标签
git tag -a v1.1.0 -m "版本 1.1.0 发布"

# 查看标签
git tag

# 查看特定标签
git show v1.0.0

# 推送标签到远程
git push origin v1.0.0

# 推送所有标签
git push origin --tags

# 删除标签
git tag -d v1.0.0
git push origin :refs/tags/v1.0.0
```

---

## 常见问题

### 1. 推送失败

```bash
# 错误：You do not have permission to push to...
# 解决：检查是否正确添加 SSH 密钥，或使用正确的用户名
git remote set-url origin git@github.com:正确用户名/仓库名.git
```

### 2. 合并冲突

```bash
# 当遇到冲突时，Git 会标记冲突的文件
# 手动解决冲突后：
git add resolved-file.txt
git commit -m "resolve conflict in resolved-file.txt"
```

### 3. 撤销操作

```bash
# 撤销暂存
git reset HEAD filename.txt

# 撤销提交（保留更改）
git reset HEAD~1

# 撤销提交（删除更改）
git reset --hard HEAD~1

# 撤销已推送的提交（需要强制推送）
git reset --hard HEAD~1
git push --force-with-lease
```

### 4. 修改历史

```bash
# 修改最后一次提交
git commit --amend

# 修改多个提交
git rebase -i HEAD~3

# 压缩多个提交
git rebase -i HEAD~3
# 将多个 pick 改为 s（squash）
```

### 5. 忽略文件不生效

```bash
# 确保文件未被追踪
git rm --cached filename.txt

# 然后重新添加到 .gitignore
echo "filename.txt" >> .gitignore

# 提交更改
git add .gitignore
git commit -m "添加 .gitignore"
```

---

## 最佳实践

### 1. 提交信息规范

使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```bash
# 类型说明：
feat:     新功能
fix:      修复 bug
docs:     文档更新
style:    代码格式（不影响功能）
refactor: 重构
perf:     性能优化
test:     测试相关
chore:    构建或辅助工具的变动

# 示例：
feat: 添加用户登录功能
fix: 修复登录页面的布局问题
docs: 更新 README 安装说明
style: 格式化代码
refactor: 提取公共组件
perf: 优化数据库查询
```

### 2. 分支管理策略

```
main/master    # 主分支，始终保持稳定
develop       # 开发分支
feature/      # 功能分支
hotfix/       # 紧急修复分支
release/      # 发布分支
```

### 3. 工作流程

1. **创建功能分支**：
   ```bash
   git checkout -b feature/user-login develop
   ```

2. **开发完成后提交**：
   ```bash
   git add .
   git commit -m "feat: 实现用户登录功能"
   ```

3. **推送到远程**：
   ```bash
   git push origin feature/user-login
   ```

4. **创建 Pull Request**：
   - 在 GitHub 上创建 PR
   - 指定 reviewers
   - 添加描述

### 4. 保护分支

在 GitHub 仓库设置中：
1. Settings → Branches → Branch protection rules
2. Add rule
3. Branch name pattern：`main`
4. Require pull request reviews
5. Require status checks to pass

### 5. 使用 .gitattributes

```bash
# .gitattributes 文件示例
*.text linguist-language=Markdown
*.js linguist-language=JavaScript
*.vue linguist-language=JavaScript
*.json linguist-language=JSON
*.md linguist-detectable=true
```

### 6. 使用 Git Hooks

```bash
# 安装 pre-commit hook
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/sh
# 运行代码检查
npm run lint || exit 1
echo "代码检查通过"
EOF
chmod +x .git/hooks/pre-commit
```

---

## 常用命令速查

```bash
# 仓库操作
git init                    # 初始化仓库
git clone <url>             # 克隆仓库
git status                  # 查看状态

# 文件操作
git add <file>              # 添加文件
git add .                   # 添加所有文件
git rm <file>               # 删除文件
git mv <old> <new>         # 移动/重命名

# 提交操作
git commit -m "message"    # 提交
git commit --amend         # 修改提交
git push origin <branch>   # 推送
git pull origin <branch>   # 拉取

# 分支操作
git branch                  # 查看分支
git checkout <branch>       # 切换分支
git branch <new>            # 创建分支
git merge <branch>          # 合并分支

# 查看历史
git log                     # 查看提交历史
git log --oneline          # 简洁历史
git log --graph            # 图形化历史

# 远程仓库
git remote -v               # 查看远程仓库
git remote add <name> <url> # 添加远程仓库
git push -u origin <branch> # 设置上游并推送
git branch --set-upstream-to=origin/main main # 设置上游分支
```

---

## 总结

上传项目到 GitHub 的基本流程：

1. **准备环境**：安装 Git，配置 SSH 密钥
2. **创建仓库**：在 GitHub 创建新仓库
3. **初始化**：在本地初始化 Git 或克隆仓库
4. **添加文件**：使用 `git add` 添加文件
5. **提交**：使用 `git commit -m "说明"` 提交
6. **推送**：使用 `git push origin main` 推送到 GitHub

记住遵循良好的提交规范和分支管理策略，这样可以让团队协作更加顺畅。遇到问题时，善用 `git status` 和 `git log` 来了解当前状态和历史记录。