# Docker 命令速查卡片

## 🚀 基本命令

```bash
# 拉取镜像
docker pull nginx:latest

# 查看镜像
docker images
docker image ls

# 运行容器
docker run -d -p 8080:80 --name web nginx
docker run -d --name myapp myimage:1.0

# 查看容器
docker ps
docker ps -a

# 停止容器
docker stop <容器名>
docker stop $(docker ps -q)

# 启动容器
docker start <容器名>

# 删除容器
docker rm <容器名>
docker rm -f <容器名>
```

## 🔧 容器管理

```bash
# 进入容器
docker exec -it <容器名> /bin/bash
docker exec -it <容器名> sh

# 查看日志
docker logs <容器名>
docker logs -f <容器名>
docker logs --tail 100 <容器名>

# 查看资源使用
docker stats
docker stats <容器名>

# 复制文件
docker cp <容器名>:/file/path /host/path
docker cp /host/file <容器名>:/container/path

# 查看端口映射
docker port <容器名>
```

## 📦 镜像管理

```bash
# 删除镜像
docker rmi <镜像名>
docker rmi -f <镜像名>

# 清理无用镜像
docker image prune
docker system prune

# 导出镜像
docker save nginx:latest -o nginx.tar

# 导入镜像
docker load -i nginx.tar

# 构建镜像
docker build -t myapp:v1.0 .
docker build -t myapp:v1.0 -Dockerfile.custom .

# 查看镜像历史
docker history <镜像名>
```

## 🌐 网络管理

```bash
# 查看网络
docker network ls

# 创建网络
docker network create mynet

# 连接容器到网络
docker network connect mynet <容器名>

# 使用网络运行容器
docker run -d --name web --network mynet nginx

# 端口映射
docker run -d -p 80:80 nginx
docker run -d -p 8080:80 nginx
```

## 💾 数据卷管理

```bash
# 创建数据卷
docker volume create myvol

# 查看数据卷
docker volume ls
docker volume inspect myvol

# 使用数据卷
docker run -d -v myvol:/app/data nginx
docker run -d -v /host/path:/container/path nginx

# 备份数据卷
docker run --rm -v myvol:/vol -v $(pwd):/backup \
  alpine tar czf /backup/backup.tar.gz -C /vol .

# 删除数据卷
docker volume rm myvol
docker volume prune
```

## 📋 Docker Compose

```bash
# 启动服务
docker-compose up
docker-compose up -d

# 停止服务
docker-compose down
docker-compose down -v

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs
docker-compose logs -f web

# 执行命令
docker-compose exec web bash
docker-compose exec db mysql -u root -p

# 构建服务
docker-compose build
docker-compose build --no-cache
```

## 🔍 监控和调试

```bash
# 查看 Docker 信息
docker info

# 查看系统资源
docker system df

# 查看事件
docker events
docker events --since 1h

# 查看容器详情
docker inspect <容器名>

# 查看健康状态
docker inspect <容器名> --format='{{.State.Health.Status}}'
```

## 🛡️ 安全命令

```bash
# 以非 root 用户运行
docker run -u 1000 nginx

# 只读文件系统
docker run --read-only nginx

# 资源限制
docker run --memory=512m --cpus=1.0 nginx

# 扫描镜像漏洞
docker scan nginx:latest
```

## 🔄 批量操作

```bash
# 停止所有容器
docker stop $(docker ps -q)

# 删除所有容器
docker rm $(docker ps -aq)

# 删除所有镜像
docker rmi $(docker images -q)

# 清理所有
docker system prune -a

# 删除所有悬空镜像
docker image prune
```

## 🎯 实用别名

```bash
# 添加到 ~/.bashrc 或 ~/.zshrc
alias d='docker'
alias dps='docker ps'
alias dpsa='docker ps -a'
alias di='docker images'
alias drmi='docker rmi'
alias drmf='docker rmi -f'
alias drmc='docker rm -f'
alias dc='docker-compose'
alias dcu='docker-compose up -d'
alias dcd='docker-compose down'
alias dcl='docker-compose logs'
alias dce='docker-compose exec'
alias dlog='docker logs -f'
alias dstop='docker stop'
alias dstart='docker start'
alias dexec='docker exec -it'
alias dinspect='docker inspect --format='{{json .}}' | jq .'
```

## 📝 使用技巧

1. **查看帮助**：
   ```bash
   docker --help
   docker run --help
   ```

2. **格式化输出**：
   ```bash
   docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Status}}"
   ```

3. **过滤容器**：
   ```bash
   docker ps -f "name=web"
   docker ps -f "status=running"
   ```

4. **查找容器**：
   ```bash
   docker ps -aq | xargs docker inspect --format '{{.Name}}'
   ```

5. **查看容器 IP**：
   ```bash
   docker inspect <容器名> --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'
   ```