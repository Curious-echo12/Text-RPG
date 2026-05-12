#!/bin/bash
# =============================================
# 文字江湖 - 更新部署脚本
# 在服务器 /opt/text-rpg 目录下执行
# =============================================

set -e

PROJECT_DIR="/opt/text-rpg"
JAR_NAME="text-rpg-1.0.0.jar"
SERVICE_NAME="textrpg"

echo "=========================================="
echo "  文字江湖 - 更新部署"
echo "=========================================="

# 1. 构建后端
echo "[1/3] 构建后端..."
cd "$PROJECT_DIR/backend"
mvn clean package -DskipTests -q
cp "target/$JAR_NAME" "$PROJECT_DIR/"
echo "后端构建完成"

# 2. 构建前端
echo "[2/3] 构建前端..."
cd "$PROJECT_DIR/frontend"
npm run build
FRONTEND_DIR="/www/wwwroot/text-rpg"
rm -rf "$FRONTEND_DIR/assets" "$FRONTEND_DIR/index.html" "$FRONTEND_DIR/favicon.ico" 2>/dev/null
cp -r dist/* "$FRONTEND_DIR/"
chown -R www:www "$FRONTEND_DIR"
echo "前端构建完成"

# 3. 重启服务
echo "[3/3] 重启后端服务..."
systemctl restart ${SERVICE_NAME}
sleep 3
if systemctl is-active --quiet ${SERVICE_NAME}; then
    echo "服务启动成功！"
else
    echo "❌ 服务启动失败，请检查日志："
    echo "   journalctl -u ${SERVICE_NAME} -n 50 --no-pager"
    exit 1
fi

echo ""
echo "=========================================="
echo "  更新完成！"
echo "=========================================="
