# Docker 常用命令大全

## 目录

1. [镜像管理](#镜像管理)
2. [容器管理](#容器管理)
3. [数据卷管理](#数据卷管理)
4. [网络管理](#网络管理)
5. [Docker Compose](#docker-compose)
6. [系统信息](#系统信息)
7. [资源监控](#资源监控)
8. [安全相关](#安全相关)
9. [高级操作](#高级操作)
10. [故障排查](#故障排查)

---

## 镜像管理

### 基本操作

```bash
# 拉取镜像
docker pull <镜像名>:<标签>    # 例如: docker pull nginx:latest
docker pull <镜像名>           # 默认拉取 latest 标签

# 查看本地镜像
docker images                 # 显示所有本地镜像
docker image ls              # 同上
docker image ls --format "{{.Repository}}:{{.Tag}}"  # 只显示镜像名和标签
docker image ls -q            # 只显示镜像ID

# 删除镜像
docker rmi <镜像名>:<标签>     # 删除指定镜像
docker rmi <镜像ID>           # 删除指定ID的镜像
docker image prune            # 删除所有未被使用的镜像
docker image prune -a        # 删除所有未被使用的镜像，包括悬空镜像
docker system prune          # 清理所有未使用的对象（镜像、容器、网络等）

# 搜索镜像
docker search <关键词>         # 在 Docker Hub 搜索镜像
```

### 导入导出

```bash
# 导出镜像为 tar 文件
docker save <镜像名>:<标签> -o <文件名>.tar
docker save <镜像ID> -o <文件名>.tar

# 从 tar 文件导入镜像
docker load -i <文件名>.tar

# 查看镜像详细信息
docker image inspect <镜像名>:<标签>
docker image inspect <镜像ID>

# 查看镜像历史
docker history <镜像名>:<标签>
docker history <镜像ID>
```

### 构建镜像

```bash
# 构建镜像
docker build -t <镜像名>:<标签> <路径>       # 路径可以是 Dockerfile 所在目录或 ./
docker build -t myapp:v1.0 . -f Dockerfile.custom  # 使用指定的 Dockerfile

# 构建时选项
docker build --no-cache -t myapp:v1.0 .      # 不使用缓存构建
docker build --build-arg <参数名>=<值> .      # 构建时传递变量

# 查看构建过程
docker build --progress=plain -t myapp:v1.0 .  # 显示详细构建过程
```

### 标签管理

```bash
# 添加标签
docker tag <源镜像>:<标签> <目标镜像>:<标签>
# 例如：docker tag nginx:latest my-registry/nginx:latest

# 查看所有标签
docker image ls --format "{{.Repository}}:{{.Tag}} | {{.Size}}"
```

---

## 容器管理

### 基本操作

```bash
# 运行容器
docker run <镜像名>                    # 使用镜像运行容器
docker run -d <镜像名>                # 后台运行容器
docker run -d --name <容器名> <镜像名>  # 指定容器名称
docker run -d -it <镜像名> /bin/bash   # 交互式运行容器

# 运行时常用选项
docker run -d \
  --name web-server \
  -p 8080:80 \
  -v /host/path:/container/path \
  -e ENV_VAR=value \
  --restart always \
  nginx:latest

# 停止容器
docker stop <容器名或ID>       # 停止指定容器
docker stop $(docker ps -q)   # 停止所有运行中的容器

# 启动容器
docker start <容器名或ID>       # 启动已停止的容器
docker restart <容器名或ID>      # 重启容器

# 删除容器
docker rm <容器名或ID>          # 删除指定容器
docker rm -f <容器名或ID>       # 强制删除运行中的容器
docker container prune        # 删除所有已停止的容器
```

### 查看容器

```bash
# 查看运行中的容器
docker ps                      # 显示运行中的容器
docker ls                     # 同上

# 查看所有容器（包括已停止的）
docker ps -a                   # 显示所有容器

# 查看容器详细信息
docker inspect <容器名或ID>

# 查看容器日志
docker logs <容器名或ID>       # 查看完整日志
docker logs -f <容器名或ID>    # 实时查看日志
docker logs --tail 100 <容器名或ID>  # 只查看最后100行日志
docker logs --since 1h <容器名或ID>   # 查看最近1小时的日志

# 查看容器进程
docker top <容器名或ID>        # 查看容器内进程
docker stats                  # 查看所有容器资源使用情况
docker stats <容器名或ID>     # 查看指定容器资源使用情况
```

### 进入容器

```bash
# 进入正在运行的容器
docker exec -it <容器名或ID> /bin/bash    # 交互式进入
docker exec -it <容器名或ID> sh           # 使用 sh 进入
docker exec <容器名或ID> "ls -la"         # 在容器内执行命令并退出

# 从容器复制文件
docker cp <容器名或ID>:<容器内路径> <宿主机路径>
docker cp <宿主机路径> <容器名或ID>:<容器内路径>

# 例如：
docker cp myapp:/app/config.json ./backup/
docker cp ./app.js myapp:/app/
```

### 暂停和恢复

```bash
# 暂停容器
docker pause <容器名或ID>      # 暂停容器中的所有进程
docker unpause <容器名或ID>    # 恢复容器中的所有进程
```

---

## 数据卷管理

### 基本操作

```bash
# 创建数据卷
docker volume create <卷名>
docker volume create --name data-volume

# 查看数据卷
docker volume ls              # 列出所有数据卷
docker volume inspect <卷名>  # 查看数据卷详细信息

# 删除数据卷
docker volume rm <卷名>      # 删除指定数据卷
docker volume prune          # 删除所有未使用的数据卷

# 使用数据卷运行容器
docker run -d -v data-volume:/app/data nginx
docker run -d -v /host/path:/container/path nginx
```

### 数据卷备份和恢复

```bash
# 备份数据卷
docker run --rm -v <卷名>:/volume -v $(pwd):/backup \
  alpine tar czf /backup/backup-$(date +%Y%m%d).tar.gz -C /volume .

# 恢复数据卷
docker run --rm -v <卷名>:/volume -v $(pwd):/backup \
  alpine tar xzf /backup/backup-20231201.tar.gz -C /volume
```

---

## 网络管理

### 基本操作

```bash
# 查看网络
docker network ls            # 列出所有网络
docker network inspect <网络名>  # 查看网络详情

# 创建网络
docker network create <网络名>
docker network create --driver bridge my-network

# 连接容器到网络
docker network connect <网络名> <容器名或ID>
docker network disconnect <网络名> <容器名或ID>
```

### 运行时指定网络

```bash
# 使用网络运行容器
docker run -d --name web --network my-network nginx
docker run -d --name db --network my-network mysql

# 端口映射
docker run -d -p 8080:80 nginx           # 映射主机8080端口到容器80端口
docker run -d -p 80:80 -p 443:443 nginx # 多端口映射
docker run -d -P nginx                   # 随机映射端口

# 查看端口映射
docker port <容器名或ID>                  # 查看容器端口映射
```

### 网络模式

```bash
# 不同网络模式运行容器
docker run -d --network bridge nginx       # 桥接模式（默认）
docker run -d --host none nginx           # 无网络模式
docker run -d --network host nginx        # 主机模式（共享主机网络）
docker run -d --network container:<容器名> nginx  # 使用容器的网络
```

---

## Docker Compose

### 基本操作

```bash
# 启动服务
docker-compose up           # 启动所有服务
docker-compose up -d        # 后台启动服务
docker-compose up --build  # 构建并启动服务

# 停止服务
docker-compose down         # 停止并删除容器、网络等
docker-compose down -v      # 停止并删除包含数据卷

# 管理服务
docker-compose start        # 启动已创建的服务
docker-compose stop         # 停止服务
docker-compose restart      # 重启服务
docker-compose pause        # 暂停服务
docker-compose unpause      # 恢复服务

# 查看状态
docker-compose ps           # 查看服务状态
docker-compose logs         # 查看所有服务日志
docker-compose logs -f <服务名>  # 查看指定服务日志
docker-compose logs --tail 100   # 只显示最后100行

# 执行命令
docker-compose exec <服务名> <命令>  # 在服务容器中执行命令
docker-compose exec web bash        # 进入web服务的bash

# 构建服务
docker-compose build         # 构建所有服务
docker-compose build <服务名>       # 构建指定服务
docker-compose build --no-cache     # 不使用缓存构建
```

### Compose 文件操作

```bash
# 使用不同文件
docker-compose -f docker-compose.yml up
docker-compose -f docker-compose.prod.yml up -d

# 验证文件
docker-compose config         # 验证并显示配置
docker-compose config -q     # 验证配置，静默模式

# 查看配置
docker-compose config > config-validated.yml
```

### 扩缩容

```bash
# 扩缩服务
docker-compose up --scale <服务名>=<数量>
docker-compose up --scale web=3 -d    # 启动3个web实例
```

---

## 系统信息

### 查看 Docker 信息

```bash
# 查看 Docker 系统信息
docker info                  # 显示 Docker 系统信息
docker version              # 显示 Docker 版本信息

# 查看磁盘使用
docker system df            # 显示磁盘使用情况
docker system df -v         # 显示详细磁盘使用信息

# 查看事件
docker events               # 实时显示 Docker 事件
docker events --since 1h   # 显示最近1小时的事件
docker events --filter event=stop  # 过滤事件类型
```

---

## 资源监控

### 容器监控

```bash
# 实时监控
docker stats                # 实时显示容器资源使用
docker stats --no-stream    # 显示当前资源使用（不实时更新）

# 查看详细信息
docker inspect <容器名或ID> | grep -i memory
docker inspect <容器名或ID> --format='{{.State.Pid}}'  # 查看容器PID

# 资源限制示例
docker run -d --name constrained \
  --memory 512m \
  --cpus 1.0 \
  nginx
```

### 系统监控

```bash
# 查看容器限制
docker run --rm alpine sh -c "cat /proc/meminfo" | grep Mem
docker run --rm alpine sh -c "cat /proc/cpuinfo" | grep processor
```

---

## 安全相关

### 镜像安全

```bash
# 扫描镜像漏洞
docker scan <镜像名>:<标签>   # 使用 Docker Scout 扫描

# 查看镜像内容
docker history <镜像名>:<标签>    # 查看镜像历史层
docker image history <镜像名>:<标签>
```

### 容器安全

```bash
# 以非 root 用户运行
docker run -u 1000 nginx   # 指定用户ID运行

# 只读文件系统
docker run --read-only nginx

# 容器资源限制
docker run --memory=512m --cpus=1.0 nginx

# 禁用特权模式
docker run --security-opt=no-new-privileges nginx
```

### 密钥管理

```bash
# 创建密钥
docker secret create db_password SecretPassword.txt
docker secret create app_config config.json

# 使用密钥
docker service create --secret db_password nginx
docker run --secret source=db_password,target=password.txt nginx
```

---

## 高级操作

### 多阶段构建

```dockerfile
# Dockerfile 示例
FROM node:18 AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Dockerfile 指令

```dockerfile
# 基础镜像
FROM ubuntu:20.04

# 工作目录
WORKDIR /app

# 复制文件
COPY . .

# 添加文件（自动解压）
ADD archive.tar.gz /app

# 运行命令
RUN apt-get update && apt-get install -y nginx

# 设置环境变量
ENV NODE_ENV production

# 暴露端口
EXPOSE 80

# 运行命令
CMD ["nginx", "-g", "daemon off;"]

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost/ || exit 1

# 用户
USER nginx
```

---

## 故障排查

### 常见问题

```bash
# 查看容器错误
docker logs <容器名或ID> --tail 50

# 进入容器调试
docker exec -it <容器名或ID> /bin/bash

# 查看资源使用
docker stats <容器名或ID>

# 查看网络问题
docker network inspect <网络名>

# 查看存储问题
docker volume inspect <卷名>
```

### 调试技巧

```bash
# 查看系统资源
df -h
free -h
docker system df

# 检查端口占用
netstat -tunlp | grep :80
ss -tunlp | grep :80

# 查看容器详情
docker inspect <容器名或ID> --format='{{json .State}}' | jq .

# 查看容器网络
docker inspect <容器名或ID> --format='{{json .NetworkSettings}}' | jq .
```

---

## 实用技巧

### 批量操作

```bash
# 停止所有容器
docker stop $(docker ps -q)

# 删除所有容器
docker rm $(docker ps -aq)

# 删除所有镜像
docker rmi $(docker images -q)

# 清理所有未使用的对象
docker system prune -a

# 删除所有悬空镜像
docker image prune
```

### 别名配置

```bash
# 在 ~/.bashrc 或 ~/.zshrc 中添加
alias d='docker'
alias dps='docker ps'
alias dpsa='docker ps -a'
alias dimgs='docker images'
alias drmi='docker rmi'
alias drmf='docker rmi -f'
alias drmc='docker rm -f'
alias dexec='docker exec -it'
alias dlog='docker logs -f'
alias dstop='docker stop'
alias dstart='docker start'
alias drestart='docker restart'

# 组合命令
alias dclear='docker system prune -a'
alias dcleanup='docker system prune'
```

---

## 总结

Docker 命令虽然繁多，但可以按照以下分类记忆：

1. **镜像管理**：`pull`, `images`, `rmi`, `build`, `save`, `load`
2. **容器管理**：`run`, `start`, `stop`, `restart`, `rm`, `exec`, `logs`
3. **数据管理**：`volume`, `cp`
4. **网络管理**：`network`, `port`
5. **系统管理**：`info`, `version`, `system`
6. **编排工具**：`docker-compose` 相关命令

记住常用命令的参数和选项，可以通过 `--help` 查看更详细的信息：
```bash
docker --help
docker <命令> --help
```

熟练使用这些命令，可以大大提高 Docker 的使用效率。