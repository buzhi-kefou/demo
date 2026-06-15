#!/bin/bash

# Docker 离线快速部署示例
# 演示如何使用工具包快速部署一个 Nginx + MySQL 的应用

set -e

# 配置变量
OFFLINE_DIR="/path/to/docker-offline"
APP_NAME="myapp"
APP_PORT="8080"

log_info "开始快速部署 $APP_NAME..."

# 1. 初始化环境
log_info "1. 初始化环境..."
if [ ! -d "$OFFLINE_DIR" ]; then
    log_error "离线工具包目录不存在: $OFFLINE_DIR"
    exit 1
fi

# 2. 安装 Docker
log_info "2. 安装 Docker..."
$OFFLINE_DIR/scripts/install-docker.sh

# 3. 导入镜像
log_info "3. 导入镜像..."
for img in $OFFLINE_DIR/images/*.tar; do
    if [ -f "$img" ]; then
        docker load -i $img
        log_info "已加载: $img"
    fi
done

# 4. 创建应用目录
APP_DIR="/opt/$APP_NAME"
log_info "4. 创建应用目录..."
mkdir -p $APP_DIR/{html,config,logs}

# 5. 创建 Nginx 配置
cat > $APP_DIR/config/nginx.conf << EOF
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html index.htm;

    location / {
        try_files \$uri \$uri/ =404;
    }

    location /api {
        proxy_pass http://mysql:3306;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    }

    access_log /var/log/nginx/access.log;
    error_log /var/log/nginx/error.log;
}
EOF

# 6. 创建简单的 HTML 页面
cat > $APP_DIR/html/index.html << EOF
<!DOCTYPE html>
<html>
<head>
    <title>Docker 离线部署示例</title>
</head>
<body>
    <h1>欢迎使用 Docker 离线部署</h1>
    <p>这是一个简单的 Nginx + MySQL 应用示例</p>
    <p>部署时间：$(date)</p>
</body>
</html>
EOF

# 7. 创建 docker-compose.yml
cat > $APP_DIR/docker-compose.yml << EOF
version: '3.8'

services:
  nginx:
    image: nginx:latest
    ports:
      - "$APP_PORT:80"
    volumes:
      - ./html:/usr/share/nginx/html
      - ./config/nginx.conf:/etc/nginx/nginx.conf
      - ./logs:/var/log/nginx
    depends_on:
      - mysql
    networks:
      - app-network

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: $APP_NAME
      MYSQL_USER: user
      MYSQL_PASSWORD: password
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - app-network

networks:
  app-network:
    driver: bridge

volumes:
  mysql-data:
EOF

# 8. 启动应用
log_info "8. 启动应用..."
cd $APP_DIR
docker-compose up -d

# 9. 等待服务启动
log_info "9. 等待服务启动..."
sleep 10

# 10. 检查服务状态
log_info "10. 检查服务状态..."
docker-compose ps

# 11. 测试访问
log_info "11. 测试访问..."
if curl -s http://localhost:$APP_PORT > /dev/null; then
    log_info "✅ 应用部署成功！访问地址：http://localhost:$APP_PORT"
else
    log_error "❌ 应用访问失败，请检查服务状态"
fi

# 12. 显示管理命令
echo ""
echo "管理命令："
echo "  启动: cd $APP_DIR && docker-compose up -d"
echo "  停止: cd $APP_DIR && docker-compose down"
echo "  日志: cd $APP_DIR && docker-compose logs -f"
echo "  状态: cd $APP_DIR && docker-compose ps"
echo ""

log_info "部署完成！"