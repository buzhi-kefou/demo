# Docker 离线部署目录结构说明

```
docker-offline/                           # 离线部署工具包根目录
├── README.md                             # 说明文档
├── deploy-toolkit.sh                     # 主工具脚本
├── quick-deploy.sh                      # 快速部署示例
├── deploy-checklist.md                   # 部署检查清单
├── docker-offline-guide.md               # 详细部署指南
├── certificates/                         # 证书目录
│   ├── ca.crt                          # CA 证书
│   ├── server.crt                      # 服务器证书
│   └── server.key                      # 服务器私钥
├── docker/                              # Docker 安装包目录
│   ├── docker-ce.rpm                    # Docker CE RPM 包
│   ├── docker-ce-cli.rpm               # Docker CLI RPM 包
│   ├── containerd.io.rpm              # Containerd RPM 包
│   ├── docker-compose                  # Docker Compose 二进制文件
│   └── images.list                     # 镜像列表文件
├── images/                              # 镜像文件目录
│   ├── alpine_latest.tar               # Alpine 基础镜像
│   ├── nginx_latest.tar                # Nginx 镜像
│   ├── mysql_8.0.tar                  # MySQL 8.0 镜像
│   ├── redis_latest.tar                # Redis 镜像
│   └── app_latest.tar                  # 应用镜像
├── harbor/                              # Harbor 相关文件
│   ├── harbor-offline-installer-v2.8.0.tgz  # Harbor 离线安装包
│   ├── harbor.yml                      # Harbor 配置文件
│   └── install-harbor.sh               # Harbor 安装脚本
├── scripts/                             # 脚本目录
│   ├── install-docker.sh               # Docker 安装脚本
│   ├── deploy-app.sh                   # 应用部署脚本
│   ├── health-check.sh                 # 健康检查脚本
│   ├── monitoring.sh                   # 监控脚本
│   ├── backup-docker.sh                # Docker 备份脚本
│   └── update-app.sh                   # 应用更新脚本
├── config/                              # 配置模板目录
│   ├── daemon.json                     # Docker 守护进程配置
│   ├── docker-compose.yml              # Docker Compose 模板
│   └── nginx.conf                      # Nginx 配置模板
├── apps/                                # 应用示例目录
│   ├── web-app/                        # Web 应用示例
│   │   ├── Dockerfile
│   │   ├── docker-compose.yml
│   │   └── src/
│   └── microservice/                   # 微服务示例
│       ├── service-a/
│       │   ├── Dockerfile
│       │   └── docker-compose.yml
│       └── service-b/
│           ├── Dockerfile
│           └── docker-compose.yml
├── logs/                                # 日志目录
│   ├── install.log                     # 安装日志
│   ├── deploy.log                      # 部署日志
│   └── error.log                       # 错误日志
└── backup/                             # 备份目录
    ├── docker/                         # Docker 备份
    ├── images/                         # 镜像备份
    └── config/                         # 配置备份
```

## 目录说明

### 📂 核心目录

- **docker/**: 存储 Docker 相关的安装包和配置文件
- **images/**: 存储所有离线镜像的 tar 文件
- **scripts/**: 存储各种自动化脚本
- **config/**: 存储配置模板文件

### 📂 应用相关

- **apps/**: 包含不同类型的应用示例
- **certificates/**: 存储 TLS 证书文件
- **harbor/**: Harbor 私有仓库相关文件

### 📂 运维相关

- **logs/**: 存储各种操作日志
- **backup/**: 存储备份文件

## 使用方法

### 1. 初始化工具包

```bash
cd docker-offline
./deploy-toolkit.sh init
```

### 2. 准备安装文件

```bash
# 下载 Docker 安装包到 docker/ 目录
# 导出镜像到 images/ 目录
./deploy-toolkit.sh download
```

### 3. 快速部署示例

```bash
# 部署一个 Nginx + MySQL 应用
./quick-deploy.sh
```

### 4. 部署应用

```bash
# 使用工具包部署
./deploy-toolkit.sh install    # 安装 Docker
./deploy-toolkit.sh deploy     # 部署应用
```

### 5. 使用检查清单

在部署过程中，参考 `deploy-checklist.md` 确保所有项目都已完成。

## 注意事项

1. **镜像准备**: 确保所有必需的镜像都已导出并放到 images/ 目录
2. **网络配置**: 确保内网环境可以访问私有仓库
3. **证书配置**: 生产环境建议使用正式的 SSL 证书
4. **权限设置**: 确保 Docker 服务有足够的权限访问相关目录
5. **资源限制**: 根据服务器资源合理配置容器资源限制