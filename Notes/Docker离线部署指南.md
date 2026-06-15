# Docker 项目离线部署完整指南

## 目录

1. [概述](#概述)
2. [准备工作](#准备工作)
3. [获取 Docker 离线安装包](#获取-docker-离线安装包)
4. [部署私有镜像仓库](#部署私有镜像仓库)
5. [离线环境配置](#离线环境配置)
6. [部署 Docker Compose 项目](#部署-docker-compose-项目)
7. [常用操作指南](#常用操作指南)
8. [故障排查](#故障排查)
9. [最佳实践](#最佳实践)

---

## 概述

在无法访问互联网的内网环境中部署 Docker 项目，需要提前准备所有必要的安装包、依赖和镜像。本文档详细介绍了完整的离线部署流程，适用于企业内网、生产环境等场景。

---

## 准备工作

### 1. 环境信息收集

```bash
# 检查系统信息
uname -a
cat /etc/os-release

# 检查磁盘空间
df -h

# 检查内存
free -h
```

### 2. 网络规划

```bash
# 确保各节点之间网络互通
ping registry-server
ping docker-host

# 检查端口占用
netstat -tunlp | grep :5000  # Harbor 默认端口
netstat -tunlp | grep :8080   # 其他服务端口
```

### 3. 目录结构规划

```
/opt/docker-offline/
├── docker/
│   ├── docker-ce.rpm         # Docker 安装包
│   ├── docker-compose       # Docker Compose 可执行文件
│   └── plugins/              # 插件
├── images/
│   ├── mysql-*.tar           # MySQL 镜像
│   ├── nginx-*.tar           # Nginx 镜像
│   └── app-*.tar            # 应用镜像
├── harbor/
│   ├── harbor-offline-installer-v2.8.0.tgz
│   └── harbor.yml           # Harbor 配置文件
└── scripts/
    ├── install-docker.sh     # 安装脚本
    └── deploy-app.sh         # 部署脚本
```

---

## 获取 Docker 离线安装包

### 1. Docker Engine 安装包

**CentOS/RHEL:**
```bash
# 在联网机器上下载所有依赖
yum install yum-utils -y
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
yum install docker-ce docker-ce-cli containerd.io -y --downloadonly --downloaddir=/opt/docker-packages

# 打包所有 RPM 包
cd /opt/docker-packages
tar -czf docker-centos-packages.tar.gz *.rpm
```

**Ubuntu/Debian:**
```bash
# 下载 DEB 包
apt-get update
apt-get install docker-ce docker-ce-cli containerd.io -y --download-only --reinstall -o Dir::Etc::sourceparts="/etc/apt"

# 打包
tar -czf docker-ubuntu-packages.tar.gz /var/cache/apt/archives/*.deb
```

### 2. Docker Compose

```bash
# 下载 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.23.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 单独打包
cp /usr/local/bin/docker-compose /opt/docker-offline/docker-compose/
```

### 3. 必要的 Docker 镜像

```bash
# 创建镜像导出脚本
cat > export-images.sh << 'EOF'
#!/bin/bash

# 基础镜像
docker pull alpine:latest
docker pull nginx:latest
docker pull mysql:8.0
docker pull redis:latest
docker pull php:8.2-fpm

# 应用镜像
docker pull your-app-image:latest

# 导出所有镜像
for image in $(docker images | awk '{print $1":"$2}' | grep -v REPOSITORY); do
    docker save $image -o /opt/docker-offline/images/${image//\//_}.tar
done
EOF

chmod +x export-images.sh
./export-images.sh
```

---

## 部署私有镜像仓库

### 1. 部署 Harbor Registry

```bash
# 解压 Harbor
cd /opt/docker-offline
tar -xzf harbor-offline-installer-v2.8.0.tgz
cd harbor

# 修改配置
cat > harbor.yml << EOF
hostname: harbor.example.com
http:
  port: 80
https:
  port: 443
certificate: /data/cert/server.crt
private_key: /data/cert/server.key
data_volume: /data/harbor
# ... 其他配置 ...
EOF

# 创建证书目录
mkdir -p /data/cert

# 生成自签名证书（生产环境建议使用正式证书）
openssl genrsa -out /data/cert/server.key 4096
openssl req -new -x509 -key /data/cert/server.key -out /data/cert/server.crt -days 365 -subj "/C=CN/ST=Beijing/L=Beijing/O=YourCompany/OU=IT/CN=harbor.example.com"

# 安装并启动 Harbor
./prepare
./install.sh
```

### 2. 配置 Docker 访问 Harbor

```bash
# 创建 Docker 配置文件
mkdir -p /etc/docker
cat > /etc/docker/daemon.json << EOF
{
  "insecure-registries": ["harbor.example.com"],
  "registry-mirrors": ["https://harbor.example.com"]
}
EOF

# 重启 Docker
systemctl restart docker
```

### 3. 导入镜像到 Harbor

```bash
# 加载本地镜像
for img in /opt/docker-offline/images/*.tar; do
    docker load -i $img
done

# 标记并推送到 Harbor
docker tag mysql:8.0 harbor.example.com/library/mysql:8.0
docker push harbor.example.com/library/mysql:8.0

# 创建脚本自动导入所有镜像
cat > import-all-images.sh << 'EOF'
#!/bin/bash

for image_file in /opt/docker-offline/images/*.tar; do
    echo "Loading ${image_file}..."
    docker load -i ${image_file}
done
echo "All images loaded successfully!"
EOF

chmod +x import-all-images.sh
```

---

## 离线环境配置

### 1. 安装 Docker

```bash
#!/bin/bash
# install-docker.sh

set -e

echo "Installing Docker offline..."

# 安装依赖
yum localinstall -y /opt/docker-offline/docker/*.rpm

# 配置 Docker
mkdir -p /etc/docker
cp /opt/docker-offline/docker/daemon.json /etc/docker/daemon.json

# 启动 Docker
systemctl enable docker
systemctl start docker

# 验证安装
docker --version
docker-compose --version
```

### 2. 配置镜像加速

```bash
# 创建镜像加速配置
cat > /etc/docker/daemon.json << EOF
{
  "registry-mirrors": [
    "https://harbor.example.com",
    "http://mirrors.aliyun.com"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}
EOF
```

### 3. 配置网络

```bash
# 配置固定 IP（可选）
cat > /etc/sysconfig/network-scripts/ifcfg-eth0 << EOF
TYPE=Ethernet
BOOTPROTO=static
NAME=eth0
DEVICE=eth0
ONBOOT=yes
IPADDR=192.168.1.100
NETMASK=255.255.255.0
GATEWAY=192.168.1.1
DNS1=8.8.8.8
DNS2=8.8.4.4
EOF

# 重启网络
systemctl restart network
```

---

## 部署 Docker Compose 项目

### 1. 准备项目文件

```bash
# 创建项目目录
mkdir -p /opt/myapp
cd /opt/myapp

# 创建 docker-compose.yml
cat > docker-compose.yml << EOF
version: '3.8'

services:
  web:
    image: harbor.example.com/myapp/web:latest
    ports:
      - "80:80"
    volumes:
      - ./web.conf:/etc/nginx/nginx.conf
    depends_on:
      - php
    networks:
      - app-network

  php:
    image: harbor.example.com/myapp/php:latest
    volumes:
      - ./src:/var/www/html
    networks:
      - app-network

  mysql:
    image: harbor.example.com/library/mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: myapp
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - app-network

  redis:
    image: harbor.example.com/library/redis:latest
    volumes:
      - redis-data:/data
    networks:
      - app-network

volumes:
  mysql-data:
  redis-data:

networks:
  app-network:
    driver: bridge
EOF
```

### 2. 创建启动脚本

```bash
#!/bin/bash
# deploy-app.sh

set -e

APP_DIR="/opt/myapp"
IMAGE_DIR="/opt/docker-offline/images"

echo "Starting application deployment..."

# 导入镜像
echo "Importing images..."
$IMAGE_DIR/import-all-images.sh

# 启动服务
cd $APP_DIR
docker-compose up -d

# 检查服务状态
echo "Checking service status..."
docker-compose ps

# 查看日志
echo "Viewing logs..."
docker-compose logs -f
```

### 3. 创建管理脚本

```bash
#!/bin/bash
# app-manage.sh

APP_DIR="/opt/myapp"

case "$1" in
    start)
        cd $APP_DIR
        docker-compose up -d
        ;;
    stop)
        cd $APP_DIR
        docker-compose down
        ;;
    restart)
        cd $APP_DIR
        docker-compose down
        docker-compose up -d
        ;;
    logs)
        cd $APP_DIR
        docker-compose logs -f ${2:-}
        ;;
    status)
        cd $APP_DIR
        docker-compose ps
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|logs|status}"
        exit 1
        ;;
esac
```

---

## 常用操作指南

### 1. 镜像管理

```bash
# 查看本地镜像
docker images

# 导出镜像
docker save image:tag -o image.tar

# 导入镜像
docker load -i image.tar

# 删除镜像
docker rmi image:tag

# 清理悬空镜像
docker image prune
```

### 2. 容器管理

```bash
# 查看运行中的容器
docker ps

# 查看所有容器
docker ps -a

# 启动容器
docker start container_id

# 停止容器
docker stop container_id

# 进入容器
docker exec -it container_id /bin/bash

# 查看容器日志
docker logs -f container_id
```

### 3. Docker Compose 操作

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 重启服务
docker-compose restart service_name

# 更新服务
docker-compose pull
docker-compose up -d --force-recreate

# 停止并删除
docker-compose down

# 清理
docker-compose down -v --remove-orphans
```

### 4. 网络管理

```bash
# 查看网络
docker network ls

# 创建自定义网络
docker network create --driver bridge app-network

# 连接容器到网络
docker network connect app-network container_id

# 断开网络
docker network disconnect app-network container_id
```

---

## 故障排查

### 1. Docker 服务无法启动

```bash
# 检查服务状态
systemctl status docker

# 查看错误日志
journalctl -u docker.service

# 检查配置文件
cat /etc/docker/daemon.json

# 修复权限问题
usermod -aG docker $USER
```

### 2. 镜像拉取失败

```bash
# 检查网络连接
ping harbor.example.com

# 检查证书
ls -la /etc/docker/certs.d/harbor.example.com/

# 重新加载证书
update-ca-trust

# 重启 Docker
systemctl restart docker
```

### 3. 容器启动失败

```bash
# 查看容器状态
docker inspect container_id

# 查看详细错误
docker logs container_id

# 检查资源使用
docker stats container_id

# 检查端口占用
netstat -tunlp | grep :80
```

### 4. 存储问题

```bash
# 检查 Docker 磁盘使用
df -h /var/lib/docker

# 清理无用镜像和容器
docker system prune -a

# 查看容器存储使用
docker system df
```

---

## 最佳实践

### 1. 安全配置

```bash
# 创建非 root 用户运行 Docker
useradd -s /bin/false dockeradmin
usermod -aG docker dockeradmin

# 配置镜像扫描
docker scan harbor.example.com/app:latest

# 启用内容信任
export DOCKER_CONTENT_TRUST=1
```

### 2. 性能优化

```bash
# 配置 Docker 使用 overlay2
cat > /etc/docker/daemon.json << EOF
{
  "storage-driver": "overlay2",
  "storage-opts": [
    "overlay2.size=10G"
  ]
}
EOF

# 限制容器资源
cat > docker-compose.yml << EOF
services:
  app:
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
        reservations:
          cpus: '0.25'
          memory: 256M
EOF
```

### 3. 监控和日志

```bash
# 配置日志轮转
cat > /etc/docker/daemon.json << EOF
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF

# 使用 Prometheus 监控
cat > prometheus.yml << EOF
scrape_configs:
  - job_name: 'docker'
    static_configs:
      - targets: ['172.17.0.1:9323']
EOF
```

### 4. 备份和恢复

```bash
#!/bin/bash
# backup-docker.sh

BACKUP_DIR="/backup/docker/$(date +%Y%m%d)"
mkdir -p $BACKUP_DIR

# 备份镜像
docker images --format "{{.Repository}}:{{.Tag}}" | xargs -I {} docker save {} -i $BACKUP_DIR/$(echo {} | sed 's|/|_|g' | sed 's|:|_|g').tar

# 备份容器配置
docker ps -a --format "{{.ID}} {{.Image}}" | while read id image; do
    docker inspect $id > $BACKUP_DIR/container_$id.json
done

# 备份数据卷
docker volume ls --format "{{.Name}}" | xargs -I {} tar -czf $BACKUP_DIR/volume_{}.tar /var/lib/docker/volumes/{}/_data

echo "Backup completed to $BACKUP_DIR"
```

### 5. 更新策略

```bash
# 创建更新脚本
cat > update-app.sh << 'EOF'
#!/bin/bash

# 拉取新镜像
docker-compose pull

# 停止旧容器
docker-compose down

# 启动新容器
docker-compose up -d

# 验证服务
sleep 10
curl http://localhost/healthcheck

# 清理旧镜像
docker image prune -f
EOF

chmod +x update-app.sh
```

---

## 总结

Docker 离线部署是一个系统工程，需要：

1. **充分准备**：提前下载所有必要的安装包和镜像
2. **网络规划**：配置好内网环境和私有仓库
3. **标准化操作**：编写自动化脚本简化部署流程
4. **持续维护**：建立监控、备份和更新机制

通过以上步骤，您可以在任何无法访问互联网的环境中成功部署 Docker 项目。记住定期更新和维护您的离线环境，确保安全性和稳定性。