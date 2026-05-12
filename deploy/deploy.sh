#!/bin/bash
# =============================================
# 文字江湖 - 一键部署脚本
# 在服务器 /opt/text-rpg 目录下执行
# =============================================

set -e

PROJECT_DIR="/opt/text-rpg"
DEPLOY_DIR="$PROJECT_DIR/deploy"
JAR_NAME="text-rpg-1.0.0.jar"
SERVICE_NAME="textrpg"

echo "=========================================="
echo "  文字江湖 - 部署脚本"
echo "=========================================="

# 1. 检查 Java
echo "[1/6] 检查 Java 环境..."
if ! command -v java &> /dev/null; then
    echo "错误: 未找到 Java，请先安装 JDK 8"
    echo "宝塔面板 → 软件商店 → 安装 Java 项目管理器"
    exit 1
fi
java -version 2>&1 | head -1

# 2. 检查 MySQL
echo "[2/6] 检查 MySQL 连接..."
if ! command -v mysql &> /dev/null; then
    echo "错误: 未找到 MySQL 客户端"
    exit 1
fi

# 3. 初始化数据库
echo "[3/6] 初始化数据库..."
echo "请输入 MySQL root 密码："
read -s MYSQL_ROOT_PWD

echo "创建数据库和用户..."
mysql -u root -p"$MYSQL_ROOT_PWD" << 'EOSQL'
CREATE DATABASE IF NOT EXISTS text_rpg DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'text_rpg'@'localhost' IDENTIFIED BY 'Textrpg2026!Secure';
GRANT ALL PRIVILEGES ON text_rpg.* TO 'text_rpg'@'localhost';
FLUSH PRIVILEGES;
EOSQL

echo "导入表结构..."
mysql -u root -p"$MYSQL_ROOT_PWD" text_rpg < "$DEPLOY_DIR/init.sql"

echo "导入游戏配置数据..."
mysql -u root -p"$MYSQL_ROOT_PWD" text_rpg < "$PROJECT_DIR/backend/src/main/resources/migration_config.sql"

echo "数据库初始化完成！"

# 4. 构建后端
echo "[4/6] 构建后端..."
cd "$PROJECT_DIR/backend"
if [ ! -f "target/$JAR_NAME" ]; then
    echo "首次构建，编译中..."
    mvn clean package -DskipTests -q
else
    echo "增量编译..."
    mvn package -DskipTests -q
fi
echo "后端构建完成: target/$JAR_NAME"

# 5. 构建前端
echo "[5/6] 构建前端..."
cd "$PROJECT_DIR/frontend"
if [ ! -d "node_modules" ]; then
    echo "安装前端依赖..."
    npm install --production=false
fi
npm run build
echo "前端构建完成"

# 6. 部署
echo "[6/6] 部署..."

# 复制 JAR
cp "$PROJECT_DIR/backend/target/$JAR_NAME" "$PROJECT_DIR/"

# 复制生产配置（如果不存在）
if [ ! -f "$PROJECT_DIR/application-prod.yml" ]; then
    cp "$DEPLOY_DIR/application-prod.yml" "$PROJECT_DIR/"
    echo ""
    echo "⚠️  请编辑 $PROJECT_DIR/application-prod.yml 修改以下配置："
    echo "   - 数据库密码 (spring.datasource.password)"
    echo "   - QQ邮箱和授权码 (spring.mail.*)"
    echo ""
fi

# 复制前端到 Nginx 目录
FRONTEND_DIR="/www/wwwroot/text-rpg"
mkdir -p "$FRONTEND_DIR"
cp -r "$PROJECT_DIR/frontend/dist/"* "$FRONTEND_DIR/"
chown -R www:www "$FRONTEND_DIR"

# 创建 systemd 服务
cat > /etc/systemd/system/${SERVICE_NAME}.service << EOF
[Unit]
Description=TextRPG Game Server
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=$PROJECT_DIR
ExecStart=/usr/bin/java -jar $PROJECT_DIR/$JAR_NAME --spring.profiles.active=prod --spring.config.location=$PROJECT_DIR/application-prod.yml
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable ${SERVICE_NAME}
systemctl restart ${SERVICE_NAME}

echo ""
echo "=========================================="
echo "  部署完成！"
echo "=========================================="
echo ""
echo "后端服务: systemctl status ${SERVICE_NAME}"
echo "前端目录: $FRONTEND_DIR"
echo "配置文件: $PROJECT_DIR/application-prod.yml"
echo ""
echo "⚠️  请确保已完成以下配置："
echo "  1. 编辑 application-prod.yml 设置数据库密码和邮箱"
echo "  2. Nginx 反向代理指向 localhost:8080"
echo "  3. 域名解析到服务器 IP"
echo ""
