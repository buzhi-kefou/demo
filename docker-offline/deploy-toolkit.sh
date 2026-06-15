#!/bin/bash

# Docker 离线部署工具包
# 使用方法：./deploy-toolkit.sh [command]

set -e

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
OFFLINE_DIR="$PROJECT_ROOT/docker-offline"
BACKUP_DIR="$PROJECT_ROOT/backup"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

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

# 创建目录结构
setup_directories() {
    log_info "创建目录结构..."
    mkdir -p $OFFLINE_DIR/{docker,images,harbor,scripts,config}
    mkdir -p $BACKUP_DIR/{docker,images,data}
    log_info "目录结构创建完成"
}

# 下载 Docker 安装包
download_docker_packages() {
    log_info "下载 Docker 安装包..."

    if [ ! -f $OFFLINE_DIR/docker/docker-ce.rpm ]; then
        log_warn "请手动将 Docker 安装包放到 $OFFLINE_DIR/docker/ 目录"
        log_info "需要的文件："
        echo "  - docker-ce.rpm"
        echo "  - docker-ce-cli.rpm"
        echo "  - containerd.io.rpm"
        echo "  - docker-compose"
        read -p "按回车继续..."
    else
        log_info "Docker 安装包已存在"
    fi
}

# 准备镜像列表
prepare_images() {
    log_info "准备镜像列表..."

    cat > $OFFLINE_DIR/docker/images.list << EOF
# 基础镜像
alpine:latest
nginx:latest
mysql:8.0
redis:latest
php:8.2-fpm
node:18
openjdk:17

# 应用镜像（请替换为实际使用的镜像）
your-app:latest
your-db:latest
your-cache:latest
EOF

    log_info "镜像列表已创建到 $OFFLINE_DIR/docker/images.list"
    log_info "请使用以下命令导出镜像："
    echo "  for img in \$(cat images.list); do docker save \$img -i images/\${img//[:\/]/_}.tar; done"
}

# 生成安装脚本
generate_install_script() {
    log_info "生成安装脚本..."

    cat > $OFFLINE_DIR/scripts/install-docker.sh << 'EOF'
#!/bin/bash
# Docker 离线安装脚本

set -e

DOCKER_DIR="/opt/docker"
CONFIG_DIR="/etc/docker"

log_info "开始安装 Docker..."

# 创建目录
mkdir -p $DOCKER_DIR
mkdir -p $CONFIG_DIR

# 复制安装包
cp -r $OFFLINE_DIR/docker/*.rpm $DOCKER_DIR/

# 安装依赖
yum localinstall -y $DOCKER_DIR/*.rpm

# 配置 Docker
if [ -f $OFFLINE_DIR/config/daemon.json ]; then
    cp $OFFLINE_DIR/config/daemon.json $CONFIG_DIR/daemon.json
else
    cat > $CONFIG_DIR/daemon.json << JSON_EOF
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}
JSON_EOF
fi

# 启动服务
systemctl enable docker
systemctl start docker

# 验证安装
docker --version || { log_error "Docker 安装失败"; exit 1; }
log_info "Docker 安装完成"
EOF

    chmod +x $OFFLINE_DIR/scripts/install-docker.sh
}

# 生成部署脚本
generate_deploy_script() {
    log_info "生成部署脚本..."

    cat > $OFFLINE_DIR/scripts/deploy-app.sh << 'EOF'
#!/bin/bash
# Docker 应用部署脚本

set -e

APP_DIR="/opt/myapp"
BACKUP_DIR="/backup/myapp"

log_info "开始部署应用..."

# 导入镜像
log_info "导入镜像..."
for img_file in $OFFLINE_DIR/images/*.tar; do
    if [ -f "$img_file" ]; then
        docker load -i $img_file
    fi
done

# 备份旧应用
if [ -d "$APP_DIR" ]; then
    log_info "备份旧应用..."
    mkdir -p $BACKUP_DIR
    cp -r $APP_DIR $BACKUP_DIR/backup-$(date +%Y%m%d-%H%M%S)
fi

# 部署新应用
mkdir -p $APP_DIR
cp -r $OFFLINE_DIR/config/* $APP_DIR/

# 启动服务
cd $APP_DIR
if [ -f "docker-compose.yml" ]; then
    log_info "启动 Docker Compose 服务..."
    docker-compose up -d

    # 检查服务状态
    sleep 10
    docker-compose ps
else
    log_error "未找到 docker-compose.yml"
    exit 1
fi

log_info "应用部署完成"
EOF

    chmod +x $OFFLINE_DIR/scripts/deploy-app.sh
}

# 生成配置模板
generate_config_templates() {
    log_info "生成配置模板..."

    # Docker 配置模板
    mkdir -p $OFFLINE_DIR/config
    cat > $OFFLINE_DIR/config/daemon.json << EOF
{
  "registry-mirrors": ["http://harbor.example.com"],
  "insecure-registries": ["harbor.example.com"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  },
  "storage-driver": "overlay2"
}
EOF

    # Docker Compose 模板
    cat > $OFFLINE_DIR/config/docker-compose.yml << EOF
version: '3.8'

services:
  app:
    image: harbor.example.com/your-app:latest
    ports:
      - "80:80"
    volumes:
      - ./app.conf:/etc/app/config
    depends_on:
      - db
      - redis
    networks:
      - app-network

  db:
    image: harbor.example.com/mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: app
    volumes:
      - db-data:/var/lib/mysql
    networks:
      - app-network

  redis:
    image: harbor.example.com/redis:latest
    volumes:
      - redis-data:/data
    networks:
      - app-network

networks:
  app-network:
    driver: bridge

volumes:
  db-data:
  redis-data:
EOF
}

# 生成健康检查脚本
generate_health_check() {
    log_info "生成健康检查脚本..."

    cat > $OFFLINE_DIR/scripts/health-check.sh << 'EOF'
#!/bin/bash
# 健康检查脚本

APP_DIR="/opt/myapp"
EMAIL="admin@example.com"

check_service() {
    local service=$1
    local port=$2

    if nc -z localhost $port; then
        echo "✓ $service 端口 $port 正常"
    else
        echo "✗ $service 端口 $port 异常"
        echo "$service 异常，请检查服务状态" | mail -s "Docker 服务告警" $EMAIL
    fi
}

# 检查关键服务
check_service "Web应用" 80
check_service "数据库" 3306
check_service "Redis" 6379

# 检查 Docker 服务
if systemctl is-active --quiet docker; then
    echo "✓ Docker 服务运行正常"
else
    echo "✗ Docker 服务未运行"
    systemctl restart docker
fi
EOF

    chmod +x $OFFLINE_DIR/scripts/health-check.sh
}

# 生成监控脚本
generate_monitoring() {
    log_info "生成监控脚本..."

    cat > $OFFLINE_DIR/scripts/monitoring.sh << 'EOF'
#!/bin/bash
# 监控脚本

while true; do
    # CPU 使用率
    CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | sed "s/%us,//")

    # 内存使用率
    MEM_USAGE=$(free | grep Mem | awk '{printf "%.2f", $3/$2 * 100.0}')

    # 磁盘使用率
    DISK_USAGE=$(df -h / | awk 'NR==2{print $5}' | sed 's/%//')

    # 容器数量
    CONTAINER_COUNT=$(docker ps -q | wc -l)

    # 镜像数量
    IMAGE_COUNT=$(docker images -q | wc -l)

    # 输出监控信息
    echo "$(date): CPU=${CPU_USAGE}%, MEM=${MEM_USAGE}%, DISK=${DISK_USAGE}%, CONTAINERS=${CONTAINER_COUNT}, IMAGES=${IMAGE_COUNT}"

    # 检告警
    if (( $(echo "$CPU_USAGE > 80" | bc -l) )); then
        echo "警告：CPU 使用率超过 80%"
    fi

    if (( $(echo "$MEM_USAGE > 80" | bc -l) )); then
        echo "警告：内存使用率超过 80%"
    fi

    sleep 60
done
EOF

    chmod +x $OFFLINE_DIR/scripts/monitoring.sh
}

# 生成主菜单
generate_menu() {
    echo ""
    echo "=========================================="
    echo "  Docker 离线部署工具包"
    echo "=========================================="
    echo ""
    echo "请选择操作："
    echo "1. 初始化工具包"
    echo "2. 下载 Docker 安装包"
    echo "3. 准备镜像列表"
    echo "4. 生成安装脚本"
    echo "5. 生成部署脚本"
    echo "6. 生成配置模板"
    echo "7. 生成监控脚本"
    echo "8. 查看工具包结构"
    echo "9. 退出"
    echo ""

    read -p "请输入选择 (1-9): " choice

    case $choice in
        1)
            setup_directories
            generate_config_templates
            log_info "工具包初始化完成"
            ;;
        2)
            download_docker_packages
            ;;
        3)
            prepare_images
            ;;
        4)
            generate_install_script
            ;;
        5)
            generate_deploy_script
            ;;
        6)
            generate_config_templates
            ;;
        7)
            generate_health_check
            generate_monitoring
            ;;
        8)
            echo ""
            echo "工具包结构："
            tree $OFFLINE_DIR
            ;;
        9)
            exit 0
            ;;
        *)
            log_error "无效选择"
            ;;
    esac
}

# 主函数
main() {
    case "${1:-menu}" in
        "init")
            setup_directories
            generate_config_templates
            generate_health_check
            generate_monitoring
            log_info "工具包初始化完成"
            ;;
        "install")
            $OFFLINE_DIR/scripts/install-docker.sh
            ;;
        "deploy")
            $OFFLINE_DIR/scripts/deploy-app.sh
            ;;
        "menu")
            generate_menu
            ;;
        *)
            log_error "未知命令: $1"
            echo "使用方法: $0 {init|install|deploy|menu}"
            exit 1
            ;;
    esac
}

main "$@"